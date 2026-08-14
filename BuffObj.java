import java.awt.geom.Rectangle2D;

public class BuffObj extends Object{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //buff objects variables

    
    BuffObj(float x_pos, float y_pos, float width, float height, boolean movable, boolean active, int obj_type, int obj_id){
        super(x_pos, y_pos, width, height, false, active, obj_type, obj_id);
        
        hit_box = new Rectangle2D.Float(x_pos,y_pos,width,height);

        this.global_quantity++;
        if(active) this.global_active++;
    }

}
