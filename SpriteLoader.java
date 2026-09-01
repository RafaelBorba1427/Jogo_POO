import java.awt.image.BufferedImage;
import java.util.*;

public class SpriteLoader {
    private static final HashMap<String, BufferedImage> spritesheets = new HashMap<>();
    private static final HashMap<String, AnimationFrame[]> sprites_spliced = new HashMap<>();

    // Loads a spritesheet and splices it into an array of sprites
    // sprite_height_offset is used to specify the row in which the desired sprites are located, in case the spritesheet contains multiple rows of sprites
    // Use the position of the desired row, not the amount of pixels to offset by.
    public static void loadSpritesheet(String animation_key, String image_path, int sprite_width,
                                        int sprite_height, int num_sprites, int sprite_height_offset) {
        if(spritesheets.containsKey(animation_key)) {
            System.out.println("Spritesheet with key " + animation_key + " already loaded.");
            return;
        }

        BufferedImage spritesheet = null;
        AnimationFrame[] sprites = new AnimationFrame[num_sprites];
        
        try{
            spritesheet = javax.imageio.ImageIO.read(new java.io.File(image_path));
            spritesheets.put(animation_key, spritesheet);

            for (int i = 0; i < num_sprites; i++) {
                BufferedImage sprite_image = spritesheet.getSubimage(i * sprite_width, 0, sprite_width, sprite_height * (1 + sprite_height_offset));
                sprites[i] = new AnimationFrame(sprite_image, sprite_width, sprite_height);
            }

            sprites_spliced.put(animation_key, sprites);

        } catch (Exception e) {
            System.out.println("Error loading spritesheet: " + e.getMessage());
        }

        spritesheets.put(animation_key, spritesheet);
        sprites_spliced.put(animation_key, sprites);
    }

    public static AnimationFrame[] getSplicedSprites(String animation_key) {
        return sprites_spliced.get(animation_key);
    }
}