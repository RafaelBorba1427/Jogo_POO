import javax.sound.sampled.*;

import java.util.*;

public class SoundEffectPlayer {

    private static HashMap<String, String> soundFilePaths = new HashMap<>();

    private static float original_sample_rate;

    // Settings and the like
    private static boolean supports_sample_rate_control = false;

    private static float volume;

    private static DeltaTime delta_time = new DeltaTime();

    // Must be called before any sound is played to load the sound file paths into the HashMap
    public static void initialiseSoundEffectPlayer() {
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


        Clip test_clip;
        try {
            test_clip = AudioSystem.getClip();
            original_sample_rate = ((FloatControl) test_clip.getControl(FloatControl.Type.SAMPLE_RATE)).getValue();
            supports_sample_rate_control = test_clip.isControlSupported(FloatControl.Type.SAMPLE_RATE);
            test_clip.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            supports_sample_rate_control = false;
            test_clip = null;
        }

        volume = 0.1f;
    }


    public static void playSoundWithPitchShift(String sound){
        if(supports_sample_rate_control) {
            String soundFilePath = soundFilePaths.get(sound);
            if (soundFilePath == null) {
                System.err.println("Sound file not found for key: " + sound);
                return;
            }

            java.net.URL sound_url = SoundEffectPlayer.class.getResource(soundFilePath);
            if (sound_url == null) {
                warnMissingSound(sound, soundFilePath);
                return;
            }

            try {
                // Load the sound file
                AudioInputStream input_stream = AudioSystem.getAudioInputStream(sound_url);
                Clip clip = AudioSystem.getClip();

                clip.open(input_stream);

                FloatControl sample_rate_control = (FloatControl) clip.getControl(FloatControl.Type.SAMPLE_RATE);
                float random_sample_rate = original_sample_rate * (0.9f + (float) Math.random() * 0.2f); // Randomize between 90% and 110%
                sample_rate_control.setValue(random_sample_rate);

                MusicPlayer.applyGain((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN), volume);

                clip.start();

                sample_rate_control.setValue(original_sample_rate); // Reset to original sample rate after playing
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            // Failsafe if the system doesn't support sample rate control, just play the sound normally
            playSound(sound);
        }
    }


    public static void playSound(String sound) {
        String soundFilePath = soundFilePaths.get(sound);
        if (soundFilePath == null) {
            System.err.println("Sound file not found for key: " + sound);
            return;
        }
        // Guarda adicionada na refatoracao: getResource devolve null quando o
        // .wav nao esta no classpath, e AudioSystem.getAudioInputStream(null)
        // lanca NullPointerException. Antes isso virava um stack trace por
        // quique da bola. Agora avisa uma vez e o jogo segue mudo.
        java.net.URL sound_url = SoundEffectPlayer.class.getResource(soundFilePath);
        if (sound_url == null) {
            warnMissingSound(sound, soundFilePath);
            return;
        }

        try {
            // Load the sound file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(sound_url);

            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            MusicPlayer.applyGain((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN), volume);

            clip.start();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playBounceSound() {
        if(delta_time.get() < 20) {
            return; // Do not play the sound if it has been less than 20 milliseconds since the last bounce sound
        }
        playSoundWithPitchShift("bounce_realistic");
    }


    // Subroutines for playing random sounds from a category
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


    public static void updateVolume(float new_volume) {
        SoundEffectPlayer.volume = MusicPlayer.clampVolume(new_volume);
    }

    public static float getVolume() {
        return volume;
    }

    // Avisa uma unica vez por som que falta, para nao inundar o console a cada
    // quique.
    private static final HashSet<String> warned_missing_sounds = new HashSet<>();

    private static void warnMissingSound(String key, String path) {
        if (warned_missing_sounds.add(key)) {
            System.err.println("SoundEffectPlayer: arquivo nao encontrado no classpath para \""
                    + key + "\": " + path);
        }
    }


    /*
    // Example usage, delete later
    public static void main(String[] args) {
        SoundEffectPlayer.initialiseSoundEffectPlayer(); // Initialize the sound effect player
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
    */
}
