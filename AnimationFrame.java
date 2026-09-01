import java.awt.image.BufferedImage;

public class AnimationFrame {
  private BufferedImage image;
  private int dimension_x;
  private int dimension_y;

  public AnimationFrame(BufferedImage image, int dimension_x, int dimension_y) {
    this.image = image;
    this.dimension_x = dimension_x;
    this.dimension_y = dimension_y;
  }

  public BufferedImage getImage() {
    return image;
  }

  public int getDimension_x() {
    return dimension_x;
  }

  public int getDimension_y() {
    return dimension_y;
  }
}
