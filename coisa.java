import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.*;

public class coisa extends Rectangle {
  public int x, y, diametro;
  public boolean bateu = false;
  public boolean buff = false;
  int id;

  public coisa(int x, int y, int diametro, int id) {
    super(x - diametro / 2, y - diametro / 2, diametro, diametro);
    this.id = id;
    this.diametro = diametro;
    this.x = x;
    this.y = y;
  };

  public int getId() {
    return id;
  }

  public void verify(ball bola) {
    System.out.println("Works");

    if ((intersects(bola.getX() - bola.getDiameter() / 2.0, bola.getY() - bola.getDiameter() / 2, bola.getDiameter(),
        bola.getDiameter())
        || contains(bola.getX() - bola.getDiameter() / 2.0, bola.getY() - bola.getDiameter() / 2.0))
        && (bola.bateuX == false || bola.bateuY == false)) {

      bola.bounce(this);
      if (id == 1) {
        game.fecha = true;
      }
      game.hitting = true;
    } else {

      game.add++;

    }
  }

  @Override
  public boolean contains(double x, double y) {
    double dx = x - this.x, dy = y - this.y;
    return dx * dx + dy * dy <= (diametro / 2) * (diametro / 2);
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
    double r = diametro / 2.0;
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
    return new Rectangle2D.Double(this.x, this.y, diametro, diametro);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at) {
    // Delegate to Ellipse2D, it already knows how to iterate a circle
    return new Ellipse2D.Double(this.x, this.y, diametro, diametro).getPathIterator(at);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return new FlatteningPathIterator(getPathIterator(at), flatness);
  }
}
