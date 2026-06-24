import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Items implements MouseListener {
  game current;
  JDialog dialog;
  boolean active;
  JPanel panel;
  int numero = 2;
  private Map<JButton, Integer> list = new HashMap<>();
  private Map<JButton, Integer> list = new HashMap<>();

  Items(game current, int numero) {
    this.current = current;
    this.active = false;
    this.numero = numero;
  }

  void dialog_init(int numero) {
    game.timer.stop();
    this.numero = numero; // reset here
    list.clear();
    current.ball.setPosition(10 + (int) current.ball.getDiameter(),
        current.y_boundary - (int) current.ball.getDiameter());
    // Create panel FIRST
    panel = new JPanel() {
      Image img = new ImageIcon("Dialog.png").getImage();

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
      }
    };
    panel.setOpaque(false);
    panel.setLayout(null);
    JButton local = new JButton() {
      @Override
      protected void paintComponent(Graphics g) {
        g.drawImage(current.sheet,
            0, 0, getWidth(), getHeight(),
            current.anime * current.sprite_col,
            1 * current.sprite_lin,
            current.anime * current.sprite_col + current.sprite_col,
            1 * current.sprite_lin + current.sprite_lin,
            null);
      }
    };
    local.setBounds(10 + 0 * 70, 80, 60, 60);
    local.addMouseListener(this);
    list.put(local, 1);
    panel.add(local);
    // THEN create and add buttons
    for (int i = 1; i < 4; i++) {
      int l;
      do {
        l = (int) (Math.random() * coisa.Quant_IDs);
      } while (l == coisa.ID_BALDE || l == coisa.ID_ESTILINGUE); //
      final int random_id = l;
      local = new JButton() {
        @Override
        protected void paintComponent(Graphics g) {
          g.drawImage(current.sheet,
              0, 0, getWidth(), getHeight(),
              current.anime * current.sprite_col,
              random_id * current.sprite_lin,
              current.anime * current.sprite_col + current.sprite_col,
              random_id * current.sprite_lin + current.sprite_lin,
              null);
        }
      };
      local.setBounds(10 + i * 70, 80, 60, 60);
      local.addMouseListener(this);
      list.put(local, random_id);
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
      if (game.collided.isEmpty()) {
        System.out.println("NO DICE");
      } else {
        Iterator<buff> it = game.collided.iterator();
        while (it.hasNext()) {
          game.lvl_map.add(it.next());
          it.remove();
        }

      }
      dialog.dispose();
      game.mode = game.GameModes.EDIT;
      game.timer.start();
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
