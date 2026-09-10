import java.util.*;

import javax.sound.sampled.*;


public class MusicPlayer {

    public static Signal<String> musicFinished = new Signal<>();

    private static HashMap<String, String> musicFilePaths = new HashMap<>();

    private static String currentTrack = null; // Variable to store the current track name
    private static Clip clip;

    // Play settings
    private static boolean isLooping = true; // Set to true to loop the music continuously
    private static float volume; // Default volume level (0.0 to 1.0)

    public static void initialiseMusicPlayer() {
        
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

        volume = 0.1f; // Set the default volume level
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

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(volume));

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


    public static void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
    public static void pauseMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
    public static void resumeMusic() {
        if (clip != null && !clip.isRunning()) {
            clip.start();
        }
    }


    public static void updateVolume(float volume) {
        MusicPlayer.volume = volume; // Update the volume variable
        if (clip != null) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(volume));
        }
    }


    public static boolean isLooping() {
        return isLooping;
    }
    public static String getCurrentTrack() {
        return currentTrack;
    }

    
    public static void main(String[] args) {
        MusicPlayer.initialiseMusicPlayer(); // Initialize the music player

        MusicPlayer.setTrackAndPlay("menu"); // Play the initial music
        
        // Errors annoy me, should probably remove this later
        @SuppressWarnings("resource")
        Scanner in = new Scanner(System.in);
        while(true) {
            int a = Integer.parseInt(in.nextLine()); // Wait for user input to change the track
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
            else if(a == 4) {
                MusicPlayer.stopMusic(); // Stop the music
                System.out.println("Music stopped.");
            }
            else if(a == 5) {
                MusicPlayer.pauseMusic(); // Pause the music
                System.out.println("Music paused.");
            }
            else if(a == 6) {
                MusicPlayer.resumeMusic(); // Resume the music
                System.out.println("Music resumed.");
            }
            else if(a == 7) {
                System.out.print("Enter volume (0.0 to 1.0): ");
                float newVolume = Float.parseFloat(in.nextLine());
                MusicPlayer.updateVolume(newVolume); // Update the volume
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
                // If we ever wanna do syncing with music
                MusicPlayer.musicFinished.emit(MusicPlayer.getCurrentTrack());
            }
        }
    }
}