// ------------------------------------------------------------
// Axis Aligned Bounding Box
// * used to make the partition system for the collision detection system very efficient
// ------------------------------------------------------------
public class AABB {
    public Vector2D min_pos, max_pos;

    public double width() {
    return max_pos.x - min_pos.x;}

    public double height() {
        return max_pos.y - min_pos.y;}

    //Constructors
    public AABB(double min_x, double min_y, double max_x, double max_y){
        min_pos = new Vector2D(min_x, min_y);
        max_pos = new Vector2D(max_x, max_y);
    }

    public AABB(RectangularHitBox OBB){
        Vector2D sine_cos = OBB.getAxisX(),
                center = OBB.getCenter(); 

        double aabbHalfWidth =
            Math.abs(OBB.getHalfWidth() * sine_cos.x) +
            Math.abs(OBB.getHalfHeight() * sine_cos.y);

        double aabbHalfHeight =
                Math.abs(OBB.getHalfWidth() * sine_cos.y) +
                Math.abs(OBB.getHalfHeight() * sine_cos.x);

        min_pos = new Vector2D(center.x - aabbHalfWidth, center.y - aabbHalfHeight);
        max_pos = new Vector2D(center.x + aabbHalfWidth, center.y + aabbHalfHeight);
    }

    public AABB(CircularHitBox OBB){
        Vector2D center = OBB.getCenter();
        double radius = OBB.getRadius();
        min_pos = new Vector2D(center.x - radius, center.y - radius);
        max_pos = new Vector2D(center.x + radius, center.y + radius);
    }


    //Update Functions
    public void update(RectangularHitBox OBB){
        Vector2D sine_cos = OBB.getAxisX(),
                center = OBB.getCenter(); 

        double aabbHalfWidth =
            Math.abs(OBB.getHalfWidth() * sine_cos.x) +
            Math.abs(OBB.getHalfHeight() * sine_cos.y);

        double aabbHalfHeight =
                Math.abs(OBB.getHalfWidth() * sine_cos.y) +
                Math.abs(OBB.getHalfHeight() * sine_cos.x);

        min_pos.x =  center.x - aabbHalfWidth;
        min_pos.y = center.y - aabbHalfHeight;
        max_pos.x = center.x + aabbHalfWidth;
        max_pos.y = center.y + aabbHalfHeight;
    }

    public void update(CircularHitBox OBB){
        Vector2D center = OBB.getCenter();
        double radius = OBB.getRadius();
        min_pos.x = center.x - radius;
        min_pos.y = center.y - radius;
        max_pos.x = center.x + radius;
        max_pos.y = center.y + radius;
    }


    public boolean intersect(AABB target){
        return (this.max_pos.x >= target.min_pos.x) && (this.min_pos.x <= target.max_pos.x)
        &&  (this.max_pos.y >= target.min_pos.y) && (this.min_pos.y <= target.max_pos.y);
    }

}
