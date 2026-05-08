import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class game extends JPanel implements MouseListener, KeyListener {

    enum GameModes {
        PLAY, SHOOT, EDIT, SETPOSITION;
    }

    static final double gravity = 0.5;

    static final int x_boundary = 800;
    static final int y_boundary = 600;

    ball ball = new ball(400, 50, 20);

    ArrayList<coisa> lvl_map = new ArrayList<>();

    GameModes mode = GameModes.PLAY;

    int x_input, y_input;

    public game() {
        addMouseListener(this);
        addKeyListener(this);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));

        setFocusable(true);

        lvl_map.add(new coisa(400, 300, 40));
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
            if(mode == GameModes.SHOOT){
                applyMouseInput();
                mode = GameModes.PLAY;
                ball.enable_physics = true;
            }
            else if(mode == GameModes.EDIT){
                lvl_map.add(new coisa(x_input, y_input, 40));
            }
            else if(mode == GameModes.SETPOSITION){
                ball.setPosition(x_input, y_input);
            }
            x_input = 0;
            y_input = 0;
        }

        for(coisa c : lvl_map){
            c.verify(ball);
        }

        ball.update();
        // clears the console
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("Ball position: (" + ball.getX() + ", " + ball.getY() + ")");
        System.out.println("Ball velocity: (" + ball.getXVel() + ", " + ball.getYVel() + ")");

        repaint();
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

        // Desenha o ball branco
        
        if(mode == GameModes.EDIT){
            g.setColor(Color.BLUE);
        }
        else if(mode == GameModes.SETPOSITION){
            g.setColor(Color.GREEN);
        }
        else if(mode == GameModes.SHOOT){
            g.setColor(Color.RED);
        }
        else{
            g.setColor(Color.WHITE);
        }

        g.fillOval((int) (ball.getX() - ball.getDiameter()), (int) (ball.getY() - ball.getDiameter()),
                (int) ball.getDiameter(), (int) ball.getDiameter());
        
        for(coisa c : lvl_map){
            g.fillRect(c.x - c.diametro, c.y - c.diametro, c.diametro, c.diametro);
        }
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
        if(e.getKeyChar() == 'q'){
            mode = GameModes.PLAY;
            ball.enable_physics = true;
        }
        else if(e.getKeyChar() == 'w'){
            mode = GameModes.SHOOT;
            ball.enable_physics = false;
        }
        else if(e.getKeyChar() == 'e'){
            mode = GameModes.EDIT;
            ball.enable_physics = false;
        }
        else if(e.getKeyChar() == 'r'){
            mode = GameModes.SETPOSITION;
            ball.enable_physics = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
