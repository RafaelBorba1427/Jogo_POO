
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.math.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;

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
    setPreferredSize(new Dimension(game.x_boundary,game.y_boundary));
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
    start.setPreferredSize(new Dimension(game.rescaleX(200), game.rescaleY(200)));
    start.setContentAreaFilled(false);
    start.setBorderPainted(false);

    settings.setPreferredSize(new Dimension(game.rescaleX(200),game.rescaleY(200)));
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
    game.musicMaster.changeTrackAndPlay("menu");
  }

  public void settingsPanel() {
        JPanel settingsJPanel = new JPanel(new BorderLayout()),
               screenSizePanel = new JPanel(new BorderLayout()),
               ballColorPanel = new JPanel(new BorderLayout());
 
        // Give the whole panel some breathing room from the frame edges
        settingsJPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
 
        // Screen size
        JLabel screenSizeLabel = new JLabel("Screen Size:");
        String[] resolutions = {"800x600", "1140x720", "1280x800" ,"1440x900", "1920x1080"};
        JComboBox<String> resolutionBox = new JComboBox<>(resolutions);
        resolutionBox.setSelectedItem(game.selected_resolution);
 
        resolutionBox.addActionListener(e ->
            game.selected_resolution = (String) resolutionBox.getSelectedItem()
        );
 
        screenSizePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        screenSizePanel.add(screenSizeLabel, BorderLayout.WEST);
        screenSizePanel.add(resolutionBox, BorderLayout.EAST);
 
        // Ball color
        JLabel ballColorLabel = new JLabel("Ball Color:");
        JButton colorButton = new JButton("Choose Color");
 
        colorButton.addActionListener(e -> {
            Color picked = JColorChooser.showDialog(
                    settingsJPanel, "Pick a Ball Color", game.ball_color);
            if (picked != null) {
                game.ball_color = picked;
                colorButton.setBackground(game.ball_color);
            }
        });
 
        ballColorPanel.add(ballColorLabel, BorderLayout.WEST);
        ballColorPanel.add(colorButton, BorderLayout.EAST);
 
        // Panels stacked in rows vertically
        JPanel rowsContainer = new JPanel();
        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.add(screenSizePanel);
        rowsContainer.add(ballColorPanel);
 
        settingsJPanel.add(rowsContainer, BorderLayout.NORTH);
 
        // Apply/Close buttons 
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e ->
            ApplySettingChanges(settingsJPanel)
        );
 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(applyButton);
        settingsJPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        settingsJPanel.setPreferredSize(new Dimension(game.x_boundary,game.y_boundary));
        settingsJPanel.setVisible(true);
        game_sys.frame.remove(this);
        game_sys.frame.add(settingsJPanel);
    }

    void ApplySettingChanges(JPanel settingsJPanel){
      System.out.println("Applied: Resolution = " + game.selected_resolution + ", color = " + game.ball_color);
      game_sys.frame.remove(settingsJPanel);
      game.updateResolution();
      game_sys.frame.add(this);
      game_sys.frame.pack();
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
      game.musicMaster.changeTrackAndPlay("gameOverworld0");
      
      game_sys.frame.add(game_sys.gaming);
      game.game_start = true;
      game_sys.frame.remove(this);
      game_sys.frame.revalidate(); // recalculates layout
      game_sys.frame.repaint(); // redraws
      game_sys.frame.pack();
    }
    if(e.getSource() == settings) {
      settingsPanel();
      game_sys.frame.revalidate(); // recalculates layout
      game_sys.frame.repaint(); // redraws
      game_sys.frame.pack();
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
