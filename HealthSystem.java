import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;

public class HealthSystem extends JPanel {
    private int max_hp;
    private int current_hp;

    private ArrayList<heart> hp_sprites = new ArrayList<heart>();
    private Vector2D scale_factor;

    private static final int heart_spacing = 40;
    private static final int default_height = 10;

    public Signal<Boolean> desperation_mode = new Signal<>();
    public Signal<Boolean> is_dead = new Signal<>();

    public HealthSystem(int max_hp, boolean is_visible, Vector2D scale_factor) {
        this.max_hp = max_hp;
        this.current_hp = max_hp;
        this.scale_factor = scale_factor;
        setVisible(is_visible);
        setOpaque(false);
        setPreferredSize(new Dimension(300, 100)); // Set preferred size of the panel

        for (int i = 0; i < max_hp; i++) {
            hp_sprites.add(new heart(i * heart_spacing, default_height, scale_factor)); // Adjust position as needed
        }
    }

    public synchronized void addMaxHearts(int hearts){
        
        for (int i = max_hp; i < max_hp + hearts; i++) {
            hp_sprites.add(new heart(i * heart_spacing, default_height, scale_factor)); // Adjust position as needed
            hp_sprites.get(i).playDamageAnimation();
        }
        max_hp += hearts;
    }


    // returns true if the player dies, false otherwise
    public boolean takeDamageAndCheckDeath() {
        if (current_hp > 0) {
            current_hp--;

            hp_sprites.get(current_hp).playDamageAnimation();
            SoundEffectPlayer.playSound("damage");

            if(current_hp <= max_hp / 2) {
                desperation_mode.emit(true);
            }
            else{
                desperation_mode.emit(false);
            }
        }

        if (current_hp <= 0) {
            is_dead.emit(true);
            return true; // Dead
        }
        return false; // Not dead
    }

    public void heal() {
        if (current_hp < max_hp) {
            current_hp++;
            hp_sprites.get(current_hp - 1).playHealAnimation();
            SoundEffectPlayer.playSound("heal");
        }

        if(current_hp <= max_hp / 2) {
                desperation_mode.emit(true);
        }
        else{
            desperation_mode.emit(false);
        }
    }

    // Getters
    public int getCurrentHp() {
        return current_hp;
    }

    public int getMaxHp() {
        return max_hp;
    }

    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        for(heart h : hp_sprites){
            h.paint(g);
        }
        repaint();
    }

    public static void main(String[] args){
        testCase test = new testCase();
        @SuppressWarnings("resource")
        Scanner in = new Scanner(System.in);
        int command;
        while(true){
            command = in.nextInt();

            test.test(command);
        }
    }
}

class heart {
    private AnimationPlayer animation_player;

    private int pos_x = 0; // Position of the heart sprite, can be set as needed
    private int pos_y = 0;

    private Vector2D scale_factor;

    private static final String key = "heart";
    private static final String path = "spritesheet/heart.png";
    private static final int sprite_width = 16;
    private static final int sprite_height = 16;
    private static final int row = 0;
    private static final int frames = 5;
    private static final int fps = 5;

    public heart(int x, int y, Vector2D scale_factor) {
        this.pos_x = x;
        this.pos_y = y;
        this.scale_factor = scale_factor;

        try{
            animation_player = new AnimationPlayer(key, path, sprite_width, sprite_height, row, frames, fps, false, true);
        }
        catch(Exception e){
            System.out.println("Error loading heart animation: " + e.getMessage());
        }
    }


    public void playDamageAnimation() {
        animation_player.reverseAnimation(false);
        animation_player.play();
    }
    public void playHealAnimation() {
        animation_player.reverseAnimation(true);
        animation_player.play();
    }


    public void paint(Graphics g) {
        animation_player.paint((Graphics2D)g, pos_x, pos_y, scale_factor, 0.0);
    }
}


// Class for tests, may delete later
class testCase extends JFrame{
    private HealthSystem health_system;

    public void test(int command)
    {
        if(command == 1){
            health_system.takeDamageAndCheckDeath();
        }
        else if(command == 2){
            health_system.heal();
        }
        else if(command == 3){
            health_system.addMaxHearts(1);
        }
    }
    public testCase(){
        super("test");
        health_system = new HealthSystem(5, true, new Vector2D(32, 32));
        health_system.setVisible(true);
        add(health_system);
        setVisible(true);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SoundEffectPlayer.initialiseSoundEffectPlayer();

        health_system.desperation_mode.connect(this::desperation);
    }

    public void desperation(boolean d){
        if(d){
            System.out.println("Desperation!!!");
        }
        else{
            System.out.println("Phew!");
        }
    }
}