import java.math.*;

public class MovableObj extends GameObject{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //movable objects variables
    protected Vector2D 
              velocity = new Vector2D(0,0),
              acceleration = new Vector2D(0, GameRules.gravity);

    protected double elastic_factor;
    public static final double MIN_VELOCITY = 0.01F;


    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------

    MovableObj(double x_pos, double y_pos, double width, double height, double rotation, boolean rotatable, boolean active, int obj_id, double elastic_factor){
        super(x_pos, y_pos, width, height, rotation, true, rotatable, active, GameObject.MOVABLE_OBJ, obj_id);

        this.elastic_factor = elastic_factor; 

        global_quantity++;
        if(active) global_active++;
    }

    // ------------------------------------------------------------
    // MovableObj exclusive methods
    // ------------------------------------------------------------

    public void updateVelocity(double new_x_vel, double new_y_vel){
        this.velocity.x = new_x_vel;
        this.velocity.y = new_y_vel;
    }

    public void updateAcceleration(double new_x_accel, double new_y_accel){
        this.acceleration.x = new_x_accel;
        this.acceleration.y = new_y_accel;
    }

    public void changeElasticFactor(double new_elastic_factor){
        this.elastic_factor = new_elastic_factor;   
    }

    public void bounceX(){
        velocity.x = -velocity.x*elastic_factor;
    }

    public void bounceY(){
        velocity.y = -velocity.y*elastic_factor;
    }

    public void bounce(GameObject outro_obj){
        double target_height = outro_obj.dimensions.x,
              target_width = outro_obj.dimensions.y;
        
        //---------------------------------------------------------------------
        // Implementação temporária, o sistema correto será implementado depois
        bounceX();
        bounceY();
        //---------------------------------------------------------------------
    }

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
}
