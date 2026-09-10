import javax.swing.JFrame;

@Deprecated
class End_of_game {

   /** @deprecated use {@link GameOverScreen} pelo {@link GameFlow#showGameOver(long)} */
   @Deprecated
   End_of_game(JFrame owner, int points) {
      new GameOverScreen(owner, points, GameFlow::showMenu);
   }
}
