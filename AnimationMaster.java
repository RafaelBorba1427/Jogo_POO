import java.util.*;
import java.awt.image.BufferedImage;

public class AnimationMaster {
  int last_frame;
  int current_frame;
  ArrayList<BufferedImage> animations = new ArrayList<BufferedImage>();

  Signal<Integer> update_animations = new Signal<>();

  public AnimationMaster(int last_frame) {
    this.last_frame = last_frame;
    current_frame = 0;

    update_animations.connect((Integer value) -> {
      update();
    });
  }

  public void update() {
    current_frame++;
    if (current_frame > last_frame) {
      current_frame = 0;
    }
  }
}