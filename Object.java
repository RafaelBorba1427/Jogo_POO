import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Object {
    //object variables
    private float x_pos, y_pos;
    private DimensionFloat dimensions;
    
    //object parameters            
    private boolean movable;
    private boolean active;
    
    //sprite reference
    private BufferedImage sprite;
    
    //object type codes
    private int obj_type;
    public static final int   
            RIGID_OBJ = 0,
          MOVABLE_OBJ = 1,
             BALL_OBJ = 2,
             BUFF_OBJ = 3,
    EVENT_TRIGGER_OBJ = 4;
    
    //object IDs
    private int obj_id;
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
                    
    Object(float x_pos, float y_pos, float width, float height, boolean movable, boolean active, int obj_type, int obj_id){
        this.x_pos = x_pos;
        this.y_pos = y_pos;
         this.dimensions = new DimensionFloat(width, height);
        this.movable = movable;
        this.active = active;
        this.obj_type = obj_type;
        this.obj_id = obj_id;
    }

    public void draw(Graphics2D g2){
        if(this.isActive())
        g2.drawImage(sprite, (int) x_pos, (int) y_pos, (int) dimensions.width, (int) dimensions.height, null);
    }


    public float getX(){
        return this.x_pos;}
    public float getY(){
        return this.y_pos;}
    public DimensionFloat getDimension(){
        return this.dimensions;}
    public int getObjType(){
        return this.obj_type;}
    public int getObjId(){
        return this.obj_id;}

    public boolean isActive(){
        return active;}
    public boolean isMovable(){
        return movable;}
    
    public BufferedImage getSprite(){
        return this.sprite;}


    public void move(int new_x_pos, int new_y_pox){
        this.x_pos = new_x_pos;
        this.y_pos = new_y_pox;
    }
    public void changeDimensions(float new_width, float new_height){
        this.dimensions.setSize(new_width, new_height);
    }
    public void changeSprite(BufferedImage new_sprite){
        this.sprite = new_sprite;
    }
    

    public void deactivate(Object object){
        object.active = false;
    }
}
