import java.awt.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
import static java.util.Map.entry;

public class CutsceneOverlay extends JPanel {
  public JLayeredPane laypane;
  public boolean OPEN = true, FIRST_CUP = true, CHANGE_LEVEL = true;
  private String dialogText = "";
  private BufferedImage spriteImage;
  private int spriteX = 100; // freely movable on x-axis
  private int spriteY; // fixed y, "above" the box
  private boolean visible = false;
  public List<BufferedImage> images = new ArrayList<BufferedImage>();
  private static final int BOX_HEIGHT = 120;
  private static final int BOX_MARGIN = 20;
  public int moment = 0, final_moment = 0, sprite_moment = 0;
  GameRules.GameModes next;
  public List<String> talks = new ArrayList<String>(List.of("Yoooooooo bro, whats up homie?\nIve been waiting for you",
      "Lets play some awsome beer pong bro!",
      "The rules are simple: you get x points for hitting platforms,\n y points for hitting buffs and a bonus of z points for hitting the cup",
      "There are also there dope things called buffs.\nThey are like power ups but you get drunker every time \nyou add them to the map",
      "You will get it eventually",
      "When you hit the knarly cup, you are given the broskiets option of choosing what you can put in the map",
      "The teleporter buff lets you traverse through the map, the lag buff gives you lag, the speed buff give you speed, the bounce buf makes your ball bouncy, the clock buff makes you go back in time, and the power buff give your ball POWEEEEEEER!!! ",
      "......", "Yo my homie I am back yo",
      "Now, you reach me\nTHE GOD OF THE MOST AWSOME-EST COOLEST MOST BADD-BOOTY-EST FRAT!!!!",
      "As a prize for being so dope, you get some special items",
      "You can pick between BOMBING THE PLATFORMS THAT YOU PUT, saving the platforms added, healing you life with the BLOOD OF YOU ENEMIES (water with red food coloring), or putting a band-aid on you boboo",
      "you see, I am a very forgetfull dude, so you havae to tell me to save all of the cool platforms and buffs that you put on the map. Or ill just delete them",
      "Se ya broskie"));

  static enum Stage {
    OPEN, FIRST_CUP, CHANGE_LEVEL;
  };

  private Map<Stage, Integer> maxiStagetoIndex = Map.ofEntries(
      entry(Stage.OPEN, 4),
      entry(Stage.FIRST_CUP, 7), entry(Stage.CHANGE_LEVEL, 13));
  private Map<Stage, Integer> miniStagetoIndex = Map.ofEntries(
      entry(Stage.OPEN, 0),
      entry(Stage.FIRST_CUP, 5),
      entry(Stage.CHANGE_LEVEL, 8));// constants

  public CutsceneOverlay(JLayeredPane laypane) {
    try {
      images.add(ImageIO.read(new File("spritesheet/HelloNarator.png")));
      images.add(ImageIO.read(new File("spritesheet/top_left.png")));
      images.add(ImageIO.read(new File("spritesheet/top_right.png")));
      images.add(ImageIO.read(new File("spritesheet/bottom_left.png")));
      images.add(ImageIO.read(new File("spritesheet/bottom_right.png")));

    } catch (Exception e) {
      System.out.println(e);
    }
    setOpaque(false); // so it can sit transparently over the game panel
    this.laypane = laypane;
  }

  public GameRules.GameModes advanceLine() {
    if (moment > final_moment) {
      visible = false;
      return next;
    }
    dialogText = talks.get(moment);
    spriteImage = images.get((int) (Math.random() * 2));
    moment++;
    repaint();
    return GameRules.GameModes.CUTSCENE;

  }

  public void showDialog(Stage stage, GameRules.GameModes next) {
    moment = miniStagetoIndex.get(stage);
    final_moment = maxiStagetoIndex.get(stage);
    visible = true;
    GameRules.current_game_mode = GameRules.GameModes.CUTSCENE;
    advanceLine();
    this.next = next;
  }

  private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight, int maxLines) {
    FontMetrics fm = g2.getFontMetrics();
    int lineCount = 0;

    for (String paragraph : text.split("\n")) {
      StringBuilder line = new StringBuilder();
      for (String word : paragraph.split(" ")) {
        String candidate = line.length() == 0 ? word : line + " " + word;
        if (fm.stringWidth(candidate) > maxWidth) {
          if (lineCount >= maxLines)
            return; // stop drawing, don't overflow box
          g2.drawString(line.toString(), x, y);
          y += lineHeight;
          lineCount++;
          line = new StringBuilder(word);
        } else {
          line = new StringBuilder(candidate);
        }
      }
      if (lineCount >= maxLines)
        return;
      g2.drawString(line.toString(), x, y);
      y += lineHeight;
      lineCount++;
    }
  }

  public void hideDialog() {
    this.visible = false;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (!visible)
      return;

    Graphics2D g2 = (Graphics2D) g;
    int panelWidth = getWidth();
    int panelHeight = getHeight();
    int boxTop = panelHeight - BOX_HEIGHT - BOX_MARGIN;

    // Define target scaled dimensions
    int targetWidth = 400;
    int targetHeight = 500;

    if (spriteImage != null) {
      // Enable crisp nearest-neighbor scaling for pixel art
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

      // Calculate Y position using targetHeight so it sits right above the box
      int spriteYPos = boxTop - targetHeight + 100;

      g2.drawImage(spriteImage, spriteX, spriteYPos, targetWidth, targetHeight, null);
    }

    // The dialog box
    g2.setColor(new Color(0, 0, 0, 220)); // semi-transparent black
    g2.fillRoundRect(BOX_MARGIN, boxTop, panelWidth - BOX_MARGIN * 2, BOX_HEIGHT, 12, 12);
    g2.setColor(Color.WHITE);
    g2.drawRoundRect(BOX_MARGIN, boxTop, panelWidth - BOX_MARGIN * 2, BOX_HEIGHT, 12, 12);

    // Text
    int textAreaWidth = panelWidth - BOX_MARGIN * 2 - 40; // minus padding
    int maxLines = BOX_HEIGHT / 20; // lineHeight = 20, so this fits inside box height
    drawWrappedText(g2, dialogText, BOX_MARGIN + 20, boxTop + 40, textAreaWidth, 20, maxLines);
  }
}
