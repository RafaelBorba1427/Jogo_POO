import java.awt.geom.Ellipse2D;

public class BallObj extends MovableObj{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //movable objects variables
    

    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------
    
    BallObj(double x_pos, double y_pos, double radius, double rotation, boolean rotatable, boolean active, int obj_type, int obj_id, double elastic_factor){
        super(x_pos, y_pos, radius, radius, rotation, rotatable, active, obj_type, GameObject.BALL_OBJ, elastic_factor);
    }

    @Override
    public void createHitBox(){ 
        hit_box = new CircularHitBox( position, dimensions.x);
    }

    @Override
    public void updateHitBox(){
        ((CircularHitBox)hit_box).updateHitBox(position, dimensions.x);
    }

    @Override
    //Colision Detection
    public boolean collides(GameObject outro_obj){
        if(outro_obj.getObjType() == GameObject.BALL_OBJ){
            return ((CircularHitBox) this.hit_box).intersects((CircularHitBox) outro_obj.getHitBox()); 
        }
        return ((CircularHitBox) this.hit_box).intersects((RectangularHitBox) outro_obj.getHitBox());
    }

    @Override
    public void update(){
        if(GameRules.physics_on){
            velocity.x += acceleration.x;
            velocity.y += acceleration.y;
            position.x += velocity.x;
            position.y += velocity.y;
            
            if(Math.abs(velocity.x) < MIN_VELOCITY) velocity.x = 0;
            if(Math.abs(velocity.y) < MIN_VELOCITY) velocity.y = 0;

            updateHitBox();
        }
    }

    // ------------------------------------------------------------
    // Ball Obj Exclusive methods
    // ------------------------------------------------------------
    public double getRadius(){
        return dimensions.x;
    }

}
