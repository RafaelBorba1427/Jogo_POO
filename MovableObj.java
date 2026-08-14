import java.awt.geom.Rectangle2D;
import java.math.*;

public class MovableObj extends Object{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //movable objects variables
    protected float 
                x_vel = 0, y_vel = 0,
            x_accel = 0, y_accel = GameRules.gravity;
    protected float elastic_factor;
    public static final float MIN_VELOCITY = 0.01F;

    
    MovableObj(float x_pos, float y_pos, float width, float height, boolean active, int obj_type, int obj_id, float elastic_factor){
        super(x_pos, y_pos, width, height, true, active, obj_type, obj_id);

        createHitBox();
        this.elastic_factor = elastic_factor; 

        this.global_quantity++;
        if(active) this.global_active++;
    }

    public void createHitBox(){
        hit_box = new Rectangle2D.Float( x_pos, y_pos, dimensions.width, dimensions.height);
    }

    public void updateVelocity(float new_x_vel, float new_y_vel){
        this.x_vel = new_x_vel;
        this.y_vel = new_y_vel;
    }

    public void updateAcceleration(float new_x_accel, float new_y_accel){
        this.x_accel = new_x_accel;
        this.y_accel = new_y_accel;
    }

    public void changeElasticFactor(float new_elastic_factor){
        this.elastic_factor = new_elastic_factor;   
    }

    public void bounceX(){
        x_vel = -x_vel*elastic_factor;
    }

    public void bounceY(){
        y_vel = -y_vel*elastic_factor;
    }

    public void bounce(Object outro_obj){
        float target_height = outro_obj.dimensions.height,
              target_width = outro_obj.dimensions.width;
        //terminar a implementação              
    }

    public void update(){
        if(GameRules.physics_on){
            x_vel += x_accel;
            y_vel += y_accel;
            x_pos += x_vel;
            y_pos += y_vel;
            
            if(Math.abs(x_vel) < MIN_VELOCITY) x_vel = 0;
            if(Math.abs(y_vel) < MIN_VELOCITY) y_vel = 0;
        }
    }
}
