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
import java.util.Vector;


public class healthSystem{
    private int max_hp;
    private int current_hp;

    // A bit redundant, but might be useful
    private boolean is_dead;



    public healthSystem(int max_hp) {
        this.max_hp = max_hp;
        this.current_hp = max_hp;
        this.is_dead = false;
    }


    //returns true if the player dies, false otherwise
    public boolean takeDamageAndCheckDeath(){
        if(current_hp > 0){
            current_hp--;
        }
        else{
            this.is_dead = true;
            return true; // Dead
        }
        return false; // Not dead
    }


    public void heal(){
        if(current_hp < max_hp){
            current_hp++;
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


    // For testing purposes
    public static void main(String[] args) {
        JFrame frame = new JFrame("Health System Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        healthSystem health = new healthSystem(5);
    }
}

class heart{
    private BufferedImage sprite;

    public heart() {
        this.sprite = sprite;

        try {
            sprite = ImageIO.read(new File("spritesheet/heart.png"));
            System.out.println("heart loaded: " + sprite); // ✅ null or not?
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("heart FAILED to load"); // ❌ path wrong?
        }
    }

}