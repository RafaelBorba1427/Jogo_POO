import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class coisa {
  public int x, y, diametro;

  public coisa(int x, int y, int diametro) {
    this.x = x;
    this.y = y;
    this.diametro = diametro;
  };

  public void verify(ball bola) {
    if (y <= bola.getY() + 15 && y >= bola.getY() - 15 && x <= bola.getX() + 15 && x >= bola.getX() - 15) {
      bola.bounceY();

    }
  }
}
