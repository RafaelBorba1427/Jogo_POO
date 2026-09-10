import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

// ------------------------------------------------------------
// GameFlow
//
// Classe nova. Ela e o ciclo Menu -> Game -> GameOver -> Menu.
//
// Por que ela existe: antes cada tela sabia como construir a proxima. O
// MainMenu dava "new Game(...)" e "frame.add(...)" no clique do botao, o
// LevelRules dava "new End_of_game(...)" no meio da logica de nivel, e ninguem
// removia nada do JFrame nem parava o Timer da partida anterior. Resultado: o
// caminho de volta ao menu simplesmente nao existia, e reentrar no jogo
// empilhava paineis e laços de 16ms rodando ao mesmo tempo.
//
// Aqui a troca de tela acontece num lugar so, e cada transicao:
//   - para o laco da partida que esta saindo (Game.stopGame),
//   - remove o painel antigo do frame antes de adicionar o novo,
//   - zera o estado GLOBAL do jogo (os sistemas sao estaticos, entao sobrevivem
//     entre partidas se ninguem limpar),
//   - devolve o foco de teclado ao painel que entrou.
// ------------------------------------------------------------
public class GameFlow {

    private static JFrame frame;
    private static MainMenu menu;
    private static Game game;

    public static void initialise(JFrame frame) {
        GameFlow.frame = frame;
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static Game getGame() {
        return game;
    }

    // ------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------

    public static void showMenu() {
        if (frame == null)
            return;

        stopCurrentGame();
        stopCurrentMenu();

        // A partida troca o content pane por um JLayeredPane com layout nulo.
        // Reaproveitar aquele painel aqui deixaria o menu com tamanho 0x0.
        JPanel menu_pane = new JPanel(new BorderLayout());
        frame.setContentPane(menu_pane);

        menu = new MainMenu(frame);
        menu_pane.add(menu, BorderLayout.CENTER);

        frame.pack();
        frame.revalidate();
        frame.repaint();

        MusicPlayer.setTrackAndPlay("menu");

        SwingUtilities.invokeLater(() -> menu.requestFocusInWindow());
    }

    // ------------------------------------------------------------
    // Partida
    // ------------------------------------------------------------

    public static void startNewGame() {
        if (frame == null)
            return;

        stopCurrentGame();
        stopCurrentMenu();
        resetGlobalState();

        frame.getContentPane().removeAll();

        Dimension resolution = new Dimension(Main.DEFAULT_RESOLUTION);
        game = new Game(resolution);
        game.startGame();

        // O LevelRules precisa apontar para a partida NOVA, senao os dialogos
        // de item e o game over continuariam falando com a partida anterior.
        LevelRules.startRules(frame, game);

        

        JLayeredPane laypane = new JLayeredPane();
        laypane.setLayout(null); // JLayeredPane manages z-order internally via add(comp, layer) — don't assign a
                                // LayoutManager here

        CutsceneOverlay cut = new CutsceneOverlay(laypane);

        game.setPreferredSize(Main.DEFAULT_RESOLUTION);
        cut.setPreferredSize(Main.DEFAULT_RESOLUTION);
        game.setOverlay(cut);

        laypane.add(cut, JLayeredPane.PALETTE_LAYER);
        laypane.add(game, JLayeredPane.DEFAULT_LAYER);

        game.setBounds(0, 0, Main.DEFAULT_RESOLUTION.width, Main.DEFAULT_RESOLUTION.height);
        cut.setBounds(0, 0, Main.DEFAULT_RESOLUTION.width, Main.DEFAULT_RESOLUTION.height);

        laypane.setPreferredSize(Main.DEFAULT_RESOLUTION); // no LayoutManager to derive this now, so set explicitly

        frame.setContentPane(laypane);

        // pack() dimensiona pela area util; setSize dimensionava a JANELA inteira
        // (encolhendo o jogo pelas bordas) e ainda desfazia a centralizacao, por
        // vir depois dela.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
        cut.showDialog(CutsceneOverlay.Stage.OPEN, GameRules.GameModes.GAMELOOP);
        } catch (Exception e) {
        System.out.println(e);
        e.printStackTrace(); // print full stack trace so you can see exactly where/why it failed, not just
                            // the message
        }
        
        frame.revalidate();
        frame.repaint();

        MusicPlayer.setTrackAndPlay("gameOverworld0");

        SwingUtilities.invokeLater(() -> game.requestFocusInWindow());
    }

    // ------------------------------------------------------------
    // Game over
    // ------------------------------------------------------------

    public static void showGameOver(long final_points) {
        if (frame == null)
            return;

        stopCurrentGame();

        // A tela nao e modal: esta chamada retorna na hora. Quem fecha o ciclo
        // e o callback, disparado pelo botao "Voltar ao Menu".
        new GameOverScreen(frame, final_points, GameFlow::showMenu);
    }

    // ------------------------------------------------------------
    // Limpeza entre telas
    // ------------------------------------------------------------

    private static void stopCurrentGame() {
        if (game != null) {
            game.stopGame();
            game = null;
        }
    }

    // O menu tem um Timer proprio repintando os icones animados dos botoes.
    // Sem parar aqui, ele continuaria rodando por cima da partida.
    private static void stopCurrentMenu() {
        if (menu != null) {
            menu.dispose();
            menu = null;
        }
    }

    // Os sistemas do jogo (PointSystem, BuffSystem, GameRules, LevelRules) sao
    // estaticos, entao o estado da partida anterior sobrevive a troca de tela.
    // Sem esta limpeza, a segunda partida comecaria com os pontos, os buffs e o
    // contador de niveis da primeira.
    private static void resetGlobalState() {
        PointSystem.removeALLPoints();
        BuffSystem.reset(Game.pingbongBall);
        LevelRules.resetForNewGame();

        GameRules.current_game_mode = GameRules.GameModes.GAMELOOP;
        GameRules.physics_on = false;
        GameRules.global_gravity_on = true;

        Game.next_level = false;
    }
}
