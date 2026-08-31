
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class MainMenu extends JPanel implements MouseListener, ActionListener {
  Image img;
  BufferedImage sprite;
  JButton start, settings;
  JPanel second = new JPanel(new GridBagLayout()), third = new JPanel(new BorderLayout());
  JFrame frame;

  MainMenu(JFrame frame) {
    int animation = 0;
    this.frame = frame;
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(400, 400));
    img = new ImageIcon("MainMenu_screen.png").getImage();
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
            animation % 4 * 32,
            0 * 32,
            animation % 4 * 32 + 32,
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
            animation % 4 * 32,
            1 * 32,
            animation % 4 * 32 + 32,
            1 * 32 + 32,
            null);

      }
    };
    start.setPreferredSize(new Dimension(200,
        200));
    start.setContentAreaFilled(false);
    start.setBorderPainted(false);

    settings.setPreferredSize(new Dimension(200,
        200));
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

    // starts menu music every time menu is instanciated
    // game.musicMaster.changeTrackAndPlay("menu");
  }

  public void settingsPanel() {
    JPanel settingsJPanel = new JPanel(new BorderLayout()) {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
      }
    },
        screenSizePanel = new JPanel(new BorderLayout()),
        ballColorPanel = new JPanel(new BorderLayout());
    screenSizePanel.setOpaque(false);
    ballColorPanel.setOpaque(false);

    // Add frame edges
    settingsJPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Panels stacked in rows vertically
    JPanel rowsContainer = new JPanel();
    rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
    rowsContainer.setOpaque(false);
    rowsContainer.add(screenSizePanel);
    rowsContainer.add(ballColorPanel);

    settingsJPanel.add(rowsContainer, BorderLayout.NORTH);

    // Apply/Close buttons
    JButton applyButton = new JButton("Apply");
    applyButton.addActionListener(e -> ApplySettingChanges(settingsJPanel));

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setOpaque(false);
    buttonPanel.add(applyButton);
    settingsJPanel.add(buttonPanel, BorderLayout.SOUTH);
    frame.remove(this);
    frame.add(settingsJPanel);
    frame.revalidate();
    frame.repaint();
  }

  void ApplySettingChanges(JPanel settingsJPanel) {
    frame.add(this);
    frame.remove(settingsJPanel);
    frame.revalidate();
    frame.repaint();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getSource() == start) {
      // game.musicMaster.changeTrackAndPlay("gameOverworld0");
      Game gamimg = new Game(400, 400);
      frame.add(gamimg);
      frame.remove(this);
      frame.revalidate();
      frame.repaint();
    }
    if (e.getSource() == settings) {

      settingsPanel();

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
