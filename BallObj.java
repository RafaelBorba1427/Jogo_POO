import java.awt.geom.Ellipse2D;

public class BallObj extends MovableObj{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //movable objects variables

    
    BallObj(float x_pos, float y_pos, float width, float height, boolean active, int obj_type, int obj_id, float elastic_factor){
        super(x_pos, y_pos, width, height, active, obj_type, obj_id, elastic_factor);
    }

    @Override
    public void createHitBox(){
        hit_box = new Ellipse2D.Float( x_pos, y_pos, dimensions.width, dimensions.height);
    }

}
