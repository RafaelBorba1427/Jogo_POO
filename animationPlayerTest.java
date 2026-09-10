// Test file, delete later
import javax.swing.*;
import java.awt.*;


public class animationPlayerTest {
  static double rotation = 0;
  public static void main(String[] args) {
    JFrame frame = new JFrame("Animation Player Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 400);

    AnimationPlayer.initializeAnimationPlayerTimer();
    
    try {
      AnimationPlayer animation_player = new AnimationPlayer("heart", "spritesheet/heart.png", 16, 16, 0, 5, 5);

      JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Graphics2D g2d = (Graphics2D) g;
          animation_player.paint(g2d, 100, 100, new Vector2D(128, 128), rotation); // Draw the animation at (100, 100)

          repaint();
        }
      };

      frame.add(panel);
      frame.setVisible(true);

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    javax.swing.Timer animation_timer = new javax.swing.Timer(1, e -> {
      rotation += .01; // Increment rotation for testing
      }
    );
    animation_timer.start();
  }
}
