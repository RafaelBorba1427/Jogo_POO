import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.awt.*;
import javax.swing.*;

public class ballLauncher implements MouseListener {

    Point mouse_position;
    public int mouse_x_relative;
    public int mouse_y_relative;

    public ball ball;

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

    public void update() {

        int ball_position_x = (int) ball.getX();
        int ball_position_y = (int) ball.getY();

        mouse_position = MouseInfo.getPointerInfo().getLocation();
        mouse_x_relative = ball_position_x - (int) mouse_position.getX();
        mouse_y_relative = ball_position_y - (int) mouse_position.getY();

        launch_power = (int) Math.sqrt(Math.pow(mouse_x_relative - ball_position_x, 2) + Math.pow(mouse_y_relative - ball_position_y, 2));
        launch_angle = Math.atan2(mouse_y_relative - ball_position_y, mouse_x_relative - ball_position_x);

        System.out.println(mouse_x_relative + " " + mouse_y_relative);
    }

    public void draw(Graphics g, int ball_position_x, int ball_position_y) {
        if(aiming) {
            g.drawLine(mouse_x_relative + ball_position_x, mouse_y_relative + ball_position_y, ball_position_x, ball_position_y); // Draw line from ball to mouse position
        }
    }


    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        aiming = true;
    }
    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        aiming = false;
        launchBall();
    }
    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {}
    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {}
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {}


    // For testing, delete in final game
    public static void main(String[] args) {
        ball ball = new ball(400, 400, 20);
        ballLauncher launcher = new ballLauncher(ball);
        JFrame frame = new JFrame("Ball Launcher Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.addMouseListener(launcher);
        frame.setVisible(true);

        while(true){
            launcher.update(); // Example ball position, replace with actual ball position in the game
            Graphics g = frame.getGraphics();

            launcher.draw(g, 400, 400); // Example ball position, replace with actual ball position in the game
            frame.repaint();
        }
    }
}