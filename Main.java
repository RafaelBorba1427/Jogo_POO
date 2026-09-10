import java.awt.Dimension;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
    frame = new JFrame("TRICKSHOT_FRAT_LEGENDS_2_ASCENDANCE_TM");

    // MainMenu menu = new MainMenu(frame);
    // frame.add(menu);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    MusicPlayer.initialiseMusicPlayer();
    GameFlow.initialise(frame);
    GameFlow.showMenu();

    AnimationPlayer.initializeAnimationPlayerTimer();
    SoundEffectPlayer.initialiseSoundEffectPlayer(); // Initialize the sound effect player

    // Isso não deveria estar no game ao invés de estar na main????
    // rules = new LevelRules(frame);
    // rules.nextLevel();

    // setSize define o tamanho da JANELA (com barra de titulo e bordas), o que
    // deixava a area de jogo menor que 800x600. pack() dimensiona pela area
    // util, e a centralizacao tem que vir DEPOIS dele.
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    frame.setResizable(false);
  }
}
