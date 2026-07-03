
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.math.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.io.File;

public class inicial extends JPanel implements MouseListener, ActionListener {
  game game_sys;
  Image img;
  BufferedImage sprite;
  JButton start, settings;
  JPanel second = new JPanel(new GridBagLayout()), third = new JPanel(new BorderLayout());

  inicial(game game_sys) {
    setLayout(new BorderLayout());
    this.game_sys = game_sys;
    setPreferredSize(new Dimension(800, 600));

    img = new ImageIcon("inicial_screen.png").getImage();
    try {
      sprite = ImageIO.read(new File("spritesheet/Menu_Stuff(1).png"));
    } catch (Exception e) {
      System.out.println(e);
    }
    start = new JButton() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(sprite,
            0, 0, getWidth(), getHeight(),
            game_sys.anime % 4 * 32,
            0 * 32,
            game_sys.anime % 4 * 32 + 32,
            0 * 32 + 32,
            null);

      }
    };
    settings = new JButton() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(sprite,
            0, 0, getWidth(), getHeight(),
            game_sys.anime % 4 * 32,
            1 * 32,
            game_sys.anime % 4 * 32 + 32,
            1 * 32 + 32,
            null);

      }
    };
    start.setPreferredSize(new Dimension(200, 200));
    start.setContentAreaFilled(false);
    start.setBorderPainted(false);

    settings.setPreferredSize(new Dimension(200, 200));
    settings.setContentAreaFilled(false);
    settings.setBorderPainted(false);
    second.setOpaque(false);
    start.setOpaque(false);
    settings.setOpaque(false);
    start.repaint();
    settings.repaint();
    second.add(start);
    second.add(settings);

    add(second, BorderLayout.EAST);
    setFocusable(true);
    start.addMouseListener(this);
    settings.addMouseListener(this);
    setVisible(true);

    repaint();

  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(img, 0, 0, game_sys.frame.getWidth(), game_sys.frame.getHeight(), this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getSource() == start) {
      game_sys.frame.add(game_sys.gaming);
      game.game_start = true;
      game_sys.frame.remove(this);
      game_sys.frame.pack();
      game_sys.frame.revalidate(); // recalculates layout
      game_sys.frame.repaint(); // redraws
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
