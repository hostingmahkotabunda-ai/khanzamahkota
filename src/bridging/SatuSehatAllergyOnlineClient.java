package bridging;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/** Adapter katalog publik resmi dan GET KFA konfigurasi lokal sebagai cadangan. */
public final class SatuSehatAllergyOnlineClient implements SatuSehatAllergyReferenceService.Catalogue {
    public static final String KFA_BROWSER="https://satusehat.kemkes.go.id/kfa-browser/farmasi/api/search/active-ingredients";
    public static final String SNOMED_SHEET="https://docs.google.com/spreadsheets/d/1tN9lFaK2GJ3itObaMOQds8JkI8rjQE2tacYRRSNgPF8/gviz/tq?tqx=out:csv&gid=0";
    public static final String FOODS="https://terminology.hl7.org/en/ValueSet-edible-substance-type.html";
    public interface Transport { String request(String url,String jsonBody,String bearer)throws IOException; }
    public static final class HttpTransport implements Transport {
        public String request(String url,String body,String bearer)throws IOException{
            HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
            c.setConnectTimeout(4000);c.setReadTimeout(8000);c.setInstanceFollowRedirects(false);
            c.setRequestProperty("Accept","application/json,text/csv");
            try{
                if(bearer!=null)c.setRequestProperty("Authorization","Bearer "+bearer);
                if(body!=null){c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
                    try(OutputStream out=c.getOutputStream()){out.write(body.getBytes(StandardCharsets.UTF_8));}}
                if(c.getResponseCode()!=200)throw new IOException("Layanan referensi mengembalikan HTTP "+c.getResponseCode()+".");
                try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                    byte[] buffer=new byte[8192];int count;
                    while((count=in.read(buffer))>=0){if(out.size()+count>4*1024*1024)throw new IOException("Katalog referensi melebihi batas ukuran.");out.write(buffer,0,count);}
                    return new String(out.toByteArray(),StandardCharsets.UTF_8);
                }
            }finally{c.disconnect();}
        }
    }
    private final Transport transport;
    private final String kfaBase;
    private final Supplier<String> token;
    private final ObjectMapper json=new ObjectMapper();
    private List<SatuSehatAllergyReferenceService.Candidate> snomed;
    private long snomedLoaded;
    public SatuSehatAllergyOnlineClient(String kfaBase,Supplier<String> token){this(kfaBase,token,new HttpTransport());}
    public SatuSehatAllergyOnlineClient(String kfaBase,Supplier<String> token,Transport transport){this.kfaBase=kfaBase;this.token=token;this.transport=transport;}
    @Override public SatuSehatAllergyReferenceService.Result search(String query)throws IOException{
        List<SatuSehatAllergyReferenceService.Candidate> found=new ArrayList<>();boolean complete=true;String warning="";
        try{complete=searchKfa(query,found);}catch(IOException e){complete=false;warning="KFA belum dapat diakses. ";}
        try{
            for(SatuSehatAllergyReferenceService.Candidate c:snomed())if(SatuSehatAllergyMapping.normalize(c.display).contains(query))found.add(c);
        }catch(IOException e){complete=false;warning+="Referensi SNOMED belum dapat diakses. ";}
        if(!complete)warning+="Hasil parsial memerlukan pilihan petugas; tidak disimpan otomatis.";
        return new SatuSehatAllergyReferenceService.Result(found,complete,warning);
    }
    private boolean searchKfa(String query,List<SatuSehatAllergyReferenceService.Candidate> found)throws IOException{
        try{
            ObjectNode body=json.createObjectNode();body.put("page",1);body.put("size",50);body.put("search",query);body.put("search_by","name");body.put("farmalkes_type","");body.putObject("sorts");
            JsonNode root=json.readTree(transport.request(KFA_BROWSER,json.writeValueAsString(body),null));
            if(root==null||root.path("status").asInt()!=200||root.path("error").asBoolean()||!root.path("data").isArray()||!root.path("meta").has("totalPage"))throw new IOException("Format referensi KFA berubah.");
            for(JsonNode n:root.path("data"))addKfa(found,n.path("kfaCode").asText(),n.path("name").asText(),KFA_BROWSER);
            return root.path("meta").path("totalPage").asInt()<=1;
        }catch(IOException publicUnavailable){
            // Katalog browser bisa berubah; cadangan memakai endpoint dan kredensial faskes yang sudah ada.
            URI base;
            try{base=URI.create(kfaBase);}catch(Exception e){throw new IOException("Konfigurasi KFA belum valid.");}
            if(!"https".equals(base.getScheme())||!Arrays.asList("api-satusehat.kemkes.go.id","api-satusehat-stg.dto.kemkes.go.id").contains(base.getHost())||base.getRawQuery()!=null||base.getRawUserInfo()!=null||!"/kfa-v2".equals(base.getPath()))
                throw new IOException("Endpoint KFA lokal belum valid.");
            String bearer=token.get();if(bearer==null||bearer.isEmpty())throw new IOException("Akses KFA belum tersedia.");
            String url=kfaBase+"/products/all?page=1&size=100&product_type=farmasi&keyword="+URLEncoder.encode(query,"UTF-8");
            JsonNode root=json.readTree(transport.request(url,null,bearer));
            if(root==null||!root.path("items").path("data").isArray()||!root.has("total"))throw new IOException("Format produk KFA belum valid.");
            for(JsonNode product:root.path("items").path("data"))
                for(JsonNode ingredient:product.path("active_ingredients"))if(ingredient.path("active").asBoolean()&&"valid".equals(ingredient.path("state").asText()))
                    addKfa(found,ingredient.path("kfa_code").asText(),ingredient.path("zat_aktif").asText(),kfaBase+"/products/all");
            return root.path("total").asInt()<=100;
        }
    }
    private void addKfa(List<SatuSehatAllergyReferenceService.Candidate> found,String code,String name,String source){
        if(code.matches("91[0-9]{6}")&&!name.trim().isEmpty())found.add(new SatuSehatAllergyReferenceService.Candidate("medication",SatuSehatAllergyMapping.KFA,code,name,source));
    }
    private synchronized List<SatuSehatAllergyReferenceService.Candidate> snomed()throws IOException{
        if(snomed!=null&&System.currentTimeMillis()-snomedLoaded<3600000)return snomed;
        Map<String,String> foods=new LinkedHashMap<>();
        // Resource JSON publik hanya berisi ECL; pasangan kode/display ada pada ekspansi terbitan HTML.
        String foodHtml=transport.request(FOODS,null,null);
        if(!foodHtml.contains("762766007"))throw new IOException("Definisi referensi makanan berubah.");
        java.util.regex.Matcher codes=java.util.regex.Pattern.compile("data-clipboard-text=\"([^\"]*)\"").matcher(foodHtml);
        while(codes.find()){
            String value=org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(codes.group(1));
            if(value.startsWith("{")){
                JsonNode coding=json.readTree(value);String code=coding.path("code").asText(),display=coding.path("display").asText();
                if(SatuSehatAllergyMapping.SNOMED.equals(coding.path("system").asText())&&code.matches("[0-9]{6,18}")&&!display.isEmpty()){
                    if(foods.containsKey(code)&&!foods.get(code).equals(display))throw new IOException("Kode referensi makanan bertentangan.");
                    foods.put(code,display);
                }
            }
        }
        if(foods.isEmpty())throw new IOException("Referensi makanan kosong.");
        Map<String,SatuSehatAllergyReferenceService.Candidate> all=new LinkedHashMap<>();
        String csv=transport.request(SNOMED_SHEET,null,null);
        List<List<String>> rows=parseCsv(csv);
        if(rows.isEmpty()||!rows.get(0).equals(Arrays.asList("Code","Display","Codesystem")))throw new IOException("Format lampiran SATUSEHAT berubah.");
        for(int row=1;row<rows.size();row++){
            List<String> r=rows.get(row);if(r.size()!=3)throw new IOException("Baris lampiran terminologi tidak lengkap.");
            String code=r.get(0).trim(),display=r.get(1).trim();
            if(!code.matches("[0-9]{6,18}")||!SatuSehatAllergyMapping.SNOMED.equals(r.get(2).trim()))continue;
            String category=foodCategory(code);
            if(category.isEmpty()&&Arrays.asList("111088007","256259004","256277009","256260009","256292005","256303006").contains(code))category="environment";
            if(foods.containsKey(code))display=foods.get(code);
            all.put(code,new SatuSehatAllergyReferenceService.Candidate(category,SatuSehatAllergyMapping.SNOMED,code,display,SNOMED_SHEET));
        }
        for(Map.Entry<String,String> food:foods.entrySet())all.put(food.getKey(),new SatuSehatAllergyReferenceService.Candidate(foodCategory(food.getKey()),SatuSehatAllergyMapping.SNOMED,food.getKey(),food.getValue(),FOODS));
        if(all.isEmpty())throw new IOException("Lampiran terminologi kosong.");
        snomed=new ArrayList<>(all.values());snomedLoaded=System.currentTimeMillis();return snomed;
    }
    private String foodCategory(String code){
        // Edible substance juga mencakup bahan kimia/obat. Keanggotaan katalog tersebut saja
        // tidak membuktikan bahwa konsep lolos pengecualian drug pada ECL SATUSEHAT.
        return Arrays.asList("3718001","10827009","44027008","226726003").contains(code)?"food":"";
    }
    /** CSV berpetik, koma/newline di dalam field, dan CRLF. */
    static List<List<String>> parseCsv(String text)throws IOException{
        List<List<String>> rows=new ArrayList<>();List<String> row=new ArrayList<>();StringBuilder field=new StringBuilder();boolean quoted=false;
        text=text.replaceFirst("^\\uFEFF","");
        for(int i=0;i<text.length();i++){
            char c=text.charAt(i);
            if(c=='"'){
                if(quoted&&i+1<text.length()&&text.charAt(i+1)=='"'){field.append('"');i++;}else quoted=!quoted;
            }else if(!quoted&&(c==','||c=='\n')){
                row.add(field.toString());field.setLength(0);if(c=='\n'){rows.add(row);row=new ArrayList<>();}
            }else if(c!='\r'||quoted)field.append(c);
        }
        if(quoted)throw new IOException("CSV referensi tidak lengkap.");
        if(field.length()>0||!row.isEmpty()){row.add(field.toString());rows.add(row);}return rows;
    }
}
