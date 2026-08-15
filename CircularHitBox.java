public class CircularHitBox implements HitBox{
    private Vector2D center;
    private double radius;
    private AABB aabb;

    public CircularHitBox(double x, double y, double radius) {
        this.center = new Vector2D(x, y);
        this.radius = radius;
        aabb = new AABB(this);
    }

    public CircularHitBox(Vector2D center, double radius) {
        this.center = center;
        this.radius = radius;
        aabb = new AABB(this);
    }

    public Vector2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public AABB getAABB(){
        return this.aabb;
    }
    
    public void updateHitBox(Vector2D center, double radius){
        this.center = center;
        this.radius = radius;
        this.aabb.update(this);
    }

    public void setCenter(double x, double y) {
        center.x = x;
        center.y = y;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean intersects(RectangularHitBox rectangle) {
        return rectangle.intersects(this);
    }

    public boolean intersects(CircularHitBox circle) {
        double sum_of_radiuses = this.radius + circle.radius;
        return ( (this.center.subtract(circle.getCenter())).lengthSquared() <=
         (sum_of_radiuses)*(sum_of_radiuses) );
    }
}
