package bridging;

import java.io.IOException;
import java.util.*;

/** Pencarian referensi saja; tidak mempunyai kemampuan mengirim resource klinis. */
public final class SatuSehatAllergyReferenceService {
    public interface Catalogue { Result search(String query)throws IOException; }
    public static final class Candidate {
        public final String category,system,code,display,source;
        public Candidate(String category,String system,String code,String display,String source){
            this.category=category;this.system=system;this.code=code;this.display=display.trim();this.source=source;
        }
        public boolean exact(String query){return normalizeDisplay(display).equals(normalizeDisplay(query));}
        private static String normalizeDisplay(String value){return SatuSehatAllergyMapping.normalize(value).replaceFirst(" \\(substance\\)$","");}
        public SatuSehatAllergyMapping.Mapping mapping(String original,String chosenCategory){
            SatuSehatAllergyMapping.Mapping m=new SatuSehatAllergyMapping.Mapping(original,chosenCategory,system,code,display,"Alergi "+display);
            m.validate();return m;
        }
        @Override public String toString(){return display+" | "+code+" | "+(category.isEmpty()?"kategori perlu ditinjau":category);}
    }
    public static final class Result {
        public final List<Candidate> candidates;
        public final boolean complete;
        public final String warning;
        public Result(List<Candidate> candidates,boolean complete,String warning){this.candidates=Collections.unmodifiableList(new ArrayList<>(candidates));this.complete=complete;this.warning=warning;}
    }
    public static final class Lookup {
        public final String original,query;
        public final Result result;
        Lookup(String original,String query,Result result){this.original=original;this.query=query;this.result=result;}
        public Candidate automatic(){
            if(!result.complete)return null;
            Candidate found=null;
            for(Candidate c:result.candidates)if(c.exact(query)){
                if(c.category.isEmpty())return null;
                if(found!=null)return null;found=c;
            }
            return found;
        }
    }
    private final Catalogue catalogue;
    private final Map<String,Result> cache=new LinkedHashMap<>();
    public SatuSehatAllergyReferenceService(Catalogue catalogue){this.catalogue=catalogue;}
    public Lookup search(String text)throws IOException{
        String query=SatuSehatAllergySearchTerm.extract(text);
        Result r;
        synchronized(cache){r=cache.get(query);}
        if(r==null){
            r=catalogue.search(query);
            Map<String,Candidate> unique=new LinkedHashMap<>();
            Set<String> conflicts=new HashSet<>();
            for(Candidate c:r.candidates){
                if(!c.code.matches("[0-9]{6,18}")||c.display.isEmpty())continue;
                if(!SatuSehatAllergyMapping.SNOMED.equals(c.system)&&!SatuSehatAllergyMapping.KFA.equals(c.system))continue;
                String key=c.system+"|"+c.code;
                Candidate previous=unique.get(key);
                if(previous!=null&&(!previous.display.equalsIgnoreCase(c.display)||!previous.category.equals(c.category)))conflicts.add(key);
                else unique.put(key,c);
            }
            for(String conflict:conflicts)unique.remove(conflict);
            r=new Result(new ArrayList<>(unique.values()),r.complete&&conflicts.isEmpty(),conflicts.isEmpty()?r.warning:"Referensi kode/display bertentangan; perlu ditinjau.");
            // Gangguan jaringan tidak disimpan sehingga klik berikutnya dapat mencoba kembali.
            if(r.complete)synchronized(cache){if(cache.size()>=100)cache.clear();cache.put(query,r);}
        }
        return new Lookup(text,query,r);
    }
    public boolean saveAutomatic(Lookup lookup,SatuSehatAllergyMapping store)throws IOException{
        Candidate c=lookup.automatic();if(c==null)return false;
        store.saveReference(c.mapping(lookup.original,c.category),c.source,false);return true;
    }
}
