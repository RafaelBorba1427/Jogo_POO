import java.awt.geom.Rectangle2D;

public class RigidObj extends GameObject{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //Rigid objects variables
    

    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------
    
    RigidObj(double x_pos, double y_pos, double width, double height, double rotation, boolean rotatable, boolean active, int obj_type, int obj_id){
        super(x_pos, y_pos, width, height, rotation, false, rotatable, active, GameObject.RIGID_OBJ, obj_id);

        hit_box = new RectangularHitBox(position, dimensions, rotation);

        global_quantity++;
        if(active) global_active++;
    }
}
