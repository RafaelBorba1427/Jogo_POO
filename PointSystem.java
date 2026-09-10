import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

// ------------------------------------------------------------
// PointSystem
// Modelo de pontuacao (igual ao antigo):
//   potential_points = points earned before getting to the bucket
//   points           = points saved after hitting the bucket
//   processPoints()  = convert potential points into points (by hitting the bucket)
//   removePotentialPoints() = lose all potential points (after losing the game)
// ------------------------------------------------------------
public class PointSystem {

    // ------------------------------------------------------------
    // state
    // ------------------------------------------------------------

    private static long points = 0L;
    private static long potential_points = 0L;

    // Meta de pontos do nivel atual. Se o jogador terminar o nivel (ou a bola
    // parar) sem atingir esse valor, o HealthSystem tira um coracao.
    private static long target_points = 0L;

    // Emitido uma unica vez por nivel, no instante em que a meta e batida.
    public static final Signal<Boolean> reached_target_points = new Signal<>();
    private static boolean target_already_emitted = false;

    // ------------------------------------------------------------
    // Tabela de pontos por tipo de objeto
    // ------------------------------------------------------------

    public static final long DEFAULT_OBJECT_POINTS = 10L;

    private static final Map<Integer, Long> id_to_points = new HashMap<>();

    static {
        id_to_points.put(GameObject.ID_FROZEN_PLATFORM, 200L);
        id_to_points.put(GameObject.ID_PLATFORM, 100L);
        id_to_points.put(GameObject.ID_TABLE, 150L);
        id_to_points.put(GameObject.ID_WALL, 50L);
        id_to_points.put(GameObject.ID_BUCKET, 1000L);

        id_to_points.put(GameObject.ID_BUFF_ICED, 300L);
        id_to_points.put(GameObject.ID_BUFF_SPEED_BOOST, 50L);
        id_to_points.put(GameObject.ID_BUFF_INTANGIBLE, -150L);
        id_to_points.put(GameObject.ID_BUFF_TIME_TRAVEL, 150L);
        id_to_points.put(GameObject.ID_BUFF_LAG, -100L);
        id_to_points.put(GameObject.ID_BUFF_ELASTIC_COLLISION, 50L);

        // Cenario fixo do mapa: nao pontua, para nao dar pontos infinitos
        // quicando no chao.
        id_to_points.put(GameObject.ID_PERMANENT_FLOOR, 0L);
        id_to_points.put(GameObject.ID_PERMANENT_WALL, 0L);
        id_to_points.put(GameObject.ID_INVISIBLE_OBJ, 0L);
    }

    // ------------------------------------------------------------
    // Animacao do bonus no HUD
    // ------------------------------------------------------------

    // Equivalente a game.point_bonus / game.point_bonus_anime do jogo antigo.
    public static final int BONUS_ANIMATION_TICKS = 90;
    private static long last_bonus = 0L;
    private static int bonus_animation_counter = 0;

    // Precisa ser chamado uma vez por tick do jogo (Game.startGame faz isso).
    public static void tickAnimation() {
        if (bonus_animation_counter > 0)
            bonus_animation_counter--;
    }

    // ------------------------------------------------------------
    // Pontuacao
    // ------------------------------------------------------------

    // Pontua a colisao com um objeto do mapa, usando a tabela acima.
    public static void addPotentialPoints(GameObject obj) {
        if (obj == null)
            return;
        addPotentialPoints(pointsForObject(obj));
    }

    public static void addPotentialPoints(long amount) {
        if (amount == 0)
            return;

        potential_points += amount;
        last_bonus = amount;
        bonus_animation_counter = BONUS_ANIMATION_TICKS;

        checkTarget();
    }

    // Mantido com esse nome porque o GameMap ja chamava PointCounter.addPoints
    // no ponto em que a bola quica em alguma coisa.
    public static void addPoints(int amount) {
        addPotentialPoints((long) amount);
    }

    public static long pointsForObject(GameObject obj) {
        if (obj == null)
            return 0L;
        Long value = id_to_points.get(obj.getObjId());
        return (value != null) ? value : DEFAULT_OBJECT_POINTS;
    }

    // Confirma os pontos da tentativa atual. Chamado quando o nivel e vencido.
    public static void processPoints() {
        points += potential_points;
        potential_points = 0L;
        resetTargetEmission();
    }

    // Descarta os pontos da tentativa atual. Chamado quando o nivel e perdido.
    public static void removePotentialPoints() {
        potential_points = 0L;
        resetTargetEmission();
    }

    // Zera tudo. Chamado ao comecar uma partida nova a partir do menu.
    public static void removeALLPoints() {
        points = 0L;
        potential_points = 0L;
        last_bonus = 0L;
        bonus_animation_counter = 0;
        resetTargetEmission();
    }

    // ------------------------------------------------------------
    // Meta do nivel
    // ------------------------------------------------------------

    private static void checkTarget() {
        if (target_already_emitted)
            return;
        if (target_points > 0 && potential_points >= target_points) {
            target_already_emitted = true;
            reached_target_points.emit(true);
        }
    }

    // true se a meta do nivel atual ja foi atingida.
    public static boolean hasReachedTarget() {
        return target_points <= 0 || potential_points >= target_points;
    }

    private static void resetTargetEmission() {
        target_already_emitted = false;
    }

    public static void setTargetPoints(long target) {
        target_points = Math.max(0L, target);
        resetTargetEmission();
        checkTarget();
    }

    public static long getTargetPoints() {
        return target_points;
    }

    // ------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------

    public static long getPoints() {
        return points;
    }

    public static void setPoints(long new_points) {
        points = new_points;
    }

    public static long getPotentialPoints() {
        return potential_points;
    }

    // Total que o jogador veria se o nivel terminasse agora.
    public static long getDisplayPoints() {
        return points + potential_points;
    }

    // ------------------------------------------------------------
    // HUD
    // ------------------------------------------------------------

    // Desenha o painel de pontos no canto superior direito, em espaco de tela.
    // panel_width / panel_height sao o tamanho do JPanel do jogo, para o HUD
    // acompanhar mudancas de resolucao (era o papel de game.rescaleX/rescaleY).
    public static void drawHud(Graphics2D g2d, int panel_width, int panel_height) {
        double scale_x = panel_width / (double) Main.DEFAULT_RESOLUTION.width;
        double scale_y = panel_height / (double) Main.DEFAULT_RESOLUTION.height;

        Graphics2D g2 = (Graphics2D) g2d.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int hud_width = (int) (220 * scale_x);
        int hud_height = (int) (80 * scale_y);
        int hud_x = panel_width - hud_width - (int) (20 * scale_x);
        int hud_y = (int) (20 * scale_y);

        g2.setColor(new Color(170, 8, 0, 65));
        g2.fillRoundRect(hud_x, hud_y, hud_width, hud_height, 10, 15);

        g2.setColor(new Color(173, 133, 0));

        g2.setFont(new Font("TeX Gyre Bonum", Font.BOLD, (int) (22 * scale_y)));
        g2.drawString("Total Points: " + points, hud_x + (int) (15 * scale_x), hud_y + (int) (30 * scale_y));

        g2.setFont(new Font("TeX Gyre Bonum", Font.BOLD, (int) (18 * scale_y)));
        String points_text = "Points: " + potential_points;
        g2.drawString(points_text, hud_x + (int) (15 * scale_x), hud_y + (int) (60 * scale_y));

        // "+150" piscando ao lado dos pontos, igual ao HUD antigo
        if (bonus_animation_counter != 0) {
            FontMetrics fm = g2.getFontMetrics();
            String plus_minus = (last_bonus >= 0) ? " +" : " ";
            g2.setColor((last_bonus >= 0) ? new Color(90, 200, 90) : new Color(220, 70, 70));
            g2.drawString(plus_minus + last_bonus,
                    hud_x + (int) (15 * scale_x) + fm.stringWidth(points_text),
                    hud_y + (int) (60 * scale_y));
        }

        // Meta do nivel, logo abaixo do painel
        if (target_points > 0) {
            g2.setFont(new Font("TeX Gyre Bonum", Font.BOLD, (int) (14 * scale_y)));
            g2.setColor(hasReachedTarget() ? new Color(90, 200, 90) : new Color(220, 200, 120));
            String goal = "Meta: " + potential_points + " / " + target_points;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(goal, hud_x + hud_width - fm.stringWidth(goal),
                    hud_y + hud_height + (int) (16 * scale_y));
        }

        g2.dispose();
    }
}
