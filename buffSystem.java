import java.util.*;

public class buffSystem {
    public enum buffs {
        SPEED_BOOST, // implemented
        ELASTIC_COLLISION, // implemented
        MASSIVE_DRAG, // implemented
        ICED, // implemented
        SLIPPERY, // implemented
        INTANGIBLE, // implemented
        LAG, // implemented
        TIME_TRAVEL // implemented
    };

    public boolean speed_boost_active = false;
    public boolean ICED_active = false;
    public boolean LAG_active = false;
    public boolean TIME_TRAVEL_active = false;
    public boolean any_buff_active = false;
    private EnumMap<buffs, Integer> active_buffs = new EnumMap<>(buffs.class);

    public void ApplyBuff(buffs buff_applied, int duration_in_seconds) {
        // 90*game_tick ~= 1 segundo
        active_buffs.replace(buff_applied, duration_in_seconds * 90);
        any_buff_active = true;
        if(buff_applied == buffs.ICED) ICED_active = false;
        else if(buff_applied == buffs.SPEED_BOOST) speed_boost_active = false;
        else if(buff_applied == buffs.LAG) LAG_active = false;
        else if(buff_applied == buffs.TIME_TRAVEL) TIME_TRAVEL_active = false;
    }

    static buffs returnBuff(int choice) {
        switch (choice) {
            case 5:
                return buffs.ICED;

            case 6:
                return buffs.SPEED_BOOST;
        }
        return buffs.SPEED_BOOST;
    }

    void DecrementBuffTimers() {
        if (any_buff_active) {
            boolean cont = false;
            for (EnumMap.Entry<buffs, Integer> buff : active_buffs.entrySet()) {
                if (buff.getValue() > 0) {
                    buff.setValue(buff.getValue() - 1);
                    cont = true;
                }
            }
            any_buff_active = cont; // se cont = false -> desativa check de buffs
        }
    }

    void EndBuffs(){
            for (EnumMap.Entry<buffs, Integer> buff : active_buffs.entrySet()) {
                    buff.setValue(0);
            }
    }

    void EndBuff(buffs buff){
        active_buffs.replace(buff, 0);
    }

    void DrecementABuff(buffs buff, int duration_in_ticks) {
        active_buffs.replace(buff, active_buffs.get(buff) - (duration_in_ticks));
    }

    boolean HasBuff(buffs buff) {
        if (active_buffs.get(buff) > 0)
            return true;
        else
            return false;
    }

    buffSystem() {
        for (buffs buff : buffs.values()) {
            active_buffs.put(buff, 0);
        }
    }

    int BuffDuration(buffs buff) {
        return active_buffs.get(buff);
    }

    // debug function
    void CheckDuration(buffs buff) {
        System.out.printf("%d\n", active_buffs.get(buff));

    }
}
