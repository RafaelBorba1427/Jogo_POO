import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

class LevelRules {
   static int bomradius = 40;
   static int bomticks = 60;
   static int bombCounter = 0;
   static int counter = 0;
   static int level_cap = 2;
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
   static ArrayList<GameObject> currentMap = new ArrayList<GameObject>();
   static BallObj bomb;
   static boolean bomb_away = false;
   static List<GameObject> explode = new ArrayList<GameObject>();

   static void startRules(JFrame frame2, Game jogo) {
      frame = frame2;
      god = new Item_Select(frame2);
      adition = new Adding_to_Map(frame2);
      game = jogo;

      // -----------------------------------------------------------------------------------------------
      // change latter
      background_image = SpriteLoader.loadNewBackgroundImage("spritesheet/Frat_background.png");
      bg_dimensions = new Dimension(1536, 1024);
      // -----------------------------------------------------------------------------------------------
   }

   static int generate_cap() {
      return level_cap == 0 ? 1 : level_cap + 2 * (level_cap - 1);
   }

   static void nextLevel(GameMap map) {
      for (GameObject erase : currentMap) {
         GameObject.deactivate(erase);
      }

      if (health == 0) {
         new End_of_game(frame, points);
      } else {
         game.item_select_list = adition.dialog_init(4, 4, map);

         counter++;
         if (Game.cut.FIRST_CUP || (Game.cut.CHANGE_LEVEL && LevelRules.counter >= LevelRules.level_cap)) {
            if (Game.cut.FIRST_CUP)
               Game.cut.showDialog(CutsceneOverlay.Stage.FIRST_CUP, GameRules.GameModes.EDIT);
            else
               Game.cut.showDialog(CutsceneOverlay.Stage.CHANGE_LEVEL, GameRules.GameModes.EDIT);
            GameRules.current_game_mode = GameRules.GameModes.CUTSCENE;
            Game.cut.FIRST_CUP = false;
         } else {
            GameRules.current_game_mode = GameRules.GameModes.EDIT;
            adition.dialog.setVisible(true);
         }
         if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
            GameRules.current_game_mode = GameRules.GameModes.EDIT;
            System.out.println("Game mode edit");
         }

         if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
            GameRules.current_game_mode = GameRules.GameModes.EDIT;
            System.out.println("Game mode edit");
         }
         generate_cap();

      }
      int randomInt = 1 + (int) (Math.random() * ((Maps.number)));
      currentMap = Maps.generation(randomInt);
      for (GameObject add : currentMap) {
         game.game_map.addObject(add);

      }

      Game.pingbongBall.changeVelocity(0, 0);
   }
}
