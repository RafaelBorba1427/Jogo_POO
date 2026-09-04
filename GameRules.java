public class GameRules {
    // settings
    static boolean physics_on = false;
    static boolean debug_mode = false;
    static boolean cheats1 = false;

    // constants
    static double GRAVITY = 0.3;
    static double DEFAULT_FRICTION = 0.3;
    static boolean qubeGravity = false;
    static boolean ballGravity = true;

    static enum GameModes {
        GAMELOOP, EDIT;
    };

}
