import javax.swing.*;

public class Main {
  public static void main(String[] args){
    JFrame frame = new JFrame("Game");
    
    frame.setSize(800, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);

    //render test, delete later
    Game game = new Game(800,600);
    frame.add(game);
    frame.pack();
    game.startGame();
    //------------------------
  }
}
