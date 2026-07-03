import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Choices implements MouseListener {
  game current;
  JDialog dialog;
  boolean active;
  JPanel panel;
  int numero = 2, spriteWidth = 100, spriteHeight = 100, quantidade = 5, seta = 3;
  private Map<JButton, Integer> list = new HashMap<>();
  public BufferedImage sheet;

  Choices(game current, int numero) {
    this.current = current;
    this.active = false;
    this.numero = numero;
  }

  void dialog_init(int numero, String back) {
    game.timer.stop();
    System.out.println("Hello World");
    this.numero = numero;
    list.clear();
    current.ball.setPosition(10 + (int) current.ball.getDiameter(),
        current.y_boundary - (int) current.ball.getDiameter());

    try {
      sheet = ImageIO.read(new File("spritesheet/New Piskel(2)(1).png"));
      System.out.println("sheet loaded: " + sheet);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("sheet FAILED to load");
    }

    // Create panel FIRST

    panel = new JPanel() {
      Image img = new ImageIcon(back).getImage();

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
        g.drawImage(sheet,
            0, 0, getWidth(), getHeight(),
            current.anime * spriteWidth,
            seta * spriteHeight,
            current.anime * spriteWidth + spriteWidth,
            seta * spriteHeight + spriteHeight,
            null);
      }
    };

    local.setBounds(10 + 1 * 70, 80, 60, 60);
    local.addMouseListener(this);
    list.put(local, 3);
    panel.add(local); // safe now

    int l;
    do {
      l = (int) (Math.random() * 5);
    } while (l == 3); //
    final int random_id = l;

    local = new JButton() {
      @Override
      protected void paintComponent(Graphics g) {
        g.drawImage(sheet,
            0, 0, getWidth(), getHeight(),
            current.anime * spriteWidth,
            random_id * spriteHeight,
            current.anime * spriteWidth + spriteWidth,
            random_id * spriteHeight + spriteHeight,
            null);
      }
    };
    local.setBounds(10 + 1 * 70, 80, 60, 60);
    local.addMouseListener(this);
    list.put(local, random_id);
    panel.add(local); // safe now

    dialog = new JDialog(current.frame, "Make a Choice", true);
    dialog.setUndecorated(true);
    dialog.setBackground(new Color(0, 0, 0));
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
    if (chosen > 5) {
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
