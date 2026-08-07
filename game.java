import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.math.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Vector;
import java.util.Iterator;

public class game extends JPanel implements MouseListener, KeyListener {
    static int lose_sprite = 0;
    static int sling_counter = 0;
    static Items dialog;
    private Image image;
    static int anime = 0;
    static double anime_help = 0;
    static int point_bonus_anime = 0;
    static Long point_bonus = 0L;
    static boolean fecha = false;
    static boolean hitting = false;
    static int add = 0;
    private BufferedImage offscreen;
    public BufferedImage sheet;
    static Timer timer;
    private Graphics2D offscreenG;
    static JFrame frame;
    static int x_cool_sqr, y_cool_sqr;
    static int w_frame, h_frame;
    static buffSystem buffSys;
    static pointSystem pointSys;
    static healthSystem healthSys;
    static Level lvl;
    static boolean game_start = false;
    static Choice Choice;
    int sprite_col = 16, sprite_lin = 16;
    int option = 0;

    static soundMaster soundMaster = new soundMaster();
    static musicMaster musicMaster = new musicMaster();

    private Image floorImage;
    static end fin;
    static Queue<coisa> list = new LinkedList<>();

    public enum GameModes {
        PLAY, SHOOT, EDIT, SETPOSITION, ITEM_PANEL;
    }

    static double gravity;

    static int x_boundary = 800;
    static int y_boundary = 600;
    static double rescale_factor_x;
    static double rescale_factor_y;
    static double rescale_factor_average;
    static String selected_resolution = "800x600";
    static {
        updateRescaleFactors();
    }

    ball ball = new ball(rescaleX(100), rescaleY(50), rescaleByAverage(20));
    static Color ball_color = Color.WHITE;

    static volatile ArrayList<coisa> lvl_map = new ArrayList<>();
    static volatile Queue<buff> collided = new LinkedList<>();
    static GameModes mode = GameModes.PLAY;
    static game gaming;
    private volatile int x_input;
    private volatile int y_input;
    private volatile boolean mouse_clicked;
    static inicial menu;

    static int rescaleX(int x) {
        return (int) (x * rescale_factor_x);
    }

    static int rescaleY(int y) {
        return (int) (y * rescale_factor_y);
    }

    static int rescaleByAverage(int n) {
        return (int) (n * rescale_factor_average);
    }

    static void updateRescaleFactors() {
        rescale_factor_x = x_boundary / 800.0;
        rescale_factor_y = y_boundary / 600.0;
        rescale_factor_average = (rescale_factor_x + rescale_factor_y) / 2.0;
        gravity = 0.5 * rescale_factor_y;
    }

    static void updateResolution() { // "800x600", "1140x720", "1280x800" ,"1440x900", "1920x1080"
        if (selected_resolution == "800x600") {
            x_boundary = 800;
            y_boundary = 600;
        } else if (selected_resolution == "1140x720") {
            x_boundary = 1140;
            y_boundary = 720;
        } else if (selected_resolution == "1280x800") {
            x_boundary = 1280;
            y_boundary = 800;
        } else if (selected_resolution == "1440x900") {
            x_boundary = 1440;
            y_boundary = 900;
        } else {
            x_boundary = 1920;
            y_boundary = 1080;
        }

        gaming.setPreferredSize(new Dimension(x_boundary, y_boundary));
        menu.setPreferredSize(new Dimension(x_boundary, y_boundary));
        lvl_map.get(0).setX(x_boundary - rescaleX(40));
        lvl_map.get(0).setY(y_boundary - rescaleY(15));
        frame.pack();
    }

    public boolean createEnd() {
        if (fin != null) {
            return false;
        }
        fin = new end(10, this);
        return true;
    }

    public void update_end() {
        fin.update_panel();
    }

    public void SetBallVelocity(int x, int y) {
        ball.setVelocity(x, y);
    }

    public game(String imagePath) {
        try {
            sheet = ImageIO.read(new File("spritesheet/New Piskel(2)(1).png"));
            System.out.println("sheet loaded: " + sheet); // null or not?
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("sheet FAILED to load"); // path wrong?
        }
        image = new ImageIcon(imagePath).getImage();
        floorImage = new ImageIcon("floor.png").getImage();
        addMouseListener(this);
        addKeyListener(this);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(x_boundary, y_boundary));
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                x_input = e.getX(); // updates every time the mouse moves
                y_input = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                x_input = e.getX(); // updates every time the mouse moves
                y_input = e.getY();
            }
        });
        setFocusable(true);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (ball.getX() >= j * w_frame && ball.getX() < (j + 1) * w_frame && ball.getY() >= i * h_frame
                        && ball.getY() < (i + 1) * h_frame) {
                    x_cool_sqr = j;
                    y_cool_sqr = i;
                }

            }
        }
        // lvl_map.add(new coisa(400, 300, 40, 0));

        setVisible(true);

        musicMaster.changeTrackAndPlay("menu");
    }

    public static void main(String[] args) {
        gaming = new game("Frat_background.png");
        frame = new JFrame("Ball Game");
        updateRescaleFactors();
        // frame.add(gaming);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> gaming.requestFocusInWindow());

        gaming.frame = frame;
        menu = new inicial(gaming);
        frame.add(menu);
        frame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 4,
                Toolkit.getDefaultToolkit().getScreenSize().height / 8);

        // debug buffs
        // buffSys.ApplyBuff(buffSystem.buffs.TIME_TRAVEL, 10);

        Choice = new Choice(gaming);

        dialog = new Items(gaming, 2);
        buffSys = new buffSystem();
        healthSys = new healthSystem(5, true);
        gaming.add(healthSys); // Adds health as a panel on gaming
        pointSys = new pointSystem();

        lvl = new Level();
        lvl_map.add(new coisa(x_boundary - rescaleX(40), y_boundary - rescaleY(15), rescaleByAverage(40),
                coisa.ID_BALDE, gaming));

        frame.pack();
        w_frame = (int) Math.ceil(frame.getWidth() / 4.0);
        h_frame = (int) Math.ceil(frame.getHeight() / 4.0);

        timer = new Timer(16, e -> {
            if (fin != null)
                fin.update_panel();
            if (lose_sprite == 0) {
                lose_sprite = 1;
            } else
                lose_sprite = 0;

            gaming.update();
            if (game_start)
                gaming.repaint();
            else
                menu.repaint();
            buffSys.DecrementBuffTimers();
            if (point_bonus_anime != 0)
                point_bonus_anime--;
            anime_help++;
            if (anime_help > 4) {
                anime_help = 0;
                anime++;
                if (gaming.mode == GameModes.SHOOT && sling_counter < 5)
                    sling_counter++;
                else if (gaming.mode != GameModes.SHOOT && sling_counter != 0)
                    sling_counter = 0;

            }

            if (anime >= 15) {
                anime = 0;
            }
            if (fecha) {
                frame.dispose();
            }
        });

        timer.start();
    }

    public void EditCouse() {

        if (list.isEmpty()) {
            mode = GameModes.PLAY;
            ball.enable_physics = true;
            game.pointSys.processPoints();
            game.pointSys.removePotentialPoints();
            return;
        }

        if (mouse_clicked) {
            lvl_map.add(list.peek());
            list.peek().setX(x_input);
            list.peek().setY(y_input);
            list.poll();

            mouse_clicked = false;
        }
    }

    // Added a tracker to check if a trick shot is in progress
    boolean trickshot_in_progress = false;

    public void update() {
        if (ball.enable_physics)
            ball.update();
        if (mode == GameModes.EDIT) {
            ball.update();

            EditCouse();

            while (!collided.isEmpty()) {
                lvl_map.add(collided.peek());
                collided.poll();
            }

            return;
        }
        // use mouse input to change ball trajectory
        else if (mouse_clicked) {
            if (mode == GameModes.SHOOT) {
                ball.setVelocity(0, 0);
                applyMouseInput();
                trickshot_in_progress = true;
            } else if (mode == GameModes.SETPOSITION) {
                // System.out.println("Processing click: mode = " + mode);
                ball.setPosition(x_input, y_input);
                ball.setVelocity(0, 0);
            }

            mode = GameModes.PLAY;
            ball.enable_physics = true;
            mouse_clicked = false;
        }

        Iterator<coisa> it = lvl_map.iterator();
        while (it.hasNext()) {
            coisa c = it.next();

            if (c instanceof buff b) {
                b.verify(ball);
                if (b.bateu == true) {
                    System.out.println("Bateu no buff");
                    b.bateu = false;
                    collided.add(b);
                    it.remove();
                }
            } else {
                c.verify(ball);
            }
        }

        if (add == lvl_map.size()) {
            hitting = false;
        }
        add = 0;

        // Check if the ball has stopped moving after a trick shot
        // False flags ball as dead on goal hit and on the hand debuff
        if (trickshot_in_progress && ball.getXVel() == 0 && ball.getYVel() == 0) {
            trickshot_in_progress = false;
            boolean is_dead = healthSys.takeDamageAndCheckDeath();

            if (is_dead) {
                // Handle game over logic here
                // System.out.println("Game Over!");
                SwingUtilities.invokeLater(() -> createEnd());
                // Implement game over logic
            }
        }
        // clears the console
        // System.out.print("\033[H\033[2J"); System.out.flush();
        /*
         * System.out.println("Ball position: (" + ball.getX() + ", " + ball.getY() +
         * ")");
         * System.out.println("Ball velocity: (" + ball.getXVel() + ", " +
         * ball.getYVel() + ")");
         */

    }

    public void applyMouseInput() {

        int x_diff = x_input - ((int) ball.getX());
        int y_diff = y_input - ((int) ball.getY());
        ball.x_vel += (x_diff > 0) ? Math.min(x_diff * 0.1, rescaleX(15)) : Math.max(x_diff * 0.1, -rescaleX(15));
        ball.y_vel += (y_diff > 0) ? Math.min(y_diff * 0.1, rescaleY(15)) : Math.max(y_diff * 0.1, -rescaleY(22));

        x_input = 0;
        y_input = 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);

        g.drawImage(floorImage, 0, getHeight() - getHeight() / 4,
                getWidth(), getHeight() / 4, this);

        // Recreate buffer if needed
        if (offscreen == null || offscreen.getWidth() != getWidth() || offscreen.getHeight() != getHeight()) {
            offscreen = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            offscreenG = offscreen.createGraphics();
            offscreenG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            offscreenG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }

        offscreenG.setComposite(AlphaComposite.Clear);
        offscreenG.fillRect(0, 0, getWidth(), getHeight());
        offscreenG.setComposite(AlphaComposite.SrcOver);

        // Draw ball in mode color
        if (mode == GameModes.EDIT) {
            offscreenG.setColor(Color.BLUE);
        } else if (mode == GameModes.SETPOSITION) {
            offscreenG.setColor(Color.GREEN);
        } else if (mode == GameModes.SHOOT) {
            offscreenG.setColor(Color.RED);
        } else {
            offscreenG.setColor(ball_color);
        }

        // --- Compute sling's destination rect, pivot, and angle ONCE, shared
        // everywhere ---
        boolean aiming = (mode == GameModes.SHOOT);
        double centerX = 0, centerY = 0, angle = 0;
        double slingDx1 = 0, slingDx2 = 0, slingDy1 = 0, slingDy2 = 0;

        if (aiming) {
            slingDx1 = ball.getX() - 2 * ball.getWidth() + 20;
            slingDx2 = ball.getX() + ball.getWidth() / 2 + 20;
            slingDy1 = ball.getY() - 2 * ball.getHeight() + 25;
            slingDy2 = ball.getY() + ball.getHeight() / 2 + 25;

            centerX = (slingDx1 + slingDx2) / 2.0;
            centerY = (slingDy1 + slingDy2) / 2.0;

            double dx = x_input - centerX;
            double dy = y_input - centerY;
            angle = Math.atan2(dy, dx);
        }

        // --- Ball position: orbit around the same pivot while aiming ---
        double ballDrawX, ballDrawY;
        if (aiming) {
            double pouchRadius = rescaleByAverage(20); // tune so the ball sits in the pouch
            ballDrawX = centerX + pouchRadius * Math.cos(angle);
            ballDrawY = centerY + pouchRadius * Math.sin(angle);
        } else {
            ballDrawX = ball.getPaintX();
            ballDrawY = ball.getPaintY();
        }

        offscreenG.fillOval(
                (int) (ballDrawX - ball.getDiameter() / 2),
                (int) (ballDrawY - ball.getDiameter() / 2),
                (int) ball.getDiameter(),
                (int) ball.getDiameter());

        // Draw obstacles
        for (coisa c : lvl_map) {
            g.drawImage(
                    sheet,
                    c.x - c.width / 2,
                    c.y - c.height / 2,
                    c.x + c.width / 2,
                    c.y + c.height / 2,
                    anime * sprite_col,
                    c.id * sprite_lin,
                    anime * sprite_col + sprite_col,
                    c.id * sprite_lin + sprite_lin,
                    null);
        }

        // Draw transparent buffer over background
        g.drawImage(offscreen, 0, 0, null);

        // PointSystem HUD
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int hudWidth = rescaleX(220);
        int hudHeight = rescaleY(80);
        int hudX = getWidth() - hudWidth - rescaleX(20);
        int hudY = rescaleY(20);

        g2.setColor(new Color(170, 8, 0, 65));
        g2.fillRoundRect(hudX, hudY, hudWidth, hudHeight, 10, 15);

        g2.setColor(new Color(173, 133, 0));

        g2.setFont(new Font("TeX Gyre Bonum", Font.BOLD, rescaleY(22)));
        g2.drawString("Total Points: " + pointSys.getPoints(), hudX + 15, hudY + 30);

        g2.setFont(new Font("TeX Gyre Bonum", Font.BOLD, rescaleY(18)));
        g2.drawString("Points: " + pointSys.getPotentialPoints(), hudX + 15, hudY + 60);

        if (point_bonus_anime != 0) {
            String pointsText = "Points: " + pointSys.getPotentialPoints();
            FontMetrics fm = g2.getFontMetrics();
            String plus_minus = (point_bonus >= 0) ? " +" : " ";
            g2.drawString(plus_minus + point_bonus, hudX + 15 + fm.stringWidth(pointsText), hudY + 60);
        }

        // --- Sling drawing reuses the SAME rect/angle/pivot computed above ---
        if (aiming) {
            BufferedImage slingFrame = sheet.getSubimage(
                    sling_counter * sprite_col,
                    coisa.ID_ESTILINGUE * sprite_lin,
                    sprite_col,
                    sprite_lin);

            g2.rotate(angle, centerX, centerY);
            g2.drawImage(slingFrame,
                    (int) slingDx1, (int) slingDy1,
                    (int) (slingDx2 - slingDx1), (int) (slingDy2 - slingDy1),
                    null);
        }

        g2.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // System.out.println("Click: mode = " + mode);
        x_input = e.getX();
        y_input = e.getY();
        mouse_clicked = true;
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
        if (e.getKeyChar() == 'q') {
            mode = GameModes.PLAY;
            ball.enable_physics = true;
            frame.requestFocus();
            gaming.requestFocusInWindow();
        } else if (e.getKeyChar() == 'w') {
            mode = GameModes.SHOOT;
            ball.enable_physics = false;
            frame.requestFocus();
            gaming.requestFocusInWindow();
        } else if (e.getKeyChar() == 'e') {
            mode = GameModes.EDIT;
            ball.enable_physics = false;
            frame.requestFocus();
            gaming.requestFocusInWindow();
        } else if (e.getKeyChar() == 'r') {
            mode = GameModes.SETPOSITION;
            ball.enable_physics = false;
            frame.requestFocus();
            gaming.requestFocusInWindow();
        } else if (e.getKeyChar() == 'y') {
            mode = GameModes.ITEM_PANEL;
            ball.enable_physics = false;
            dialog.dialog_init(2);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
