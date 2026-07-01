import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.math.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import java.io.File;

public class healthSystem extends JPanel {
    private int max_hp;
    private int current_hp;

    private boolean is_dead; // A bit redundant, but might be useful

    private ArrayList<heart> hp_sprites = new ArrayList<heart>();

    public healthSystem(int max_hp, boolean is_visible) {
        this.max_hp = max_hp;
        this.current_hp = max_hp;
        this.is_dead = false;
        setVisible(is_visible);
        setOpaque(false);
        setPreferredSize(new Dimension(800, 100)); // Set preferred size of the panel

        for (int i = 0; i < max_hp; i++) {
            hp_sprites.add(new heart(i * 70, 10)); // Adjust position as needed
        }

        Timer animation_timer = new Timer();
        TimerTask update_frame = new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        };
        animation_timer.scheduleAtFixedRate(update_frame, 0, 100); // 10 FPS
    }

    // returns true if the player dies, false otherwise
    public boolean takeDamageAndCheckDeath() {
        if (current_hp > 0) {
            current_hp--;

            hp_sprites.get(current_hp).playDamageAnimation(); // Assuming you have a method to play the damage animation
                                                              // on the heart sprite
        }

        System.out.println("Current HP: " + current_hp); // Debugging output

        if (current_hp <= 0) {
            this.is_dead = true;
            return true; // Dead
        }
        return false; // Not dead
    }

    public void heal() {
        if (current_hp < max_hp) {
            current_hp++;
            hp_sprites.get(current_hp - 1).playDamageAnimationReverse();
        }
    }

    // Getters
    public int getCurrentHp() {
        return current_hp;
    }

    public int getMaxHp() {
        return max_hp;
    }

    public boolean isDead() {
        return is_dead;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (heart h : hp_sprites) {
            h.paintHeart(g);
        }
    }

    public void setVisibile(boolean visible) {
        this.setVisible(visible);
    }

    // TO DO
    // For testing purposes, delete later
    /*
     * public static void main(String[] args) {
     * JFrame frame = new JFrame("Health System Test");
     * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     * frame.setSize(1000, 1000);
     * healthSystem health = new healthSystem(5, true);
     * frame.add(health);
     * 
     * healthSystemTest test = new healthSystemTest();
     * frame.addKeyListener(test);
     * 
     * frame.setVisible(true);
     * frame.setFocusable(true);
     * 
     * while (true) {
     * if (test.input == 'a') {
     * health.takeDamageAndCheckDeath();
     * } else if (test.input == 'd') {
     * health.heal();
     * <<<<<<< HEAD
     * }
     * else if(test.input == 'q'){
     * health.setVisible(false);
     * System.out.println("Health system hidden!"); // Debugging output
     * }
     * else if(test.input == 'e'){
     * health.setVisible(true);
     * =======
     * } else if (test.input == 'q') {
     * health.setVisibile(false);
     * System.out.println("Health system hidden!"); // Debugging output
     * } else if (test.input == 'e') {
     * health.setVisibile(true);
     * >>>>>>> guiEndCycle
     * System.out.println("Health system shown!"); // Debugging output
     * }
     * 
     * test.input = '\0'; // Reset input after processing
     * 
     * System.out.println("Current HP: " + health.getCurrentHp() + "/" +
     * health.getMaxHp() + " | Is Dead: "
     * + health.isDead()); // Debugging output
     * }
     * }
     */
}

class heart {
    private BufferedImage heart_spritesheet;
    private BufferedImage[] take_damage_animation = new BufferedImage[5]; // Assuming 5 frames for the damage animation

    int pos_x = 0; // Position of the heart sprite, can be set as needed
    int pos_y = 0;

    private int current_frame = 0;

    private boolean play_damage_animation = false;
    private boolean play_damage_animation_reverse = false;

    public heart(int x, int y) {
        this.pos_x = x;
        this.pos_y = y;

        try {
            heart_spritesheet = ImageIO.read(new File("spritesheet/heart.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 5; i++) {
            take_damage_animation[i] = heart_spritesheet.getSubimage(i * 16, 0, 16, 16);
        }
    }

    public void playDamageAnimation() {
        play_damage_animation = true;
    }

    public void playDamageAnimationReverse() {
        play_damage_animation_reverse = true;
    }

    public void paintHeart(Graphics g) {
        g.drawImage(take_damage_animation[current_frame], pos_x, pos_y, 64, 64, null);
        if (play_damage_animation) {
            if (current_frame >= take_damage_animation.length - 1) {
                play_damage_animation = false;
                return;
            }
            current_frame++;
        } else if (play_damage_animation_reverse) {
            if (current_frame <= 0) {
                play_damage_animation_reverse = false;
                return;
            }
            current_frame--;
        }
    }
}

// TO DO
// Delete later
/*
 * class healthSystemTest implements KeyListener {
 * public char input;
 * 
 * public void keyPressed(KeyEvent e) {
 * if (e.getKeyChar() == 'a') {
 * input = 'a';
 * System.out.println("Damage taken!"); // Debugging output
 * }
 * if (e.getKeyChar() == 'd') {
 * input = 'd';
 * System.out.println("Healed!"); // Debugging output
 * }
 * if (e.getKeyChar() == 'q') {
 * input = 'q';
 * System.out.println("Health system will be hidden!"); // Debugging output
 * }
 * if (e.getKeyChar() == 'e') {
 * input = 'e';
 * System.out.println("Health system will be shown!"); // Debugging output
 * }
 * }
 * 
 * public void keyReleased(KeyEvent e) {
 * // Not needed for this test
 * }
 * 
 * public void keyTyped(KeyEvent e) {
 * // Not needed for this test
 * }
 * }
 * <<<<<<< HEAD
 */
