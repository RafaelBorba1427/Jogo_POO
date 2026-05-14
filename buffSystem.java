import java.util.*;

public class buffSystem {
    public enum buffs{
        SPEED_BOOST, // implemented
        ELASTIC_COLLISION, // implemented
        MASSIVE_DRAG, // implemented
        ICED, // implemented
        SLIPPERY, // implemented
        INTANGIBLE,
        LAG,
        TIME_TRAVEL
    };

    public boolean speed_boost_active = false;
    public boolean ICED_active = false;
    private boolean any_buff_active = false;
    private EnumMap<buffs,Integer> active_buffs = new EnumMap<>(buffs.class);

    void ApplyBuff(buffs buff_applied, int duration_in_seconds){
        // 90*game_tick ~= 1 segundo
        active_buffs.replace(buff_applied,duration_in_seconds*90);
        any_buff_active = true;
    }

    void DecrementBuffTimers(){
        if(any_buff_active){
            boolean cont=false;
            for (EnumMap.Entry<buffs, Integer> buff : active_buffs.entrySet()) {
                if(buff.getValue() > 0){
                    buff.setValue(buff.getValue()-1);
                    cont=true;
                }
            }
            any_buff_active = cont; // se cont = false -> desativa check de buffs
        }
    }

    void DrecementABuff(buffs buff, int duration_in_ticks){
        active_buffs.replace(buff, active_buffs.get(buff)-(duration_in_ticks));
    }

    boolean HasBuff(buffs buff){
        if(active_buffs.get(buff) > 0) return true;
        else return false;
    }

    buffSystem(){
        for(buffs buff : buffs.values()){
            active_buffs.put(buff,0);
        }
    }
    
    int BuffDuration(buffs buff){
        return active_buffs.get(buff);
    }
    //debug function
    void CheckDuration(buffs buff){
        System.out.printf("%d\n",active_buffs.get(buff));
    }
}
