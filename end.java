import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class end extends JDialog {
  int points;

  end(int points, game current) {
    super(current.frame, "You Lose", true);
    this.points = points;

    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(new Color(139, 0, 0));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
      }
    };
    panel.setOpaque(false);
    panel.setLayout(null);

    setUndecorated(true);
    setBackground(new Color(0, 0, 0, 0));
    setAlwaysOnTop(true);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setSize(current.frame.getWidth(), current.frame.getHeight());
    setLocationRelativeTo(current.frame);
    setContentPane(panel);
    setVisible(true);
  }
}
