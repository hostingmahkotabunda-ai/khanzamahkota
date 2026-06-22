package widget;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * HTMLEditorKit yang me-render elemen form &lt;input&gt; TANPA border/kotak,
 * sehingga field teks tampil menyatu dengan sel tabel (seperti teks biasa)
 * namun tetap bisa diketik. Dipakai untuk editor HTML riwayat perawatan.
 *
 * Selain itu kit ini menyimpan referensi tiap field input (berdasarkan atribut
 * 'name'), sehingga nilai live yang sedang diketik bisa diambil untuk dipakai
 * saat cetak/preview lewat {@link #getNilaiForm(String)}.
 *
 * @author khanzasoft
 */
public class HtmlKitFormPolos extends HTMLEditorKit {

    private final Map<String, JTextComponent> formFields = new HashMap<>();

    /** Ambil nilai live (yang sedang diketik) dari field input berdasarkan name. */
    public String getNilaiForm(String name) {
        JTextComponent c = formFields.get(name);
        return c == null ? null : c.getText();
    }

    @Override
    public ViewFactory getViewFactory() {
        return new HTMLFactory() {
            @Override
            public View create(Element elem) {
                Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
                if (o instanceof HTML.Tag && HTML.Tag.INPUT.equals(o)) {
                    return new FormView(elem) {
                        @Override
                        protected Component createComponent() {
                            Component c = super.createComponent();
                            if (c instanceof JComponent) {
                                JComponent jc = (JComponent) c;
                                jc.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                                jc.setOpaque(false);
                                jc.setBackground(new Color(255, 255, 255));
                                jc.setFont(new java.awt.Font("Tahoma", 0, 11));
                                jc.setForeground(new Color(50, 50, 50));
                            }
                            if (c instanceof JTextComponent) {
                                Object nm = getElement().getAttributes().getAttribute(HTML.Attribute.NAME);
                                if (nm != null) {
                                    formFields.put(nm.toString(), (JTextComponent) c);
                                }
                            }
                            return c;
                        }
                    };
                }
                return super.create(elem);
            }
        };
    }
}
