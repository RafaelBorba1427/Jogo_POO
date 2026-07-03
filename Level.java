import java.util.ArrayList;
import java.util.Queue;

public class Level{
    private int current_hp;
    private int current_max_hp;
    private ArrayList<coisa> objects_buffs;
    private Queue<Long> points_till_next_stage;
    private static String current_background_image_path = "Frat_background.png";  
    private int stages_till_next_background;
    private static boolean[] blocked_buffs = new boolean[coisa.Quant_IDs];

    static {
        for (int i = 0; i < blocked_buffs.length; i++) {
            blocked_buffs[i] = false;
        }
        // bloquear todos os buffs inicialmente
        for (int i = 6; i < blocked_buffs.length; i++) {
            blocked_buffs[i] = true;
        }
    }

    Level(){
        stages_till_next_background = 6;
        updateCurrentHP();
        updateCurrentMaxHP();
    }

    void updateCurrentHP(){
         current_hp = game.healthSys.getCurrentHp();
    }

    void updateCurrentMaxHP(){
         current_max_hp = game.healthSys.getMaxHp();
    }

    int getCurrentHp(){
        return current_hp;
    }

    int getCurrentMaxHp(){
        return current_max_hp;
    }

    ArrayList<coisa> getLevelObjectsBuffs(){
        return objects_buffs;
    }

    void addPointsNextStageQueue(Long points){
        points_till_next_stage.add(points);
    }

    void removePointsNextStageQueue(){
        points_till_next_stage.remove();
    }

    Long getPointsNextStageQueue(){
        return points_till_next_stage.peek();
    }

    void changeCurrentBackgroundImage(String path){
        current_background_image_path = path;
    }

    void changeNumStagesTillNextBackground(int num){
        stages_till_next_background = num;
    }

    boolean isBuffBlocked(int id){
        return blocked_buffs[id];
    }

    void unblockBuff(int id){
        blocked_buffs[id] = false; 
    }

    void blockBuff(int id){
        blocked_buffs[id] = true; 
    }

    
}