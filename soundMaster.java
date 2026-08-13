import javax.sound.sampled.*;
import java.util.*;

public class soundMaster {

    public HashMap<String, String> soundFilePaths;

    public soundMaster() {
        soundFilePaths = new HashMap<>();

        // Add sound file paths to the HashMap
        // Use this exclusively for sounds to avoid confusion
        // Original file names kept for reference, key name indicates actual purpose


        soundFilePaths.put("bounce_realistic", "sounds/sfx/table-tennis-ball-hit-om-fx-1-00-01.wav"); // implemented
        // implemented
        soundFilePaths.put("bounce0", "sounds/sfx/400 Sounds Pack/UI/select_1.wav");
        soundFilePaths.put("bounce1", "sounds/sfx/400 Sounds Pack/UI/select_2.wav");
        soundFilePaths.put("bounce2", "sounds/sfx/400 Sounds Pack/UI/select_3.wav");
        soundFilePaths.put("bounce3", "sounds/sfx/400 Sounds Pack/UI/select_4.wav");

        soundFilePaths.put("goal", "sounds/sfx/400 Sounds Pack/Musical Effects/8_bit_level_start.wav"); // implemented
        soundFilePaths.put("damage", "sounds/sfx/400 Sounds Pack/Retro/lose.wav");
        soundFilePaths.put("napkin", "sounds/sfx/400 Sounds Pack/Other/paste.wav");

        // implemented
        soundFilePaths.put("buff0", "sounds/sfx/400 Sounds Pack/Retro/power_up.wav");
        soundFilePaths.put("buff1", "sounds/sfx/400 Sounds Pack/Retro/power_up_2.wav");
        soundFilePaths.put("buff2", "sounds/sfx/SweetSounds_SFX/WAV/Powerup.wav");

        soundFilePaths.put("debuff0", "sounds/sfx/400 Sounds Pack/Retro/power_down.wav");
        soundFilePaths.put("debuff1", "sounds/sfx/400 Sounds Pack/Retro/power_down_2.wav");
        soundFilePaths.put("debuff2", "sounds/sfx/SweetSounds_SFX/WAV/Powerdown.wav");
    }


    public void playSound(String sound) {
        String soundFilePath = soundFilePaths.get(sound);
        if (soundFilePath == null) {
            System.err.println("Sound file not found for key: " + sound);
            return;
        }
        try {
            // Load the sound file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundMaster.class.getResource(soundFilePath));
            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            clip.start();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Subroutines for playing random sounds from a category
        public void playBounceSound() {

            /*
            Random random = new Random();
            int randomIndex = random.nextInt(4); // Generate a random index between 0 and 3
            String soundKey = "bounce" + randomIndex; // Construct the sound key based on the random index
            playSound(soundKey); // Play the randomly selected bounce sound
            */

            playSound("bounce_realistic"); // Play the realistic bounce sound
        }
        public void playBuffSound() {
            Random random = new Random();
            int randomIndex = random.nextInt(3); // Generate a random index between 0 and 2
            String soundKey = "buff" + randomIndex; // Construct the sound key based on the random index
            playSound(soundKey); // Play the randomly selected buff sound
        }
        public void playDebuffSound() {
            Random random = new Random();
            int randomIndex = random.nextInt(3); // Generate a random index between 0 and 2
            String soundKey = "debuff" + randomIndex; // Construct the sound key based on the random index
            playSound(soundKey); // Play the randomly selected debuff sound
        }

        
    // Example usage, delete later
    public static void main(String[] args) {
        // Example usage: play a sound file located in the resources folder
        soundMaster soundMaster = new soundMaster();

        Scanner in = new Scanner(System.in);

        while(true) {
            int a = in.nextInt(); // Wait for user input to play a sound

            if(a == 1) {
                soundMaster.playBounceSound(); // Play a random bounce sound
            }
            else if(a == 2) {
                soundMaster.playBuffSound(); // Play a random buff sound
            }
            else if(a == 3) {
                soundMaster.playDebuffSound(); // Play a random debuff sound
            }
            else{
                break; // Exit the loop if the input is not recognized
            }
        }

    }
}
