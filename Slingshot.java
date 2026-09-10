import java.awt.Graphics2D;

// ------------------------------------------------------------
// Slingshot
//
// Classe nova. Traz de volta o estilingue que existia no oldGame/game.java
// (o bloco "if (aiming)" do paintComponent, com sling_counter e o
// g2.rotate(angle, centerX, centerY)), agora como um objeto com estado
// proprio em vez de meia duzia de variaveis estaticas espalhadas pelo painel.
//
// Por que nao usa AnimationPlayer: o AnimationPlayer roda em loop, sempre
// para frente, no mesmo compasso para todas as instancias da mesma chave. O
// estilingue tem duas fases distintas -- carregar (avanca ate um quadro e
// SEGURA la enquanto o jogador mira) e soltar (dispara uma vez e some) -- que
// nao cabem num loop. Os quadros vem do SpriteLoader, que e o mesmo cache que
// o AnimationPlayer usa, so que indexados a mao. Mesma abordagem do
// HealthSystem.
//
// A fatia do estilingue na combined_spritesheet.png e a linha
// GameObject.ID_SLINGSHOT (5), com 15 quadros de 16x16.
// ------------------------------------------------------------
public class Slingshot {

    private static final String ANIMATION_KEY = "slingshot";
    private static final String SPRITESHEET = "spritesheet/combined_spritesheet.png";
    private static final int SPRITE_SIZE = 16;
    private static final int FRAME_COUNT = 15;

    // Quadros 0..9 sao a fase de carregar (o elastico sendo puxado).
    // Quadros 10..14 sao o disparo.
    private static final int CHARGE_LAST_FRAME = 9;
    private static final int RELEASE_FIRST_FRAME = 10;

    // Ticks de 16ms entre quadros.
    private static final int TICKS_PER_FRAME = 3;

    // Tamanho do estilingue em unidades de mapa, relativo ao diametro da bola.
    private static final double SIZE_FACTOR = 2.6;

    // Distancia entre o centro da bola e o centro do estilingue, tambem
    // relativa ao diametro: e o que faz a bola parecer encaixada na bolsa.
    private static final double PIVOT_OFFSET_FACTOR = 0.55;

    private static boolean sprites_loaded = false;

    private static void loadSprites() {
        if (sprites_loaded)
            return;
        SpriteLoader.loadSpritesheet(ANIMATION_KEY, SPRITESHEET,
                SPRITE_SIZE, SPRITE_SIZE, FRAME_COUNT, GameObject.ID_SLINGSHOT);
        sprites_loaded = true;
    }

    // ------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------

    private boolean aiming = false;
    private boolean releasing = false;

    private int current_frame = 0;
    private int frame_timer = 0;

    // Angulo para onde o estilingue aponta, em radianos.
    private double rotation = 0.0;

    // Centro da bola, em unidades de mapa.
    private Vector2D anchor = new Vector2D();
    private double ball_diameter = 40;

    public Slingshot() {
        loadSprites();
    }

    // ------------------------------------------------------------
    // Controle
    // ------------------------------------------------------------

    // Entra (ou continua) no estado de mira. Chamado a cada tick enquanto a
    // bola esta armada, com a posicao do mouse em unidades de mapa.
    public void aim(Vector2D ball_center, double ball_diameter, Vector2D target) {
        if (releasing)
            return;

        if (!aiming) {
            aiming = true;
            current_frame = 0;
            frame_timer = 0;
        }

        this.anchor = new Vector2D(ball_center);
        this.ball_diameter = ball_diameter;

        // O estilingue aponta na direcao contraria ao lancamento: o jogador
        // clica para onde quer mandar a bola, e o elastico e puxado para tras.
        Vector2D direction = target.subtract(ball_center);
        if (direction.lengthSquared() > 0.0001) {
            this.rotation = Math.atan2(direction.y, direction.x);
        }
    }

    // Dispara. Toca os quadros finais uma vez e some.
    public void release(Vector2D ball_center, Vector2D target) {
        if (!aiming && !releasing)
            return;

        Vector2D direction = target.subtract(ball_center);
        if (direction.lengthSquared() > 0.0001) {
            this.rotation = Math.atan2(direction.y, direction.x);
        }

        this.anchor = new Vector2D(ball_center);
        aiming = false;
        releasing = true;
        current_frame = RELEASE_FIRST_FRAME;
        frame_timer = 0;
    }

    // Some imediatamente, sem tocar o disparo. Usado quando a bola e movida
    // por outro motivo (troca de nivel, tecla de debug, game over).
    public void cancel() {
        aiming = false;
        releasing = false;
        current_frame = 0;
        frame_timer = 0;
    }

    public boolean isVisible() {
        return aiming || releasing;
    }

    // ------------------------------------------------------------
    // Atualizacao
    // ------------------------------------------------------------

    // Um tick do jogo.
    public void tick() {
        if (!aiming && !releasing)
            return;

        frame_timer++;
        if (frame_timer < TICKS_PER_FRAME)
            return;
        frame_timer = 0;

        if (aiming) {
            // Carrega e SEGURA no ultimo quadro da fase, para o jogador poder
            // mirar o tempo que quiser sem o sprite ficar piscando em loop.
            if (current_frame < CHARGE_LAST_FRAME)
                current_frame++;
            return;
        }

        // releasing
        if (current_frame >= FRAME_COUNT - 1) {
            releasing = false;
            current_frame = 0;
            return;
        }
        current_frame++;
    }

    // ------------------------------------------------------------
    // Desenho
    // ------------------------------------------------------------

    // Desenhado em ESPACO DE MUNDO (dentro do Graphics2D ja escalado e
    // transladado pela camera), para acompanhar a bola quando a camera se
    // move.
    public void draw(Graphics2D world_g2d) {
        if (!isVisible())
            return;

        AnimationFrame[] frames = SpriteLoader.getSplicedSprites(ANIMATION_KEY);
        if (frames == null || current_frame >= frames.length || frames[current_frame] == null)
            return;

        double size = ball_diameter * SIZE_FACTOR;
        double offset = ball_diameter * PIVOT_OFFSET_FACTOR;

        Graphics2D g2d = (Graphics2D) world_g2d.create();

        // Pivo no centro da bola, deslocado para tras da direcao de mira: a
        // bolsa do estilingue fica em cima da bola.
        g2d.translate(anchor.x, anchor.y);
        g2d.rotate(rotation);
        g2d.translate(-offset, 0);

        g2d.drawImage(frames[current_frame].getImage(),
                (int) (-size / 2), (int) (-size / 2),
                (int) size, (int) size, null);

        g2d.dispose();
    }

    // ------------------------------------------------------------
    // Getters (uteis para depuracao)
    // ------------------------------------------------------------

    public int getCurrentFrame() {
        return current_frame;
    }

    public boolean isAiming() {
        return aiming;
    }

    public boolean isReleasing() {
        return releasing;
    }
}
