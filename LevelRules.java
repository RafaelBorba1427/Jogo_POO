import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import java.io.IOException;
import java.util.ArrayList;

class LevelRules {
   static int counter = 0;
   static int level_cap = 10;
   static int level_count = 0;
   static int health = 1;
   static int points = 0;
   static JFrame frame;
   static Item_Select god;
   static Adding_to_Map adition;
   static boolean bateu = false;
   static int Adding_to_Map_counter = 4;
   ArrayList<GameObject> adicionar;
   static Game game;
   static BufferedImage background_image;
   static Dimension bg_dimensions = new Dimension(); 

   static void startRules(JFrame frame2, Game jogo) {
      frame = frame2;
      god = new Item_Select(frame2);
      adition = new Adding_to_Map(frame2);
      game = jogo;

      //-----------------------------------------------------------------------------------------------
      //change latter 
      background_image = SpriteLoader.loadNewBackgroundImage("spritesheet/Frat_background.png");
      bg_dimensions = new Dimension(1536,1024);
      //-----------------------------------------------------------------------------------------------
   }

   static int generate_cap() {
      return level_cap == 0 ? 1 : level_cap + 2 * (level_cap - 1);
   }

   static void nextLevel(GameMap map) {

      if (health == 0) {
         new End_of_game(frame, points);
      } else {

         counter++;
         game.item_select_list = adition.dialog_init(4, 4, map);

         if (counter >= level_cap) {
            god.dialogInit();

         }
         counter = 0;
         generate_cap();
      }
   }
}
