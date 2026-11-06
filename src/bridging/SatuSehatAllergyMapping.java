package bridging;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Locale;

/** Kamus lokal alergi. Mapping sebagian hanya melalui pilihan eksplisit operator. */
public final class SatuSehatAllergyMapping {
    private static final Object FILE_LOCK=new Object();
    public static final String SNOMED="http://snomed.info/sct",KFA="http://sys-ids.kemkes.go.id/kfa";
    private final Path file;
    private final ObjectMapper mapper=new ObjectMapper();
    public static final class Mapping {
        public final String keyword,category,system,code,display,text;
        public final boolean partial;
        public Mapping(String keyword,String category,String system,String code,String display,String text){
            this(keyword,category,system,code,display,text,false);
        }
        public Mapping(String keyword,String category,String system,String code,String display,String text,boolean partial){
            this.keyword=normalize(keyword);this.category=clean(category);this.system=clean(system);
            this.code=clean(code);this.display=clean(display);this.text=clean(text);
            this.partial=partial;
        }
        public void validate(){
            if(keyword.isEmpty()||keyword.equals("-"))throw new IllegalArgumentException("Catatan alergi kosong/tanda '-' belum menyatakan tidak ada alergi. Lengkapi catatan klinis terlebih dahulu.");
            if(SatuSehatAllergySearchTerm.multiple(keyword)&&!partial)throw new IllegalArgumentException("Catatan memuat beberapa zat. Pilih satu zat melalui Mapping Alergi untuk menyimpannya sebagai mapping sebagian.");
            if(!Arrays.asList("food","medication","environment","biologic").contains(category))throw new IllegalArgumentException("Kategori alergi belum valid; pilih food, medication, environment, atau biologic.");
            if(!SNOMED.equals(system)&&!KFA.equals(system))throw new IllegalArgumentException("Sistem terminologi belum valid; gunakan SNOMED CT atau KFA sesuai terminologi zat penyebab.");
            if(!code.matches("[0-9]{6,18}")||KFA.equals(system)&&!code.matches("9[123][0-9]{6}"))throw new IllegalArgumentException("Kode terminologi belum lengkap/masih placeholder. KFA alergi menggunakan kode BZA/POV/POA yang sudah diverifikasi.");
            if(KFA.equals(system)&&!"medication".equals(category))throw new IllegalArgumentException("Kode KFA obat harus memakai kategori medication.");
            if(SNOMED.equals(system)&&"medication".equals(category))throw new IllegalArgumentException("Untuk alergi obat gunakan referensi KFA BZA/POV/POA sesuai panduan SATUSEHAT.");
            if(display.isEmpty()||text.isEmpty())throw new IllegalArgumentException("Display terminologi dan keterangan alergi wajib diisi.");
            if(SNOMED.equals(system)){
                String expected=null;
                if("227183005".equals(code))expected="Fish roe";
                if("10827009".equals(code))expected="Milk protein";
                if("3718001".equals(code))expected="Cow's milk";
                if("44027008".equals(code))expected="Seafood";
                if("226726003".equals(code))expected="Popcorn";
                if(expected!=null&&!normalize(display).replaceFirst(" \\(substance\\)$","").equals(normalize(expected)))
                    throw new IllegalArgumentException("Kode/display SNOMED tidak sesuai referensi: "+code+" berarti "+expected+". Tinjau mapping, jangan hanya mengandalkan respons berhasil API.");
            }
            if(SNOMED.equals(system)&&"44027008".equals(code)&&!"food".equals(category))throw new IllegalArgumentException("Seafood harus memakai kategori food.");
            if("716186003".equals(code)&&!explicitNone(keyword))throw new IllegalArgumentException("No known allergy hanya digunakan untuk catatan yang secara eksplisit menyatakan tidak ada alergi.");
        }
    }
    public SatuSehatAllergyMapping(File file){this.file=file.toPath().toAbsolutePath().normalize();}
    private static String clean(String text){return text==null?"":text.trim();}
    public static String normalize(String text){return clean(text).replace('\u00a0',' ').replaceAll("[\\s\\p{Z}]+"," ").trim().toLowerCase(Locale.ROOT);}
    private static boolean explicitNone(String value){return Arrays.asList("tidak ada alergi","tidak ada riwayat alergi","no known allergy","no known allergies","nka").contains(normalize(value));}
    private static Mapping builtin(String text){
        String key=normalize(text);
        if(Arrays.asList("seafood","sea food","makanan laut","alergi seafood","alergi makanan laut").contains(key))
            return new Mapping(key,"food",SNOMED,"44027008","Seafood","Alergi seafood");
        if(explicitNone(key))return new Mapping(key,"environment",SNOMED,"716186003","No known allergy","No known allergy");
        return null;
    }
    private ObjectNode read()throws IOException{
        if(!Files.exists(file)){ObjectNode root=mapper.createObjectNode();root.putArray("alergi");return root;}
        JsonNode root;
        try(Reader in=Files.newBufferedReader(file,StandardCharsets.UTF_8)){root=mapper.readTree(in);}
        if(root==null||!root.isObject()||!root.path("alergi").isArray())throw new IOException("Format kamus alergi tidak valid. File dipertahankan; periksa/restore cadangan sebelum menyimpan.");
        return (ObjectNode)root;
    }
    private ObjectNode find(ObjectNode root,String key)throws IOException{
        ObjectNode match=null;
        for(JsonNode node:root.path("alergi")){
            if(!node.isObject())throw new IOException("Entri kamus alergi harus berupa object.");
            boolean fits=normalize(node.path("keyword").asText()).equals(key);
            for(JsonNode alias:node.path("aliases"))if(normalize(alias.asText()).equals(key))fits=true;
            if(fits){if(match!=null)throw new IOException("Keyword/alias alergi duplikat. Rapikan kamus sebelum mengirim.");match=(ObjectNode)node;}
        }
        return match;
    }
    private Mapping mapping(JsonNode node){return new Mapping(node.path("keyword").asText(),node.path("category").asText(),node.path("coding_system").asText(),node.path("coding_code").asText(),node.path("coding_display").asText(),node.path("text").asText(),node.path("partial_mapping").asBoolean());}
    public Mapping forEditing(String text)throws IOException{
        synchronized(FILE_LOCK){
            String key=normalize(text);ObjectNode node=find(read(),key);Mapping known=builtin(key);
            if(known!=null&&(node==null||placeholder(node)))return known;
            if(node!=null)return mapping(node);
            return new Mapping(key,"",SNOMED,"","","Alergi "+clean(text));
        }
    }
    private static boolean placeholder(JsonNode n){String code=n.path("coding_code").asText().trim();return code.isEmpty()||code.equalsIgnoreCase("unknown");}
    private Mapping ready(ObjectNode root,String text)throws IOException{
        String key=normalize(text);
        if(key.isEmpty()||key.equals("-"))throw new IllegalArgumentException("Catatan kosong/tanda '-' tidak otomatis dianggap No known allergy. Tinjau catatan alergi terlebih dahulu.");
        ObjectNode node=find(root,key);Mapping known=builtin(key);
        if(SatuSehatAllergySearchTerm.multiple(key)&&(node==null||!node.path("partial_mapping").asBoolean()||!SatuSehatAllergySearchTerm.multiple(node.path("keyword").asText())))
            throw new IllegalArgumentException("Catatan memuat beberapa zat. Tinjau pilihan di Mapping Alergi dan simpan sebagai mapping sebagian terlebih dahulu.");
        Mapping m=node==null?known:known!=null&&placeholder(node)?known:mapping(node);
        if(m==null)return null;
        if(known==null&&node!=null&&placeholder(node)&&!node.path("review_reason").asText().trim().isEmpty())
            throw new IllegalArgumentException(node.path("review_reason").asText());
        m.validate();
        if("716186003".equals(m.code)&&!explicitNone(key))throw new IllegalArgumentException("Alias tidak boleh mengubah catatan alergi menjadi No known allergy.");
        return m;
    }
    public Mapping resolve(String text)throws IOException{
        synchronized(FILE_LOCK){
            ObjectNode root=read();Mapping m=ready(root,text);if(m!=null)return m;
            Mapping pending=forEditing(text);ObjectNode node=((ArrayNode)root.path("alergi")).addObject();writeFields(node,pending);
            node.put("review_status","pending");write(root);
            throw new IllegalArgumentException("Placeholder alergi dicatat, belum siap kirim. Lengkapi melalui tombol Mapping Alergi.");
        }
    }
    /** Baca saja: null berarti mapping belum lengkap, bukan bukti tidak ada alergi. */
    public Mapping lookup(String text)throws IOException{
        synchronized(FILE_LOCK){
            ObjectNode root=read();String key=normalize(text);ObjectNode node=find(root,key);
            if(node!=null&&placeholder(node)&&node.path("review_reason").asText().trim().isEmpty()&&builtin(key)==null)return null;
            return ready(root,text);
        }
    }
    public void saveReference(Mapping m,String source,boolean reviewed)throws IOException{
        if(m.partial&&!reviewed)throw new IllegalArgumentException("Mapping sebagian memerlukan pilihan operator.");
        m.validate();synchronized(FILE_LOCK){
            ObjectNode root=read(),node=findForSave(root,m);
            if(node!=null&&!placeholder(node)){
                Mapping existing=mapping(node);
                boolean same=existing.system.equals(m.system)&&existing.code.equals(m.code)&&existing.category.equals(m.category)&&existing.display.equals(m.display)&&existing.partial==m.partial;
                if(same)return;
                if(!reviewed){existing.validate();throw new IOException("Mapping lokal sudah berubah/terisi. Muat ulang dan tinjau mapping sebelum menggantinya.");}
            }
            String canonical=node==null?m.keyword:node.path("keyword").asText();
            if(node==null)node=((ArrayNode)root.path("alergi")).addObject();
            writeFields(node,m);node.put("keyword",canonical);node.put("review_status",m.partial?"partial":"ready");node.remove("review_reason");
            node.put("source_url",source);node.put("source_checked_at",java.time.OffsetDateTime.now().toString());
            node.put("reference_selection",reviewed?"operator":"exact_unique");write(root);
        }
    }
    public String status(String text){
        synchronized(FILE_LOCK){
            try{Mapping m=ready(read(),text);return m==null?"Belum dimapping":m.partial?"Sebagian terpetakan: "+m.display+"; zat lain belum terpetakan":"Lengkap secara lokal";}
            catch(Exception e){return "Perlu ditinjau: "+e.getMessage();}
        }
    }
    public void save(Mapping m)throws IOException{
        m.validate();synchronized(FILE_LOCK){
            ObjectNode root=read(),node=findForSave(root,m);
            if(node==null)node=((ArrayNode)root.path("alergi")).addObject();
            // Alias/custom metadata lama dipertahankan.
            writeFields(node,m);node.put("review_status",m.partial?"partial":"ready");node.remove("review_reason");write(root);
        }
    }
    private ObjectNode findForSave(ObjectNode root,Mapping m)throws IOException{
        ObjectNode node=find(root,m.keyword);
        if(m.partial&&node!=null&&!normalize(node.path("keyword").asText()).equals(m.keyword)){
            // Pilihan sebagian untuk alias gabungan menjadi entri tersendiri, agar mapping
            // kanonis dan alias satu zat yang sudah ada tidak ikut berubah.
            JsonNode aliases=node.path("aliases");
            if(aliases.isArray())for(int i=aliases.size()-1;i>=0;i--)
                if(normalize(aliases.get(i).asText()).equals(m.keyword))((ArrayNode)aliases).remove(i);
            return null;
        }
        return node;
    }
    private void writeFields(ObjectNode node,Mapping m){
        node.put("keyword",m.keyword);node.put("category",m.category);node.put("coding_system",m.system);
        node.put("coding_code",m.code);node.put("coding_display",m.display);node.put("text",m.text);
        node.put("partial_mapping",m.partial);
    }
    private void write(ObjectNode root)throws IOException{
        Path parent=file.getParent();Files.createDirectories(parent);
        Path temp=Files.createTempFile(parent,"allergy-write-",".tmp");
        try{
            try(Writer out=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){mapper.writerWithDefaultPrettyPrinter().writeValue(out,root);}
            if(Files.exists(file)){
                Path backup=Files.createTempFile(parent,"alergisatusehat-backup-",".iyem");
                Files.copy(file,backup,StandardCopyOption.REPLACE_EXISTING);
            }
            try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temp);}
    }
}
