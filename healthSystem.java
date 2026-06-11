import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.math.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.File;


public class healthSystem extends JPanel {
    private int max_hp;
    private int current_hp;

    private boolean is_dead; // A bit redundant, but might be useful

    private ArrayList<heart> hp_sprites = new ArrayList<heart>();


    public healthSystem(int max_hp) {
        this.max_hp = max_hp;
        this.current_hp = max_hp;
        this.is_dead = false;

        for(int i = 0; i < max_hp; i++){
            hp_sprites.add(new heart(i * 70, 10)); // Adjust position as needed
        }
    }


    //returns true if the player dies, false otherwise
    public boolean takeDamageAndCheckDeath() {
        if (current_hp > 0) {
            current_hp--;

            hp_sprites.get(current_hp).playDamageAnimation(); // Assuming you have a method to play the damage animation on the heart sprite
        }

        System.out.println("Current HP: " + current_hp); // Debugging output

        if (current_hp <= 0) {
            this.is_dead = true;
            return true; // Dead
        }
        return false; // Not dead
    }


    public void heal(){
        if(current_hp < max_hp){
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


    // For testing purposes
    public static void main(String[] args) {
        JFrame frame = new JFrame("Health System Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        healthSystem health = new healthSystem(5);
        frame.add(health);
        
        frame.setVisible(true);
    }
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
        }
        else if (play_damage_animation_reverse) {
            if (current_frame <= 0) {
                play_damage_animation_reverse = false;
                return;
            }
            current_frame--;
        }
    }
}