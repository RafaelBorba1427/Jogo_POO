
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

// ------------------------------------------------------------
// MainMenu
// ------------------------------------------------------------

public class MainMenu extends JPanel implements ActionListener {

  private static final String BACKGROUND_PATH = "spritesheet/inicial_screen.png";

  // Icones do menu: 128x192 = 4 quadros de 32x32 por linha, 6 linhas.
  // Linha 0 = START, linha 1 = opcoes.
  private static final String MENU_ICONS_PATH = "spritesheet/Menu_Icons.png";
  private static final int MENU_ICON_SIZE = 32;
  private static final int MENU_ICON_FRAMES = 4;
  private static final int MENU_ICON_FPS = 8;
  private static final int MENU_PLAY_ROW = 0;
  private static final int MENU_OPTIONS_ROW = 1;

  // Sprites da bola: linhas 14 a 17 da combined_spritesheet.png, 16x16,
  // 15 quadros.
  private static final String SPRITESHEET_PATH = "spritesheet/combined_spritesheet.png";
  private static final int BALL_SPRITE_SIZE = 16;
  private static final int BALL_FRAMES = 15;
  private static final int BALL_FPS = 15;

  private Image background;

  private AnimationPlayer play_animation;
  private AnimationPlayer options_animation;

  // Uma animacao por sprite de bola, indexada igual a GameRules.BALL_IDS.
  private final AnimationPlayer[] ball_animations = new AnimationPlayer[GameRules.BALL_IDS.length];

  private JButton start, settings;
  private final JPanel button_column = new JPanel(new GridBagLayout());
  private final JFrame frame;

  // Painel de opcoes, guardado para o timer poder repinta-lo enquanto estiver
  // na tela (a previa da bola e animada).
  private JPanel settings_panel;

  // So repinta; os quadros em si andam no relogio do AnimationPlayer.
  private Timer icon_repaint_timer;

  MainMenu(JFrame frame) {
    this.frame = frame;

    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(Main.DEFAULT_RESOLUTION));

    background = loadBackground();

    play_animation = loadMenuIcon("menu_icon_play", MENU_PLAY_ROW);
    options_animation = loadMenuIcon("menu_icon_options", MENU_OPTIONS_ROW);

    for (int i = 0; i < GameRules.BALL_IDS.length; i++) {
      ball_animations[i] = loadBallIcon(GameRules.BALL_IDS[i]);
    }

    start = createMenuButton("PLAY", play_animation);
    settings = createMenuButton("OPTIONS", options_animation);

    start.addActionListener(this);
    settings.addActionListener(this);

    button_column.setOpaque(false);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.insets = new Insets(12, 24, 12, 24);
    constraints.gridy = 0;
    button_column.add(start, constraints);
    constraints.gridy = 1;
    button_column.add(settings, constraints);

    add(button_column, BorderLayout.EAST);

    setFocusable(true);
    setVisible(true);

    // ~60 FPS de repintura, folgado para nunca perder uma troca de quadro.
    icon_repaint_timer = new Timer(16, e -> {
      if (settings_panel != null && settings_panel.isShowing()) {
        settings_panel.repaint();
        return;
      }
      if (start != null)
        start.repaint();
      if (settings != null)
        settings.repaint();
    });
    icon_repaint_timer.start();
  }

  // Chamado pelo GameFlow quando o menu sai da tela, para nao deixar um Timer
  // repintando um painel que ninguem mais ve.
  public void dispose() {
    if (icon_repaint_timer != null) {
      icon_repaint_timer.stop();
      icon_repaint_timer = null;
    }
  }

  // ------------------------------------------------------------
  // Carregamento de arte, com degradacao suave
  // ------------------------------------------------------------

  private Image loadBackground() {
    File file = new File(BACKGROUND_PATH);
    if (!file.exists()) {
      System.out.println("MainMenu: fundo nao encontrado em " + BACKGROUND_PATH);
      return null;
    }
    return new ImageIcon(BACKGROUND_PATH).getImage();
  }

  // Cada icone precisa de uma CHAVE propria: o SpriteLoader guarda os quadros
  // fatiados por chave, entao duas linhas diferentes com a mesma chave
  // devolveriam a mesma animacao.
  private AnimationPlayer loadMenuIcon(String animation_key, int row) {
    try {
      return new AnimationPlayer(animation_key, MENU_ICONS_PATH,
          MENU_ICON_SIZE, MENU_ICON_SIZE, row, MENU_ICON_FRAMES, MENU_ICON_FPS);
    } catch (Exception e) {
      System.out.println("MainMenu: falha ao carregar o icone da linha " + row
          + " em " + MENU_ICONS_PATH + " (" + e.getMessage() + ")");
      return null;
    }
  }

  // A chave "objects1_<id>" e a MESMA que GameObject.createAnimationPlayer
  // usa para os objetos do jogo, de proposito: a previa do menu e o sprite da
  // bola em partida compartilham os quadros ja fatiados no SpriteLoader, em vez
  // de carregar a folha de novo.
  private AnimationPlayer loadBallIcon(int ball_id) {
    try {
      return new AnimationPlayer("objects1_" + ball_id, SPRITESHEET_PATH,
          BALL_SPRITE_SIZE, BALL_SPRITE_SIZE, ball_id, BALL_FRAMES, BALL_FPS);
    } catch (Exception e) {
      System.out.println("MainMenu: falha ao carregar o sprite da bola " + ball_id
          + " (" + e.getMessage() + ")");
      return null;
    }
  }

  // Botao que desenha um icone animado. Se o spritesheet nao carregar, cai para
  // um botao desenhado em codigo com o texto, para o menu continuar utilizavel.
  private JButton createMenuButton(String label, AnimationPlayer animation) {
    JButton button = new JButton(label) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (animation != null) {
          // Um leve crescimento no hover, para o botao responder ao mouse
          // sem precisar de outra linha de sprites.
          double scale = getModel().isRollover() ? 1.12 : 1.0;
          double width = getWidth() * scale;
          double height = getHeight() * scale;
          int offset_x = (int) ((getWidth() - width) / 2);
          int offset_y = (int) ((getHeight() - height) / 2);

          animation.paint(g2d, offset_x, offset_y, new Vector2D(width, height), 0.0);
          g2d.dispose();
          return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean hovered = getModel().isRollover();
        g2d.setColor(hovered ? new Color(200, 60, 40, 220) : new Color(30, 20, 20, 200));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

        g2d.setColor(new Color(240, 210, 140));
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

        g2d.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 26));
        FontMetrics fm = g2d.getFontMetrics();
        String text = getText();
        g2d.drawString(text,
            (getWidth() - fm.stringWidth(text)) / 2,
            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

        g2d.dispose();
      }
    };

    // Os icones sao 32x32; 160 e 5x, um multiplo inteiro, para o
    // nearest-neighbour nao deixar linhas de pixel de larguras diferentes.
    button.setPreferredSize(new Dimension(160, 160));
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setOpaque(false);
    button.setToolTipText(label);
    return button;
  }

  // ------------------------------------------------------------
  // Opcoes
  // ------------------------------------------------------------

  public void settingsPanel() {
    settings_panel = new JPanel(new BorderLayout()) {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null)
          g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
      }
    };

    settings_panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Cartao escuro por tras dos controles. Sem ele, os sliders e os rotulos
    // ficam por cima da arte do fundo (uma rua cheia de detalhe) e viram uma
    // sopa visual -- e o thumb branco do JSlider some contra as luzes.
    JPanel card = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(18, 12, 16, 220));
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        g2d.setColor(new Color(240, 210, 140, 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        g2d.dispose();
      }
    };
    card.setOpaque(false);
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

    JLabel title = new JLabel("OPTIONS");
    title.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 28));
    title.setForeground(new Color(250, 220, 150));
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    card.add(title);
    card.add(Box.createVerticalStrut(12));

    // A musica acompanha ao vivo: o clip que ja esta tocando muda de volume
    // enquanto o slider e arrastado.
    card.add(createVolumeRow("Music Volumes",
        MusicPlayer.getVolume(),
        (value, adjusting) -> MusicPlayer.updateVolume(value)));

    card.add(Box.createVerticalStrut(6));

    // Os efeitos so podem ser ouvidos disparando um. O som de amostra toca
    // apenas quando o slider e SOLTO, senao tocaria dezenas de vezes durante
    // o arraste.
    card.add(createVolumeRow("Effect Volume",
        SoundEffectPlayer.getVolume(),
        (value, adjusting) -> {
          SoundEffectPlayer.updateVolume(value);
          if (!adjusting)
            SoundEffectPlayer.playSound("bounce_realistic");
        }));

    card.add(Box.createVerticalStrut(6));
    card.add(createBallPickerRow());
    card.add(Box.createVerticalStrut(14));

    JButton back_button = new JButton("Return");
    back_button.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 16));
    back_button.setFocusPainted(false);
    back_button.addActionListener(e -> ApplySettingChanges(settings_panel));

    JPanel button_row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    button_row.setOpaque(false);
    button_row.setAlignmentX(Component.LEFT_ALIGNMENT);
    button_row.add(back_button);
    card.add(button_row);

    // Largura fixa para o cartao nao esticar de ponta a ponta da tela.
    Dimension card_size = new Dimension(520, card.getPreferredSize().height);
    card.setPreferredSize(card_size);
    card.setMaximumSize(card_size);

    // GridBagLayout sem restricoes centraliza o unico filho, na altura e na
    // largura, mantendo o tamanho preferido dele.
    JPanel center = new JPanel(new GridBagLayout());
    center.setOpaque(false);
    center.add(card);
    settings_panel.add(center, BorderLayout.CENTER);

    frame.remove(this);
    frame.add(settings_panel);
    frame.revalidate();
    frame.repaint();
  }

  // ------------------------------------------------------------
  // Escolha do sprite da bola
  // ------------------------------------------------------------

  // Uma fileira com as quatro bolas para escolher, mais uma previa maior da
  // escolhida ao lado. A escolha vale a partir da proxima partida, porque o
  // Game.startGame le GameRules.selected_ball_id na hora de criar a bola.
  private JPanel createBallPickerRow() {
    JPanel row = new JPanel(new BorderLayout(12, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.setBorder(titledBorder("Player options"));

    JPanel choices = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    choices.setOpaque(false);

    JLabel name_label = new JLabel();
    name_label.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 15));
    name_label.setForeground(new Color(245, 230, 200));
    name_label.setHorizontalAlignment(SwingConstants.CENTER);

    // Previa da bola escolhida, animada, do lado direito.
    JPanel preview = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2d.setColor(new Color(240, 210, 140));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        AnimationPlayer animation = ball_animations[GameRules.getSelectedBallIndex()];
        if (animation != null) {
          int size = Math.min(getWidth(), getHeight()) - 16;
          animation.paint(g2d, (getWidth() - size) / 2, (getHeight() - size) / 2,
              new Vector2D(size, size), 0.0);
        }
        g2d.dispose();
      }
    };
    preview.setOpaque(false);
    preview.setPreferredSize(new Dimension(88, 88));

    // Os botoes precisam ser conhecidos uns dos outros para o realce de
    // selecao acompanhar a troca, entao ficam num array.
    JButton[] buttons = new JButton[GameRules.BALL_IDS.length];

    for (int i = 0; i < GameRules.BALL_IDS.length; i++) {
      final int index = i;
      final AnimationPlayer animation = ball_animations[i];

      JButton choice = new JButton() {
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);

          boolean selected = GameRules.getSelectedBallIndex() == index;

          Graphics2D g2d = (Graphics2D) g.create();
          g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

          g2d.setColor(selected ? new Color(240, 190, 60, 150) : new Color(0, 0, 0, 90));
          g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

          if (selected || getModel().isRollover()) {
            g2d.setColor(selected ? new Color(255, 235, 150) : new Color(200, 200, 200, 160));
            g2d.setStroke(new BasicStroke(selected ? 3f : 2f));
            g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
          }

          g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

          if (animation != null) {
            int size = Math.min(getWidth(), getHeight()) - 12;
            animation.paint(g2d, (getWidth() - size) / 2, (getHeight() - size) / 2,
                new Vector2D(size, size), 0.0);
          } else {
            g2d.setColor(new Color(230, 120, 40));
            int size = Math.min(getWidth(), getHeight()) - 16;
            g2d.fillOval((getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
          }

          g2d.dispose();
        }
      };

      choice.setPreferredSize(new Dimension(56, 56));
      choice.setContentAreaFilled(false);
      choice.setBorderPainted(false);
      choice.setFocusPainted(false);
      choice.setOpaque(false);
      choice.setToolTipText(GameRules.BALL_NAMES[i]);

      choice.addActionListener(e -> {
        GameRules.selected_ball_id = GameRules.BALL_IDS[index];
        name_label.setText(GameRules.BALL_NAMES[index]);
        SoundEffectPlayer.playSound("napkin");
        for (JButton b : buttons) {
          if (b != null)
            b.repaint();
        }
        preview.repaint();
      });

      buttons[i] = choice;
      choices.add(choice);
    }

    name_label.setText(GameRules.BALL_NAMES[GameRules.getSelectedBallIndex()]);

    JPanel preview_column = new JPanel(new BorderLayout(0, 4));
    preview_column.setOpaque(false);
    preview_column.add(preview, BorderLayout.CENTER);
    preview_column.add(name_label, BorderLayout.SOUTH);

    row.add(choices, BorderLayout.CENTER);
    row.add(preview_column, BorderLayout.EAST);
    return row;
  }

  // ------------------------------------------------------------
  // Volume
  // ------------------------------------------------------------

  // Uma linha de volume: rotulo, slider de 0 a 100 e a porcentagem ao lado.
  private JPanel createVolumeRow(String label, float initial_volume, VolumeChange on_change) {
    JPanel row = new JPanel(new BorderLayout(10, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.setBorder(titledBorder(label));

    int initial_percent = Math.round(initial_volume * 100f);

    JSlider slider = new JSlider(0, 100, initial_percent);
    slider.setOpaque(false);
    slider.setMajorTickSpacing(25);
    slider.setPaintTicks(true);

    JLabel value_label = new JLabel(initial_percent + "%");
    value_label.setFont(new Font("TeX Gyre Bonum", Font.BOLD, 16));
    value_label.setForeground(new Color(245, 230, 200));
    value_label.setPreferredSize(new Dimension(55, 20));
    value_label.setHorizontalAlignment(SwingConstants.RIGHT);

    slider.addChangeListener(e -> {
      value_label.setText(slider.getValue() + "%");
      on_change.apply(slider.getValue() / 100f, slider.getValueIsAdjusting());
    });

    row.add(slider, BorderLayout.CENTER);
    row.add(value_label, BorderLayout.EAST);
    return row;
  }

  // Callback do slider. "adjusting" e true enquanto o jogador ainda esta
  // arrastando o controle e false no passo final, quando ele solta.
  private interface VolumeChange {
    void apply(float value, boolean adjusting);
  }

  private TitledBorder titledBorder(String label) {
    return BorderFactory.createTitledBorder(
        BorderFactory.createEmptyBorder(4, 4, 4, 4),
        label, TitledBorder.LEADING, TitledBorder.TOP,
        new Font("TeX Gyre Bonum", Font.BOLD, 16), new Color(245, 230, 200));
  }

  void ApplySettingChanges(JPanel panel) {
    frame.remove(panel);
    frame.add(this);
    frame.revalidate();
    frame.repaint();
    settings_panel = null;
    SwingUtilities.invokeLater(this::requestFocusInWindow);
  }

  // ------------------------------------------------------------
  // Render
  // ------------------------------------------------------------

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (background != null) {
      g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
      return;
    }

    // Fundo de emergencia, caso o PNG suma da pasta.
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setPaint(new GradientPaint(0, 0, new Color(28, 18, 30),
        0, getHeight(), new Color(80, 30, 40)));
    g2d.fillRect(0, 0, getWidth(), getHeight());
    g2d.dispose();
  }

  // ------------------------------------------------------------
  // Acoes
  // ------------------------------------------------------------

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == start) {
      GameFlow.startNewGame();
      return;
    }
    if (e.getSource() == settings) {
      settingsPanel();
    }
  }
}
