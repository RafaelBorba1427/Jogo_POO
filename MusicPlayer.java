import java.util.*;

import javax.sound.sampled.*;


public class MusicPlayer {

    public static Signal<String> musicFinished = new Signal<>();

    private static HashMap<String, String> musicFilePaths = new HashMap<>();

    private static String currentTrack = null; // Variable to store the current track name
    private static Clip clip;

    // Play settings
    private static boolean isLooping = true; // Set to true to loop the music continuously

    public void initialiseMusicPlayer() {
        
        try{
            clip = AudioSystem.getClip();
            clip.addLineListener(new MusicPlayerListener());
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        // Add music file paths to the HashMap
        // Use this exclusively for music to avoid confusion
        // Original file names kept for reference, key name indicates actual purpose

        // Notably, doesn't preload the files, just stores paths
        // I found it doesn't affect performance too badly, but can be changed if lag shows up later

        musicFilePaths.put("menu", "sounds/music/three-red-hearts-prepare-to-dev-download/Three Red Hearts - Connected.wav");

        musicFilePaths.put("gameOverworld0", "sounds/music/three-red-hearts-prepare-to-dev-download/Three Red Hearts - Candy.wav");
        musicFilePaths.put("gameOverworld1", "sounds/music/three-red-hearts-prepare-to-dev-download/Three Red Hearts - Go.wav");

        musicFilePaths.put("defeat", "sounds/music/EloLeChan - Funky Victory Draw Loss Themes/lose...wav");
    }


    // Set the track and play it, with an option to loop or not
    public static void setTrackAndPlay(String music) {
        setTrackAndPlay(music, true); // Default to looping
    }
    public static void setTrackAndPlay(String music, boolean loop) {

        isLooping = loop;
        currentTrack = music; // Update the current track name
        String musicFilePath = musicFilePaths.get(music);
        if (musicFilePath == null) {
            System.err.println("Music file not found for key: " + music);
            return;
        }
        try {
            
            // Load the music file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(MusicPlayer.class.getResource(musicFilePath));
            
            clip.stop(); // Stop the current music if any
            clip.close(); // Close the current clip to release resources

            clip.open(audioInputStream);

            if(!isLooping){
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

    // We use clip for all the tracks, never close the clip or the whole thing breaks
    // Dw about resource leak, its just one clip, and it will be closed when the program ends
    public static void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }


    public static boolean isLooping() {
        return isLooping;
    }
    public static String getCurrentTrack() {
        return currentTrack;
    }

    public static void main(String[] args) {

        MusicPlayer.setTrackAndPlay("menu", true); // Play the initial music
        
        Scanner in = new Scanner(System.in);
        while(true) {
            int a = in.nextInt(); // Wait for user input to change the track
            if(a == 1) {
                MusicPlayer.setTrackAndPlay("gameOverworld0"); // Change to a different track
            }
            else if(a == 2) {
                MusicPlayer.setTrackAndPlay("gameOverworld1"); // Change to another track
            }
            else if(a == 0) {
                MusicPlayer.setTrackAndPlay("menu"); // Change back to the initial track
            }
            else if(a == 3) {
                MusicPlayer.setTrackAndPlay("defeat", false); // Change to defeat track
            }
            else{
                break; // Exit the loop if the input is not recognized
            }
        }
    }
}

class MusicPlayerListener implements LineListener {
    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.START) {
            if (MusicPlayer.isLooping() == false) {
                // If we ever wanna do syncing with the music
                MusicPlayer.musicFinished.emit(MusicPlayer.getCurrentTrack());
            }
        }
    }
}