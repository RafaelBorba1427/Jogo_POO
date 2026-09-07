import java.awt.Dimension;
import javax.swing.*;

public class Main {
  static LevelRules rules;
  static JFrame frame;
  static final Dimension DEFAULT_RESOLUTION = new Dimension(800,600);

  public static void main(String[] args) {
    frame = new JFrame("Game");
    // MainMenu menu = new MainMenu(frame);

    // frame.add(menu);
    frame.setSize(DEFAULT_RESOLUTION.width, DEFAULT_RESOLUTION.height);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    AnimationPlayer.initializeAnimationPlayerTimer();
    // Isso não deveria estar no game ao invés de estar na main????
    // rules = new LevelRules(frame);
    // rules.nextLevel();
    //

    // render test, delete later
    Game game = new Game(DEFAULT_RESOLUTION);
    frame.add(game);
    frame.pack();
    game.startGame();
    LevelRules.startRules(frame, game);

    // ------------------------
  }
}
