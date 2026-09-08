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
  MovableObj obj_render_test7 = new MovableObj(1000f, 400f, 67, 40, 0, 1, GameRules.DEFAULT_FRICTION, true, true, true,
      GameObject.ID_PERMANENT_WALL, 0.3);
  MovableObj obj_render_test6 = new MovableObj(900f, 400f, 80, 180, 0, 1, GameRules.DEFAULT_FRICTION, true, true, true,
      GameObject.ID_PERMANENT_WALL, 0.3);
  MovableObj obj_render_test5 = new MovableObj(800f, 400f, 99, 300, 0, 1, GameRules.DEFAULT_FRICTION, true, true, true,
      GameObject.ID_PERMANENT_WALL, 0.3);
  MovableObj obj_render_test4 = new MovableObj(700f, 400f, 30, 400, 0, 1, GameRules.DEFAULT_FRICTION, true, true, true,
      GameObject.ID_PERMANENT_WALL, 0.3);
  RigidObj obj_render_test3 = new RigidObj(100f, 300f, 50f, 40f, (Math.PI / 4), GameRules.DEFAULT_FRICTION, true, true,
      GameObject.ID_PLATFORM);

  BallObj obj_render_test2 = new BallObj(200f, 200f, 60f, 1, GameRules.DEFAULT_FRICTION, true, GameObject.ID_BALL_2,
      0.8);
  static BallObj pingbongBall = new BallObj(700f, 200f, 45f, 1, GameRules.DEFAULT_FRICTION, true, GameObject.ID_BALL_1,
      0.8);
  // ---------------------------------------------------------

  // ArrayList with items from item_select
  Queue<Integer> item_select_list = new ArrayDeque<Integer>();

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

    // map can have any size, this is just temporary
    // map unit: 1000,750 <=> pixel: 800,600
    Vector2D player_spawn_location = new Vector2D(100, 500);
    game_map = new GameMap(2000, 1000, player_spawn_location);
    game_camera = new Camera(game_map.getPlayerSpawn(), resolution);
  }

  public void startGame() {

    // render test, delete later
    GameRules.physics_on = true;
    game_map.addObject(obj_render_test7);
    game_map.addObject(obj_render_test6);
    game_map.addObject(obj_render_test5);
    game_map.addObject(obj_render_test4);
    game_map.addObject(obj_render_test3);
    game_map.addObject(obj_render_test2);
    game_map.addObject(pingbongBall);

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

      Vector2D ball_pos = pingbongBall.getCenterOfMass(), map_size = game_map.getMapSize();
      game_camera.follow(ball_pos.x, ball_pos.y, map_size.x, map_size.y);
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
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

    if (GameMap.is_loaded) {

      if (LevelRules.background_image != null) {
        // Draws the image stretched to fill the entire panel
        Vector2D map_size = game_map.getMapSize();
        int sx = (int) (LevelRules.bg_dimensions.width * game_camera.map_position.x / map_size.x);
        int sy = (int) (LevelRules.bg_dimensions.height * game_camera.map_position.y / map_size.y);
        int width = (int) (LevelRules.bg_dimensions.width * game_camera.size_game_units.x / map_size.x);
        int height = (int) (LevelRules.bg_dimensions.height * game_camera.size_game_units.y / map_size.y);

        g.drawImage(LevelRules.background_image,
            0, 0, game_camera.size.width, game_camera.size.height, // fill the whole screen
            sx, sy, sx + width, sy + height, // a part of the background
            null);
      }

      // ------------------------------------------------------------------
      // Object Culling implementation
      // ------------------------------------------------------------------

      // AABB of the camera in Map units
      AABB camera_view = new AABB(
          game_camera.map_position.x,
          game_camera.map_position.y,
          game_camera.map_position.x + game_camera.size_game_units.x,
          game_camera.map_position.y + game_camera.size_game_units.y);

      double scale = GameMap.MAP_UNIT_TO_PIXEL;

      Graphics2D obj_g2d = (Graphics2D) g2d.create();
      obj_g2d.scale(scale, scale);
      obj_g2d.translate(-game_camera.map_position.x, -game_camera.map_position.y);

      for (ArrayList<GameObject> obj_list : GameMap.getAllObjects()) {
        for (GameObject object : obj_list) {
          if (!object.isActive())
            continue;

          // Skip objects outside the camera
          if (object.getHitBox() != null && !object.getHitBox().getAABB().intersect(camera_view))
            continue;

          if (show_hit_boxes)
            object.drawHitbox(obj_g2d);
          if (object.obj_id != GameObject.ID_INVISIBLE_OBJ)
            object.drawSprite(obj_g2d);
        }
      }

      obj_g2d.dispose();

    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Vector2D xy = GameMap.Pixel_to_MapUnit(new Vector2D(e.getX(), e.getY())).add(game_camera.map_position);
    if (GameRules.current_game_mode == GameRules.GameModes.EDIT && item_select_list.size() > 0) {
      int temp_num;
      System.out.print(item_select_list.size() + " is the size");
      GameObject temp;
      temp_num = item_select_list.poll();
      temp = new RigidObj(xy.x, xy.y, 60, 60, 0, 0, true, true, temp_num);
      if (temp_num >= GameObject.ID_BUFF_ICED) {
        temp = new BuffObj(xy.x, xy.y, 60.0, 60.0, 0, true, true, GameObject.BUFF_OBJ, temp_num);
      }
      game_map.addObject(temp);
      System.out.println("printed");
      repaint();
      return;
    } else if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
      pingbongBall.velocity = xy.subtract(pingbongBall.getCenterOfMass()).multiply(0.05 * pingbongBall.inverse_mass)
          .add(pingbongBall.velocity);
    }

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
