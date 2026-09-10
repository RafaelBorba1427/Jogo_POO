import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

// ------------------------------------------------------------
// GameOverScreen
//
// Refatoracao de oldGame/end.java. Substitui End_of_game.java, que era codigo
// descompilado (variaveis var1/var2/var3, constantes numericas cruas) e nao
// tinha nem animacao nem saida: abria e ficava la.
//
// O que mudou em relacao ao end.java antigo:
//
// 1. Nao depende mais da classe game. O construtor recebia um "game current"
//    so para alcancar current.frame e game.pointSys. Agora recebe o JFrame e a
//    pontuacao ja pronta, entao a tela nao conhece nada do jogo.
//
// 2. Anima sozinha. O update_panel() antigo precisava ser chamado pelo timer do
//    jogo, e o timer do jogo para justamente quando o jogador perde, entao a
//    animacao dependia de o laco continuar rodando. Agora a tela tem o proprio
//    javax.swing.Timer, encerrado quando ela fecha.
//
// 3. Tem saida: um botao "Voltar ao Menu" que dispara o callback recebido, que
//    e o que fecha o ciclo Menu -> Game -> GameOver -> Menu (ver GameFlow).
//
// 4. O JLabel era adicionado duas vezes (uma no painel, outra no proprio
//    dialog), e o setContentPane depois do add descartava metade do trabalho.
//    Agora ha uma unica arvore de componentes.
// ------------------------------------------------------------
public class GameOverScreen extends JDialog {

    // done.png tem 200x100: 2 quadros de 100x100.
    private static final int SPRITE_WIDTH = 100;
    private static final int SPRITE_HEIGHT = 100;
    private static final int FRAME_COUNT = 2;

    // Ticks de 16ms entre um quadro e outro (o antigo trocava a cada 40).
    private static final int TICKS_PER_FRAME = 40;

    private final long points;

    private int current_column = 0;
    private int frame_counter = 0;

    private BufferedImage cached_background;
    private Timer animation_timer;

    // Nao modal, de proposito. Um dialogo modal bloqueia dentro do proprio
    // setVisible(true), entao GameFlow.showGameOver so retornaria depois da
    // tela fechar e a volta ao menu aconteceria aninhada dentro dela. Sem
    // modalidade, o callback e quem conduz a transicao.
    public GameOverScreen(JFrame owner, long points, Runnable on_return_to_menu) {
        super(owner, "You Lose", false);
        this.points = points;

        JPanel panel = new JPanel() {
            private final Image sprite_sheet = new ImageIcon("spritesheet/done.png").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Fundo vermelho translucido, cacheado uma vez por tamanho para
                // nao recriar o BufferedImage a cada repaint.
                if (cached_background == null
                        || cached_background.getWidth() != getWidth()
                        || cached_background.getHeight() != getHeight()) {

                    if (getWidth() <= 0 || getHeight() <= 0)
                        return;

                    cached_background = new BufferedImage(getWidth(), getHeight(),
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics2D bg = cached_background.createGraphics();
                    bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    bg.setColor(new Color(139, 0, 0));
                    bg.fillRect(0, 0, getWidth(), getHeight());
                    bg.dispose();
                }

                g.drawImage(cached_background, 0, 0, this);

                int draw_width = getWidth() / 2;
                int draw_height = getHeight() / 2;
                int centered_x = (getWidth() - draw_width) / 2;
                int centered_y = (getHeight() - draw_height) / 2;

                g.drawImage(sprite_sheet,
                        centered_x, centered_y,
                        centered_x + draw_width, centered_y + draw_height,
                        current_column * SPRITE_WIDTH, 0,
                        current_column * SPRITE_WIDTH + SPRITE_WIDTH, SPRITE_HEIGHT,
                        this);
            }
        };
        panel.setOpaque(true);
        panel.setLayout(new BorderLayout());

        // ---- Rodape: pontuacao + botao de voltar ----
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel score_label = new JLabel("Pontuacao final: " + points);
        score_label.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 22));
        score_label.setHorizontalAlignment(SwingConstants.CENTER);
        score_label.setForeground(Color.BLACK);
        footer.add(score_label, BorderLayout.CENTER);

        JButton back_button = new JButton("Voltar ao Menu");
        back_button.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 18));
        back_button.setFocusPainted(false);
        back_button.addActionListener(e -> {
            closeScreen();
            if (on_return_to_menu != null) {
                on_return_to_menu.run();
            }
        });

        JPanel button_row = new JPanel(new FlowLayout(FlowLayout.CENTER));
        button_row.setOpaque(false);
        button_row.add(back_button);
        footer.add(button_row, BorderLayout.SOUTH);

        panel.add(footer, BorderLayout.SOUTH);

        setUndecorated(true);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Dimension size = (owner != null)
                ? new Dimension(owner.getWidth(), owner.getHeight())
                : new Dimension(Main.DEFAULT_RESOLUTION);
        setSize(size);
        setLocationRelativeTo(owner);
        setContentPane(panel);

        // Fechar a janela pelo X tambem devolve ao menu, senao o jogo fica
        // preso sem tela nenhuma.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopAnimation();
            }
        });

        animation_timer = new Timer(16, e -> updatePanel(panel));
        animation_timer.start();

        MusicPlayer.setTrackAndPlay("defeat", false);

        setVisible(true);
    }

    // Avanca a animacao do sprite. Equivalente ao update_panel() antigo, mas
    // dirigido pelo timer proprio da tela.
    private void updatePanel(JPanel panel) {
        frame_counter++;
        if (frame_counter >= TICKS_PER_FRAME) {
            frame_counter = 0;
            current_column = (current_column + 1) % FRAME_COUNT;
        }
        panel.repaint();
    }

    private void stopAnimation() {
        if (animation_timer != null) {
            animation_timer.stop();
            animation_timer = null;
        }
    }

    private void closeScreen() {
        stopAnimation();
        dispose();
    }

    public long getPoints() {
        return points;
    }
}
