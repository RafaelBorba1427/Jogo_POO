import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.*;

import javax.imageio.ImageIO;
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
    BufferedImage[] laucher_sprite;

    BufferedImage power_meter_spritesheet;
    BufferedImage[] power_meter_sprite;

    public ballLauncher(ball ball) {
        this.ball = ball;

        power_meter_sprite = new BufferedImage[32];
        laucher_sprite = new BufferedImage[15];

        try{
            power_meter_spritesheet = ImageIO.read(new File("spritesheet/Power_Slider.png"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        /*
        try{
            launcher_spritesheet = ImageIO.read(new File("spritesheet/New_Piskel(2)(1).png"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        */

        for (int i = 0; i < 32; i++) {
            power_meter_sprite[i] = power_meter_spritesheet.getSubimage(i * 16, 0, 16, 32);
        }
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

        launch_power = (int) Math.sqrt(Math.pow(mouse_x_relative, 2) + Math.pow(mouse_y_relative, 2));
        launch_angle = Math.atan2(mouse_y_relative, mouse_x_relative);

        //System.out.println(mouse_x_relative + " " + mouse_y_relative);
        System.out.println("Power: " + launch_power + " Angle: " + launch_angle);
    }

    public void draw(Graphics g) {
        if(aiming) {
            g.setColor(Color.BLACK);
            g.drawLine( mouse_x_relative + ball_position_x  + (int)ball.getDiameter(),
                        mouse_y_relative + ball_position_y  + (int)ball.getDiameter(),
                        ball_position_x  + (int)(ball.getDiameter() / 2.0),
                        ball_position_y  + (int)(ball.getDiameter() / 2.0)); // Draw line from ball to mouse position
        }
        g.drawOval((int)ball.getX(), (int)ball.getY(), (int)ball.getDiameter(), (int)ball.getDiameter());
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

class powerMeter extends JPanel{
    BufferedImage[] power_meter_sprite;
    int current_frame;

    int pos_x, pos_y;

    static int max_power = 300;
    int current_power = 0;

    public powerMeter(BufferedImage[] power_meter_sprite, int x, int y){
        this.power_meter_sprite = power_meter_sprite;

        this.pos_x = x;
        this.pos_y = y;
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        g.drawImage(power_meter_sprite[current_frame], pos_x, pos_y, 32, 64, null);
    }

    public void update_power(int power){
        if(power > max_power){
            current_power = power;
        }
        else{
            current_power = max_power;
        }
    }
}