// Test file, delete later
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class animationPlayerTest {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Animation Player Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 400);

    // Load spritesheet and create AnimationPlayer
    SpriteLoader.loadSpritesheet("heart", "spritesheet/heart.png", 16, 16, 5, 0);

    AnimationPlayer.initializeAnimationPlayerTimer();
    
    try {
      AnimationPlayer animation_player = new AnimationPlayer("heart", 5, 5);

      JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          animation_player.paint(g, 100, 100, new Vector2D(16, 16), 1); // Draw the animation at (100, 100)

          repaint();
        }
      };

      frame.add(panel);
      frame.setVisible(true);

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
