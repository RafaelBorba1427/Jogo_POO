import java.awt.Dimension;
import javax.swing.JFrame;
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

        frame.getContentPane().removeAll();

        menu = new MainMenu(frame);
        frame.add(menu);

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
        frame.add(game);

        frame.revalidate();
        frame.repaint();

        // O LevelRules precisa apontar para a partida NOVA, senao os dialogos
        // de item e o game over continuariam falando com a partida anterior.
        LevelRules.startRules(frame, game);

        game.startGame();

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
