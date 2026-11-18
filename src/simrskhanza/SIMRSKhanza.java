/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simrskhanza;

import usu.widget.util.WidgetUtilities;

/**
 *
 * @author khanzasoft
 */
public class SIMRSKhanza {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        StartupProgress.mulai();
        WidgetUtilities.invokeLater(() -> {
           try {
               frmUtama utama=frmUtama.getInstance();
               StartupProgress.tahap(StartupProgress.PROFIL);
               utama.isWall();
               StartupProgress.selesai();
               utama.setVisible(true);
               // dijalankan launcher di latar -> Windows bisa menolak menaruh jendela di depan
               StartupProgress.munculkanJendela(utama);
           } finally {
               // konsol disembunyikan launcher selama loading; munculkan lagi (juga saat startup error)
               StartupProgress.tampilkanKonsol();
           }
        });
    }
    
}
