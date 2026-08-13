import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.*;

public class end extends JDialog {
  int points;
  int spriteWidth = 100;
  int spriteHeight = 100;
  int col = 0;
  int row = 0;
  int contador = 0;
  public JPanel panel;
  private BufferedImage cachedBackground;
  private JLabel label;

  end(int points, game current) {
    super(current.frame, "You Lose", false);
    this.points = points;

    panel = new JPanel() {
      Image img = new ImageIcon("spritesheet/done.png").getImage();

      @Override
      protected void paintComponent(Graphics g) {
        // build background cache once
        if (cachedBackground == null ||
            cachedBackground.getWidth() != getWidth() ||
            cachedBackground.getHeight() != getHeight()) {
          cachedBackground = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
          Graphics2D bg = cachedBackground.createGraphics();
          bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
          bg.setColor(new Color(139, 0, 0));
          bg.fillRect(0, 0, getWidth(), getHeight());
          bg.dispose();
        }

        // draw cached background — no flicker
        g.drawImage(cachedBackground, 0, 0, this);

        // draw sprite on top
        int drawWidth = getWidth() / 2;
        int drawHeight = getHeight() / 2;
        int centeredX = (getWidth() - drawWidth) / 2;
        int centeredY = (getHeight() - drawHeight) / 2;
        g.drawImage(img,
            centeredX, centeredY, centeredX + drawWidth, centeredY + drawHeight,
            col * spriteWidth, row * spriteHeight,
            col * spriteWidth + spriteWidth, row * spriteHeight + spriteHeight,
            this);
      }
    };
    panel.setOpaque(true);
    panel.setOpaque(true);
    panel.setLayout(new BorderLayout());
    label = new JLabel("Current points: " + game.pointSys.getPoints().toString());
    label.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 22));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setBackground(new Color(0, 0, 0, 0));
    label.setForeground(Color.black);
    panel.add(label, BorderLayout.SOUTH);
    setUndecorated(true);

    setAlwaysOnTop(true);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setSize(current.frame.getWidth(), current.frame.getHeight());
    setLocationRelativeTo(current.frame);
    setLayout(new BorderLayout());
    setContentPane(panel);

    add(label, BorderLayout.SOUTH);
    setVisible(true);
  }

  public void update_panel() {
    contador++;
    if (contador <= 40) {
      col = 0;
    } else if (contador <= 80) {
      col = 1;
    } else {
      contador = 0;
    }
    panel.repaint();
  }
}
