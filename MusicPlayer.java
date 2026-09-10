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

        // Guardas adicionadas na refatoracao. Antes, se AudioSystem.getClip()
        // tivesse falhado no init (maquina sem placa de som, driver ocupado) o
        // clip ficava nulo e a primeira troca de faixa derrubava a tela inteira
        // com NullPointerException. O mesmo valia para getResource() devolvendo
        // null quando o .wav nao esta no classpath. O jogo agora segue mudo em
        // vez de morrer.
        if (clip == null) {
            System.err.println("MusicPlayer: nenhum clip de audio disponivel, seguindo sem musica.");
            return;
        }

        java.net.URL music_url = MusicPlayer.class.getResource(musicFilePath);
        if (music_url == null) {
            System.err.println("MusicPlayer: arquivo nao encontrado no classpath: " + musicFilePath);
            return;
        }

        try {
            // Load the music file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(music_url);

            clip.stop(); // Stop the current music if any
            clip.close(); // Close the current clip to release resources

            clip.open(audioInputStream);

            applyGain((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN), volume);

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


    public static void updateVolume(float new_volume) {
        MusicPlayer.volume = clampVolume(new_volume);
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            applyGain((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN), MusicPlayer.volume);
        }
    }

    public static float getVolume() {
        return volume;
    }

    // Volume linear (0.0 a 1.0) limitado ao intervalo valido.
    static float clampVolume(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }

    // Converte volume linear em decibeis e aplica no controle, respeitando os
    // limites que a placa de som declara.
    //
    // Antes era "gainControl.setValue(20f * log10(volume))" direto. Com
    // volume 0 isso da -Infinity, e FloatControl.setValue(-Infinity) lanca
    // IllegalArgumentException -- ou seja, o slider de volume no zero (mudo,
    // o valor que mais se usa) derrubava o som inteiro. Fora do intervalo do
    // controle o erro e o mesmo.
    static void applyGain(FloatControl control, float linear_volume) {
        if (control == null) return;

        float decibels = (linear_volume <= 0.0001f)
                ? control.getMinimum()
                : (float) (20.0 * Math.log10(linear_volume));

        if (decibels < control.getMinimum()) decibels = control.getMinimum();
        if (decibels > control.getMaximum()) decibels = control.getMaximum();

        control.setValue(decibels);
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