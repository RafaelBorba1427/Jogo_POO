import java.util.HashMap;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class pointSystem extends JFrame {
    private Long points, potential_points;
    private HashMap<Integer, Long> id_to_points;

    public pointSystem() {
        id_to_points = new HashMap<>(coisa.Quant_IDs);
        id_to_points.put(coisa.ID_PLATAFORMA_CONGELADA, 200L);
        id_to_points.put(coisa.ID_PLATAFORMA, 100L);
        id_to_points.put(coisa.ID_MESA, 150L);
        id_to_points.put(coisa.ID_BALDE, 1000L);
        id_to_points.put(coisa.ID_BUFF_ICED, 300L);
        id_to_points.put(coisa.ID_BUFF_SPEED_BOOST, 50L);
        id_to_points.put(coisa.ID_BUFF_INTANGIBLE, -150L);
        id_to_points.put(coisa.ID_BUFF_TIME_TRAVEL, 150L);
        id_to_points.put(coisa.ID_BUFF_LAG, -100L);
        id_to_points.put(coisa.ID_BUFF_ELASTIC_COLLISION, 50L);
        id_to_points.put(coisa.ID_PAREDE, 50L);
        this.points = 0L;
        this.potential_points = 0L;
    }

    public void addPotentialPoints(coisa obj) {
        potential_points += id_to_points.get(obj.id);
        game.point_bonus = id_to_points.get(obj.id);
        game.point_bonus_anime = 90;
    }

    public void processPoints() {
        points += potential_points;
    }

    public void removePotentialPoints() {
        potential_points = 0L;
    }

    public void removeALLPoints() {
        points = 0L;
        potential_points = 0L;
    }

    public Long getPoints() {
        return points;
    }

    public Long getPotentialPoints() {
        return potential_points;
    }
}
