import java.util.HashMap;

public class pointSystem {
    private Long points, potential_points;
    private HashMap<Integer,Long> id_to_points;

    public pointSystem() {
        this.points = 0L;
        this.potential_points = 0L;
        id_to_points = new HashMap<>(11);
        id_to_points.put(coisa.ID_PLATAFORMA_CONGELADA,200L);
        id_to_points.put(coisa.ID_PLATAFORMA,100L);
        id_to_points.put(coisa.ID_MESA,150L);
        id_to_points.put(coisa.ID_BALDE,1000L);
        id_to_points.put(coisa.ID_BUFF_ICED,300L);
        id_to_points.put(coisa.ID_BUFF_SPEED_BOOST,50L);
        id_to_points.put(coisa.ID_BUFF_INTANGIBLE,-500L);
        id_to_points.put(coisa.ID_BUFF_TIME_TRAVEL,150L);
        id_to_points.put(coisa.ID_BUFF_LAG,-150L);
        id_to_points.put(coisa.ID_BUFF_ELASTIC_COLLISION,15L);
    }

    public void addPotentialPoints(coisa obj) {
        potential_points += id_to_points.get(obj.id);
    }

    public void processPoints(){
        points += potential_points;
    }

    public Long getPoints() {
        return points;
    }
}