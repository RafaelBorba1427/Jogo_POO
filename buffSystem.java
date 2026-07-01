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
    private static HashMap<Integer, buffs> id_to_buffs;
    public static EnumMap<buffs, Integer> standard_buff_duration;

    public void ApplyBuff(buffs buff_applied, int duration_in_seconds) {
        // 90*game_tick ~= 1 segundo
        active_buffs.replace(buff_applied, duration_in_seconds * 90);
        any_buff_active = true;

        if (buff_applied == buffs.ICED)
            ICED_active = false;
        else if (buff_applied == buffs.SPEED_BOOST)
            speed_boost_active = false;
        else if (buff_applied == buffs.LAG)
            LAG_active = false;
        else if (buff_applied == buffs.TIME_TRAVEL)
            TIME_TRAVEL_active = false;
    }

    static buffs returnBuff(int choice) {
        System.out.print(choice + "is choice");
        return id_to_buffs.get(choice);
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

    void EndBuffs() {
        for (EnumMap.Entry<buffs, Integer> buff : active_buffs.entrySet()) {
            buff.setValue(0);
        }
    }

    void EndBuff(buffs buff) {
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
        id_to_buffs = new HashMap<>(buffs.values().length);
        id_to_buffs.put(coisa.ID_BUFF_ICED, buffs.ICED);
        id_to_buffs.put(coisa.ID_BUFF_SPEED_BOOST, buffs.SPEED_BOOST);
        id_to_buffs.put(coisa.ID_BUFF_INTANGIBLE, buffs.INTANGIBLE);
        id_to_buffs.put(coisa.ID_BUFF_TIME_TRAVEL, buffs.TIME_TRAVEL);
        id_to_buffs.put(coisa.ID_BUFF_LAG, buffs.LAG);
        id_to_buffs.put(coisa.ID_BUFF_ELASTIC_COLLISION, buffs.ELASTIC_COLLISION);

        standard_buff_duration = new EnumMap<>(buffs.class);
        standard_buff_duration.put(buffs.ICED, 3);
        standard_buff_duration.put(buffs.SPEED_BOOST, 3);
        standard_buff_duration.put(buffs.INTANGIBLE, 2);
        standard_buff_duration.put(buffs.TIME_TRAVEL, 5);
        standard_buff_duration.put(buffs.LAG, 4);
        standard_buff_duration.put(buffs.ELASTIC_COLLISION, 8);
    }

    int BuffDuration(buffs buff) {
        return active_buffs.get(buff);
    }

    // debug function
    void CheckDuration(buffs buff) {
        System.out.printf("%d\n", active_buffs.get(buff));

    }
}
