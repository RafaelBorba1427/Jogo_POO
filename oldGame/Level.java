import java.util.ArrayList;
import java.util.Queue;

public class Level {
    private int current_hp;
    private int current_max_hp;
    private ArrayList<coisa> objects_buffs = new ArrayList<>();
    private Queue<Long> points_till_next_stage;
    private static String current_background_image_path = "Frat_background.png";
    private static boolean[] blocked_buffs = new boolean[coisa.Quant_IDs];
    public int number = 0, count = 0;
    public int level_rec = 0;
    static {
        for (int i = 0; i < blocked_buffs.length; i++) {
            blocked_buffs[i] = false;
        }
        // bloquear todos os buffs inicialmente
        for (int i = 6; i < blocked_buffs.length; i++) {
            blocked_buffs[i] = true;
        }
    }

    Level() {
        level_rec = 3;
        updateCurrentHP();
        updateCurrentMaxHP();
        recChange();
    }

    void recChange() {
        level_rec = (int) Math.pow(count + 2, 2);
        count++;
        number = 0;
    }

    void updateCurrentHP() {
        current_hp = game.healthSys.getCurrentHp();
    }

    void updateCurrentMaxHP() {
        current_max_hp = game.healthSys.getMaxHp();
    }

    int getCurrentHp() {
        return current_hp;
    }

    int getCurrentMaxHp() {
        return current_max_hp;
    }

    ArrayList<coisa> getLevelObjectsBuffs() {
        return objects_buffs;
    }

    void addPointsNextStageQueue(Long points) {
        points_till_next_stage.add(points);
    }

    void removePointsNextStageQueue() {
        points_till_next_stage.remove();
    }

    Long getPointsNextStageQueue() {
        return points_till_next_stage.peek();
    }

    void changeCurrentBackgroundImage(String path) {
        current_background_image_path = path;
    }

    boolean isBuffBlocked(int id) {
        return blocked_buffs[id];
    }

    void unblockBuff(int id) {
        blocked_buffs[id] = false;
    }

    void blockBuff(int id) {
        blocked_buffs[id] = true;
    }

    void addListCoisa(ArrayList<coisa> objects_buffs) {
        this.objects_buffs = objects_buffs;
    }

}
