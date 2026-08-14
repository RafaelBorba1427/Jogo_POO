import java.awt.geom.Dimension2D;
import java.awt.Dimension;

public class DimensionFloat extends Dimension2D {
    public float width;
    public float height;

    public DimensionFloat() {
        this(0.0f, 0.0f);
    }

    public DimensionFloat(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setSize(double width, double height) {
        this.width = (float) width;
        this.height = (float) height;
    }

    // Overload setter for float precision directly
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public Dimension convertToInt(){
        return new Dimension((int) width, (int) height);
    }
}
