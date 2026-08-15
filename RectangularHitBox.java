public class RectangularHitBox implements HitBox{

    private Vector2D center;

    // Half dimensions.
    // Ex: width = 100 means halfWidth = 50.
    private double halfWidth;
    private double halfHeight;

    // Rotation in radians.
    private double rotation;

    //it's Axis Aligned Bounding Box
    private AABB aabb;

    public RectangularHitBox(double x, double y,
               double width, double height,
               double rotation) {

        this.center = new Vector2D(x, y);
        this.halfWidth = width / 2.0;
        this.halfHeight = height / 2.0;
        this.rotation = rotation;
        this.aabb = new AABB(this);
    }

    public RectangularHitBox(Vector2D center,
               Vector2D dimensions,
               double rotation) {

        this.center = center;
        this.halfWidth = dimensions.x / 2.0;
        this.halfHeight = dimensions.y / 2.0;
        this.rotation = rotation;
        this.aabb = new AABB(this);
    }

    // ------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------

    public Vector2D getCenter() {
        return center;
    }

    public double getWidth() {
        return halfWidth * 2.0;
    }

    public double getHeight() {
        return halfHeight * 2.0;
    }

    public double getHalfWidth() {
        return halfWidth;
    }

    public double getHalfHeight() {
        return halfHeight;
    }

    public double getRotation() {
        return rotation;
    }

    public AABB getAABB(){
        return this.aabb;
    }

    public void setPosition(double x, double y) {
        center.x = x;
        center.y = y;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void rotate(double amount) {
        rotation += amount;
    }

    public void updateHitBox(Vector2D center,
               Vector2D dimensions,
               double rotation) 
    {

        this.center = center;
        this.halfWidth = dimensions.x / 2.0;
        this.halfHeight = dimensions.y / 2.0;
        this.rotation = rotation;
        this.aabb.update(this);
    }

    // Returns the rectangle's local X axis.
    // This is the direction vector of its width.
    public Vector2D getAxisX() {
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        return new Vector2D(cos, sin);
    }

    // Returns the rectangle's local Y axis.
    // This is the direction vector of its height.
    public Vector2D getAxisY() {
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        return new Vector2D(-sin, cos);
    }

/*  Returns the four world-space corners of the rectangle.
     Order:
     
     0 = north/right
     1 = south/right
     2 = south/left
     3 = north/left */
    
    public Vector2D[] getCorners() {

        Vector2D axisX = getAxisX();
        Vector2D axisY = getAxisY();

        Vector2D x = axisX.multiply(halfWidth);
        Vector2D y = axisY.multiply(halfHeight);

        return new Vector2D[] {

            center.add(x).add(y),
            center.add(x).subtract(y),
            center.subtract(x).subtract(y),
            center.subtract(x).add(y)

        };
    }

    // ------------------------------------------------------------
    // Containment / Intersection Detection
    // ------------------------------------------------------------
    
    // Detects if point is inside the rotated rectangle
    public boolean contains(Vector2D point) {

        Vector2D relative = point.subtract(center);

        Vector2D axisX = getAxisX();
        Vector2D axisY = getAxisY();

        double localX = relative.dot(axisX);
        double localY = relative.dot(axisY);

        return Math.abs(localX) <= halfWidth
            && Math.abs(localY) <= halfHeight;
    }

    // Checks if this rectangle intersects another rotated rectangle.
    // Uses the Separating Axis Theorem.
    public boolean intersects(RectangularHitBox other) {

        Vector2D[] axes = {
            // This rectangle's axes
            getAxisX(),
            getAxisY(),
            // Other rectangle's axes
            other.getAxisX(),
            other.getAxisY()
        };

        for (Vector2D axis : axes) {

            Projection projectionA = projectOnto(axis);
            Projection projectionB = other.projectOnto(axis);

            if (projectionA.isSeparatedFrom(projectionB)) {
                return false;
            }
        }

        return true;
    }

    // Circle / Rectangle intersection
    public boolean intersects(CircularHitBox circle) {

        //Transform the circle's center into the rectangle's
        //local coordinate system.
        Vector2D relative =
                circle.getCenter().subtract(center);

        Vector2D axisX = getAxisX();
        Vector2D axisY = getAxisY();

        double localX = relative.dot(axisX);
        double localY = relative.dot(axisY);


        //Find the closest point on the rectangle to the circle.         
        double closestX =
                valueBoundedInRange(localX, -halfWidth, halfWidth);

        double closestY =
                valueBoundedInRange(localY, -halfHeight, halfHeight);


        //Convert the closest point back to game coordinate system.
        Vector2D closestPoint =
                center
                    .add(axisX.multiply(closestX))
                    .add(axisY.multiply(closestY));


        //If the closest point is within the circle's radius,
        //they intersect.
        Vector2D difference =
                circle.getCenter().subtract(closestPoint);

        return difference.lengthSquared()
                <= circle.getRadius() * circle.getRadius();
    }

    // Returns the value if it is inside the range, else it returns the max or min of the range
    private double valueBoundedInRange(double value, double min, double max) {

        return Math.max(min, Math.min(max, value));
    }


    // ------------------------------------------------------------
    // Projection
    // ------------------------------------------------------------

    // Projection helper class
    private static class Projection {

        double min;
        double max;

        Projection(double min, double max) {
            this.min = min;
            this.max = max;
        }

        boolean isSeparatedFrom(Projection other) {

            return max < other.min
                || other.max < min;
        }
    }


    //Projects this rectangle onto an arbitrary axis.
    private Projection projectOnto(Vector2D axis) {

        Vector2D normalizedAxis = axis.normalize();

        Vector2D localX = getAxisX();
        Vector2D localY = getAxisY();

        /*
         * The projection radius of an OBB onto an axis is:
         *
         * |axis dot localX| * halfWidth
         * +
         * |axis dot localY| * halfHeight
         */
        double radius =
                Math.abs(normalizedAxis.dot(localX)) * halfWidth
              + Math.abs(normalizedAxis.dot(localY)) * halfHeight;

        double projectionCenter =
                center.dot(normalizedAxis);

        return new Projection(
                projectionCenter - radius,
                projectionCenter + radius
        );
    }

    
}
