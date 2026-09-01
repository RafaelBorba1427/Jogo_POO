import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.*;


public class AnimationPlayer {
  private static final ArrayList<AnimationPlayer> animation_players = new ArrayList<>();
  
  private static int default_fps = 60;

  private String animation_key;

  private int last_frame;
  private int fps;

  private int current_frame;
  private int current_frame_time;

  private static double rescale_factor = 6.0;

  private static Signal<Boolean> update_animations = new Signal<>();

  public static javax.swing.Timer animation_timer;


  // Declares an animation with a unique key, the last frame index, and an array of sprites
  // Also connects it to the global animation loop
  // Due to variance in sprite storing method, this class expects the sprites to already be coverted into an array beforehand
  public AnimationPlayer(String animation_key, int last_frame, int fps) throws Exception {

    if(SpriteLoader.getSplicedSprites(animation_key) == null) {
      throw new Exception("AnimationPlayer: No sprites found for animation key " + animation_key);
    }

    this.animation_key = animation_key;
    this.last_frame = last_frame;
    this.fps = fps;

    animation_players.add(this);
    
    syncToAnimationType(); // Sync the current frame with other animations of the same type, if there are any

    update_animations.connect((Boolean value) -> {
        update(value);
      }
    );
  }


  // The timer is shared between all animations, so it must be initialised statically
  public static void initializeAnimationPlayerTimer() {
    // 16ms = ~60fps for reference
    animation_timer = new javax.swing.Timer(16, e -> {
        AnimationPlayer.update_animations.emit(true);
      }
    );
    animation_timer.start();
  }


  // Updates the current frame of the animation
  // The boolean parameter is not used, but is required for the Signal connection
  public void update(Boolean value) {
    current_frame_time++;
    if(default_fps / current_frame_time <= fps) {
      current_frame++;
      current_frame_time = 0;
    }

    if (current_frame >= last_frame) {
      current_frame = 0;
    }
    System.out.println("Animation key: " + animation_key + ", Current frame: " + current_frame);
  }


  // Synchronizes the current frame of this animation master with another animation master that has the same animation key
  // Ideally, all animations of a same type will be synchronised to the same frame, so it doesn't matter who gets used as reference
  public void syncToAnimationType() {
    for(AnimationPlayer animation_player : animation_players) {
      if (animation_player.animation_key == animation_key) {
        this.current_frame = animation_player.current_frame;
        return;
      }
    }
    current_frame = 0; // If no other animation master with the same key is found, start from the first frame
    return;
  }


  // Should be called inside of paint components to draw the current frame of the animation at the specified x and y coordinates
  public void paint(java.awt.Graphics g, int position_x, int position_y, Vector2D dimensions, double rotation) {
    AnimationFrame[] sprites = SpriteLoader.getSplicedSprites(animation_key);

    Graphics2D g2d = (Graphics2D) g.create(); // copy of g2d

    // 1. Compute the center of the image
    Vector2D center = new Vector2D(position_x + dimensions.x/2, position_y + dimensions.y/2);

    g2d.translate((int) center.x, (int) center.y);

    // 2. Apply the rotation
    g2d.rotate(rotation);

    // 3. Draw sprite at proper rotation and position
    if (sprites != null && current_frame < sprites.length) {
      g.drawImage(sprites[current_frame].getImage(), position_x, position_y,
                  (int)(sprites[current_frame].getDimension_x() * rescale_factor),
                  (int)(sprites[current_frame].getDimension_y() * rescale_factor), null);
    }
    
            
    g2d.dispose();
  }

  public static void setRescaleFactor(double factor) {
    rescale_factor = factor;
  }
}