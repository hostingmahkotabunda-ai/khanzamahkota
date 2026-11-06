package bridging;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Bantuan pemilihan tipe, bukan penetapan diagnosis/status atau kelayakan episode. */
public final class SatuSehatPtmRecommendation {
    public static final class Result {
        public final String type;
        public final String explanation;
        public final String route;
        public final boolean maternity;
        private Result(String type,String explanation){this(type,explanation,type.isEmpty()?"PTM perlu ditinjau":"PTM: "+type,false);}
        private Result(String type,String explanation,String route,boolean maternity){
            this.type=type;this.explanation=explanation;this.route=route;this.maternity=maternity;
        }
    }
    public static final class Diagnosis {
        public final String code,name;
        public Diagnosis(String code,String name){this.code=code;this.name=name;}
    }
    private SatuSehatPtmRecommendation(){}

    public static Result recommend(String icd10,String diseaseName){
        String code=icd10==null?"":icd10.trim().toUpperCase(Locale.ROOT);
        if(code.matches("[A-Z][0-9]{2}[0-9A-Z]{1,4}"))code=code.substring(0,3)+"."+code.substring(3);
        boolean valid=code.matches("[A-Z][0-9]{2}(?:\\.[0-9A-Z]{1,4})?");
        String name=diseaseName==null?"":diseaseName.toLowerCase(Locale.ROOT).replaceAll("\\s+"," ").trim();
        Set<String> names=new LinkedHashSet<>();
        if(has(name,"\\b(coronary|ischaemic heart|ischemic heart|angina|myocardial infarction|infark miokard|jantung koroner|pjk|cad)\\b"))names.add("CAD");
        if(has(name,"\\b(stroke|cerebrovascular|serebrovaskular|cerebral infarction|infark serebral|intracerebral haemorrhage|intracerebral hemorrhage|subarachnoid haemorrhage|subarachnoid hemorrhage)\\b"))names.add("CVD");
        if(has(name,"\\b(chronic kidney|chronic renal|gagal ginjal kronis|gagal ginjal kronik|penyakit ginjal kronis|penyakit ginjal kronik|ckd|ggk)\\b"))names.add("CKD");
        if(has(name,"\\b(malignant neoplasm|cancer|kanker|carcinoma|karsinoma|adenocarcinoma|adenokarsinoma|leukemia|leukaemia|lymphoma|limfoma|sarcoma|sarkoma)\\b"))names.add("cancer");
        String type="",group="";
        if(valid){
            int number=Integer.parseInt(code.substring(1,3));char letter=code.charAt(0);
            if(letter=='I'&&number>=20&&number<=25){type="CAD";group="I20–I25 (penyakit jantung iskemik)";}
            else if(letter=='I'&&number>=60&&number<=69){type="CVD";group="I60–I69 (penyakit serebrovaskular)";}
            else if(letter=='N'&&number==18){type="CKD";group="N18 (penyakit ginjal kronis)";}
            else if(letter=='C'&&number<=97){type="cancer";group="C00–C97 (neoplasma ganas)";}
            else if(letter=='I'&&number>=10&&number<=15)
                return new Result("","Hipertensi: pertimbangkan CAD/CKD sesuai pelayanan. CAD memerlukan penilaian risiko/kriteria layanan; kode ini saja belum menentukan satu tipe. Pilih manual setelah meninjau catatan.");
            else if(letter=='E'&&number>=10&&number<=14)
                return new Result("","Diabetes: dapat terkait alur uronefrologi (CKD) sesuai kriteria PTM. Diagnosis diabetes saja tidak menetapkan penyakit ginjal kronis. Tinjau pelayanan dan pilih manual.");
            else if(letter=='O'||(letter=='Z'&&number>=32&&number<=37)||code.startsWith("Z39"))return maternity(code,names);
        }
        if(type.isEmpty()&&has(name,"\\b(hamil|kehamilan|pregnancy|antenatal|preeklampsia|preeklamsia|pre eclampsia|pre-eclampsia|eklamsia|eclampsia|diabetes gestasional)\\b"))
            return new Result("","Nama diagnosis menunjukkan konteks kehamilan, tetapi ICD-10 perlu dilengkapi/dicocokkan. Bila kunjungan antenatal, gunakan Atur ANC; PTM penyerta memerlukan diagnosis penyakit dasarnya.","Obstetri / tinjau ANC",true);
        boolean context=has(name,"\\b(suspect|suspected|suspek|susp|rule out|ruled out|without|no|not|tidak|tanpa|history of|riwayat|benign|jinak)\\b");
        if(!type.isEmpty()){
            if(context||(!names.isEmpty()&&(names.size()!=1||!names.contains(type))))
                return new Result("","ICD-10 mengarah ke "+type+" ("+group+"), tetapi nama/konteks diagnosis perlu dicocokkan. Pilih manual setelah verifikasi ICD-10 dan penyakit.");
            return new Result(type,"Saran "+type+": ICD-10 "+code+" termasuk "+group+
                    (names.contains(type)?"; nama penyakit juga sesuai.":".")+" Konfirmasi tipe pelayanan dan kelayakan episode sebelum mengirim; status tetap ditinjau petugas.");
        }
        if(!names.isEmpty())return new Result("","Nama penyakit mengarah ke "+String.join(" / ",names)+
                ", tetapi ICD-10 belum mendukung pemilihan pasti. Cocokkan kode dan konteks pelayanan, lalu pilih manual.");
        return new Result("","Belum ada rekomendasi PTM yang cukup dari ICD-10 dan nama penyakit ini. Pilih manual bila memang memenuhi kriteria layanan; tidak semua Condition memerlukan episode PTM.");
    }

    public static Result recommend(String code,String name,java.util.List<Diagnosis> related){
        Result primary=recommend(code,name);
        if(!primary.maternity||related==null)return primary;
        Set<String> hints=new LinkedHashSet<>();
        for(Diagnosis diagnosis:related){
            if(diagnosis==null)continue;
            Result candidate=recommend(diagnosis.code,diagnosis.name);
            if(!candidate.type.isEmpty())hints.add(diagnosis.code.trim().toUpperCase(Locale.ROOT)+" → "+candidate.type);
        }
        if(hints.isEmpty())return new Result(primary.type,primary.explanation+
                " Belum ditemukan Condition penyerta terkirim pada kunjungan ini yang mendukung saran PTM spesifik.",primary.route,true);
        return new Result("",primary.explanation+" Condition penyerta terkirim pada kunjungan ini: "+String.join("; ",hints)+
                ". Untuk membuat episode PTM penyerta, buka dialog berdasarkan Condition penyerta tersebut. Diagnosis obstetri yang sedang dipilih tetap dipertahankan.",
                primary.route+"; PTM penyerta perlu ditinjau",true);
    }

    private static Result maternity(String code,Set<String> names){
        String route="Obstetri: ANC/INC/PNC sesuai fase pelayanan",detail;
        String group=code.substring(0,3);
        if(group.equals("Z34")||group.equals("Z35")||group.equals("Z36")){
            route="ANC (pemeriksaan antenatal)";
            detail="Pemantauan kehamilan normal/risiko tinggi atau skrining antenatal. Gunakan Atur ANC pada Encounter, konfirmasi HPHT dan kategori kunjungan. Kode ini tidak menentukan tipe PTM.";
        }else if(group.equals("Z32")){
            route="Konfirmasi kehamilan; tinjau ANC bila terkonfirmasi";
            detail="Pemeriksaan/konfirmasi kehamilan. Pastikan hasil dan jenis pelayanan sebelum menandai ANC; kode ini tidak menentukan tipe PTM.";
        }else if(group.equals("Z33")){
            route="Kehamilan insidental; tinjau pelayanan aktual";
            detail="Keadaan hamil yang insidental tidak otomatis berarti kunjungan antenatal atau episode PTM. Cocokkan tujuan pelayanan.";
        }else if(group.equals("Z37")){
            route="Hasil persalinan; tinjau INC";
            detail="Kode hasil persalinan. Tinjau kode pelayanan persalinan dan episode kehamilan terkait, bukan memilih tipe PTM dari hasil persalinan.";
        }else if(group.equals("Z39")){
            route="PNC / pemeriksaan nifas";
            detail="Perawatan/pemeriksaan setelah persalinan. Gunakan alur nifas sesuai pelayanan, bukan kunjungan ANC atau tipe PTM otomatis.";
        }else if(code.startsWith("O10.2"))
            detail="Hipertensi dengan penyakit ginjal yang sudah ada sebelum kehamilan. Tinjau PTM uronefrologi (CKD) berdasarkan penyakit ginjal dasarnya; cocokkan Condition N18 atau kriteria layanan. Kode O10.2 sendiri tidak menentukan stadium/penyakit ginjal kronis.";
        else if(code.startsWith("O10.3"))
            detail="Hipertensi dengan penyakit jantung dan ginjal sebelum kehamilan. PTM CAD/CKD perlu ditinjau melalui diagnosis penyerta dan kriteria layanan. Penyakit jantung hipertensif tidak otomatis penyakit arteri koroner.";
        else if(group.equals("O10")||group.equals("O11"))
            detail="Hipertensi yang sudah ada sebelum kehamilan, atau disertai preeklamsia. CAD/CKD dapat ditinjau sebagai PTM penyerta sesuai diagnosis dan kriteria risiko. Tidak dipilihkan satu tipe hanya dari kode hipertensi obstetri.";
        else if(group.equals("O12"))
            detail="Edema/proteinuria gestasional tanpa hipertensi. Proteinuria saja tidak menetapkan CKD; tinjau penyakit ginjal penyerta bila ada. Alur utama mengikuti pelayanan obstetri.";
        else if(group.equals("O13"))
            detail="Hipertensi gestasional. Tidak otomatis sama dengan penyakit arteri koroner (CAD) atau CKD. Tinjau komplikasi kehamilan serta penyakit kronis penyerta secara terpisah.";
        else if(group.equals("O14"))
            detail="Preeklamsia/HELLP sesuai subkode. Bukan dasar otomatis memilih CAD, CVD atau CKD. Gunakan alur pelayanan obstetri; episode PTM tambahan memerlukan penyakit penyerta yang terdokumentasi.";
        else if(group.equals("O15")){
            detail="Eklamsia. Kejang pada eklamsia tidak otomatis merupakan stroke (CVD). Gunakan alur obstetri; bila stroke juga didiagnosis, tinjau Condition serebrovaskular penyertanya.";
            if(code.startsWith("O15.1"))route="INC / eklamsia saat persalinan";
            if(code.startsWith("O15.2"))route="Pelayanan nifas / eklamsia masa nifas";
        }
        else if(group.equals("O16"))
            detail="Hipertensi maternal yang belum diperinci. Perlu kejelasan apakah sudah ada sebelum hamil atau timbul dalam kehamilan; jangan otomatis memilih CAD/CKD.";
        else if(code.startsWith("O24.4"))
            detail="Diabetes yang timbul dalam kehamilan (diabetes gestasional). Tidak otomatis CKD. Gunakan alur obstetri dan tinjau penyakit ginjal kronis hanya jika ada diagnosis penyerta yang sesuai.";
        else if(code.matches("O24\\.[0-3].*"))
            detail="Diabetes yang sudah ada sebelum kehamilan. Tinjau alur PTM uronefrologi sesuai kriteria layanan; kode diabetes ini sendiri tidak membuktikan CKD. Cocokkan dengan Condition penyakit ginjal dan konteks perawatan.";
        else if(group.equals("O24"))
            detail="Diabetes dalam kehamilan. Perinci subkode/riwayat untuk membedakan diabetes sebelumnya dengan diabetes gestasional. Tipe CKD tidak ditentukan hanya dari kata diabetes.";
        else if(code.startsWith("O99.4")){
            detail="Penyakit sistem sirkulasi yang menyertai kehamilan/persalinan/nifas. Kode ini mencakup kelompok luas, sehingga tidak otomatis CAD atau CVD. Cocokkan diagnosis dasarnya: I20–I25 mengarah ke CAD, I60–I69 ke CVD.";
            if(!names.isEmpty())detail+=" Nama penyakit memberi petunjuk "+String.join(" / ",names)+", yang masih perlu dicocokkan dengan Condition penyakit dasar.";
        }else if(code.startsWith("O99.0"))
            detail="Anemia yang menyertai kehamilan/persalinan/nifas. Bukan salah satu dari empat tipe episode PTM ini. Tinjau anemia melalui alur obstetri dan pemeriksaan yang sesuai.";
        else if(code.startsWith("O99.8")){
            detail="Penyakit/kondisi lain yang menyertai kehamilan. Tipe PTM harus mengikuti diagnosis penyakit dasarnya.";
            if(names.contains("cancer"))detail+=" Nama penyakit memberi petunjuk kanker: tinjau Condition C00–C97 sebelum memilih cancer.";
        }else if(group.equals("O23"))
            detail="Infeksi saluran genitourinaria dalam kehamilan. Infeksi ginjal/saluran kemih tidak otomatis CKD. Gunakan alur obstetri; tinjau penyakit ginjal kronis bila didiagnosis tersendiri.";
        else if(code.charAt(0)=='O'&&Integer.parseInt(code.substring(1,3))<=8){
            route="Pelayanan komplikasi/kehilangan kehamilan";
            detail="Kode kelompok kehamilan dengan hasil abortif/komplikasi terkait. Tidak otomatis ANC rutin atau PTM; tentukan alur sesuai pelayanan aktual.";
        }else if(code.charAt(0)=='O'&&Integer.parseInt(code.substring(1,3))>=80&&Integer.parseInt(code.substring(1,3))<=84){
            route="INC / pelayanan persalinan";
            detail="Kode persalinan. Bukan tipe PTM. Episode kehamilan dan pelayanan persalinan ditinjau sesuai alur INC, bukan menandai kunjungan ANC secara otomatis.";
        }else if(code.charAt(0)=='O'&&Integer.parseInt(code.substring(1,3))>=85&&Integer.parseInt(code.substring(1,3))<=92){
            route="Pelayanan nifas / tinjau PNC";
            detail="Komplikasi masa nifas. Fase pelayanan perlu ditinjau melalui alur nifas; episode PTM hanya ditambahkan jika penyakit penyertanya memenuhi kriteria.";
        }else detail="Diagnosis obstetri ini belum menentukan salah satu tipe CAD/CVD/CKD/cancer. Jika pelayanan antenatal, gunakan Atur ANC pada Encounter. Tinjau Condition penyakit kronis penyerta untuk episode PTM tambahan.";
        return new Result("",detail,route,true);
    }
    private static boolean has(String text,String expression){return java.util.regex.Pattern.compile(expression).matcher(text).find();}
}
