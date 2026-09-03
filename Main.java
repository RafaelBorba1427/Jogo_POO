import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Game");
    MainMenu menu = new MainMenu(frame);

    frame.add(menu);
    frame.setSize(800, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);

    // Isso não deveria estar no game ao invés de estar na main????
    LevelRules rules = new LevelRules(frame);
    rules.nextLevel();
    //

    //render test, delete later
    Game game = new Game(800,600);
    frame.add(game);
    frame.pack();
    game.startGame();
    //------------------------
  }
}
