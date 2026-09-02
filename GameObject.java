import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class GameObject {

    // ------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------

    //object variables
    protected Vector2D position;
    protected Vector2D dimensions;
    protected double rotation; // in radians
    protected double mass; 
    protected double moment_inertia; 
    protected double inverse_mass;
    protected double inverse_moment_inertia;
    protected HitBox hit_box; 

    //object parameters            
    protected boolean movable;
    protected boolean rotatable;
    protected boolean active;
    
    //sprite reference
    protected BufferedImage sprite;
    
    //object type codes
    protected int obj_type;
    public static final int   
            RIGID_OBJ = 0,
          MOVABLE_OBJ = 1,
             BALL_OBJ = 2,
             BUFF_OBJ = 3,
    EVENT_TRIGGER_OBJ = 4;
    
    
    //object IDs
    protected int obj_id;
    public static final int
   ID_PLATAFORMA_CONGELADA = 0,
             ID_PLATAFORMA = 1,
                   ID_MESA = 2,
                 ID_PAREDE = 3,
                  ID_BALDE = 4,
             ID_ESTILINGUE = 5,
              ID_BUFF_ICED = 6,
       ID_BUFF_SPEED_BOOST = 7,
        ID_BUFF_INTANGIBLE = 8,
       ID_BUFF_TIME_TRAVEL = 9,
              ID_BUFF_LAG = 10,
ID_BUFF_ELASTIC_COLLISION = 11,
                Quant_IDs = 12;
    

    // ------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------

    GameObject(double x_pos, double y_pos, double width, double height, double rotation, double mass, boolean movable, boolean rotatable, boolean active, int obj_type, int obj_id){
        this.position = new Vector2D(x_pos, y_pos);
        this.dimensions = new Vector2D(width, height);
        this.rotation = rotation;
        this.mass = mass;
        this.updateInertialVariables();
        this.movable = movable;
        this.rotatable = rotatable;
        this.active = active;
        this.obj_type = obj_type;
        this.obj_id = obj_id;

        createHitBox();
    }

    public void drawHitbox(Graphics2D g2d){
        //g2.drawImage(sprite, (int) position.x, (int) position.y, (int) dimensions.x, (int) dimensions.y, null);
            g2d = (Graphics2D) g2d.create(); // copy of g2d

            // 1. Compute the center of the image
            Vector2D center = new Vector2D(position.x + dimensions.x/2, position.y + dimensions.y/2);

            g2d.translate((int) center.x, (int) center.y);

            // 2. Apply the rotation
            g2d.rotate(rotation);

            // 3. Draw hitbox body at target location
            g2d.setColor(new Color(255,0,0,64));
            g2d.fillRect((int) (-dimensions.x/2), (int) (-dimensions.y/2), (int) dimensions.x, (int) dimensions.y);

            // 4. Draw hitbox outline
            g2d.setColor(new Color(255,0,0,255));
            g2d.drawRect((int) (-dimensions.x/2), (int) (-dimensions.y/2), (int) dimensions.x, (int) dimensions.y);
            
            g2d.dispose();
    }

    //Getter methods
    public double getX(){
        return this.position.x;}
        
    public double getY(){
        return this.position.y;}

    public Vector2D getPosition(){
        return new Vector2D(this.position);
    } 

    public Vector2D getDimension(){
        return new Vector2D(this.dimensions);}
    
    public double getRotation(){
        return this.rotation;
    }

    public double getMass(){
        return this.mass;
    }

    public double getMomentOfInertia(){
        return this.moment_inertia;
    }

    public Vector2D getInverseInertialVariables(){
        return new Vector2D(inverse_mass,inverse_moment_inertia);
    }

    public HitBox getHitBox(){
        return this.hit_box;}

    public int getObjType(){
        return this.obj_type;}

    public int getObjId(){
        return this.obj_id;}

    public BufferedImage getSprite(){
        return this.sprite;}

    public boolean isActive(){
        return active;}

    public boolean isMovable(){
        return movable;}
    

    //Setter methods
    public void move(int new_x_pos, int new_y_pox){
        this.position.x = new_x_pos;
        this.position.y = new_y_pox;
    }

    public void move(Vector2D new_pos){
        this.position.x = new_pos.x;
        this.position.y = new_pos.y;
    }

    public void changeRotation(double rotation){
        if(this.rotatable){
            this.rotation = rotation;
            updateHitBox();    
        }
    }

    public void rotate(double rotation){
        if(this.rotatable){
            this.rotation =
            Math.IEEEremainder(this.rotation + rotation, 2.0 * Math.PI);
            updateHitBox();
        }
    }

    public void changeMass(double mass){
        this.mass = mass;
        updateInertialVariables();
    }

    protected void updateInertialVariables(){ 
    }

    public void createHitBox(){
        hit_box = new RectangularHitBox(this.position, this.dimensions, this.rotation);
    }

    public void updateHitBox(){
        ((RectangularHitBox)hit_box).updateHitBox(position, dimensions, rotation);
    }

    public void changeDimensions(double new_width, double new_height){
        this.dimensions.setSize(new_width, new_height);
        updateHitBox();
    }

    public void changeSprite(BufferedImage new_sprite){
        this.sprite = new_sprite;
    }

    //Colision Detection
    public boolean collides(GameObject outro_obj){
        if(outro_obj.getObjType() == GameObject.BALL_OBJ){
            return ((RectangularHitBox) this.hit_box).intersects((CircularHitBox) outro_obj.getHitBox()); 
        }
        return ((RectangularHitBox) this.hit_box).intersects((RectangularHitBox) outro_obj.getHitBox());
    }

    public void deactivate(GameObject object){
        object.active = false;
    }
}
