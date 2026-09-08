import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

public class SpriteLoader {
    private static final HashMap<String, BufferedImage> spritesheets = new HashMap<>();
    private static final HashMap<String, AnimationFrame[]> sprites_spliced = new HashMap<>();

    // Loads a spritesheet and splices it into an array of sprites
    // sprite_height_offset is used to specify the row in which the desired sprites are located, in case the spritesheet contains multiple rows of sprites
    // Use the position of the desired row, not the amount of pixels to offset by.
    public static void loadSpritesheet(String animation_key, String image_path, int sprite_width,
                                        int sprite_height, int num_sprites, int sprite_row_index) {
        if(spritesheets.containsKey(animation_key)) {
            return;
        }

        BufferedImage spritesheet = null;
        AnimationFrame[] sprites = new AnimationFrame[num_sprites];
        
        try{ 
            spritesheet = javax.imageio.ImageIO.read(new java.io.File(image_path));
            spritesheets.put(animation_key, spritesheet);

            for (int i = 0; i < num_sprites; i++) {
                BufferedImage sprite_image = spritesheet.getSubimage(i * sprite_width, sprite_row_index * sprite_height, sprite_width, sprite_height);
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

    public static BufferedImage loadNewBackgroundImage(String path) {
        try {
            BufferedImage backgroundImage = javax.imageio.ImageIO.read(new java.io.File(path));
            return backgroundImage;

        } catch (IOException e) {
            System.out.println("Error loading background: " + e.getMessage());
            return null;
        }

    }
}