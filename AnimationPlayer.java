import java.util.*;
import java.awt.*;

public class AnimationPlayer {
  private static final ArrayList<AnimationPlayer> animation_players = new ArrayList<>();

  private static final int default_fps = 60;
  private static boolean animation_player_initialised = false;

  private String animation_key;

  private int last_frame;
  private int fps;

  private int current_frame;
  private int current_frame_time;

  // Settings
  private boolean loop = true;
  private boolean play_reversed = false;
  private boolean is_unique = false; // If true, this animation player will not sync with other animations of the same type
  private boolean play = false; // If true, the animation will play, otherwise it will pause

  private static Vector2D render_rescale_factor = new Vector2D(1, 1);

  private static Signal<Boolean> update_animations = new Signal<>();

  public static javax.swing.Timer animation_timer;

  // Declares an animation with a unique key, the last frame index, and an array
  // of sprites
  // Also connects it to the global animation loop
  // Due to variance in sprite storing method, this class expects the sprites to
  // already be coverted into an array beforehand
  public AnimationPlayer(String animation_key, int fps) throws Exception {
    if(animation_player_initialised == false){
      AnimationPlayer.initializeAnimationPlayerTimer();
    }
    if (SpriteLoader.getSplicedSprites(animation_key) == null) {
      throw new Exception("AnimationPlayer: No sprites found for animation key " + animation_key);
    }
    this.animation_key = animation_key;
    this.last_frame = SpriteLoader.getSplicedSprites(animation_key).length - 1;
    this.fps = fps;

    animation_players.add(this);

    syncToAnimationType(); // Sync the current frame with other animations of the same type, if there are
                           // any

    update_animations.connect((Boolean value) -> {
      update(value);
    });
  }


  // Optimised constructor that loads the spritesheet and creates an animation
  // player in one step
  // Kept the old one for compatibility with existing code, but this one is
  // preferred
  public AnimationPlayer(String animation_key, String image_path, int sprite_width, int sprite_height,
      int sprite_row_index, int num_sprites, int fps) throws Exception {
    
    if(animation_player_initialised == false){
      AnimationPlayer.initializeAnimationPlayerTimer();
    }

    SpriteLoader.loadSpritesheet(animation_key, image_path, sprite_width, sprite_height, num_sprites, sprite_row_index);
    if (SpriteLoader.getSplicedSprites(animation_key) == null) {
      throw new Exception("AnimationPlayer: No sprites found for animation key " + animation_key);
    }
    this.animation_key = animation_key;
    this.last_frame = SpriteLoader.getSplicedSprites(animation_key).length - 1;
    this.fps = fps;

    if(is_unique == false){
      syncToAnimationType(); // Sync the current frame with other animations of the same type, if there are any
    }

    animation_players.add(this);

    update_animations.connect((Boolean value) -> {
        update(value);
      }
    );
  }


  // Yet another constructor that allows for more settings
  // Again, kept the old ones for compatibility, but this one is preferred
  public AnimationPlayer(String animation_key, String image_path, int sprite_width, int sprite_height,
      int sprite_row_index, int num_sprites, int fps, boolean loop, boolean is_unique) throws Exception {
    this(animation_key, image_path, sprite_width, sprite_height, sprite_row_index, num_sprites, fps);
    this.loop = loop;
    this.is_unique = is_unique;
  }


  // The timer is shared between all animations, so it must be initialised
  // statically
  public static void initializeAnimationPlayerTimer() {
    // 16ms = ~60fps
    animation_timer = new javax.swing.Timer(16, e -> {
      updateRenderRescaleFactors();
      AnimationPlayer.update_animations.emit(true);
    });
    animation_timer.start();
  }


  // No idea why this is here, but it breaks things so into the try/catch it goes~
  private static void updateRenderRescaleFactors() {
    try{
      Vector2D current_dim = new Vector2D(Main.frame.getSize());
      render_rescale_factor.setSize(current_dim.x / Main.DEFAULT_RESOLUTION.width,
      current_dim.y / Main.DEFAULT_RESOLUTION.height);
    }
    catch(Exception e){
      return;
    }
  }


  public void reverseAnimation(boolean reverse){
    this.play_reversed = reverse;
  }


  // Updates the current frame of the animation
  // The boolean parameter is not used, but is required for the Signal connection
  public void update(Boolean value) {
    if (!play) {
      return;
    }

    if(play_reversed){
      current_frame_time--;

      if (current_frame_time <= 0) {
        current_frame_time = default_fps / fps;
        current_frame--;

        if (current_frame < 0) {
          if (loop) {
            current_frame = last_frame;
          } else {
            current_frame = 0;
            play = false; // Stop the animation if it reaches the end and is not looping
          }
        }
      }
    }
    else{
      current_frame_time++;
      if (current_frame_time >= default_fps / fps) {
        current_frame_time = 0;
        current_frame++;

        if (current_frame > last_frame) {
          if (loop) {
            current_frame = 0;
          } else {
            current_frame = last_frame;
            play = false; // Stop the animation if it reaches the end and is not looping
          }
        }
      }
    }
  }


  // Synchronizes the current frame of this animation master with another
  // animation master that has the same animation key
  // Ideally, all animations of a same type will be synchronised to the same
  // frame, so it doesn't matter who gets used as reference
  public void syncToAnimationType() {
    for (AnimationPlayer animation_player : animation_players) {
      if (animation_player.animation_key == animation_key) {
        this.current_frame = animation_player.current_frame;
        return;
      }
    }
    current_frame = 0; // If no other animation master with the same key is found, start from the first frame
    return;
  }


  // Should be called inside of paint components to draw the current frame of the
  // animation at the specified x and y coordinates
  public void paint(Graphics2D g2d, int position_x, int position_y, Vector2D dimensions, double rotation) {
    AnimationFrame[] sprites = SpriteLoader.getSplicedSprites(animation_key);

    g2d = (Graphics2D) g2d.create(); // copy of g2d

    // 1. Compute the center of the image
    Vector2D center = new Vector2D((position_x + dimensions.x / 2), (position_y + dimensions.y / 2));

    g2d.translate((int) center.x, (int) center.y);

    // 2. Apply the rotation
    g2d.rotate(rotation);

    // 3. Draw sprite at proper rotation and position
    if (sprites != null && current_frame < sprites.length) {
      int drawWidth = (int) (dimensions.x);
      int drawHeight = (int) (dimensions.y);

      g2d.drawImage(sprites[current_frame].getImage(), -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight, null);
    }
    g2d.dispose();
  }


  // Now that animations arent always loops, this function is necessary
  public void play() {
    play = true;
  }
}
