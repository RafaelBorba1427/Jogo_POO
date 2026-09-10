import java.util.Map;
import static java.util.Map.entry;

public class GameRules {
    // settings

    static boolean physics_on = false;
    static boolean debug_mode = false;
    static boolean cheats1 = false;
    static Map<Integer, Vector2D> sizes = Map.ofEntries(
            entry(GameObject.ID_BUFF_ELASTIC_COLLISION, new Vector2D(40, 40)),
            entry(GameObject.ID_BUFF_ICED, new Vector2D(40, 40)),
            entry(GameObject.ID_BUFF_INTANGIBLE, new Vector2D(40, 40)),
            entry(GameObject.ID_BUFF_LAG, new Vector2D(40, 40)),
            entry(GameObject.ID_BUFF_SPEED_BOOST, new Vector2D(40, 40)),
            entry(GameObject.ID_BUFF_TIME_TRAVEL, new Vector2D(40, 40)),
            entry(GameObject.ID_BUCKET, new Vector2D(60, 60)),
            entry(GameObject.ID_FROZEN_PLATFORM, new Vector2D(50, 50)),
            entry(GameObject.ID_WALL, new Vector2D(50, 30)),
            entry(GameObject.ID_PLATFORM, new Vector2D(50, 50)),
            entry(GameObject.ID_TABLE, new Vector2D(50, 50))

    );// constants

    static double GRAVITY = 0.3;
    static boolean global_gravity_on = true;
    static double DEFAULT_FRICTION = 0.3;
    static boolean ballGravity = true;

    // ------------------------------------------------------------
    // Aparencia da bola do jogador
    // ------------------------------------------------------------
    static final int[] BALL_IDS = {
            GameObject.ID_BALL_1,
            GameObject.ID_BALL_2,
            GameObject.ID_BALL_3,
            GameObject.ID_BALL_4
    };

    static final String[] BALL_NAMES = {
            "FRAT KING", "PISSY BOY", "NERD", "PRINCESA"
    };

    static int selected_ball_id = GameObject.ID_BALL_1;

    static int getSelectedBallIndex() {
        for (int i = 0; i < BALL_IDS.length; i++) {
            if (BALL_IDS[i] == selected_ball_id)
                return i;
        }
        return 0;
    }

    static enum GameModes {
        GAMELOOP, EDIT, BOMB_CUTSCENE, CUTSCENE;
    };

    static GameModes current_game_mode = GameModes.GAMELOOP;

}
