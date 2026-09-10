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
import java.awt.Graphics2D;
import java.util.*;

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
   Map<JButton, Integer> list = new HashMap<JButton, Integer>();
   Set<GameObject> list_of_Objects = new HashSet<GameObject>();
   ArrayList<ArrayList<GameObject>> current_map = new ArrayList<ArrayList<GameObject>>();

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

   public void dialogInit(ArrayList<ArrayList<GameObject>> mapList) {
      current_map = mapList;
      this.panel.removeAll();
      JButton local;
      try {
         AnimationPlayer animate;
         animate = new AnimationPlayer("Platform object: " + GameObject.ID_COPY,
               "spritesheet/combined_spritesheet.png",
               16,
               16, GameObject.ID_COPY, 15, 15);

         local = new JButton() {
            {
               setBounds(100, 160, 60,
                     60);
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
         local.addActionListener(this);
         this.first = local;
         this.panel.add(local);
         this.list.put(local, GameObject.ID_COPY);
      } catch (Exception e) {
         System.out.println("Animation error");
      }

      int var3 = -1;
      do {
         var3 = GameObject.ID_BOMB + (int) (Math.random() * GameObject.Quant_GODItems);
      } while (var3 == GameObject.ID_COPY);
      System.out.println("l is " + var3);
      this.id = var3;
      try {
         AnimationPlayer animate;
         animate = new AnimationPlayer("Platform object: " + var3,
               "spritesheet/combined_spritesheet.png",
               16,
               16, var3, 15, 15);

         local = new JButton() {
            {
               setBounds(300, 160, (int) GameRules.sizes.get(GameObject.ID_PLATFORM).x,
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

         local.addActionListener(this);
         this.second = local;
         this.panel.add(this.second);
         this.list.put(local, var3);
      } catch (Exception e) {
         System.out.println("Animation error");
      }

      this.dialog = new JDialog(this.frame, "Choose Your Item", true);
      this.dialog.setUndecorated(true);
      this.dialog.setBackground(new Color(225, 225, 225));
      this.dialog.setAlwaysOnTop(false);
      this.dialog.setDefaultCloseOperation(2);
      this.dialog.setSize(500, 300);
      this.dialog.setLocationRelativeTo(this.frame);
      this.dialog.setContentPane(this.panel);

      System.out.println("This continued");
   }

   @Override

   public void actionPerformed(ActionEvent var1) {
      if (list.get(var1.getSource()) == GameObject.ID_COPY) {
         for (ArrayList<GameObject> obj : current_map) {
            for (GameObject obj2 : obj) {
               list_of_Objects.add(obj2);
            }
         }

         this.finished = true;
         this.dialog.dispose();
         this.dialog = null;
         return;
      }
      if (list.get(var1.getSource()) == GameObject.ID_BOMB) {
         GameRules.current_game_mode = GameRules.GameModes.BOMB_CUTSCENE;
      }
      for (ArrayList<GameObject> obj : GameMap.getAllObjects()) {
         for (GameObject obj2 : obj) {
            if (!list_of_Objects.contains(obj2) && !(obj2.getObjType() == GameObject.PLAYER
                  || obj2.getObjId() == GameObject.ID_PERMANENT_FLOOR
                  || obj2.getObjId() == GameObject.ID_PERMANENT_WALL
                  || obj2.getObjId() == GameObject.ID_BUCKET)) {
               obj2.active = false;
            }
         }
      }
      this.finished = true;
      this.dialog.dispose();
      this.dialog = null;
   }
}
