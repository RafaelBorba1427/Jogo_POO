import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.swing.*;


public class ballLauncher extends MouseAdapter{

    Point mouse_position;
    public int mouse_x_relative;
    public int mouse_y_relative;

    public ball ball;

    public int ball_position_x;
    public int ball_position_y;

    boolean aiming = false;

    int launch_power = 0;
    double launch_angle = 0;

    BufferedImage launcher_spritesheet;
    BufferedImage laucher_sprite[];

    BufferedImage power_meter_spritesheet;
    BufferedImage power_meter_sprite[];

    public ballLauncher(ball ball) {
        this.ball = ball;
    }

    public void launchBall() {
        
    }

    public void update(JPanel game) {

        ball_position_x = (int) ball.getX();
        ball_position_y = (int) ball.getY();

        Point point = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(point, game);
        int mouse_X = point.x;
        int mouse_Y = point.y;

        mouse_x_relative = ball_position_x - mouse_X;
        mouse_y_relative = ball_position_y - mouse_Y;

        launch_power = (int) Math.sqrt(Math.pow(mouse_x_relative - ball_position_x, 2) + Math.pow(mouse_y_relative - ball_position_y, 2));
        launch_angle = Math.atan2(mouse_y_relative - ball_position_y, mouse_x_relative - ball_position_x);

        System.out.println(mouse_x_relative + " " + mouse_y_relative);
    }

    public void draw(Graphics g) {
        if(aiming) {
            g.setColor(Color.RED);
            g.drawLine(mouse_x_relative + ball_position_x, mouse_y_relative + ball_position_y, ball_position_x, ball_position_y); // Draw line from ball to mouse position
            g.drawOval((int)ball.getX(), (int)ball.getY(), (int)ball.getDiameter(), (int)ball.getDiameter());
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {
        aiming = true;
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        aiming = false;
        launchBall();
    }


    // For testing, delete in final game
    public static void main(String[] args) {
        ball ball = new ball(400, 400, 20);
        ballLauncher launcher = new ballLauncher(ball);
        JFrame frame = new JFrame("Ball Launcher Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.addMouseListener(launcher);
        frame.setVisible(true);

        JPanel panel = new JPanel();
        frame.add(panel);

        Graphics g = panel.getGraphics();

        while(true){
            launcher.update(panel); // Example ball position, replace with actual ball position in the game

            launcher.draw(g); // Example ball position, replace with actual ball position in the game
            frame.repaint();
        }
    }
}