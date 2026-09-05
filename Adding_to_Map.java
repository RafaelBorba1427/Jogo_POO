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
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;

class Adding_to_Map implements ActionListener {
   JDialog dialog;
   boolean active;
   JPanel panel;
   int numero = 2;
   private Map<JButton, Integer> list = new HashMap<>();
   JFrame frame;
   boolean finished = false;
   GameMap current;
   Queue<GameObject> objects = new ArrayDeque<GameObject>();
   int counter = 0, aux_counter = 0;

   Adding_to_Map(JFrame var1) {
      this.frame = var1;

   }

   Queue<GameObject> dialog_init(int var1, int counter, GameMap current) {
      objects.clear();
      this.current = current;

      this.numero = var1;
      this.counter = counter;
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
      JButton local = new JButton();
      local.setBounds(10, 80, 60, 60);
      local.addActionListener(this);
      this.list.put(local, 1);
      this.panel.add(local);

      for (int i = 1; i < numero; i++) {
         int aux;
         // TODO:: change ID for correct ID
         do {
            aux = (int) (Math.random() * 11.0);
         } while (aux == GameObject.ID_BALDE || aux == GameObject.ID_ESTILINGUE);

         int id = aux;
         local = new JButton();
         local.setBounds(10 + i * 70, 80, 60, 60);
         local.addActionListener(this);
         this.list.put(local, id);
         this.panel.add(local);
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
      return objects;
   }

   @Override
   public void actionPerformed(ActionEvent var1) {
      JButton var2 = (JButton) var1.getSource();
      this.panel.remove(var2);
      this.dialog.repaint();
      aux_counter++;

      // current.addObject();
      if (list.get(var2) < GameObject.ID_BUFF_ICED) {
         objects.add(new RigidObj(400, 400, 60, 60, 0, 0.1, true, true, list.get(var2)));
      }

      if (aux_counter == counter) {
         this.finished = true;
         this.dialog.dispose();
         aux_counter = 0;
      }
   }
}
