import javax.sound.sampled.*;
import java.util.*;

public class SoundEffectPlayer {

    public static HashMap<String, String> soundFilePaths = new HashMap<>();

    public static void loadSoundEffects() {
        // Add sound file paths to the HashMap
        // Use this exclusively for sounds to avoid confusion
        // Original file names kept for reference, key name indicates actual purpose


        soundFilePaths.put("bounce_realistic", "sounds/sfx/table-tennis-ball-hit-om-fx-1-00-01.wav");

        soundFilePaths.put("goal", "sounds/sfx/400 Sounds Pack/Musical Effects/8_bit_level_start.wav");
        soundFilePaths.put("damage", "sounds/sfx/400 Sounds Pack/Retro/lose.wav");
        soundFilePaths.put("napkin", "sounds/sfx/400 Sounds Pack/Other/paste.wav");

        soundFilePaths.put("buff0", "sounds/sfx/400 Sounds Pack/Retro/power_up.wav");
        soundFilePaths.put("buff1", "sounds/sfx/400 Sounds Pack/Retro/power_up_2.wav");
        soundFilePaths.put("buff2", "sounds/sfx/SweetSounds_SFX/WAV/Powerup.wav");

        soundFilePaths.put("debuff0", "sounds/sfx/400 Sounds Pack/Retro/power_down.wav");
        soundFilePaths.put("debuff1", "sounds/sfx/400 Sounds Pack/Retro/power_down_2.wav");
        soundFilePaths.put("debuff2", "sounds/sfx/SweetSounds_SFX/WAV/Powerdown.wav");
    }


    public static void playSound(String sound) {
        String soundFilePath = soundFilePaths.get(sound);
        if (soundFilePath == null) {
            System.err.println("Sound file not found for key: " + sound);
            return;
        }
        try {
            // Load the sound file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(SoundEffectPlayer.class.getResource(soundFilePath));
            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            clip.start();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Subroutines for playing random sounds from a category
    public static void playBounceSound() {
        playSound("bounce_realistic"); // Play the realistic bounce sound
    }
    public static void playBuffSound() {
        Random random = new Random();
        int randomIndex = random.nextInt(3); // Generate a random index between 0 and 2
        String soundKey = "buff" + randomIndex; // Construct the sound key based on the random index
        playSound(soundKey); // Play the randomly selected buff sound
    }
    public static void playDebuffSound() {
        Random random = new Random();
        int randomIndex = random.nextInt(3); // Generate a random index between 0 and 2
        String soundKey = "debuff" + randomIndex; // Construct the sound key based on the random index
        playSound(soundKey); // Play the randomly selected debuff sound
    }

        
    // Example usage, delete later
    public static void main(String[] args) {
        // Example usage: play a sound file located in the resources folder
        SoundEffectPlayer SoundEffectPlayer = new SoundEffectPlayer();

        Scanner in = new Scanner(System.in);

        while(true) {
            int a = in.nextInt(); // Wait for user input to play a sound

            if(a == 1) {
                SoundEffectPlayer.playBounceSound(); // Play a random bounce sound
            }
            else if(a == 2) {
                SoundEffectPlayer.playBuffSound(); // Play a random buff sound
            }
            else if(a == 3) {
                SoundEffectPlayer.playDebuffSound(); // Play a random debuff sound
            }
            else{
                break; // Exit the loop if the input is not recognized
            }
        }

    }
}
