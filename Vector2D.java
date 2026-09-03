public class Vector2D {
    public double x;
    public double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D other_vec){
        this.x = other_vec.x;
        this.y = other_vec.y;
    }

    public void setSize(double new_x, double new_y){
        this.x = new_x;
        this.y = new_y;
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }

    public double cross(Vector2D other) {
        return (x * other.y) - (other.x * y);
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vector2D normalize() {
        double length = length();

        if (length == 0) {
            return new Vector2D(0, 0);
        }

        return new Vector2D(x / length, y / length);
    }

    public Vector2D perpendicular() {
        return new Vector2D(-y, x);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public void printValues(){
        System.out.print("(" + x + ", " + y + ")" + "\n");
    }
}