import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.math.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class Choice implements MouseListener {
  game current;
  JDialog dialog;
  boolean active;
  JPanel panel;

  private ArrayList<JButton> list = new ArrayList<JButton>();
  public BufferedImage sheet;
  int width = 100, height = 100, number_of_things = 5, pass = 4;
  static Timer timer;

  Choice(game current) {
    try {
      sheet = ImageIO.read(new File("spritesheet/New Piskel(3).png"));
      System.out.println("sheet loaded: " + sheet); // null or not?
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("sheet FAILED to load"); // path wrong?
    }

    this.current = current;
    this.active = false;
  }

  void dialog_init() {
    game.timer.stop();
    // game.healthSys.AddMaxHearts(1);

    list.clear();
    current.ball.setPosition(10 + (int) current.ball.getDiameter(),
        current.y_boundary - (int) current.ball.getDiameter());
    // Create panel FIRST
    panel = new JPanel() {
      Image img = new ImageIcon("Frat_God.png").getImage();

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        for (JButton c : list) {
          c.repaint();
        }
      }
    };
    panel.setOpaque(false);
    panel.setLayout(null);
    JButton local = new JButton() {
      @Override
      protected void paintComponent(Graphics g) {

        g.drawImage(sheet,
            0, 0, getWidth(), getHeight(),
            current.anime * width,
            4 * height,
            current.anime * width + width,
            4 * height + height,
            null);
      }
    };

    local.setBounds(100, 160, 100, 100);
    local.addMouseListener(this);
    local.setOpaque(false);
    local.setBorderPainted(false);
    list.add(local);
    panel.add(local);
    // THEN create and add buttons

    int l;
    do {
      l = (int) (Math.random() * number_of_things);
    } while (l != 1); //
    final int random_id = l;
    local = new JButton() {
      @Override
      protected void paintComponent(Graphics g) {
        g.drawImage(sheet,
            0, 0, getWidth(), getHeight(),
            current.anime * width,
            random_id * height,
            current.anime * width + width,
            random_id * height + height,
            null);
      }
    };
    local.setBounds(300, 160, 100, 100);
    local.addMouseListener(this);
    local.setOpaque(false);
    local.setBorderPainted(false);
    list.add(local);
    panel.add(local); // safe now

    dialog = new JDialog(current.frame, "Choose Your Item", true);
    dialog.setUndecorated(true);
    dialog.setBackground(new Color(225, 225, 225));
    dialog.setAlwaysOnTop(true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setSize(500, 300);
    dialog.setLocationRelativeTo(current.frame);
    dialog.setContentPane(panel);
    timer = new Timer(16, e -> {

      current.anime_help++;
      if (current.anime_help > 4) {
        current.anime_help = 0;
        current.anime++;
      }

      if (current.anime >= 15) {
        current.anime = 0;
      }
      panel.repaint();
    });

    timer.start();
    dialog.setVisible(true);
  }

  @Override
  public void mouseClicked(MouseEvent e) {

    dialog.dispose();
    game.mode = game.GameModes.EDIT;
    game.timer.start();
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
