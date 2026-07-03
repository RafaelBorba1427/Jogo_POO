import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.*;

public class coisa extends Rectangle {
  public int x, y, width, height;
  public boolean bateu = false;
  public boolean bateuY = false;
  public boolean bateuX = false;
  public boolean buff = false;
  public game current;
  int id;
  public static final int Quant_IDs = 12;
  public static final int ID_PLATAFORMA_CONGELADA = 0;
  public static final int ID_PLATAFORMA = 1;
  public static final int ID_MESA = 2;
  public static final int ID_PAREDE = 3;

  public static final int ID_BALDE = 4;
  public static final int ID_ESTILINGUE = 5;
  public static final int ID_BUFF_ICED = 6;
  public static final int ID_BUFF_SPEED_BOOST = 7;
  public static final int ID_BUFF_INTANGIBLE = 8;
  public static final int ID_BUFF_TIME_TRAVEL = 9;
  public static final int ID_BUFF_LAG = 10;
  public static final int ID_BUFF_ELASTIC_COLLISION = 11;
  private int number = 1;
  // public JButton local;
  coisa self;

  public coisa(int x, int y, int diametro, int id, game current) {
    int width = diametro, height = diametro;

    super(x - width / 2, y - height / 2, width, height);

    this.id = id;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;
    this.current = current;
    self = this;

    /*
     * local = new JButton() {
     * 
     * @Override
     * protected void paintComponent(Graphics g) {
     * g.drawImage(
     * current.sheet,
     * 0, 0, (int) getWidth(), (int) getHeight(), // 0,0 not x,y
     * current.anime * current.sprite_col,
     * id * current.sprite_lin,
     * current.anime * current.sprite_col + current.sprite_col,
     * id * current.sprite_lin + current.sprite_lin,
     * null);
     * }
     * };
     * 
     * local.setOpaque(false);
     * local.setContentAreaFilled(false);
     * local.setBorderPainted(false);
     * local.setBounds(x - diametro / 2, y - diametro / 2, diametro, diametro);
     * position + size
     * current.add(local);
     */

  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getId() {
    return id;
  }

  public void verify(ball bola) {

    if ((intersects(bola.getX() - bola.getDiameter() / 2, bola.getY() - bola.getDiameter() / 2, bola.getDiameter(),
        bola.getDiameter())
        || contains(bola.getX(), bola.getY()))
        && (bateuX == false || bateuY == false)) {
      bateu = true;
      bola.bounce(this);
      bateuX = bola.bateuX;
      bateuY = bola.bateuY;
      game.pointSys.addPotentialPoints(this);

      if (id == ID_BALDE) {

        game.pointSys.processPoints();
        game.pointSys.removePotentialPoints();
        current.mode = game.GameModes.EDIT;
        if (game.pointSys.getPoints() < 1000 * number) {

          SwingUtilities.invokeLater(() -> current.createEnd());
          current.mode = game.GameModes.END;
          return;
        }

        number += 1.5;
        game.hitting = false;
        game.buffSys.EndBuffs();
        current.SetBallVelocity(0, 0);

        System.out.println("Bye World");

        if (current.mode == game.GameModes.EDIT)
          SwingUtilities.invokeLater(() -> game.dialog.dialog_init(2));

        // in coisa verify():

        return;
      } else if (id == ID_PLATAFORMA_CONGELADA) {
        game.gaming.buffSys.ApplyBuff(buffSystem.buffs.SLIPPERY, 1);
      }

      game.hitting = true;
    } else if (!(intersects(bola.getX() - bola.getDiameter() / 2.0, bola.getY() - bola.getDiameter() / 2,
        bola.getDiameter(),
        bola.getDiameter())
        || contains(bola.getX() - bola.getDiameter() / 2.0, bola.getY() - bola.getDiameter() / 2.0))) {
      bateu = false;
      if (bateuX) {
        bateuX = false;
      }
      if (bateuY) {
        bateuY = false;
      }
      game.add++;
    }
  }

  @Override
  public boolean contains(double x, double y) {
    double dx = x - this.x, dy = y - this.y;
    return dx * dx + dy * dy <= (width / 2) * (height / 2);
  }

  @Override
  public boolean contains(Point2D p) {
    return contains(p.getX(), p.getY());
  }

  @Override
  public boolean contains(double x, double y, double w, double h) {
    return contains(x, y) && contains(x + w, y) &&
        contains(x, y + h) && contains(x + w, y + h);
  }

  @Override
  public boolean contains(Rectangle2D r) {
    return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  @Override
  public boolean intersects(double x, double y, double w, double h) {
    // closest point on rect to circle center
    double nearX = Math.max(x, Math.min(this.x, x + w));
    double nearY = Math.max(y, Math.min(this.y, y + h));
    double dx = nearX - this.x;
    double dy = nearY - this.y;
    double r = height / 2.0;
    return dx * dx + dy * dy <= r * r;
  }

  @Override
  public boolean intersects(Rectangle2D rect) {
    return intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
  }

  @Override
  public Rectangle getBounds() {
    return getBounds2D().getBounds();
  }

  @Override
  public Rectangle2D getBounds2D() {
    return new Rectangle2D.Double(this.x, this.y, width, height);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at) {
    // Delegate to Ellipse2D, it already knows how to iterate a circle
    return new Ellipse2D.Double(this.x, this.y, width, height).getPathIterator(at);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return new FlatteningPathIterator(getPathIterator(at), flatness);
  }
}
