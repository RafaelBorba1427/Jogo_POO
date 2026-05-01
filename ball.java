import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.*;
import java.awt.*;

public class ball extends Ellipse2D.Double {
    static final double bounce_factor = 0.7;
    static final double min_X_speed = 1.0;
    static final double min_Y_speed = 5.0;
    static final double friction = 0.99;
    static boolean bateu = false;
    private double x, y, diameter;

    double x_vel, y_vel;

    boolean is_touching_ground = false;

    public ball(int x, int y, int diameter) {
        super((double) (x - diameter / 2), (double) (y - diameter / 2), (double) (diameter), (double) diameter);

        this.diameter = diameter;
        this.x = x - diameter / 2;
        this.y = y - diameter / 2;
        this.x_vel = 0;
        this.y_vel = 0;
    }

    public void update() {
        x += x_vel;
        y += y_vel;
        setFrame(x, y, diameter, diameter);
        if (y >= game.y_boundary - diameter) {
            is_touching_ground = true;
        } else {
            is_touching_ground = false;
        }

        if (!is_touching_ground) {
            y_vel += game.gravity;
        } else {
            x_vel *= friction;
            // We only apply friction when the ball is grounded
            // Whenever the ball bounces, it stays grounded for a frame, reducing speed a
            // bit
            // Applying friction all the time makes the ball slow down too much
        }

        if (Math.abs(x_vel) < min_X_speed) {
            x_vel = 0;
        }
    }

    public void bounce(coisa coisa) {
        if (coisa.y + coisa.diametro > this.y + diameter / 2 || coisa.y + coisa.diametro < this.y - diameter / 2
                || coisa.y - coisa.diametro > this.y + diameter / 2
                || coisa.y - coisa.diametro < this.y - diameter / 2) {
            bounceY();
        }
        if (coisa.x + coisa.diametro > this.x + diameter / 2 || coisa.x + coisa.diametro < this.x - diameter / 2
                || coisa.x - coisa.diametro > this.x + diameter / 2
                || coisa.x - coisa.diametro < this.x - diameter / 2) {
            bounceX();

        }
        bateu = true;

    }

    public void bounceX() {
        if (x <= 0) {
            x = 0;
        }

        x_vel = -x_vel * bounce_factor; // Apply damping
    }

    public void bounceY() {
        if (y <= 0) {
            y = 0;
        }

        if (Math.abs(y_vel) < min_Y_speed) {
            y_vel = 0;
        }

        y_vel = -y_vel * bounce_factor; // Apply damping
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getXVel() {
        return x_vel;
    }

    public double getYVel() {
        return y_vel;
    }

    public double getDiameter() {
        return diameter;
    }
}
