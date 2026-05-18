import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.math.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.io.File;

public class game extends JPanel implements MouseListener, KeyListener {
    private Image image;

    static boolean hitting = false;
    static int add = 0;
    private BufferedImage offscreen, sheet;
    private Graphics2D offscreenG;
    static JFrame frame;
    static int x_cool_sqr, y_cool_sqr;
    static int w_frame, h_frame;
    static buffSystem buffSys;
    int sprite_col = 16, sprite_lin = 16;
    int option = 0;

    enum GameModes {
        PLAY, SHOOT, EDIT, SETPOSITION;
    }

    static final double gravity = 0.5;

    static final int x_boundary = 800;
    static final int y_boundary = 600;

    ball ball = new ball(400, 50, 20);

    static ArrayList<coisa> lvl_map = new ArrayList<>();

    GameModes mode = GameModes.PLAY;
    static game gaming = new game("Frat_background.png");
    int x_input, y_input;

    public game(String imagePath) {
        try {
            sheet = ImageIO.read(new File("spritesheet/New Piskel(2).png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        image = new ImageIcon(imagePath).getImage();
        addMouseListener(this);
        addKeyListener(this);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));

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
        lvl_map.add(new coisa(400, 300, 40, 0));
        setVisible(true);

    }

    public static void main(String[] args) {
        frame = new JFrame("Ball Game");

        frame.add(gaming);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        w_frame = (int) Math.ceil(frame.getWidth() / 4.0);
        h_frame = (int) Math.ceil(frame.getHeight() / 4.0);

        buffSys = new buffSystem();
        // buffSys.ApplyBuff(buffSystem.buffs.ICED, 10);
        Timer timer = new Timer(16, e -> {
            buffSys.CheckDuration(buffSystem.buffs.MASSIVE_DRAG);
            gaming.update();
            gaming.repaint();
            buffSys.DecrementBuffTimers();
        });
        timer.start();
    }

    public void update() {
        if ((ball.getY() >= y_boundary - ball.getDiameter() || ball.getY() <= 0)) {
            ball.bounceY();
        }
        if ((ball.getX() <= 0 || ball.getX() >= x_boundary - ball.getDiameter())) {
            ball.bounceX();
        }

        // use mouse input to change ball trajectory
        if (x_input != 0 || y_input != 0) {
            if (mode == GameModes.SHOOT) {
                applyMouseInput();
                mode = GameModes.PLAY;
                ball.enable_physics = true;
            } else if (mode == GameModes.EDIT) {
                // coisa newy = new coisa(x_input, y_input, 40);
                // buffSystem.buffs bu = buffSystem.buffs.SPEED_BOOST;
                buff newy = new buff(x_input, y_input, 40, buffSystem.buffs.MASSIVE_DRAG, 4);

                lvl_map.add(newy);
                // lvl_map.add(newy);

            } else if (mode == GameModes.SETPOSITION) {
                ball.setPosition(x_input, y_input);
            }
            x_input = 0;
            y_input = 0;
        }

        for (coisa c : lvl_map) {
            if (c instanceof buff b) {

                b.verify(ball);

            } else {
                c.verify(ball);
            }
            System.out.println("Veryfy Ativo");
        }
        lvl_map.removeIf(c -> c instanceof buff b && b.bateu);
        if (add == lvl_map.size()) {
            ball.bateuX = false;
            ball.bateuY = false;
            hitting = false;
        }
        add = 0;
        ball.update();
        // clears the console
        // System.out.print("\033[H\033[2J");
        // System.out.flush();

        System.out.println("Ball position: (" + ball.getX() + ", " + ball.getY() +
                ")");
        System.out.println("Ball velocity: (" + ball.getXVel() + ", " +
                ball.getYVel() + ")");

    }

    public void applyMouseInput() {
        int x_diff = x_input - ((int) ball.getX());
        int y_diff = y_input - ((int) ball.getY());

        ball.x_vel += x_diff * 0.1;
        ball.y_vel += y_diff * 0.1;

        x_input = 0;
        y_input = 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);

        g.drawImage(new ImageIcon("floor.png").getImage(), 0, getHeight() - getHeight() / 4, getWidth(),
                getHeight() / 4, this);

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
            offscreenG.setColor(Color.WHITE);
        }

        offscreenG.fillOval(
                (int) (ball.getX() - ball.getDiameter() / 2),
                (int) (ball.getY() - ball.getDiameter() / 2),
                (int) ball.getDiameter(),
                (int) ball.getDiameter());

        // Draw obstacles
        for (coisa c : lvl_map) {

            if ((c.buff && buffSys.HasBuff(((buff) c).buff_active)) || !c.bateu) {
                g.drawImage(
                        sheet,
                        c.x - c.diametro / 2,
                        c.y - c.diametro / 2,
                        c.x + c.diametro / 2,
                        c.y + c.diametro / 2,

                        (0) * sprite_col, // column
                        (0 + c.id) * sprite_lin, // row (do NOT add option!)
                        (0) * sprite_col + sprite_col,
                        (0 + c.id) * sprite_lin + sprite_lin,

                        null);
            }
        }

        // Draw transparent buffer over background
        g.drawImage(offscreen, 0, 0, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        x_input = e.getX();
        y_input = e.getY();
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
        if (e.getKeyChar() == 'q') {
            mode = GameModes.PLAY;
            ball.enable_physics = true;
        } else if (e.getKeyChar() == 'w') {
            mode = GameModes.SHOOT;
            ball.enable_physics = false;
        } else if (e.getKeyChar() == 'e') {
            mode = GameModes.EDIT;
            ball.enable_physics = false;
        } else if (e.getKeyChar() == 'r') {
            mode = GameModes.SETPOSITION;
            ball.enable_physics = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
