import bridging.SatuSehatKirimEpisodeOfCareMandiri;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.*;

/** GUI lifecycle regression; no database, credentials, or API access. */
public class SatuSehatEpisodeWindowTest {
    private static void require(boolean value,String message){
        if(!value)throw new AssertionError(message);
    }
    public static void main(String[] args)throws Exception{
        String source=new String(Files.readAllBytes(Paths.get("src/simrskhanza/frmUtama.java")),StandardCharsets.UTF_8);
        require(source.contains("addActionListener(this::btnKirimEpisodeOfCareActionPerformed)"),"Menu must use lifecycle handler");
        require(source.contains("this,DlgHome,episodeOfCareDialog,this::isTutup,PanelUtama"),"Handler must close home and track current window");
        require(source.split("Panelmenu.add\\(btnKirimEpisodeOfCareSatuSehat\\)",-1).length-1==3,"Register in category, full menu, and search");
        SwingUtilities.invokeAndWait(()->{
            JFrame owner=new JFrame();
            JDialog home=new JDialog(owner);
            owner.setBounds(-15000,-15000,1000,800);
            home.setBounds(-15000,-15000,1000,800);
            home.setAlwaysOnTop(true);
            final int[] closes={0};
            try{
                owner.setVisible(true);home.setVisible(true);
                SatuSehatKirimEpisodeOfCareMandiri first=SatuSehatKirimEpisodeOfCareMandiri.open(owner,home,null,()->closes[0]++,owner);
                require(!home.isDisplayable(),"Always-on-top home must be disposed");
                require(first.isVisible()&&first.isEnabled(),"Episode must be open and enabled");
                for(int i=0;i<10;i++){
                    require(first==SatuSehatKirimEpisodeOfCareMandiri.open(owner,home,first,()->closes[0]++,owner),"Repeated click duplicated dialog");
                }
                require(closes[0]==1,"Existing window must not be disposed on repeated clicks");
                require(count()==1,"Only one episode window may be displayable");
                first.dispatchEvent(new WindowEvent(first,WindowEvent.WINDOW_CLOSING));
                require(!first.isDisplayable(),"Titlebar X must dispose");
                SatuSehatKirimEpisodeOfCareMandiri second=SatuSehatKirimEpisodeOfCareMandiri.open(owner,home,first,()->closes[0]++,owner);
                require(second!=first&&second.isVisible()&&count()==1,"Reopen after close failed");
                closeButton(second.getContentPane()).doClick();
                require(!second.isDisplayable(),"Tutup must dispose");
                for(int i=0;i<5;i++){
                    SatuSehatKirimEpisodeOfCareMandiri reopened=SatuSehatKirimEpisodeOfCareMandiri.open(owner,home,null,()->{},owner);
                    require(reopened.isVisible()&&count()==1,"Open/close cycle failed");reopened.dispose();
                }
            }finally{
                for(Window w:Window.getWindows())w.dispose();
            }
        });
        System.out.println("PASS: menu registration, overlay disposal, 10 repeated clicks, X/Tutup, 5 reopen cycles; no DB/API.");
    }
    private static int count(){int count=0;for(Window w:Window.getWindows())if(w instanceof SatuSehatKirimEpisodeOfCareMandiri&&w.isDisplayable())count++;return count;}
    private static JButton closeButton(java.awt.Container parent){
        for(java.awt.Component c:parent.getComponents()){
            if(c instanceof JButton&&"closeEpisode".equals(c.getName()))return (JButton)c;
            if(c instanceof java.awt.Container){JButton b=closeButton((java.awt.Container)c);if(b!=null)return b;}
        }
        return null;
    }
}
