import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Game extends JPanel implements MouseListener, KeyListener {
  // ---------------------------------------------
  // Game variable
  // ---------------------------------------------
  boolean game_over = false;
  boolean show_hit_boxes = true;
  GameMap game_map;
  Camera game_camera;

  // --------------------------------------------------------
  // render test, delete later
  // double x_pos, double y_pos, double width, double height, double rotation,
  // boolean rotatable, boolean active, int obj_type, int obj_id
  
  EventTriggerObj balde = new EventTriggerObj(400f, 300f, 50f, 40f, (Math.PI / 4), true, true, GameObject.ID_BUCKET);

  RigidObj obj_render_test3 = new RigidObj(100f, 300f, 50f, 40f, (Math.PI / 4), GameRules.DEFAULT_FRICTION, true, true,
      GameObject.ID_PLATFORM);

  // (double x_pos, double y_pos, double radius, boolean active, int obj_id,
  // double elastic_factor)
  BallObj obj_render_test2 = new BallObj(200f, 200f, 32f, 1, GameRules.DEFAULT_FRICTION, true, GameObject.ID_BALL_2, 0.8);
  static BallObj pingbongBall = new BallObj(700f, 200f, 32f, 1, GameRules.DEFAULT_FRICTION, true, GameObject.ID_BALL_1, 0.8);

  // ------------ArrayList with items from item_select
  Queue<GameObject> item_select_list = new ArrayDeque<GameObject>();
  // ---------------------------------------------------------

  // Initialise all parameters and start the game loop
  public Game(Dimension resolution) {
    this.setPreferredSize(resolution);

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

    //map can have any size, this is just temporary
    game_map = new GameMap((double) resolution.width, (double) resolution.height, new Vector2D(100,100));
    game_camera = new Camera(GameMap.player_spawn_position, resolution);
  }

  public void startGame() {

    // render test, delete later
    GameRules.physics_on = true;
    game_map.addObject(balde);
    game_map.addObject(obj_render_test3);
    game_map.addObject(pingbongBall);
    balde.move(300, 300);
    pingbongBall.changeVelocity(0, 0);
    pingbongBall.move(300, 200);
    pingbongBall.setPlayer();
    // ------------------------

    Timer timer = new Timer(16, e -> {
      Timer t = (Timer) e.getSource();

      // Stops the game loop if the game is over
      if (gameLoop()) {
        t.stop();
        return;
      }
      repaint();

      game_map.step(1); // collision and physics simulation
    });

    timer.start();
  }

  // Main game loop
  // All game logic should be handled here
  // Returns true if game ended so that menu can be displayed again
  public boolean gameLoop() {

    return game_over;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    if (GameMap.is_loaded) {

      if (LevelRules.background_image != null) {
            // Draws the image stretched to fill the entire panel
            g2d.drawImage(LevelRules.background_image, 0, 0, Main.frame.getWidth(), Main.frame.getHeight(), this);
        }

      for (ArrayList<GameObject> obj_list : GameMap.getAllObjects()) {
        for (GameObject object : obj_list) {
          if (object.isActive()){
            if(show_hit_boxes) object.drawHitbox(g2d);
            if(object.obj_id != GameObject.ID_INVISIBLE_OBJ)
            object.drawSprite(g2d);
          }
            
        }
      }
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Vector2D xy = new Vector2D(e.getX(), e.getY()).subtract(pingbongBall.getCenterOfMass());
    if (GameRules.current_game_mode == GameRules.GameModes.EDIT && item_select_list.size() != 0) {
      GameObject temp = item_select_list.poll();

      temp.move(e.getX(), e.getY());

      game_map.addObject(temp);
      System.out.println("printed");
      repaint();
      return;
    }
    pingbongBall.velocity = xy.add(pingbongBall.velocity).multiply(0.05 * pingbongBall.inverse_mass);

    if (GameRules.current_game_mode == GameRules.GameModes.EDIT) {
      GameRules.current_game_mode = GameRules.GameModes.GAMELOOP;
      pingbongBall.changeAcceleration(0, GameRules.GRAVITY);

    }

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
