import java.awt.geom.Rectangle2D;

public class EventTriggerObj extends GameObject{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //Event Trigger objects variables
  
    
    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------
    
    EventTriggerObj(double x_pos, double y_pos, double width, double height, double rotation, boolean rotatable, boolean active, int obj_type, int obj_id){
        super(x_pos, y_pos, width, height, rotation,0,0, false, rotatable, active, GameObject.EVENT_TRIGGER_OBJ, obj_id);

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
