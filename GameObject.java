import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class GameObject {

    // ------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------

    // Collision ID
    // Obs:
    // Identificador unico por instancia. Serve para dar uma ordem estavel aos
    // pares de colisao (a normal do manifold sempre aponta do menor uid para o
    // maior) e para indexar o cache de warm starting do GameMap.
    // O obj_id nao serve para isso: ele identifica o TIPO do objeto.
    private static int next_uid = 0;
    protected final int uid = next_uid++;

    // Object variables
    // Obs:
    // position e o CANTO SUPERIOR ESQUERDO do objeto sem rotacao.
    // O centro (de massa, e em torno do qual o objeto gira) e
    // position + dimensions / 2 -> getCenterOfMass().
    protected Vector2D position;
    protected Vector2D dimensions;
    protected double rotation; // in radians
    protected double mass;
    protected double moment_inertia;
    protected double inverse_mass;
    protected double inverse_moment_inertia;
    protected HitBox hit_box;

    // Object Friction
    protected double friction = 0.3;

    // object parameters
    protected boolean movable;
    protected boolean rotatable;
    protected boolean active;

    // sprite reference
    protected AnimationPlayer animation;

    // object type codes
    protected int obj_type;
    public static final int RIGID_OBJ = 0,
            MOVABLE_OBJ = 1,
            BALL_OBJ = 2,
            BUFF_OBJ = 3,
            EVENT_TRIGGER_OBJ = 4,
            PLAYER = 5;

    // object IDs
    protected int obj_id;

    public static final int ID_INVISIBLE_OBJ = -1,
            ID_FROZEN_PLATFORM = 0,
            ID_PLATFORM = 1,
            ID_TABLE = 2,
            ID_WALL = 3,
            ID_BUCKET = 4,
            ID_SLINGSHOT = 5,
            ID_BUFF_ICED = 6,
            ID_BUFF_SPEED_BOOST = 7,
            ID_BUFF_INTANGIBLE = 8,
            ID_BUFF_TIME_TRAVEL = 9,
            ID_BUFF_LAG = 10,
            ID_BUFF_ELASTIC_COLLISION = 11,
            ID_BALL_1 = 14,
            ID_BALL_2 = 15,
            ID_BALL_3 = 16,
            ID_BALL_4 = 17,
            ID_PERMANENT_FLOOR = 16,
            ID_PERMANENT_WALL = 17,
            Quant_IDs = 19;

    // ------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------

    GameObject(double x_pos, double y_pos, double width, double height, double rotation, double mass, double friction,
            boolean movable, boolean rotatable, boolean active, int obj_type, int obj_id) {
        this.position = new Vector2D(x_pos, y_pos);
        this.dimensions = new Vector2D(width, height);
        this.rotation = rotation;
        this.mass = mass;
        this.friction = friction;
        this.updateInertialVariables();
        this.movable = movable;
        this.rotatable = rotatable;
        this.active = active;
        this.obj_type = obj_type;
        this.obj_id = obj_id;

        createHitBox();
        createAnimationPlayer();
    }

    // ------------------------------------------------------------
    // Render
    // ------------------------------------------------------------
    public void drawHitbox(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create(); // copy of g2d

        // 1. Compute the center of the image
        Vector2D center = getCenterOfMass();

        g2d.translate((int) center.x, (int) center.y);

        // 2. Apply the rotation
        g2d.rotate(rotation);

        // 3. Draw hitbox body at target location
        g2d.setColor(new Color(255, 0, 0, 64));
        g2d.fillRect((int) (-dimensions.x / 2), (int) (-dimensions.y / 2), (int) dimensions.x, (int) dimensions.y);

        // 4. Draw hitbox outline
        switch (this.obj_type) {
            case EVENT_TRIGGER_OBJ:
                g2d.setColor(new Color(255, 251, 0, 255));
                break;

            case BUFF_OBJ:
                g2d.setColor(new Color(0, 255, 0, 255));
                break;

            case MOVABLE_OBJ:
                g2d.setColor(new Color(0, 200, 255, 255));
                break;

            default:
                g2d.setColor(new Color(255, 0, 0, 255));
                break;
        }
        g2d.drawRect((int) (-dimensions.x / 2), (int) (-dimensions.y / 2), (int) dimensions.x, (int) dimensions.y);

        g2d.dispose();
    }

    protected void createAnimationPlayer() {
        try {
            if (obj_id == ID_INVISIBLE_OBJ) {
                animation = null;
                return;
            }

            if (obj_id == ID_PERMANENT_FLOOR)
                this.animation = new AnimationPlayer("permanent_floor", "spritesheet/floor_plank_760x15.png", 760, 15,
                        0, 1, 1);

            else if (obj_id == ID_PERMANENT_WALL)
                this.animation = new AnimationPlayer("permanent_wall", "spritesheet/wall_plank_20x600.png", 20, 600, 0,
                        1, 1);

            else
                this.animation = new AnimationPlayer("objects1_" + obj_id, "spritesheet/combined_spritesheet.png", 16,
                        16, obj_id,
                        15, 15);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void drawSprite(Graphics2D g2d) {// int position_x, int position_y, Vector2D dimensions, double rotation
        animation.paint(g2d, (int) position.x, (int) position.y, dimensions, rotation);
    }

    public void drawSprite(Graphics2D g2d, double position_x, double position_y) {// int position_x, int position_y,
                                                                                  // Vector2D dimensions, double
                                                                                  // rotation
        animation.paint(g2d, (int) position.x, (int) position.y, dimensions, rotation);
    }

    // ------------------------------------------------------------
    // Getter Methods
    // ------------------------------------------------------------
    public int getUid() {
        return this.uid;
    }

    public double getX() {
        return this.position.x;
    }

    public double getY() {
        return this.position.y;
    }

    public Vector2D getPosition() {
        return new Vector2D(this.position);
    }

    // Center of mass Coordinates
    public Vector2D getCenterOfMass() {
        return new Vector2D(position.x + dimensions.x / 2.0,
                position.y + dimensions.y / 2.0);
    }

    public Vector2D getDimension() {
        return new Vector2D(this.dimensions);
    }

    public double getRotation() {
        return this.rotation;
    }

    public double getMass() {
        return this.mass;
    }

    public double getMomentOfInertia() {
        return this.moment_inertia;
    }

    public double getInverseMass() {
        return this.inverse_mass;
    }

    public double getInverseMomentOfInertia() {
        return this.inverse_moment_inertia;
    }

    public Vector2D getInverseInertialVariables() {
        return new Vector2D(inverse_mass, inverse_moment_inertia);
    }

    public HitBox getHitBox() {
        return this.hit_box;
    }

    public int getObjType() {
        return this.obj_type;
    }

    public int getObjId() {
        return this.obj_id;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isMovable() {
        return movable;
    }

    // --------------------------------------------------------
    // Methods used in the Collision Solver

    // Center of mass Linear velocity, zero for static bodies.
    public Vector2D getLinearVelocity() {
        return new Vector2D(0, 0);
    }

    // zero for static bodies.
    public double getAngularVelocity() {
        return 0.0;
    }

    // static body -> zero effect
    public void applyImpulse(Vector2D impulse, Vector2D contact_arm) {
    }

    // static body -> zero effect
    public void translate(Vector2D delta) {
    }

    // Restitution Coeficient (collision after effect).
    // Obs:
    // Corpos rigidos nao devolvem energia por conta propria; o par usa o maior dos
    // dois valores.
    public double getRestitution() {
        return 0.0;
    }

    public double getFriction() {
        return this.friction;
    }

    public void changeFriction(double new_friction) {
        this.friction = Math.max(0.0, new_friction);
    }

    // ------------------------------------------------------------
    // Setter methods
    // ------------------------------------------------------------

    public void setPlayer() {
        obj_type = PLAYER;
    }

    public void move(int new_x_pos, int new_y_pox) {
        this.position.x = new_x_pos;
        this.position.y = new_y_pox;
        updateHitBox();
    }

    public void move(Vector2D new_pos) {
        this.position.x = new_pos.x;
        this.position.y = new_pos.y;
        updateHitBox();
    }

    public void changeCenterOfMass(Vector2D new_pos) {
        this.position.x = new_pos.x - dimensions.x / 2.0;
        this.position.y = new_pos.y - dimensions.y / 2.0;
        updateHitBox();
    }

    public void changeRotation(double rotation) {
        if (this.rotatable) {
            this.rotation = rotation;
            updateHitBox();
        }
    }

    public void rotate(double rotation) {
        if (this.rotatable) {
            this.rotation = Math.IEEEremainder(this.rotation + rotation, 2.0 * Math.PI);
            updateHitBox();
        }
    }

    public void changeMass(double mass) {
        this.mass = mass;
        updateInertialVariables();
    }

    protected void updateInertialVariables() {
    }

    public void createHitBox() {
        hit_box = new RectangularHitBox(this.position, this.dimensions, this.rotation);
    }

    public void updateHitBox() {
        ((RectangularHitBox) hit_box).updateHitBox(position, dimensions, rotation);
    }

    public void changeDimensions(double new_width, double new_height) {
        this.dimensions.setSize(new_width, new_height);
        updateInertialVariables();
        updateHitBox();
    }

    // ------------------------------------------------------------
    // Colision Detection
    // ------------------------------------------------------------
    public boolean collides(GameObject outro_obj) {
        if (outro_obj == null || outro_obj == this)
            return false;

        HitBox other_hit_box = outro_obj.getHitBox();
        if (this.hit_box == null || other_hit_box == null)
            return false;

        return this.hit_box.intersects(other_hit_box);
    }

    public void deactivate(GameObject object) {
        object.active = false;
    }
}
