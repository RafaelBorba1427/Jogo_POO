// ------------------------------------------------------------
// RigidObj
//
//obs:
// Corpo estatico: massa e momento de inercia infinitos, portanto inversos
// zerados. O solver aplica impulsos nele normalmente, mas como os inversos
// sao zero e applyImpulse/translate nao sao sobrescritos (herdam as versoes
// neutras de GameObject), nada acontece: ele simplesmente nao cede.
// ------------------------------------------------------------
public class RigidObj extends GameObject{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------

    RigidObj(double x_pos, double y_pos, double width, double height, double rotation, boolean rotatable, boolean active, int obj_id){
        super(x_pos, y_pos, width, height, rotation, Double.POSITIVE_INFINITY, false, rotatable, active, GameObject.RIGID_OBJ, obj_id);

        global_quantity++;
        if(active) global_active++;
    }

    @Override
    public void changeMass(double mass){
        // A massa de um corpo estatico e sempre infinita.
    }

    @Override
    protected void updateInertialVariables(){
        this.inverse_mass = 0;
        this.moment_inertia = Double.POSITIVE_INFINITY;
        this.inverse_moment_inertia = 0;
    }
}
