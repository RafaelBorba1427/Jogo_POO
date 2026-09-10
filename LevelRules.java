import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

// ------------------------------------------------------------
// LevelRules
//
// Mudancas desta refatoracao:
//
// 1. "static int health" virou o HealthSystem de verdade. Antes era um contador
//    solto que nunca era decrementado por ninguem: a unica leitura era
//    "if (health == 0)" no nextLevel, e como health comecava em 1 e ninguem
//    escrevia nele, esse if nunca era verdadeiro. A vida agora mora no
//    HealthSystem da instancia de Game.
//
// 2. "static int points" saiu. A pontuacao e do PointSystem, que ja guarda
//    total e potencial.
//
// 3. Meta de pontos por nivel. Cada nivel define um alvo; terminar o nivel sem
//    bater o alvo custa um coracao (a outra metade da regra, "a bola parou sem
//    bater a meta", esta no Game.checkBallAtRest).
//
// 4. End_of_game foi trocado pela GameOverScreen, chamada via GameFlow, que
//    devolve o jogador ao menu.
// ------------------------------------------------------------
class LevelRules {
   static double bomradius = 150;
   static int bomticks = 90;
   static double bomdiameter = 60;
   static int counter = 0;
   static int level_cap = 2;
   static int level_count = 0;
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
   static BombObj bomb;

   // ------------------------------------------------------------
   // Vida e meta de pontos
   // ------------------------------------------------------------

   static final int STARTING_HEARTS = 5;

   // Meta do primeiro nivel e quanto ela sobe a cada nivel vencido.
   static final long BASE_TARGET_POINTS = 300L;
   static final long TARGET_POINTS_PER_LEVEL = 150L;

   static long targetForLevel(int level) {
      return BASE_TARGET_POINTS + TARGET_POINTS_PER_LEVEL * Math.max(0, level);
   }

   static void startRules(JFrame frame2, Game jogo) {
      frame = frame2;
      god = new Item_Select(frame2);
      adition = new Adding_to_Map(frame2);
      game = jogo;
      // Sem isto os itens VILE e BANDAID do Frat God caem em NullPointerException.
      god.current_healthSystem = jogo.getHealthSystem();

      // -----------------------------------------------------------------------------------------------
      // change latter
      background_image = SpriteLoader.loadNewBackgroundImage("spritesheet/Frat_background.png");
      bg_dimensions = new Dimension(1536, 1024);
      // -----------------------------------------------------------------------------------------------

      PointSystem.setTargetPoints(targetForLevel(level_count));
   }

   // Estado de nivel zerado para uma partida nova vinda do menu.
   static void resetForNewGame() {
      counter = 0;
      level_count = 0;
      level_cap = 2;
      bomb = null;
      bateu = false;
      currentMap.clear();
      PointSystem.setTargetPoints(targetForLevel(0));
   }

   static int generate_cap() {
      return level_cap == 0 ? 1 : level_cap + 2 * (level_cap - 1);
   }

   // ------------------------------------------------------------
   // Limpeza do mapa depois do painel do God
   //
   // BUG CORRIGIDO (teto sem hitbox):
   // ------------------------------------------------------------

   static void wipeObjectsNotKept(GameMap map, Set<GameObject> kept) {
      ArrayList<GameObject> permanent = map.getPermanentObjects();

      for (ArrayList<GameObject> obj_list : GameMap.getAllObjects()) {
         if (obj_list == permanent)
            continue;

         for (GameObject obj : obj_list) {
            if (kept.contains(obj))
               continue;

            if (obj.getObjType() == GameObject.PLAYER
                  || obj.getObjId() == GameObject.ID_PERMANENT_FLOOR
                  || obj.getObjId() == GameObject.ID_PERMANENT_WALL
                  || obj.getObjId() == GameObject.ID_BUCKET
                  || obj.getObjId() == GameObject.ID_PINGPONG)
               continue;

            obj.active = false;
         }
      }
   }

   static void nextLevel(GameMap map) {
      for (GameObject erase : currentMap) {
         GameObject.deactivate(erase);
      }


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

      // ---------------------------------------------------------------
      // Fim de nivel: pontos do balde, checagem de meta e vida
      // ---------------------------------------------------------------

      // O balde so pontua aqui, uma vez por nivel.
      PointSystem.addPotentialPoints(PointSystem.pointsForObject(map.getBucket()));

      HealthSystem health = (game != null) ? game.getHealthSystem() : null;

      if (!PointSystem.hasReachedTarget() && health != null) {
         boolean is_dead = health.takeDamageAndCheckDeath();
         if (is_dead) {
            PointSystem.processPoints();
            if (game != null)
               game.triggerGameOver();
            return;
         }
      }

      // Meta batida (ou vida ainda restante): confirma os pontos do nivel e
      // sobe a meta do proximo.
      PointSystem.processPoints();
      level_count++;
      PointSystem.setTargetPoints(targetForLevel(level_count));

      // ---------------------------------------------------------------



      
      if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
         GameRules.current_game_mode = GameRules.GameModes.EDIT;
      }
      generate_cap();

      int randomInt = 1 + (int) (Math.random() * ((Maps.number)));
      currentMap = Maps.generation(randomInt);
      for (GameObject add : currentMap) {
         game.game_map.addObject(add);

      }

      Game.pingbongBall.changeVelocity(0, 0);
   }
}
