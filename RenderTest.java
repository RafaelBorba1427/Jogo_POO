import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RenderTest extends JPanel implements MouseListener, KeyListener {
  //---------------------------------------------
  //Game variable
  //---------------------------------------------
  int initial_resolution_x, initial_resolution_y;
  boolean game_over = false;
  GameMap game_map;

  //--------------------------------------------------------
  //render test, delete later 
  //double x_pos, double y_pos, double width, double height, double rotation, boolean rotatable, boolean active, int obj_type, int obj_id
  MovableObj obj_render_test1 = new MovableObj(400f,300f,100f,50f, (Math.PI/4),true,true,0,1);
  //(double x_pos, double y_pos, double radius, boolean active, int obj_id, double elastic_factor)
  BallObj obj_render_test2 = new BallObj(200f,200f,100f, true,0,1);
  //---------------------------------------------------------

  // Initialise all parameters and start the game loop
  public RenderTest(int initial_resolution_x, int initial_resolution_y){
    this.initial_resolution_x = initial_resolution_x;
    this.initial_resolution_y = initial_resolution_y;

    this.setPreferredSize(new Dimension(initial_resolution_x, initial_resolution_y));

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

    game_map = new GameMap((double) initial_resolution_x, (double) initial_resolution_y);

    //render test, delete later
    this.setBackground(Color.BLACK);
    //------------------------

  }



  public void startGame(){

    //render test, delete later
    game_map.addObject(obj_render_test1);
    game_map.addObject(obj_render_test2);
    //------------------------

    Timer timer = new Timer(16, e -> {
        Timer t = (Timer) e.getSource();

        // Stops the game loop if the game is over
        if (gameLoop()) {
            t.stop();
            return;
        }
        repaint();

        //render test, delete later
        obj_render_test1.rotate(0.1);
        game_map.handleCollisions();
        game_map.updateMovingObjs();
        //------------------------
        
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
    Graphics2D g2d = (Graphics2D) g;

    //render test, delete later
    obj_render_test1.drawHitbox(g2d);
    obj_render_test2.drawHitbox(g2d);
    //------------------------

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