import java.awt.geom.Rectangle2D;

public class BuffObj extends GameObject{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //buff objects variables

    
    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------
    BuffObj(double x_pos, double y_pos, double width, double height, double rotation, boolean rotatable, boolean active, int obj_type, int obj_id){
        super(x_pos, y_pos, width, height, rotation, 0,0, true, rotatable, active, GameObject.BUFF_OBJ, obj_id);

        global_quantity++;
        if(active) global_active++;
    }
    
    @Override
    public void changeMass(double mass){
    }

    @Override
    protected void updateInertialVariables(){
        this.inverse_mass = Double.POSITIVE_INFINITY;
        this.moment_inertia = 0;
        this.inverse_moment_inertia = Double.POSITIVE_INFINITY;
    }

}
