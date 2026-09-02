import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


public class BallObj extends MovableObj{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    //movable objects variables
    

    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------
    
    BallObj(double x_pos, double y_pos, double radius, double mass, boolean active, int obj_id, double elastic_factor){
        super(x_pos, y_pos, radius, radius, 0, mass, true, active, obj_id, elastic_factor);
        obj_type = GameObject.BALL_OBJ;
    }

    @Override
    public void drawHitbox(Graphics2D g2d){
        if(this.isActive()){
            g2d = (Graphics2D) g2d.create(); // copy of g2d

            // 1. Draw hitbox body at target location
            g2d.setColor(new Color(255,0,0,64));
            g2d.fillOval((int) position.x, (int) position.y, (int) dimensions.x, (int) dimensions.x);

            // 2. Draw hitbox outline
            g2d.setColor(new Color(255,0,0,255));
            g2d.drawOval((int) position.x, (int) position.y, (int) dimensions.x, (int) dimensions.x);
            
            g2d.dispose();
        }
    }

    @Override
    public void createHitBox(){ 
        hit_box = new CircularHitBox( position, dimensions.x);
    }

    @Override
    public void updateHitBox(){
        ((CircularHitBox)hit_box).updateHitBox(position, dimensions.x);
    }

    @Override
    protected void updateInertialVariables(){
        this.inverse_mass = 1.0/mass;
        this.moment_inertia = (mass*dimensions.x*dimensions.x)/2.0;
        this.inverse_moment_inertia = 1.0/this.moment_inertia; 
    }

    @Override
    //Colision Detection
    public boolean collides(GameObject outro_obj){
        if(outro_obj.getObjType() == GameObject.BALL_OBJ){
            return ((CircularHitBox) this.hit_box).intersects((CircularHitBox) outro_obj.getHitBox()); 
        }
        return ((CircularHitBox) this.hit_box).intersects((RectangularHitBox) outro_obj.getHitBox());
    }

    @Override
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

    // ------------------------------------------------------------
    // Ball Obj Exclusive methods
    // ------------------------------------------------------------
    public double getRadius(){
        return dimensions.x;
    }

}
