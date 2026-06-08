import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class Items implements MouseListener {
  game current;
  JDialog dialog;
  boolean active;
  JPanel panel;
  int numero = 2;
  private Map<JButton, coisa> list = new HashMap<>();

  Items(game current) {
    this.current = current;
    this.active = false;
  }

  void dialog_init() {
    active = true;
    list.clear();

    for (int i = 0; i < 4; i++) {
      int r;
      do {
        r = (int) (Math.random() * 7);
      } while (r == 0 || r == 3 || r == 4);

      coisa c;
      if (r < 3) {
        c = new coisa(60 * i + 40, 80, 40, r, current);
      } else {
        c = new buff(60 * i + 40, 80, 40, buffSystem.returnBuff(r), r, current);
      }
      list.put(c.local, c);
      c.local.addMouseListener(this);
    }

    panel = new JPanel() {
      Image img = new ImageIcon("Dialog.png").getImage();

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this); // ✅ background only
      }
    };
    panel.setOpaque(false);
    panel.setLayout(null); // ✅ required for setBounds

    for (JButton btn : list.keySet()) {
      panel.add(btn); // ✅ add buttons here, not in paintComponent
    }

    dialog = new JDialog(current.frame, "Choose Your Item", true);
    dialog.setUndecorated(true);
    dialog.setBackground(new Color(0, 0, 0, 0));
    dialog.setAlwaysOnTop(true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setSize(300, 200);
    dialog.setLocationRelativeTo(current.frame);
    dialog.setContentPane(panel);
    dialog.setVisible(true);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    JButton clicked = (JButton) e.getSource();
    coisa chosen = list.get(clicked);
    if (chosen != null) {
      current.list.add(chosen);
      panel.remove(clicked);
      list.remove(clicked);
      panel.repaint();
      numero--;
      if (numero == 0) {
        dialog.dispose();
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
}
