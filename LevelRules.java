import javax.swing.JFrame;
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

   static void startRules(JFrame frame2, Game jogo) {
      frame = frame2;
      god = new Item_Select(frame2);
      adition = new Adding_to_Map(frame2);
      game = jogo;
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
