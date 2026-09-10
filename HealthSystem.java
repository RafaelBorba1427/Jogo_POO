import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

// ------------------------------------------------------------
// HealthSystem
// ------------------------------------------------------------

public class HealthSystem {

    // ------------------------------------------------------------
    // Sprites
    // ------------------------------------------------------------

    private static final String HEART_ANIMATION_KEY = "heart";
    private static final String HEART_SPRITESHEET = "spritesheet/heart.png";

    // heart.png tem 80x16: 5 quadros de 16x16, do coracao cheio ao vazio.
    private static final int HEART_SPRITE_SIZE = 16;
    private static final int HEART_FRAME_COUNT = 5;

    // Ticks entre um quadro e o proximo da animacao de dano/cura.
    private static final int TICKS_PER_FRAME = 4;

    private static boolean sprites_loaded = false;

    private static void loadSprites() {
        if (sprites_loaded)
            return;
        SpriteLoader.loadSpritesheet(HEART_ANIMATION_KEY, HEART_SPRITESHEET,
                HEART_SPRITE_SIZE, HEART_SPRITE_SIZE, HEART_FRAME_COUNT, 0);
        sprites_loaded = true;
    }

    // ------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------

    private int max_hp;
    private int current_hp;
    private boolean is_dead;
    private boolean visible;

    private final ArrayList<Heart> hearts = new ArrayList<>();

    // Layout dos coracoes em unidades da resolucao padrao (800x600).
    private static final int HEART_DRAW_SIZE = 48;
    private static final int HEART_SPACING = 52;
    private static final int HEART_ORIGIN_X = 16;
    private static final int HEART_ORIGIN_Y = 12;

    public HealthSystem(int max_hp, boolean visible) {
        loadSprites();

        this.max_hp = max_hp;
        this.current_hp = max_hp;
        this.is_dead = false;
        this.visible = visible;

        for (int i = 0; i < max_hp; i++) {
            hearts.add(new Heart());
        }
    }

    // ------------------------------------------------------------
    // Vida
    // ------------------------------------------------------------

    public void AddMaxHearts(int amount) {
        if (amount <= 0)
            return;

        for (int i = 0; i < amount; i++) {
            // Coracao novo nasce VAZIO (o antigo chamava playDamageAnimation
            // logo depois de criar, com o mesmo efeito), entao ganhar um slot
            // de vida nao ganha a vida junto: e preciso curar.
            Heart heart = new Heart();
            heart.playDamageAnimation();
            hearts.add(heart);
        }
        max_hp += amount;
    }

    // Retorna true se o jogador morreu com esse dano.
    public boolean takeDamageAndCheckDeath() {
        if (current_hp > 0) {
            current_hp--;

            hearts.get(current_hp).playDamageAnimation();
            SoundEffectPlayer.playSound("damage");

            // Troca a trilha quando a vida cai pela metade, igual ao antigo.
            if (current_hp <= max_hp / 2 && current_hp > 0) {
                MusicPlayer.setTrackAndPlay("gameOverworld1");
            }
        }

        if (current_hp <= 0) {
            this.is_dead = true;
            return true;
        }
        return false;
    }

    public void heal() {
        if (current_hp < max_hp) {
            current_hp++;
            hearts.get(current_hp - 1).playDamageAnimationReverse();
            is_dead = false;
        }
    }

    // Devolve a vida cheia sem animar. Usado ao comecar uma partida nova.
    public void resetToFull() {
        current_hp = max_hp;
        is_dead = false;
        for (Heart heart : hearts) {
            heart.resetToFull();
        }
    }

    // ------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------

    public int getCurrentHp() {
        return current_hp;
    }

    public int getMaxHp() {
        return max_hp;
    }

    public boolean isDead() {
        return is_dead;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    // ------------------------------------------------------------
    // Atualizacao e desenho
    // ------------------------------------------------------------

    // Deve ser chamado uma vez por tick do jogo (Game.startGame faz isso).
    public void tick() {
        for (Heart heart : hearts) {
            heart.tick();
        }
    }

    // Desenhado em espaco de TELA, chamado por Game.paintComponent.
    public void draw(Graphics2D g2d, int panel_width, int panel_height) {
        if (!visible)
            return;

        AnimationFrame[] frames = SpriteLoader.getSplicedSprites(HEART_ANIMATION_KEY);

        double scale_x = panel_width / (double) Main.DEFAULT_RESOLUTION.width;
        double scale_y = panel_height / (double) Main.DEFAULT_RESOLUTION.height;

        Graphics2D g2 = (Graphics2D) g2d.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int draw_size_x = (int) (HEART_DRAW_SIZE * scale_x);
        int draw_size_y = (int) (HEART_DRAW_SIZE * scale_y);
        int origin_y = (int) (HEART_ORIGIN_Y * scale_y);

        for (int i = 0; i < hearts.size(); i++) {
            int x = (int) ((HEART_ORIGIN_X + i * HEART_SPACING) * scale_x);
            hearts.get(i).draw(g2, frames, x, origin_y, draw_size_x, draw_size_y);
        }

        g2.dispose();
    }

    // ------------------------------------------------------------
    // Heart
    //
    // Antes era uma classe solta no fim de healthSystem.java, com posicao
    // propria e um ImageIO.read no construtor. Agora e uma classe interna que
    // guarda SO o estado da animacao; a posicao e decidida na hora de desenhar,
    // para o HUD acompanhar a resolucao.
    // ------------------------------------------------------------
    private static class Heart {
        private int current_frame = 0;
        private int frame_timer = 0;

        private boolean play_damage_animation = false;
        private boolean play_damage_animation_reverse = false;

        void playDamageAnimation() {
            play_damage_animation = true;
            play_damage_animation_reverse = false;
        }

        void playDamageAnimationReverse() {
            play_damage_animation_reverse = true;
            play_damage_animation = false;
        }

        void resetToFull() {
            current_frame = 0;
            frame_timer = 0;
            play_damage_animation = false;
            play_damage_animation_reverse = false;
        }

        void tick() {
            if (!play_damage_animation && !play_damage_animation_reverse)
                return;

            frame_timer++;
            if (frame_timer < TICKS_PER_FRAME)
                return;
            frame_timer = 0;

            if (play_damage_animation) {
                if (current_frame >= HEART_FRAME_COUNT - 1) {
                    play_damage_animation = false;
                    return;
                }
                current_frame++;
            } else {
                if (current_frame <= 0) {
                    play_damage_animation_reverse = false;
                    return;
                }
                current_frame--;
            }
        }

        void draw(Graphics2D g2d, AnimationFrame[] frames, int x, int y, int width, int height) {
            if (frames != null && current_frame < frames.length && frames[current_frame] != null) {
                g2d.drawImage(frames[current_frame].getImage(), x, y, width, height, null);
                return;
            }

            // Fallback caso spritesheet/heart.png nao carregue: um quadrado
            // cheio ou vazio, para o jogo nao quebrar por falta de asset.
            g2d.setColor(current_frame == 0 ? new Color(200, 40, 40) : new Color(70, 70, 70));
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, width, height);
        }
    }
}
