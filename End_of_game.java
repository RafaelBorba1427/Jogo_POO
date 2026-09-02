import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class End_of_game extends JDialog {
   End_of_game(JFrame var1, int var2) {
      JPanel var3 = new JPanel();
      var3.setOpaque(true);
      var3.setOpaque(true);
      var3.setLayout(new BorderLayout());
      JLabel var4 = new JLabel("Current points: " + Integer.toString(var2));
      var4.setFont(new Font("TeX Gyre Bonum", 1, 22));
      var4.setHorizontalAlignment(0);
      var4.setBackground(new Color(0, 0, 0, 0));
      var4.setForeground(Color.black);
      var3.add(var4, "South");
      this.setUndecorated(true);
      this.setAlwaysOnTop(true);
      this.setDefaultCloseOperation(2);
      this.setSize(var1.getWidth(), var1.getHeight());
      this.setLocationRelativeTo(var1);
      this.setLayout(new BorderLayout());
      this.setContentPane(var3);
      this.add(var4, "South");
      this.setVisible(true);
   }
}
