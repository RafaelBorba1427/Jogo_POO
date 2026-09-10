import java.awt.Graphics2D;

// Bomba do Frat God: fica no mapa com o pavio queimando, explode uma vez e some.
// Os quadros vem da linha ID_BOMB da combined_spritesheet: 0..6 e o pavio,
// 7..12 e a explosao. Os quadros sao indexados a mao (e nao pelo AnimationPlayer)
// porque a explosao precisa tocar UMA vez, e o AnimationPlayer so roda em loop.
public class BombObj extends BallObj {

    private static final String ANIMATION_KEY = "objects1_" + GameObject.ID_BOMB;
    private static final String SPRITESHEET = "spritesheet/combined_spritesheet.png";
    private static final int SPRITE_SIZE = 16;
    private static final int SHEET_FRAMES = 15;

    private static final int FUSE_LAST_FRAME = 6;
    private static final int EXPLOSION_FIRST_FRAME = 7;
    private static final int EXPLOSION_LAST_FRAME = 12;
    private static final int EXPLOSION_TICKS_PER_FRAME = 4;

    private final int fuse_duration;
    private final double blast_radius;

    private int elapsed_ticks = 0;
    private int current_frame = 0;
    private int frame_timer = 0;

    private boolean exploded = false;
    private boolean finished = false;

    BombObj(double center_x, double center_y, double diameter, int fuse_duration, double blast_radius) {
        super(center_x - diameter / 2.0, center_y - diameter / 2.0, diameter,
                20, 0.5, true, GameObject.ID_BOMB, 0.5);
        this.fuse_duration = Math.max(1, fuse_duration);
        this.blast_radius = blast_radius;

        // A bomba fica exatamente onde foi clicada. Se caisse, a previa do raio
        // desenhada no cursor estaria mentindo sobre onde a explosao acontece.
        changeNoGravityStatus(true);
    }

    @Override
    protected void createAnimationPlayer() {
        SpriteLoader.loadSpritesheet(ANIMATION_KEY, SPRITESHEET,
                SPRITE_SIZE, SPRITE_SIZE, SHEET_FRAMES, GameObject.ID_BOMB);
        animation = null;
    }

    // Um tick do jogo. Devolve true no unico tick em que a bomba detona.
    public boolean tick() {
        if (finished)
            return false;

        if (!exploded) {
            elapsed_ticks++;
            current_frame = Math.min(FUSE_LAST_FRAME,
                    (elapsed_ticks * (FUSE_LAST_FRAME + 1)) / fuse_duration);

            if (elapsed_ticks < fuse_duration)
                return false;

            exploded = true;
            current_frame = EXPLOSION_FIRST_FRAME;
            frame_timer = 0;

            changeVelocity(0, 0);
            changeAngularVelocity(0);
            changeAcceleration(0, 0);
            changeNoGravityStatus(true);
            return true;
        }

        frame_timer++;
        if (frame_timer < EXPLOSION_TICKS_PER_FRAME)
            return false;
        frame_timer = 0;

        if (current_frame >= EXPLOSION_LAST_FRAME) {
            finished = true;
            return false;
        }
        current_frame++;
        return false;
    }

    // Depois de detonar a bomba vira so efeito visual: nao recebe impulso e nao
    // se move. O GameMap tambem para de gerar manifolds com ela.
    @Override
    public void applyImpulse(Vector2D impulse, Vector2D contact_arm) {
        if (exploded)
            return;
        super.applyImpulse(impulse, contact_arm);
    }

    @Override
    public void translate(Vector2D delta) {
        if (exploded)
            return;
        super.translate(delta);
    }

    public boolean hasExploded() {
        return exploded;
    }

    public boolean isFinished() {
        return finished;
    }

    public double getBlastRadius() {
        return blast_radius;
    }

    @Override
    public void drawSprite(Graphics2D g2d) {
        AnimationFrame[] frames = SpriteLoader.getSplicedSprites(ANIMATION_KEY);
        if (frames == null || current_frame >= frames.length || frames[current_frame] == null)
            return;

        // A explosao e desenhada do tamanho da area que ela destroi.
        double size = exploded ? blast_radius * 2.0 : dimensions.x;

        Vector2D center = getCenterOfMass();
        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(center.x, center.y);
        g.rotate(rotation);
        g.drawImage(frames[current_frame].getImage(),
                (int) (-size / 2), (int) (-size / 2), (int) size, (int) size, null);
        g.dispose();
    }

    @Override
    public void drawSprite(Graphics2D g2d, double position_x, double position_y) {
        drawSprite(g2d);
    }
}
