import java.awt.geom.Rectangle2D;

public class RigidObj extends Object{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //Rigid objects variables
    Rectangle2D hit_box;
    
    RigidObj(float x_pos, float y_pos, float width, float height, boolean active, int obj_type, int obj_id){
        super(x_pos, y_pos, width, height, false, active, obj_type, obj_id);
        hit_box = new Rectangle2D.Float(x_pos,y_pos,width,height);
        global_quantity++;
        if(active) global_active++;
    }
}
