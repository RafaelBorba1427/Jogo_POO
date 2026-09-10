import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// ------------------------------------------------------------
// BuffSystem
// ------------------------------------------------------------
public class BuffSystem {

    public enum buffs {
        SPEED_BOOST,
        ELASTIC_COLLISION,
        MASSIVE_DRAG,
        ICED,
        SLIPPERY,
        INTANGIBLE,
        LAG,
        TIME_TRAVEL
    }

    // Each game loop runs in 16ms.
    public static final int TICKS_PER_SECOND = 60;

    // ------------------------------------------------------------
    // Buff state timers
    // ------------------------------------------------------------

    private static final EnumMap<buffs, Integer> active_buffs = new EnumMap<>(buffs.class);
    private static final Map<Integer, buffs> id_to_buffs = new HashMap<>();
    public static final EnumMap<buffs, Integer> standard_buff_duration = new EnumMap<>(buffs.class);

    private static boolean speed_boost_active = false;
    private static boolean iced_active = false;
    private static boolean lag_active = false;
    private static boolean time_travel_active = false;
    private static boolean any_buff_active = false;

    static {
        for (buffs buff : buffs.values()) {
            active_buffs.put(buff, 0);
        }

        id_to_buffs.put(GameObject.ID_BUFF_ICED, buffs.ICED);
        id_to_buffs.put(GameObject.ID_BUFF_SPEED_BOOST, buffs.SPEED_BOOST);
        id_to_buffs.put(GameObject.ID_BUFF_INTANGIBLE, buffs.INTANGIBLE);
        id_to_buffs.put(GameObject.ID_BUFF_TIME_TRAVEL, buffs.TIME_TRAVEL);
        id_to_buffs.put(GameObject.ID_BUFF_LAG, buffs.LAG);
        id_to_buffs.put(GameObject.ID_BUFF_ELASTIC_COLLISION, buffs.ELASTIC_COLLISION);

        standard_buff_duration.put(buffs.ICED, 3);
        standard_buff_duration.put(buffs.SPEED_BOOST, 3);
        standard_buff_duration.put(buffs.INTANGIBLE, 2);
        standard_buff_duration.put(buffs.TIME_TRAVEL, 5);
        standard_buff_duration.put(buffs.LAG, 4);
        standard_buff_duration.put(buffs.ELASTIC_COLLISION, 8);
        standard_buff_duration.put(buffs.MASSIVE_DRAG, 5);
        standard_buff_duration.put(buffs.SLIPPERY, 3);
    }

    // ------------------------------------------------------------
    // Estado dos efeitos aplicados a bola
    // ------------------------------------------------------------

    //Default game values for reference
    private static boolean base_values_captured = false;
    private static double base_friction = GameRules.DEFAULT_FRICTION;
    private static double base_restitution = 0.8;

    // TIME_TRAVEL
    private static Vector2D time_travel_position = null;

    // LAG
    private static boolean lag_freeze_engaged = false;
    private static Vector2D lag_frozen_velocity = new Vector2D(0, 0);
    private static int lag_toggle_counter = 0;
    private static final int LAG_TOGGLE_PERIOD = 32;

    private static final double ICED_STOP_X = 30.0;
    private static final double ICED_STOP_Y = 35.0;
    private static final double SPEED_BOOST_KICK_X = 12.0;
    private static final double SPEED_BOOST_KICK_Y = 15.0;
    private static final double SPEED_BOOST_PER_TICK = 1.0001;
    private static final double MASSIVE_DRAG_PER_TICK = 0.96;

    // ------------------------------------------------------------
    // Buff aplication
    // ------------------------------------------------------------

    public static void ApplyBuff(buffs buff_applied, int duration_in_seconds) {
        if (buff_applied == null)
            return;

        active_buffs.put(buff_applied, duration_in_seconds * TICKS_PER_SECOND);
        any_buff_active = true;

        // gives false to the instant effects on certain buffs
        if (buff_applied == buffs.ICED)
            iced_active = false;
        else if (buff_applied == buffs.SPEED_BOOST)
            speed_boost_active = false;
        else if (buff_applied == buffs.LAG)
            lag_active = false;
        else if (buff_applied == buffs.TIME_TRAVEL)
            time_travel_active = false;
    }

    public static void ApplyBuff(buffs buff_applied) {
        Integer duration = standard_buff_duration.get(buff_applied);
        ApplyBuff(buff_applied, duration != null ? duration : 3);
    }

    public static buffs returnBuff(int obj_id) {
        return id_to_buffs.get(obj_id);
    }

    public static boolean HasBuff(buffs buff) {
        Integer remaining = active_buffs.get(buff);
        return remaining != null && remaining > 0;
    }

    public static int BuffDuration(buffs buff) {
        Integer remaining = active_buffs.get(buff);
        return remaining != null ? remaining : 0;
    }

    public static void EndBuff(buffs buff) {
        active_buffs.put(buff, 0);
    }

    public static void EndBuffs() {
        for (buffs buff : buffs.values()) {
            active_buffs.put(buff, 0);
        }
        any_buff_active = false;
    }

    public static void DecrementABuff(buffs buff, int duration_in_ticks) {
        active_buffs.put(buff, Math.max(0, BuffDuration(buff) - duration_in_ticks));
    }

    public static boolean anyBuffActive() {
        return any_buff_active;
    }

    // used by gameMap
    public static boolean isIntangible() {
        return HasBuff(buffs.INTANGIBLE);
    }

    // flag used by the game to freeze the ball in place but not reset it
    public static boolean isLagFrozen() {
        return lag_freeze_engaged;
    }

    // ------------------------------------------------------------
    // life cycle
    // ------------------------------------------------------------

    // resets everything
    public static void reset(BallObj ball) {
        EndBuffs();
        speed_boost_active = false;
        iced_active = false;
        lag_active = false;
        time_travel_active = false;
        lag_freeze_engaged = false;
        lag_toggle_counter = 0;
        time_travel_position = null;

        if (ball != null && base_values_captured) {
            ball.changeFriction(base_friction);
            ball.changeElasticFactor(base_restitution);
        }
        base_values_captured = false;
    }

    // decrement buff timers
    public static void update(BallObj ball) {
        if (ball == null)
            return;

        captureBaseValues(ball);
        DecrementBuffTimers();
        applyEffects(ball);
    }

    private static void captureBaseValues(BallObj ball) {
        if (base_values_captured)
            return;
        base_friction = ball.getFriction();
        base_restitution = ball.getRestitution();
        base_values_captured = true;
    }

    public static void DecrementBuffTimers() {
        if (!any_buff_active)
            return;

        boolean still_running = false;
        for (Map.Entry<buffs, Integer> buff : active_buffs.entrySet()) {
            if (buff.getValue() > 0) {
                buff.setValue(buff.getValue() - 1);
                still_running = true;
            }
        }
        any_buff_active = still_running;
    }

    // ------------------------------------------------------------
    // Ball effects
    // ------------------------------------------------------------
    private static void applyEffects(BallObj ball) {

        // ICED: freia a bola de uma vez e vira SLIPPERY 
        if (HasBuff(buffs.ICED)) {
            if (!iced_active) {
                double vx = ball.getVelocityX();
                double vy = ball.getVelocityY();
                vx = (vx > 0) ? Math.max(vx - ICED_STOP_X, 0) : Math.min(vx + ICED_STOP_X, 0);
                vy = (vy > 0) ? Math.max(vy - ICED_STOP_Y, 0) : Math.min(vy + ICED_STOP_Y, 0);
                ball.changeVelocity(vx, vy);

                iced_active = true;
                ApplyBuff(buffs.SLIPPERY, BuffDuration(buffs.ICED) / TICKS_PER_SECOND);
                EndBuff(buffs.ICED);
            }
        } else if (iced_active) {
            iced_active = false;
        }

        // SPEED_BOOST: empurrao inicial + aceleracao continua
        else if (HasBuff(buffs.SPEED_BOOST)) {
            double vx = ball.getVelocityX();
            double vy = ball.getVelocityY();

            if (!speed_boost_active) {
                if (vx != 0)
                    vx += (vx > 0) ? SPEED_BOOST_KICK_X : -SPEED_BOOST_KICK_X;
                if (vy != 0)
                    vy += (vy > 0) ? SPEED_BOOST_KICK_Y : -SPEED_BOOST_KICK_Y;
                speed_boost_active = true;
            }

            ball.changeVelocity(vx * SPEED_BOOST_PER_TICK, vy * SPEED_BOOST_PER_TICK);
        } else if (speed_boost_active) {
            speed_boost_active = false;
        }

        // ELASTIC_COLLISION: restituicao vai a 1.0 
        if (HasBuff(buffs.ELASTIC_COLLISION)) {
            ball.changeElasticFactor(1.0);
        } else {
            ball.changeElasticFactor(base_restitution);
        }

        // SLIPPERY: atrito zero. MASSIVE_DRAG: amortecimento por tick
        if (HasBuff(buffs.SLIPPERY)) {
            ball.changeFriction(0.0);
        } else {
            ball.changeFriction(base_friction);
        }

        if (HasBuff(buffs.MASSIVE_DRAG)) {
            ball.changeVelocity(ball.getVelocityX() * MASSIVE_DRAG_PER_TICK,
                    ball.getVelocityY() * MASSIVE_DRAG_PER_TICK);
        }

        // LAG: trava o movimento em janelas de 32 ticks 
        updateLag(ball);

        // TIME_TRAVEL: grava a posicao e volta para ela no fim
        if (HasBuff(buffs.TIME_TRAVEL)) {
            if (!time_travel_active) {
                time_travel_position = ball.getPosition();
                time_travel_active = true;
            }
        } else if (time_travel_active) {
            if (time_travel_position != null) {
                ball.move(time_travel_position);
                ball.changeVelocity(0, 0);
                ball.changeAngularVelocity(0);
            }
            time_travel_active = false;
            time_travel_position = null;
        }
    }

    // O LAG antigo alternava um flag a cada 32 ticks; enquanto ligado, a bola
    // parava de integrar e a posicao desenhada ficava congelada. Aqui o efeito
    // e o mesmo: a velocidade e guardada, a bola e congelada no lugar (sem
    // gravidade) e tudo volta quando a janela fecha.
    private static void updateLag(BallObj ball) {
        boolean has_lag = HasBuff(buffs.LAG);

        if (!has_lag) {
            if (lag_active || lag_freeze_engaged) {
                releaseLagFreeze(ball);
                lag_active = false;
                lag_toggle_counter = 0;
            }
            return;
        }

        lag_toggle_counter++;
        if (lag_toggle_counter >= LAG_TOGGLE_PERIOD) {
            lag_toggle_counter = 0;
            lag_active = !lag_active;
        }

        if (lag_active) {
            if (!lag_freeze_engaged) {
                lag_frozen_velocity = ball.getLinearVelocity();
                lag_freeze_engaged = true;
            }
            ball.changeNoGravityStatus(true);
            ball.changeVelocity(0, 0);
            ball.changeAngularVelocity(0);
        } else {
            releaseLagFreeze(ball);
        }
    }

    private static void releaseLagFreeze(BallObj ball) {
        if (!lag_freeze_engaged)
            return;
        ball.changeNoGravityStatus(false);
        ball.changeVelocity(lag_frozen_velocity.x, lag_frozen_velocity.y);
        lag_freeze_engaged = false;
    }

    // ------------------------------------------------------------
    // Buff collection
    // ------------------------------------------------------------

    // Chamado pelo GameMap quando o jogador encosta num BuffObj.
    // Devolve true se o buff foi de fato consumido.
    public static boolean pickUpBuff(GameObject buff_object) {
        if (buff_object == null || !buff_object.isActive())
            return false;

        buffs buff = returnBuff(buff_object.getObjId());
        if (buff == null)
            return false;

        Integer duration = standard_buff_duration.get(buff);
        ApplyBuff(buff, duration != null ? duration : 3);

        // Pontuacao do buff, com a tabela do PointSystem (INTANGIBLE e LAG
        // valem pontos negativos.
        long value = PointSystem.pointsForObject(buff_object);
        PointSystem.addPotentialPoints(value);

        if (value < 0)
            SoundEffectPlayer.playDebuffSound();
        else
            SoundEffectPlayer.playBuffSound();

        // O objeto some do mapa. O GameMap.deleteInactiveObjs() cuida da
        // remocao no fim do passo de fisica.
        GameObject.deactivate(buff_object);
        return true;
    }

    // ------------------------------------------------------------
    // HUD
    // ------------------------------------------------------------

    // Lista os buffs ativos e quanto falta de cada um, no canto inferior
    // esquerdo.
    public static void drawHud(Graphics2D g2d, int panel_width, int panel_height) {
        if (!any_buff_active)
            return;

        double scale_x = panel_width / (double) Main.DEFAULT_RESOLUTION.width;
        double scale_y = panel_height / (double) Main.DEFAULT_RESOLUTION.height;

        Graphics2D g2 = (Graphics2D) g2d.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("TeX Gyre Bonum", Font.BOLD, (int) (14 * scale_y)));

        int line_height = (int) (18 * scale_y);
        int x = (int) (16 * scale_x);
        int y = panel_height - (int) (16 * scale_y);

        for (buffs buff : buffs.values()) {
            int remaining = BuffDuration(buff);
            if (remaining <= 0)
                continue;

            double seconds = remaining / (double) TICKS_PER_SECOND;
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(x - 4, y - line_height + (int) (4 * scale_y),
                    (int) (170 * scale_x), line_height, 8, 8);

            g2.setColor(isDebuff(buff) ? new Color(230, 120, 120) : new Color(140, 220, 255));
            g2.drawString(String.format("%s  %.1fs", buff.name(), seconds), x, y);

            y -= line_height;
        }

        g2.dispose();
    }

    private static boolean isDebuff(buffs buff) {
        return buff == buffs.LAG || buff == buffs.INTANGIBLE || buff == buffs.MASSIVE_DRAG;
    }

    // Funcao de depuracao, herdada do sistema antigo.
    public static void CheckDuration(buffs buff) {
        System.out.printf("%s: %d ticks%n", buff.name(), BuffDuration(buff));
    }
}
