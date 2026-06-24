
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.*;
import java.awt.*;

public class ball extends Ellipse2D.Double {
    static boolean bateuX = false;
    static boolean bateuY = false;
    static final double bounce_factor = 0.7;
    static final double min_X_speed = 1.0;
    static final double min_Y_speed = 5.0;
    static final double friction = 0.99;
    static boolean bateu = false;
    private double x, y, diameter, 
                   paint_x, paint_y,
                   time_travel_x, time_travel_y;
    double x_vel, y_vel;

    boolean is_touching_ground = false;
    boolean enable_physics = true;

    public ball(int x, int y, int diameter) {
        super((double) (x - diameter / 2), (double) (y - diameter / 2), (double) (diameter), (double) diameter);

        this.diameter = diameter;
        this.x = x - diameter / 2;
        paint_x = this.x;
        this.y = y - diameter / 2;
        paint_y = this.y;
        this.x_vel = 0;
        this.y_vel = 0;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void update() {
        if (enable_physics) {
            x += x_vel;
            y += y_vel;
            if(!game.buffSys.LAG_active){
                paint_x = x;
                paint_y = y;
            }
        }

        if (y >= game.y_boundary - diameter) {
            is_touching_ground = true;
        } else {
            is_touching_ground = false;
        }

        if (!is_touching_ground && !(y_vel == 0 && game.hitting) && !game.buffSys.LAG_active) {
            y_vel += game.gravity;
        } else {

            if (!game.buffSys.HasBuff(buffSystem.buffs.SLIPPERY)) {

                x_vel *= friction;
                // We only apply friction when the ball is grounded
                // also friction is only applied if it does not have the SLIPPERY modifier
                // Whenever the ball bounces, it stays grounded for a frame, reducing speed a
                // bit
                // Applying friction all the time makes the ball slow down too much
            } else if (game.buffSys.HasBuff(buffSystem.buffs.MASSIVE_DRAG)) {
                x_vel *= friction * 0.96; // 0.95 velocity multiplier every tick
            }

        }
        
            if (game.buffSys.HasBuff(buffSystem.buffs.ICED)) {
                if (!game.buffSys.ICED_active) {
                    x_vel = (x_vel > 0) ? Math.max(x_vel - 30, 0) : Math.min(x_vel + 30, 0);
                    y_vel = (y_vel > 0) ? Math.max(x_vel - 35, 0) : Math.min(x_vel + 35, 0);
                    game.buffSys.ICED_active = true;
                    game.buffSys.ApplyBuff(buffSystem.buffs.SLIPPERY,
                        game.buffSys.BuffDuration(buffSystem.buffs.ICED));
                    game.buffSys.EndBuff(buffSystem.buffs.ICED);
                }
            }

            else if (game.buffSys.ICED_active) {
                game.buffSys.ICED_active = false;
            }

            else {
                if (game.buffSys.HasBuff(buffSystem.buffs.SPEED_BOOST)) {
                    if (!game.buffSys.speed_boost_active) {
                        x_vel += (x_vel > 0) ? 12 : -12;
                        y_vel += (y_vel > 0) ? 15 : -15;
                        game.buffSys.speed_boost_active = true;
                    }
                    x_vel *= 1.0001;
                    y_vel *= 1.0001;
                } else if (game.buffSys.speed_boost_active) {
                    game.buffSys.speed_boost_active = false;
                }
            }
        
        if(game.buffSys.LAG_active || game.buffSys.HasBuff(buffSystem.buffs.LAG)){
            if(!game.buffSys.HasBuff(buffSystem.buffs.LAG)){
                game.buffSys.LAG_active = false;
            }
            // & 63 = mod 64
            else if((game.buffSys.BuffDuration(buffSystem.buffs.LAG)&31) == 0) game.buffSys.LAG_active = !game.buffSys.LAG_active;
        }

        if(game.buffSys.HasBuff(buffSystem.buffs.TIME_TRAVEL)){
            if(!game.buffSys.TIME_TRAVEL_active){
                time_travel_x = this.x;
                time_travel_y = this.y; 
                game.buffSys.TIME_TRAVEL_active = true;
                }
            }
        
        else if(game.buffSys.TIME_TRAVEL_active){
            this.x = time_travel_x;
            this.y = time_travel_y;
            game.buffSys.TIME_TRAVEL_active = false;
        }

        if (Math.abs(x_vel) < min_X_speed) {
            x_vel = 0;
        }

    }

    public void bounce(coisa coisa) {
        if (!enable_physics || game.buffSys.HasBuff(buffSystem.buffs.INTANGIBLE) || game.buffSys.LAG_active)//
            return;

        if (coisa.y + coisa.diametro / 2 > this.y + diameter / 2 || coisa.y + coisa.diametro / 2 < this.y - diameter / 2
                || coisa.y - coisa.diametro / 2 > this.y + diameter / 2
                || coisa.y - coisa.diametro / 2 < this.y - diameter / 2) {

            bounceY();
            bateuY = true;
        } else if (coisa.x + coisa.diametro / 2 > this.x + diameter / 2
                || coisa.x + coisa.diametro / 2 < this.x - diameter / 2
                || coisa.x - coisa.diametro / 2 > this.x + diameter / 2
                || coisa.x - coisa.diametro / 2 < this.x - diameter / 2) {
            bounceX();
            bateuX = true;
        }

    }

    public void bounceX() {
        if (!enable_physics)
            return;
        if (x <= 0) {
            x = 0;
        } else if (x >= game.x_boundary - diameter) {
            x = game.x_boundary - diameter;
        }

        if(!game.buffSys.LAG_active){
            if (!game.buffSys.HasBuff(buffSystem.buffs.ELASTIC_COLLISION)) {
                x_vel *= bounce_factor;
            } // Apply damping
            x_vel = -x_vel;
        }
    }

    public void bounceY() {
        if (!enable_physics)
            return;

        if (y <= 0) {
            y = 1;
        } else if (y >= game.y_boundary - diameter) {
            y = game.y_boundary - diameter;
        }

        if (Math.abs(y_vel) < min_Y_speed) {
            y_vel = 0;
        }

        if(!game.buffSys.LAG_active){
            if (!game.buffSys.HasBuff(buffSystem.buffs.ELASTIC_COLLISION)) {
                y_vel *= bounce_factor;
            } // Apply damping
            y_vel = -y_vel;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x - diameter / 2;
        this.y = y - diameter / 2;
        setFrame(this.x, this.y, diameter, diameter);
    }

    public void setVelocity(int x, int y) {
        this.x_vel = x;
        this.y_vel = y;
    }
    
    public double getX() {
        return x;
    }

    public double getPaintX() {
        return paint_x;
    }

    public double getY() {
        return y;
    }

    public double getPaintY() {
        return paint_y;
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
