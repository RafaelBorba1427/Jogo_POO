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
    static Items dialog;
    private Image image;
    static int anime = 0;
    static double anime_help = 0;
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
    int sprite_col = 16, sprite_lin = 16;
    int option = 0;

    static Queue<coisa> list = new LinkedList<>();

    public enum GameModes {
        PLAY, SHOOT, EDIT, SETPOSITION, ITEM_PANEL;
    }

    static final double gravity = 0.5;

    static final int x_boundary = 800;
    static final int y_boundary = 600;

    ball ball = new ball(400, 200, 20);

    static ArrayList<coisa> lvl_map = new ArrayList<>();
    static Queue<buff> collided = new LinkedList<>();
    static GameModes mode = GameModes.PLAY;
    static game gaming = new game("Frat_background.png");
    private volatile int x_input;
    private volatile int y_input;
    private volatile boolean mouse_clicked;

    public void SetBallVelocity(int x, int y){
        ball.setVelocity(x,y);
    }

    public game(String imagePath) {
        try {
            sheet = ImageIO.read(new File("spritesheet/New Piskel(2)(1).png"));
            System.out.println("sheet loaded: " + sheet); // ✅ null or not?
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("sheet FAILED to load"); // ❌ path wrong?
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
        // lvl_map.add(new coisa(400, 300, 40, 0));

        setVisible(true);
    }

    public static void main(String[] args) {
        frame = new JFrame("Ball Game");

        frame.add(gaming);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> gaming.requestFocusInWindow());

        w_frame = (int) Math.ceil(frame.getWidth() / 4.0);
        h_frame = (int) Math.ceil(frame.getHeight() / 4.0);
        gaming.frame = frame;
        buffSys = new buffSystem();
        dialog = new Items(gaming);

        // debug buffs
        //buffSys.ApplyBuff(buffSystem.buffs.TIME_TRAVEL, 10);
        lvl_map.add(new coisa(700, 600, 40, 3, gaming));

        timer = new Timer(16, e -> {
            gaming.update();
            gaming.repaint();
            buffSys.DecrementBuffTimers();
            anime_help++;
            if (anime_help > 4) {
                anime_help = 0;
                anime++;
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
            return;

        }
        if (x_input != 0 || y_input != 0) {
            lvl_map.add(list.peek());
            list.peek().setX(x_input);
            list.peek().setY(y_input);
            list.poll();
            if (list.isEmpty()) {
                mode = GameModes.PLAY;
                ball.enable_physics = true;
                // for (buff c : collided) {
                // lvl_map.add(c);
                // collided.remove(c);
                // }
            }
            x_input = 0;
            y_input = 0;

        }
    }

    public void update() {

        if ((ball.getY() >= y_boundary - ball.getDiameter() || ball.getY() <= 0)) {
            ball.bounceY();
        }
        if ((ball.getX() <= 0 || ball.getX() >= x_boundary - ball.getDiameter())) {
            ball.bounceX();
        }

        if (mode == GameModes.EDIT) {
            ball.update();

            EditCouse();
            Iterator<buff> it2 = collided.iterator();
            while (it2.hasNext()) {
                buff a = it2.next();

                lvl_map.add(a);
                it2.remove();
            }
            return;
        }

        // use mouse input to change ball trajectory
        else if (mouse_clicked) {
            if (mode == GameModes.SHOOT) {
                applyMouseInput();
            } else if (mode == GameModes.SETPOSITION) {
                //System.out.println("Processing click: mode = " + mode);
                ball.setPosition(x_input, y_input);
                ball.setVelocity(0,0);
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

        ball.update();
        // clears the console
        // System.out.print("\033[H\033[2J"); System.out.flush();
        /*
        System.out.println("Ball position: (" + ball.getX() + ", " + ball.getY() +
                ")");
        System.out.println("Ball velocity: (" + ball.getXVel() + ", " +
                ball.getYVel() + ")"); */

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

        g.drawImage(new ImageIcon("floor.png").getImage(), 0, getHeight() -
                getHeight() / 4, getWidth(),
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
                (int) (ball.getPaintX() - ball.getDiameter() / 2),
                (int) (ball.getPaintY() - ball.getDiameter() / 2),
                (int) ball.getDiameter(),
                (int) ball.getDiameter());

        // Draw obstacles
        for (coisa c : lvl_map) {
            g.drawImage(
                    sheet,
                    c.x - c.diametro / 2, // destination x on screen
                    c.y - c.diametro / 2, // destination y on screen
                    c.x + c.diametro / 2, // destination x2
                    c.y + c.diametro / 2, // destination y2
                    anime * sprite_col, // source x on spritesheet
                    c.id * sprite_lin, // source y on spritesheet
                    anime * sprite_col + sprite_col, // source x2
                    c.id * sprite_lin + sprite_lin, // source y2
                    null);
        } // Draw transparent buffer over background
        g.drawImage(offscreen, 0, 0, null);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        //System.out.println("Click: mode = " + mode);
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
            dialog.dialog_init();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
