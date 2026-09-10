import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

public class Game extends JPanel implements MouseListener, KeyListener {
  // ---------------------------------------------
  // Game variable
  // ---------------------------------------------
  boolean game_over = false;
  boolean show_hit_boxes = true;
  static boolean next_level = false; //
  GameMap game_map;
  Camera game_camera;
  static CutsceneOverlay cut;

  // --------------------------------------------------------
  // render test, delete later
  // double x_pos, double y_pos, double width, double height, double rotation,
  // boolean rotatable, boolean active, int obj_type, int obj_id
  public void setOverlay(CutsceneOverlay cut) {
    Game.cut = cut;
  }

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

    game_map.addObject(pingbongBall);
    LevelRules.currentMap = Maps.generation(1);
    for (GameObject obj : LevelRules.currentMap) {
      game_map.addObject(obj);
    }
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
      if (GameRules.current_game_mode == GameRules.GameModes.CUTSCENE) {
        game_camera.setPosition(game_map.player_spawn_position);
        Game.pingbongBall.changeNoGravityStatus(true);
        Game.pingbongBall.changeVelocity(0, 0);
      }

      game_map.step(1); // collision and physics simulation

      if (next_level) {
        next_level = false;
        game_camera.setPosition(game_map.player_spawn_position);
      }

      Vector2D ball_pos = pingbongBall.getCenterOfMass(), map_size = game_map.getMapSize();
      if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
        game_camera.follow(ball_pos.x, ball_pos.y, map_size.x, map_size.y);
      }

      else if (GameRules.current_game_mode != GameRules.GameModes.GAMELOOP) {
        Point componentLocation = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(componentLocation, Main.frame);

        Vector2D xy = GameMap.Pixel_to_MapUnit(new Vector2D(componentLocation.x, componentLocation.y))
            .add(game_camera.map_position);
        game_camera.follow(xy.x, xy.y, map_size.x, map_size.y);
      }
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

      if (GameRules.current_game_mode == GameRules.GameModes.EDIT && item_select_list.size() > 0) {
        Point componentLocation = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(componentLocation, Main.frame);

        try {
          AnimationPlayer animation = new AnimationPlayer("objects1_" + item_select_list.peek(),
              "spritesheet/combined_spritesheet.png", 16, 16, item_select_list.peek(), 1, 1);
          Vector2D dimensions = new Vector2D(GameRules.sizes.get(item_select_list.peek()));
          animation.paint(g2d, (int) GameMap.PIXEL_TO_MAP_UNIT * componentLocation.x,
              (int) GameMap.PIXEL_TO_MAP_UNIT * componentLocation.y,
              GameMap.Pixel_to_MapUnit(dimensions), 0);

        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }

    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Vector2D xy = GameMap.Pixel_to_MapUnit(new Vector2D(e.getX(), e.getY())).add(game_camera.map_position);

    if (GameRules.current_game_mode == GameRules.GameModes.CUTSCENE) {
      GameRules.current_game_mode = cut.advanceLine();
      if (GameRules.current_game_mode == GameRules.GameModes.CUTSCENE) {
        return;
      }
      if (GameRules.current_game_mode == GameRules.GameModes.EDIT) {
        LevelRules.adition.dialog.setVisible(true);

      }
      return;
    }

    if (GameRules.current_game_mode == GameRules.GameModes.BOMB_CUTSCENE) {
      System.out.println("Hi bomb");

      if (LevelRules.bomb_away)
        return;
      LevelRules.bomb = new BallObj(xy.x, xy.y, 60, 20, 0.5, true, GameObject.ID_BOMB, 0.5);
      LevelRules.bomb.changeAcceleration(0, GameRules.GRAVITY);
      game_map.addObject(LevelRules.bomb);
      LevelRules.bomb_away = true;
      return;
    }

    if (GameRules.current_game_mode == GameRules.GameModes.EDIT && item_select_list.size() > 0) {

      int temp_num;
      System.out.print(item_select_list.size() + " is the size");
      GameObject temp;
      temp_num = item_select_list.poll();

      temp = new RigidObj(xy.x, xy.y, GameRules.sizes.get(temp_num).x, GameRules.sizes.get(temp_num).y, 0, 0, true,
          true, temp_num);

      if (temp_num >= GameObject.ID_BUFF_ICED) {
        temp = new BuffObj(xy.x, xy.y, GameRules.sizes.get(temp_num).x,
            GameRules.sizes.get(temp_num).y, 0, true, true, GameObject.BUFF_OBJ, temp_num);
      }
      game_map.addObject(temp);
      System.out.println("printed");
      repaint();
      return;

    }
    if (GameRules.current_game_mode == GameRules.GameModes.EDIT) {
      GameRules.current_game_mode = GameRules.GameModes.GAMELOOP;
      pingbongBall.changeAcceleration(0, GameRules.GRAVITY);
      return;
    }
    if (LevelRules.adition.dialog != null || LevelRules.god.dialog != null) {
      return;
    }
    if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
      pingbongBall.velocity = xy.subtract(pingbongBall.getCenterOfMass()).multiply(0.08 * pingbongBall.inverse_mass)
          .add(pingbongBall.velocity);
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
