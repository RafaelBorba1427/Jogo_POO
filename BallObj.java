import java.awt.Color;
import java.awt.Graphics2D;

// ------------------------------------------------------------
// BallObj
//
// Corpo movel de hitbox circular.
//
// Convencao de tamanho: dimensions.x guarda o DIAMETRO. O desenho ja usava
// dimensions.x como diametro (fillOval recebe largura, nao raio), mas a
// hitbox e o momento de inercia tratavam o mesmo numero como raio. O circulo
// fisico ficava com o dobro do tamanho do circulo desenhado e centrado no
// canto superior esquerdo dele. Agora tudo usa a mesma convencao.
// ------------------------------------------------------------
public class BallObj extends MovableObj{
    //global quantifiers
    private static int global_quantity = 0;
    private static int global_active = 0;

    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------

    // diameter: largura total da bola, igual a que aparece na tela.
    BallObj(double x_pos, double y_pos, double diameter, double mass, boolean active, int obj_id, double elastic_factor){
        super(x_pos, y_pos, diameter, diameter, 0, mass, true, active, obj_id, elastic_factor);
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
        hit_box = new CircularHitBox(position, dimensions.x);
    }

    @Override
    public void updateHitBox(){
        ((CircularHitBox)hit_box).updateHitBox(position, dimensions.x);
    }

    // Momento de inercia de um disco solido: I = m * r^2 / 2.
    @Override
    protected void updateInertialVariables(){
        double radius = dimensions.x / 2.0;
        this.inverse_mass = 1.0/mass;
        this.moment_inertia = (mass * radius * radius)/2.0;
        this.inverse_moment_inertia = 1.0/this.moment_inertia;
    }

    // A deteccao de colisao nao e mais sobrescrita: GameObject.collides()
    // pergunta a propria hitbox, que faz o despacho duplo. A versao antiga
    // olhava o obj_type do outro objeto para decidir o cast, o que dava
    // ClassCastException com qualquer outro tipo de hitbox circular.

    // ------------------------------------------------------------
    // Ball Obj Exclusive methods
    // ------------------------------------------------------------
    public double getRadius(){
        return dimensions.x / 2.0;
    }

    public double getDiameter(){
        return dimensions.x;
    }
}
