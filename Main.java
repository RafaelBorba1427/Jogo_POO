import java.awt.Dimension;
import javax.swing.*;

// ------------------------------------------------------------
// Main
// ------------------------------------------------------------

public class Main {
  static JFrame frame;
  static final Dimension DEFAULT_RESOLUTION = new Dimension(800, 600);

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Main::boot);
  }

  private static void boot() {
    frame = new JFrame("Game");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(DEFAULT_RESOLUTION.width, DEFAULT_RESOLUTION.height);

    // Subsistemas globais, antes de qualquer tela pedir som ou sprite.
    AnimationPlayer.initializeAnimationPlayerTimer();
    SoundEffectPlayer.initialiseSoundEffectPlayer();
    MusicPlayer.initialiseMusicPlayer();

    GameFlow.initialise(frame);
    GameFlow.showMenu();

    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
