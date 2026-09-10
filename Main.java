import java.awt.Dimension;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
  static LevelRules rules;
  static JFrame frame;
  static final Dimension DEFAULT_RESOLUTION = new Dimension(800, 600);

  public static void main(String[] args) {
    frame = new JFrame("Game");
    // MainMenu menu = new MainMenu(frame);
    // frame.add(menu);

    AnimationPlayer.initializeAnimationPlayerTimer();
    SoundEffectPlayer.initialiseSoundEffectPlayer(); // Initialize the sound effect player

    // Isso não deveria estar no game ao invés de estar na main????
    // rules = new LevelRules(frame);
    // rules.nextLevel();

    Game game = new Game(DEFAULT_RESOLUTION);
    game.startGame();

    JLayeredPane laypane = new JLayeredPane();
    laypane.setLayout(null); // JLayeredPane manages z-order internally via add(comp, layer) — don't assign a
                             // LayoutManager here

    CutsceneOverlay cut = new CutsceneOverlay(laypane);

    game.setPreferredSize(DEFAULT_RESOLUTION);
    cut.setPreferredSize(DEFAULT_RESOLUTION);
    game.setOverlay(cut);

    laypane.add(cut, JLayeredPane.PALETTE_LAYER);
    laypane.add(game, JLayeredPane.DEFAULT_LAYER);

    game.setBounds(0, 0, DEFAULT_RESOLUTION.width, DEFAULT_RESOLUTION.height);
    cut.setBounds(0, 0, DEFAULT_RESOLUTION.width, DEFAULT_RESOLUTION.height);

    laypane.setPreferredSize(DEFAULT_RESOLUTION); // no LayoutManager to derive this now, so set explicitly

    frame.setContentPane(laypane);
    frame.pack();
    frame.setLocationRelativeTo(null);

    LevelRules.startRules(frame, game);

    frame.setSize(DEFAULT_RESOLUTION.width, DEFAULT_RESOLUTION.height);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    try {
      cut.showDialog(CutsceneOverlay.Stage.OPEN, GameRules.GameModes.GAMELOOP);
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace(); // print full stack trace so you can see exactly where/why it failed, not just
                           // the message
    }

    frame.setVisible(true);
  }
}
