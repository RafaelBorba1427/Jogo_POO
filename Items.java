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
  private Map<JButton, Integer> list = new HashMap<>();

  Items(game current) {
    this.current = current;
    this.active = false;
  }

  void dialog_init() {
    numero = 2; // reset here
    list.clear();
    current.ball.setPosition(0, 0);
    // Create panel FIRST
    panel = new JPanel() {
      Image img = new ImageIcon("Dialog.png").getImage();

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
      }
    };
    panel.setOpaque(false);
    panel.setLayout(null);

    // THEN create and add buttons
    for (int i = 0; i < 4; i++) {
      int l;
      do {
        l = (int) (Math.random() * 7);
      } while (l == 0 || l == 3 || l == 4);
      final int r = l;
      JButton local = new JButton() {
        @Override
        protected void paintComponent(Graphics g) {
          g.drawImage(current.sheet,
              0, 0, getWidth(), getHeight(),
              current.anime * current.sprite_col,
              r * current.sprite_lin,
              current.anime * current.sprite_col + current.sprite_col,
              r * current.sprite_lin + current.sprite_lin,
              null);
        }
      };
      local.setBounds(10 + i * 70, 80, 60, 60);
      local.addMouseListener(this);
      list.put(local, r);
      panel.add(local); // safe now
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
    int chosen = list.get(clicked);
    if (chosen > 4) {
      current.list.add(new buff(0, 0, 40, current.buffSys.returnBuff(chosen), chosen, current));

    } else {
      current.list.add(new coisa(0, 0, 40, chosen, current));

    }
    panel.remove(clicked);
    list.remove(clicked);
    panel.repaint();
    numero--;
    if (numero == 0) {
      dialog.dispose();
      game.mode = game.GameModes.EDIT;
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
