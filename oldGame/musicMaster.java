import java.util.*;

import javax.sound.sampled.*;


public class musicMaster {

    HashMap<String, String> musicFilePaths;

    Clip clip;

    public musicMaster() {
        musicFilePaths = new HashMap<>();
        
        try{
            clip = AudioSystem.getClip();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // Add music file paths to the HashMap
        // Use this exclusively for music to avoid confusion
        // Original file names kept for reference, key name indicates actual purpose

        musicFilePaths.put("menu", "sounds/music/three-red-hearts-prepare-to-dev-download/Three Red Hearts - Connected.wav");

        musicFilePaths.put("gameOverworld0", "sounds/music/three-red-hearts-prepare-to-dev-download/Three Red Hearts - Candy.wav");
        musicFilePaths.put("gameOverworld1", "sounds/music/three-red-hearts-prepare-to-dev-download/Three Red Hearts - Go.wav");

        musicFilePaths.put("defeat", "sounds/music/EloLeChan - Funky Victory Draw Loss Themes/lose...wav");
    }

    public void changeTrackAndPlay(String music) {
        String musicFilePath = musicFilePaths.get(music);
        if (musicFilePath == null) {
            System.err.println("Music file not found for key: " + music);
            return;
        }
        try {
            
            // Load the music file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicMaster.class.getResource(musicFilePath));
            
            clip.stop(); // Stop the current music if any
            clip.close(); // Close the current clip to release resources

            clip.open(audioInputStream);

            if(music.equals("defeat")){
                clip.start();
                clip.loop(0); // Play the defeat music only once
            }
            else{
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music continuously
            }

            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public static void main(String[] args) {
        musicMaster musicMaster = new musicMaster();

        musicMaster.changeTrackAndPlay("menu"); // Play the initial music
        
        Scanner in = new Scanner(System.in);
        while(true) {
            int a = in.nextInt(); // Wait for user input to change the track
            if(a == 1) {
                musicMaster.changeTrackAndPlay("gameOverworld0"); // Change to a different track
            }
            else if(a == 2) {
                musicMaster.changeTrackAndPlay("gameOverworld1"); // Change to another track
            }
            else if(a == 0) {
                musicMaster.changeTrackAndPlay("menu"); // Change back to the initial track
            }
            else if(a == 3) {
                musicMaster.changeTrackAndPlay("defeat"); // Change to defeat track
            }
            else{
                break; // Exit the loop if the input is not recognized
            }
        }
    }
}

