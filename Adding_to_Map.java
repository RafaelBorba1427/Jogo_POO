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
import javax.swing.*;
import java.awt.*;

class Adding_to_Map implements ActionListener {
   JDialog dialog;
   boolean active;
   JPanel panel;
   int numero = 2;
   private Map<JButton, Integer> list = new HashMap<>();
   JFrame frame;
   boolean finished = false;
   GameMap current;
   Queue<Integer> objects = new ArrayDeque<Integer>();
   int counter = 0, aux_counter = 0;

   Adding_to_Map(JFrame var1) {
      this.frame = var1;

   }

   Queue<Integer> dialog_init(int var1, int counter, GameMap current) {
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
      JButton local;

      try {
         AnimationPlayer animate;
         animate = new AnimationPlayer("Platform object: " + GameObject.ID_PLATFORM,
               "spritesheet/combined_spritesheet.png",
               16,
               16, GameObject.ID_PLATFORM, 15, 15);

         local = new JButton() {
            {
               setBounds(10, 80, (int) GameRules.sizes.get(GameObject.ID_PLATFORM).x,
                     (int) GameRules.sizes.get(GameObject.ID_PLATFORM).y);
               setContentAreaFilled(false);
               setBorderPainted(false);
               setFocusPainted(false);
               setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
               super.paintComponent(g); // clears the panel before drawing (important!)
               Graphics2D g2d = (Graphics2D) g;
               animate.paint(g2d, 0, 0, new Vector2D(this.getWidth(),
                     this.getHeight()), 0.0);

            }
         };

         local.setBounds(10, 80, (int) GameRules.sizes.get(GameObject.ID_PLATFORM).x,
               (int) GameRules.sizes.get(GameObject.ID_PLATFORM).y);
         local.addActionListener(this);
         this.list.put(local, GameObject.ID_PLATFORM);
         this.panel.add(local);

      } catch (Exception e) {
         System.out.println("Animation error");
      }

      for (int i = 1; i < numero; i++) {
         int aux;
         // TODO:: change ID for correct ID
         do {
            aux = (int) (Math.random() * (10));
         } while (aux == GameObject.ID_BUCKET || aux == GameObject.ID_SLINGSHOT);

         int id = aux;
         int xOffset = i;
         try {
            AnimationPlayer animate = new AnimationPlayer("Other object " + id,
                  "spritesheet/combined_spritesheet.png",
                  16,
                  16, id, 15, 15);

            local = new JButton() {

               {
                  setBounds(10 + xOffset * 70, 80, 60, 60);
                  setContentAreaFilled(false);
                  setBorderPainted(false);
                  setFocusPainted(false);
                  setOpaque(false);
               }

               @Override
               protected void paintComponent(Graphics g) {
                  super.paintComponent(g);
                  Graphics2D g2d = (Graphics2D) g;
                  // g2d.setColor(Color.RED);
                  // g2d.fillRect(0, 0, getWidth(), getHeight());
                  animate.paint(g2d, 0, 0, new Vector2D(this.getWidth(),
                        this.getHeight()), 0.0);

               }
            };

            local.addActionListener(this);
            this.list.put(local, id);
            this.panel.add(local);

         } catch (Exception e) {
            System.out.println("Animation error");
         }

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
      System.out.println("Hello world");
      objects.add(list.get(var2));
      if (aux_counter == counter) {
         this.finished = true;
         this.dialog.dispose();
         aux_counter = 0;
      }
   }
}
