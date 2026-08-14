import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JPanel implements MouseListener, KeyListener {
  int resolution_x, resolution_y;

  boolean game_over = false;

  // Initialise all parameters and start the game loop
  public Game(int resolution_x, int resolution_y){
    this.resolution_x = resolution_x;
    this.resolution_y = resolution_y;

    setPreferredSize(new Dimension(resolution_x, resolution_y));

    // Implement input
    addMouseListener(this);
    addKeyListener(this);
    addMouseMotionListener(new MouseMotionListener() {
        @Override
        public void mouseMoved(MouseEvent e) {
          
        }

        @Override
        public void mouseDragged(MouseEvent e) {
          
        }
      }
    );
    setFocusable(true);
    setVisible(true);


    
  }

  public void startGame(){
    Timer timer = new Timer(16, e -> {
        Timer t = (Timer) e.getSource();

        // Stops the game loop if the game is over
        if (gameLoop()) {
            t.stop();
            return;
        }
        repaint();
      }
    );

    timer.start();
  }

  // Main game loop
  // All game logic should be handled here
  // Returns true if game ended so that menu can be displayed again
  public boolean gameLoop(){
    
    return game_over;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    

  }


  @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}