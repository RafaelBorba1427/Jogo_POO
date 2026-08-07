import javax.sound.sampled.*;
import java.util.*;

public class soundMaster {

    public HashMap<String, String> soundFilePaths;

    private Clip currentMusicClip;

    public soundMaster() {
        // sfx files
        soundFilePaths = new HashMap<>();

        // Add sound file paths to the HashMap
        // Use this exclusively for sounds to avoid confusion
        // Original file names kept for reference, map name indicates actual purpose

        soundFilePaths.put("bounce0", "sounds/sfx/400 Sounds Pack/UI/select_1.wav");
        soundFilePaths.put("bounce1", "sounds/sfx/400 Sounds Pack/UI/select_2.wav");
        soundFilePaths.put("bounce2", "sounds/sfx/400 Sounds Pack/UI/select_3.wav");
        soundFilePaths.put("bounce3", "sounds/sfx/400 Sounds Pack/UI/select_4.wav");

        soundFilePaths.put("goal", "sounds/sfx/400 Sounds Pack/Musical Effects/8_bit_level_start.wav");
        soundFilePaths.put("damage", "sounds/sfx/400 Sounds Pack/Retro/lose.wav");
        soundFilePaths.put("show_napkin", "");

        soundFilePaths.put("buff0", "sounds/sfx/400 Sounds Pack/Retro/power_up.wav");
        soundFilePaths.put("buff1", "sounds/sfx/400 Sounds Pack/Retro/power_up_2.wav");
        soundFilePaths.put("buff2", "sounds/sfx/SweetSounds_SFX/WAV/Powerup.wav");

        soundFilePaths.put("debuff0", "sounds/sfx/400 Sounds Pack/Retro/power_down.wav");
        soundFilePaths.put("debuff1", "sounds/sfx/400 Sounds Pack/Retro/power_down_2.wav");
        soundFilePaths.put("debuff2", "sounds/sfx/SweetSounds_SFX/WAV/Powerdown.wav");


        // music files
        soundFilePaths.put("lv1", "sounds/music/Three Red Hearts/Three Red Hearts - Go.ogg");
        

        currentMusicClip = null;
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

    public void changeMusic(String music) {
        if (currentMusicClip != null) {
            currentMusicClip.stop();
        }

        String musicFilePath = soundFilePaths.get(music);
        if (musicFilePath == null) {
            System.err.println("Music file not found for key: " + music);
            return;
        }
        
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundMaster.class.getResource(musicFilePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            currentMusicClip = clip;

            currentMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Example usage, delete later
    public static void main(String[] args) {
        // Example usage: play a sound file located in the resources folder
        soundMaster soundMaster = new soundMaster();
            soundMaster.playSound("bounce0");
            soundMaster.playSound("goal");
            try {
                Thread.sleep(5000); // Wait for 1 second before playing the sound again
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            soundMaster.changeMusic("lv1");

            try {
                Thread.sleep(5000); // Wait for 1 second before playing the sound again
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            soundMaster.changeMusic(null);
    }
}
