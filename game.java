import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class game extends JPanel implements MouseListener {

    static final double gravity = 0.5;

    static final int x_boundary = 800;
    static final int y_boundary = 600;

    ball ball = new ball(400, 50, 20);
    coisa coisa = new coisa(400, 300, 40);
    int x_input, y_input;

    public game() {
        addMouseListener(this);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));

        setFocusable(true);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ball Game");

        game game = new game();
        frame.add(game);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);

        while (true) {
            game.update();
        }
    }

    public void update() {
        try {
            Thread.sleep(16); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ball.getY() >= y_boundary || ball.getY() <= 0) {
            ball.bounceY();
        }
        if (ball.getX() <= 0 || ball.getX() >= x_boundary - ball.getDiameter()) {
            ball.bounceX();
        }

        // use mouse input to change ball trajectory
        if (x_input != 0 || y_input != 0) {
            applyMouseInput();
        }
        coisa.verify(ball);

        ball.update();
        // clears the console
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("Ball position: (" + ball.getX() + ", " + ball.getY() + ")");
        System.out.println("Ball velocity: (" + ball.getXVel() + ", " + ball.getYVel() + ")");

        repaint();
    }

    public void applyMouseInput() {
        int x_diff = x_input - (ball.getX());
        int y_diff = y_input - (ball.getY());

        ball.x_vel += x_diff * 0.1;
        ball.y_vel += y_diff * 0.1;

        x_input = 0;
        y_input = 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Desenha o ball branco
        g.setColor(Color.WHITE);
        g.fillOval(ball.getX(), ball.getY(), ball.getDiameter(), ball.getDiameter());
        g.fillRect(coisa.x, coisa.y, coisa.diametro, coisa.diametro);
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
}
