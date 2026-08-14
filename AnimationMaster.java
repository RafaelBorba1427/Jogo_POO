import java.util.*;
import java.awt.image.BufferedImage;


public class AnimationMaster {
  private static final HashMap<Integer, BufferedImage[]> animations = new HashMap<>();
  private static final ArrayList<AnimationMaster> animation_masters = new ArrayList<>();

  private int animation_key;

  private int last_frame;
  private int current_frame;

  private static Signal<Boolean> update_animations = new Signal<>();

  public static javax.swing.Timer animation_timer;


  // Declares an animation with a unique key, the last frame index, and an array of sprites
  // Also connects it to the global animation loop
  // Due to variance in sprite storing method, this class expects the sprites to already be coverted into an array beforehand
  public AnimationMaster(int animation_key, int last_frame, BufferedImage[] sprites) {
    if (!animations.containsKey(animation_key)) {
      animations.put(animation_key, sprites);
    }

    this.animation_key = animation_key;
    this.last_frame = last_frame;

    animations.put(animation_key, sprites);
    animation_masters.add(this);
    
    syncToAnimationType(); // Sync the current frame with other animations of the same type, if there are any

    update_animations.connect((Boolean value) -> {
        update(value);
      }
    );
  }


  // The timer is shared between all animations, so it must be initialised statically
  public static void initializeAnimationMasterTimer() {
    animation_timer = new javax.swing.Timer(32, e -> {
        AnimationMaster.update_animations.emit(true);
      }
    );
    animation_timer.start();
  }


  // Updates the current frame of the animation
  // The boolean parameter is not used, but is required for the Signal connection
  public void update(Boolean value) {
    current_frame++;
    if (current_frame >= last_frame) {
      current_frame = 0;
    }
    System.out.println("Animation key: " + animation_key + ", Current frame: " + current_frame);
  }


  // Synchronizes the current frame of this animation master with another animation master that has the same animation key
  // Ideally, all animations of a same type will be synchronised to the same frame, so it doesn't matter who gets used as reference
  public void syncToAnimationType() {
    for(AnimationMaster animation_master : animation_masters) {
      if (animation_master.animation_key == animation_key) {
        this.current_frame = animation_master.current_frame;
        return;
      }
    }
    current_frame = 0; // If no other animation master with the same key is found, start from the first frame
    return;
  }


  // Should be called inside of paint components to draw the current frame of the animation at the specified x and y coordinates
  public void paint(java.awt.Graphics g, int x, int y) {
    BufferedImage[] sprites = animations.get(animation_key);
    if (sprites != null && current_frame < sprites.length) {
      g.drawImage(sprites[current_frame], x, y, null);
    }
  }


  /*
  public static void main(String[] args) {
    BufferedImage[] sprites = new BufferedImage[5]; // Replace with actual sprite images
    AnimationMaster animationMaster = new AnimationMaster(10, 100, sprites);
    initializeAnimationMasterTimer();
  }
  */
}