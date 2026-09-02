import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Game");
    MainMenu menu = new MainMenu(frame);

    frame.add(menu);
    frame.setSize(800, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    LevelRules rules = new LevelRules(frame);
    rules.nextLevel();
  }
}
