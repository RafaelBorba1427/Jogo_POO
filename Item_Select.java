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

class Item_Select implements ActionListener {
   JDialog dialog;
   boolean active;
   JPanel panel;
   JButton first;
   JButton second;
   int width = 80;
   int height = 80;
   int number_of_things = 5;
   int pass = 4;
   int id = 1;
   JFrame frame;
   public boolean finished = false;

   Item_Select(JFrame var1) {
      this.frame = var1;
      this.panel = new JPanel() {
         Image img = new ImageIcon("spritesheet/Frat_God.png").getImage();

         @Override
         protected void paintComponent(Graphics var1) {
            super.paintComponent(var1);
            var1.drawImage(this.img, 0, 0, this.getWidth(), this.getHeight(), this);
         }
      };
      this.panel.setOpaque(false);
      this.panel.setLayout(null);
   }

   public void dialogInit() {
      this.panel.removeAll();
      JButton var2 = new JButton("First");
      var2.setBounds(100, 160, 100, 100);
      var2.addActionListener(this);
      var2.setOpaque(false);
      var2.setBorderPainted(false);
      this.first = var2;
      this.panel.add(this.first);

      int var3;
      do {
         var3 = (int) (Math.random() * this.number_of_things);
      } while (var3 == 1);

      System.out.println("l is " + var3);
      this.id = var3;
      var2 = new JButton("Second");
      var2.setBounds(300, 160, 100, 100);
      var2.addActionListener(this);
      var2.setOpaque(false);
      var2.setBorderPainted(false);
      this.second = var2;
      this.panel.add(this.second);
      this.dialog = new JDialog(this.frame, "Choose Your Item", true);
      this.dialog.setUndecorated(true);
      this.dialog.setBackground(new Color(225, 225, 225));
      this.dialog.setAlwaysOnTop(true);
      this.dialog.setDefaultCloseOperation(2);
      this.dialog.setSize(500, 300);
      this.dialog.setLocationRelativeTo(this.frame);
      this.dialog.setContentPane(this.panel);
      this.dialog.setVisible(true);
      System.out.println("This continued");
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      if (var1.getSource() == this.first) {
      }

      this.finished = true;
      this.dialog.dispose();
   }
}
