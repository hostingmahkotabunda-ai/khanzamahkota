package bridging;

import java.util.*;

/** Memisahkan nama alergen dari penanda dugaan dan keterangan yang dikenal. */
public final class SatuSehatAllergySearchTerm {
    private SatuSehatAllergySearchTerm(){}
    public static boolean multiple(String text){
        String value=SatuSehatAllergyMapping.normalize(text);int parentheses=value.indexOf('(');
        if(parentheses>=0)value=value.substring(0,parentheses);
        return value.matches(".*[,;/+&].*")||value.matches(".*\\b(dan|and|atau|or)\\b.*");
    }
    public static boolean suspected(String text){
        return SatuSehatAllergyMapping.normalize(text).matches(".*\\b(curiga|dugaan|suspek|suspected|suspect|possible)\\b.*")
                ||text!=null&&text.contains("?");
    }
    public static String extract(String text){
        String value=SatuSehatAllergyMapping.normalize(text);
        if(value.isEmpty()||value.equals("-"))throw new IllegalArgumentException("Catatan alergi belum menyebut zat penyebab.");
        if(value.matches(".*\\b(tidak|bukan|disangkal|no|denies|pasien|patient|nik|rm|rawat)\\b.*")||value.matches(".*[0-9]{4,}.*"))
            throw new IllegalArgumentException("Catatan memerlukan peninjauan sebelum pencarian nama alergen.");
        value=value.replaceFirst("^(curiga|dugaan|suspek|suspected|suspect|possible)\\s+","")
                .replaceFirst("^(riwayat\\s+)?alergi(\\s+terhadap)?\\s+","");
        int reaction=value.indexOf('(');
        if(reaction>=0){
            if(!value.endsWith(")")||value.indexOf('(',reaction+1)>=0)throw new IllegalArgumentException("Format catatan reaksi perlu ditinjau.");
            String suffix=value.substring(reaction+1,value.length()-1);
            for(String part:suffix.split("\\s*(?:,|;|\\bdan\\b)\\s*",-1))
                if(!Arrays.asList("gatal","bengkak","sesak","sesak napas","ruam","mual","muntah","kebas","mata bengkak","bibir bengkak","urtikaria","nyeri badan","berdebar").contains(part.trim()))
                    throw new IllegalArgumentException("Keterangan dalam kurung belum dikenali. Tinjau catatan lengkap agar nama alergen lain tidak terabaikan.");
            value=value.substring(0,reaction).trim();
        }
        if(!value.matches("[\\p{L}0-9 '\\-]{2,80}")||value.matches(".*\\b(dan|and|atau|or)\\b.*"))
            throw new IllegalArgumentException("Pencarian otomatis memerlukan satu nama alergen; pisahkan/tinjau catatan yang berisi beberapa zat.");
        if(Arrays.asList("antibiotik","obat","obat dosis tinggi","obat batuk pilek","makanan","unknown","tidak diketahui").contains(value))
            throw new IllegalArgumentException("Nama zat penyebab belum spesifik. Lengkapi catatan klinis terlebih dahulu.");
        // Padanan istilah utuh, bukan terjemahan bebas atau pencarian token.
        Map<String,String> terms=new HashMap<>();
        terms.put("susu sapi","cow's milk");terms.put("protein susu","milk protein");terms.put("makanan laut","seafood");
        terms.put("lateks","latex");terms.put("serbuk sari","pollen");terms.put("parasetamol","paracetamol");
        terms.put("ampisilin","ampicillin");terms.put("ampicilin","ampicillin");terms.put("amoksisilin","amoxicillin");
        terms.put("ranitidin","ranitidine");terms.put("asam mefenamat","mefenamic acid");
        terms.put("dexamethason","dexamethasone");
        terms.put("ibu profen","ibuprofen");terms.put("ibuporfen","ibuprofen");
        terms.put("cafein","caffeine");terms.put("kafein","caffeine");
        return terms.containsKey(value)?terms.get(value):value;
    }
}
