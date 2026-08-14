import java.awt.geom.Rectangle2D;

public class MovableObj extends Object{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //movable objects variables
    public Rectangle2D hit_box;
    public float 
                x_vel = 0, y_vel = 0,
            x_accel = 0, y_accel = 0;

    
    MovableObj(float x_pos, float y_pos, float width, float height, boolean active, int obj_type, int obj_id){
        super(x_pos, y_pos, width, height, false, active, obj_type, obj_id);
        hit_box = new Rectangle2D.Float(x_pos,y_pos,width,height);
        global_quantity++;
        if(active) global_active++;
    }

    public void updateVelocity(float new_x_vel, float new_y_vel){
        this.x_vel = new_x_vel;
        this.y_vel = new_y_vel;
    }

    public void updateAcceleration(float new_x_accel, float new_y_accel){
        this.x_accel = new_x_accel;
        this.y_accel = new_y_accel;
    }

}
