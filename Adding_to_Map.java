import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Map;

class Adding_to_Map implements ActionListener {
   JDialog dialog;
   boolean active;
   JPanel panel;
   int numero = 2;
   private Map<JButton, Integer> list = new HashMap<>();
   JFrame frame;
   boolean finished = false;
   LevelRules level;

   Adding_to_Map(JFrame var1) {
      this.frame = var1;
   }

   void dialog_init(int var1, LevelRules var2) {
      this.level = var2;
      this.numero = var1;
      this.panel = new JPanel() {
         Image img = new ImageIcon("spritesheet/Dialog.png").getImage();

         @Override
         protected void paintComponent(Graphics var1) {
            super.paintComponent(var1);
            var1.drawImage(this.img, 0, 0, this.getWidth(), this.getHeight(), this);
         }

      };
      this.panel.setOpaque(false);
      this.panel.setLayout(null);
      JButton var3 = new JButton();
      var3.setBounds(10, 80, 60, 60);
      var3.addActionListener(this);
      this.list.put(var3, 1);
      this.panel.add(var3);

      for (int var4 = 1; var4 < 4; var4++) {
         int var5;
         do {
            var5 = (int) (Math.random() * 11.0);
         } while (var5 == 3 || var5 == 4);

         int var6 = var5;
         var3 = new JButton();
         var3.setBounds(10 + var4 * 70, 80, 60, 60);
         var3.addActionListener(this);
         this.list.put(var3, var6);
         this.panel.add(var3);
      }

      this.dialog = new JDialog(this.frame, "Choose Your Item", true);
      this.dialog.setUndecorated(true);
      this.dialog.setBackground(new Color(0, 0, 0, 0));
      this.dialog.setAlwaysOnTop(true);
      this.dialog.setDefaultCloseOperation(2);
      this.dialog.setSize(300, 200);
      this.dialog.setLocationRelativeTo(this.frame);
      this.dialog.setContentPane(this.panel);
      this.dialog.setVisible(true);
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      JButton var2 = (JButton) var1.getSource();
      this.finished = true;
      this.dialog.dispose();
   }
}
