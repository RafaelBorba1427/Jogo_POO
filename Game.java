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
  boolean show_hit_boxes = false;
  static boolean next_level = false; //
  GameMap game_map;
  Camera game_camera;

  // Game loop timer, it is a variable so
  //that it can be ended from the game over screen
  private Timer game_loop_timer;

  // --------------------------------------------------------
  //Player ball
  static BallObj pingbongBall;
  // ---------------------------------------------------------

  // Vida do jogador. Instancia por partida; o LevelRules alcanca por
  // getHealthSystem().
  private HealthSystem health_system;
  // ---------------------------------------------
  // Lancamento por estilingue
  // ---------------------------------------------
  private boolean ball_armed = false;

  private final Slingshot slingshot = new Slingshot();

  // Mouse position in pixels, managed by MouseMotionListener.

  private final Point mouse_pixel_position = new Point();
  private boolean mouse_inside = false;

  // ---------------------------------------------
  // Stale ball detection
  // ---------------------------------------------

  // Regra de perda de vida (combinada, conforme decidido):
  // 1. a bola para depois de um arremesso sem a meta de pontos batida, e
  // 2. o nivel termina sem a meta batida (esse caso vive no LevelRules).
  private boolean trickshot_in_progress = false;
  private int ticks_at_rest = 0;

  // Velocidade abaixo da qual a bola conta como parada.
  private static final double REST_SPEED_THRESHOLD = 0.05;

  // Quantos ticks seguidos parada antes de valer como fim do arremesso
  // (~0.7s a 16ms por tick). Evita punir a bola no topo de uma parabola,
  // onde a velocidade passa perto de zero por um instante.
  private static final int REST_TICKS_REQUIRED = 45;

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
        mouse_pixel_position.setLocation(e.getX(), e.getY());
        mouse_inside = true;
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        mouse_pixel_position.setLocation(e.getX(), e.getY());
        mouse_inside = true;
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

    health_system = new HealthSystem(LevelRules.STARTING_HEARTS, true);
  }

  public HealthSystem getHealthSystem() {
    return health_system;
  }

  public void startGame() {

    // render test, delete later
    GameRules.physics_on = true;
    
    // O sprite da bola vem da escolha feita na tela de opcoes. O obj_id e o
    // que seleciona a linha da combined_spritesheet, entao trocar o id troca a
    // aparencia sem mexer em mais nada.
    pingbongBall = new BallObj(700f, 200f, 45f, 1, GameRules.DEFAULT_FRICTION, true, GameRules.selected_ball_id,
        0.8);

    game_map.addObject(pingbongBall);
    LevelRules.currentMap = Maps.generation(1);
    for (GameObject obj : LevelRules.currentMap) {
      game_map.addObject(obj);
    }
    pingbongBall.changeVelocity(0, 0);
    pingbongBall.move(300, 200);
    pingbongBall.setPlayer();
    armBall();
    // ------------------------

    game_loop_timer = new Timer(16, e -> {
      Timer t = (Timer) e.getSource();

      // Stops the game loop if the game is over
      if (gameLoop()) {
        t.stop();
        return;
      }
      repaint();

      //Reaplicado a cada tick porque o GameMap.step
      // reescreve a aceleracao de todo corpo movel em cada substep.
      updateArmedBall();

      game_map.step(1); // collision and physics simulation

      PointSystem.tickAnimation();
      health_system.tick();
      slingshot.tick();

      // Perda de vida por "a bola parou depois de um lancamento"
      checkBallAtRest();

      if (next_level) {
        next_level = false;
        game_camera.setPosition(game_map.player_spawn_position);
        // Nivel novo, lancamento novo.
        armBall();
      }

      Vector2D ball_pos = pingbongBall.getCenterOfMass(), map_size = game_map.getMapSize();
      if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
        game_camera.follow(ball_pos.x, ball_pos.y, map_size.x, map_size.y);
      }

      else {
        Point componentLocation = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(componentLocation, Main.frame);

        Vector2D xy = GameMap.Pixel_to_MapUnit(new Vector2D(componentLocation.x, componentLocation.y))
            .add(game_camera.map_position);
        game_camera.follow(xy.x, xy.y, map_size.x, map_size.y);
      }

    });

    game_loop_timer.start();
  }

  // Encerra o laco. Chamado pelo GameFlow ao sair da partida.
  public void stopGame() {
    game_over = true;
    if (game_loop_timer != null) {
      game_loop_timer.stop();
      game_loop_timer = null;
    }
  }

  // Main game loop
  // All game logic should be handled here
  // Returns true if game ended so that menu can be displayed again
  public boolean gameLoop() {

    return game_over;
  }

  // ------------------------------------------------------------
  // Lancamento por estilingue
  // ------------------------------------------------------------

  // Deixa a bola pronta para UM lancamento.
  void armBall() {
    if (pingbongBall == null)
      return;

    ball_armed = true;
    trickshot_in_progress = false;
    ticks_at_rest = 0;

    pingbongBall.changeVelocity(0, 0);
    pingbongBall.changeAngularVelocity(0);
    pingbongBall.changeAcceleration(0, 0);
    pingbongBall.changeNoGravityStatus(true);

    slingshot.cancel();
  }

  public boolean isBallArmed() {
    return ball_armed;
  }

  // Converte a posicao do mouse (pixels do painel) para unidades de mapa.
  private Vector2D mouseInMapUnits() {
    return GameMap.Pixel_to_MapUnit(
        new Vector2D(mouse_pixel_position.x, mouse_pixel_position.y))
        .add(game_camera.map_position);
  }

  // Segura a bola no lugar e mantem o estilingue mirando enquanto ela estiver
  // armada.
  private void updateArmedBall() {
    if (!ball_armed || pingbongBall == null)
      return;

    pingbongBall.changeVelocity(0, 0);
    pingbongBall.changeAngularVelocity(0);
    pingbongBall.changeNoGravityStatus(true);

    if (GameRules.current_game_mode != GameRules.GameModes.GAMELOOP)
      return;

    if (mouse_inside) {
      slingshot.aim(pingbongBall.getCenterOfMass(), pingbongBall.getDiameter(), mouseInMapUnits());
    }
  }

  // O unico lugar que da velocidade a bola por input do jogador.
  private void launchBall(Vector2D target) {
    if (!ball_armed || pingbongBall == null)
      return;

    ball_armed = false;

    pingbongBall.changeNoGravityStatus(false);
    pingbongBall.changeAcceleration(0, GameRules.GRAVITY);

    // Mesma formula do empurrao antigo, agora aplicada UMA vez.
    pingbongBall.velocity = target.subtract(pingbongBall.getCenterOfMass())
        .multiply(0.08 * pingbongBall.inverse_mass);

    slingshot.release(pingbongBall.getCenterOfMass(), target);

    trickshot_in_progress = true;
    ticks_at_rest = 0;
  }

  // ------------------------------------------------------------
  // Perda de vida
  // ------------------------------------------------------------

  private void checkBallAtRest() {
    if (!trickshot_in_progress || pingbongBall == null)
      return;
    if (GameRules.current_game_mode != GameRules.GameModes.GAMELOOP)
      return;
    // Uma bola armada esta parada de proposito, esperando o lancamento.
    if (ball_armed)
      return;

    // Enquanto o LAG estiver congelando a bola ela esta parada de mentira.
    if (BuffSystem.isLagFrozen()) {
      ticks_at_rest = 0;
      return;
    }

    double speed = pingbongBall.getLinearVelocity().length();

    if (speed > REST_SPEED_THRESHOLD) {
      ticks_at_rest = 0;
      return;
    }

    ticks_at_rest++;
    if (ticks_at_rest < REST_TICKS_REQUIRED)
      return;

    ticks_at_rest = 0;
    trickshot_in_progress = false;


    boolean is_dead = health_system.takeDamageAndCheckDeath();
    if (is_dead) {
      triggerGameOver();
      return;
    }

    // Ainda tem vida: devolve a bola ao ponto de partida e rearma o
    // lancamento. Os pontos potenciais do nivel NAO sao descartados, senao a
    // meta do nivel seria inalcancavel para quem precisa de mais de um
    // arremesso.
    respawnBall();
  }

  private void respawnBall() {
    pingbongBall.move(game_map.getPlayerSpawn());
    pingbongBall.changeRotation(0);
    pingbongBall.changeAngularAcceleration(0);
    game_camera.setPosition(game_map.getPlayerSpawn());
    armBall();
  }

  // Fim de jogo: para o laco e entrega o controle ao GameFlow, que abre a
  // GameOverScreen e volta ao menu depois.
  void triggerGameOver() {
    stopGame();
    long final_points = PointSystem.getDisplayPoints();
    SwingUtilities.invokeLater(() -> GameFlow.showGameOver(final_points));
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

      // O estilingue e desenhado em espaco de MUNDO, junto com os objetos e
      // por cima deles, para acompanhar a bola quando a camera se move.
      slingshot.draw(obj_g2d);

      // Previa da area de explosao enquanto a bomba ainda nao foi posicionada.
      if (GameRules.current_game_mode == GameRules.GameModes.BOMB_CUTSCENE
          && LevelRules.bomb == null && mouse_inside) {
        Vector2D target = mouseInMapUnits();
        double radius = LevelRules.bomradius;

        obj_g2d.setColor(new Color(255, 90, 40, 45));
        obj_g2d.fillOval((int) (target.x - radius), (int) (target.y - radius),
            (int) (radius * 2), (int) (radius * 2));
        obj_g2d.setColor(new Color(255, 150, 60, 200));
        obj_g2d.drawOval((int) (target.x - radius), (int) (target.y - radius),
            (int) (radius * 2), (int) (radius * 2));
      }

      obj_g2d.dispose();

      // ------------------------------------------------------------------
      // HUD
      // ------------------------------------------------------------------
      PointSystem.drawHud(g2d, getWidth(), getHeight());
      health_system.draw(g2d, getWidth(), getHeight());
      BuffSystem.drawHud(g2d, getWidth(), getHeight());

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

    if (GameRules.current_game_mode == GameRules.GameModes.BOMB_CUTSCENE) {
      if (LevelRules.bomb != null)
        return;

      BombObj bomb = new BombObj(xy.x, xy.y, LevelRules.bomdiameter,
          LevelRules.bomticks, LevelRules.bomradius);

      // addObject recusa um objeto que nasce sobreposto a outro. Antes a
      // referencia era guardada mesmo assim e o modo travava sem bomba nenhuma.
      if (!game_map.addObject(bomb))
        return;

      LevelRules.bomb = bomb;
      return;
    }

    if (GameRules.current_game_mode == GameRules.GameModes.EDIT && item_select_list.size() > 0) {

      int temp_num;
      GameObject temp; 
      temp_num = item_select_list.poll();

      temp = new RigidObj(xy.x, xy.y, GameRules.sizes.get(temp_num).x, GameRules.sizes.get(temp_num).y, 0, 0, true,
          true, temp_num);

      if (temp_num >= GameObject.ID_BUFF_ICED) {
        temp = new BuffObj(xy.x, xy.y, GameRules.sizes.get(temp_num).x,
            GameRules.sizes.get(temp_num).y, 0, true, true, GameObject.BUFF_OBJ, temp_num);
      }
      game_map.addObject(temp);
      repaint();
      return;

    }

    // Fim do modo de edicao. O clique que fecha a edicao NAO lanca a bola:
    // ele so devolve o controle ao jogador, com a bola armada esperando o
    // proximo clique.
    if (GameRules.current_game_mode == GameRules.GameModes.EDIT) {
      GameRules.current_game_mode = GameRules.GameModes.GAMELOOP;
      armBall();
      return;
    }

    if (GameRules.current_game_mode == GameRules.GameModes.GAMELOOP) {
      if (ball_armed) {
        launchBall(xy);
      }
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
    mouse_inside = true;
  }

  @Override
  public void mouseExited(MouseEvent e) {
    mouse_inside = false;
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  // ------------------------------------------------------------
  // Teclas de debug
  //
  //   0  liga/desliga o desenho das hitboxes
  //   1  teleporta a bola para a posicao atual do mouse
  //
  // Os dois codigos de tecla sao aceitos (fileira de numeros e teclado
  // numerico), porque VK_0 e VK_NUMPAD0 sao teclas diferentes para o Swing.
  // ------------------------------------------------------------
  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();

    if (key == KeyEvent.VK_0 || key == KeyEvent.VK_NUMPAD0) {
      show_hit_boxes = !show_hit_boxes;
      System.out.println("[debug] hitboxes: " + (show_hit_boxes ? "on" : "off"));
      repaint();
      return;
    }

    if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
      if (pingbongBall == null || !mouse_inside)
        return;

      // Move pelo CENTRO de massa, para a bola cair exatamente sob o cursor
      // em vez de encostar nele com o canto superior esquerdo.
      Vector2D target = mouseInMapUnits();
      pingbongBall.changeCenterOfMass(target);
      pingbongBall.changeAngularVelocity(0);
      pingbongBall.changeAngularAcceleration(0);

      // Rearma o lancamento: teleportar a bola e o comeco de uma tentativa
      // nova, e sem isso ela ficaria parada no lugar novo sem nada para fazer.
      armBall();

      System.out.println("[debug] bola movida para " + target);
      repaint();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }
}
