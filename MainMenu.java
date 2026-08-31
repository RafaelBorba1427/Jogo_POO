import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class MainMenu extends JPanel implements MouseListener, ActionListener {
  Main game_sys;
  Image img;
  BufferedImage sprite;
  MenuButton start, settings;
  JPanel second = new JPanel(new GridBagLayout()), third = new JPanel(new BorderLayout());

  MainMenu(Main game_sys) {
    setLayout(new BorderLayout());
    this.game_sys = game_sys;
    setPreferredSize(new Dimension(game.x_boundary,game.y_boundary));
    img = new ImageIcon("MainMenu_screen.png").getImage();
    
    start = new MenuButton();
    settings = new MenuButton();

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
        JPanel settingsJPanel = new JPanel(new BorderLayout()){
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
 
        // Screen size
        JLabel screenSizeLabel = new JLabel("Screen Size:");
        screenSizeLabel.setForeground(new Color(0,0,0));
        screenSizeLabel.setOpaque(true);
        screenSizeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5,5,5));

        Dimension[] resolutions = {new Dimension(800,600), new Dimension(1140,720), new Dimension(1280,
          800) ,new Dimension(1440,900), new Dimension(1920,1080)};

        ArrayList<String> valid_resolutions = new ArrayList<>();
        Dimension max_screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        for(int i=0; i<resolutions.length; i++){
          if(max_screen_size.width >= resolutions[i].width && max_screen_size.height >= resolutions[i].height){
            valid_resolutions.add(String.valueOf(resolutions[i].width)+"x"+String.valueOf(resolutions[i].height));
          }
        }
        String[] resolutions_strings = valid_resolutions.toArray(String[]::new);
        JComboBox<String> resolutionBox = new JComboBox<>(resolutions_strings);
        resolutionBox.setSelectedItem(game.selected_resolution);
 
        resolutionBox.addActionListener(e ->
            game.selected_resolution = (String) resolutionBox.getSelectedItem()
        );
 
        screenSizePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        screenSizePanel.add(screenSizeLabel, BorderLayout.WEST);
        screenSizePanel.add(resolutionBox, BorderLayout.EAST);
 
        // Ball color
        JLabel ballColorLabel = new JLabel("Ball Color:");
        ballColorLabel.setOpaque(true);
        ballColorLabel.setForeground(new Color(0,0,0));
        ballColorLabel.setBorder(BorderFactory.createEmptyBorder(5, 5,5,5));
        JButton colorButton = new JButton("Choose Color");
 
        colorButton.addActionListener(e -> {
        JColorChooser color_chooser = new JColorChooser(game.ball_color);

        // Panel that draws the ball color dynamically
        JPanel ballPreview = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int diameter = 60;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;

                g2.setColor(color_chooser.getColor());
                g2.fillOval(x, y, diameter, diameter);
                g2.setColor(Color.BLACK);
                g2.drawOval(x, y, diameter, diameter);
            }
        };
        ballPreview.setPreferredSize(new Dimension(150, 80));

        color_chooser.getSelectionModel().addChangeListener(ev -> ballPreview.repaint());
        color_chooser.setPreviewPanel(ballPreview);

        JDialog dialog = JColorChooser.createDialog(
                settingsJPanel,
                "Pick a Ball Color",
                true,
                color_chooser,
                ev -> {
                    game.ball_color = color_chooser.getColor();
                    colorButton.setBackground(game.ball_color);
                    colorButton.repaint();
                },
                null // Cancel button does nothing
        );
        dialog.setVisible(true);
    });

        ballColorPanel.add(ballColorLabel, BorderLayout.WEST);
        ballColorPanel.add(colorButton, BorderLayout.EAST);
 
        // Panels stacked in rows vertically
        JPanel rowsContainer = new JPanel();
        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setOpaque(false);
        rowsContainer.add(screenSizePanel);
        rowsContainer.add(ballColorPanel);
 
        settingsJPanel.add(rowsContainer, BorderLayout.NORTH);
 
        // Apply/Close buttons 
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e ->
            ApplySettingChanges(settingsJPanel)
        );
 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
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

class MenuButton extends JButton{
  AnimationMaster animation;
  BufferedImage sprite;

  int position_x, position_y;

  public MenuButton(){
    try {
      sprite = ImageIO.read(new File("spritesheet/Menu_Stuff(1).png"));
    } catch (Exception e) {
      System.out.println(e);
    }

    animation = new AnimationMaster(1, 2, null);
  }

  @Override
  public void paintComponent(Graphics g){
    super.paintComponent(g);
    
    animation.paint(g, position_x, position_y);
  }
}