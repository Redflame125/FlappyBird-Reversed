import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

public class GameUI extends JFrame {
    public static GameUI instance;
    public final ArrayList<JLabel> obstacles = new ArrayList<>();
    public final ArrayList<Rectangle> rObstacles = new ArrayList<>();
    public final ArrayList<Rectangle> greenZones = new ArrayList<>();
    public final Timer tickrate;
    public final JLabel player, score, gameOver, pauseScreen;
    public final Rectangle rPlayer;
    public final JPanel mainPanel;
    private final Logic logic;
    private final ArrayList<Integer> userInput = new ArrayList<>();
    private final int[] KONAMI_CODE = { KeyEvent.VK_UP, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_DOWN,
            KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_B, KeyEvent.VK_A };
    public int points;

    // Konstruktor
    public GameUI(Utils utils, Movement movement, int width, int height, String title, String icon, boolean resizable, String backgroundImage, String playerImage, String rainbowImage, int JumpHeight, int percentage, int verticalGap, String obstacleTopImage, String obstacleBottomImage, String gameOverImage, String pauseScreenImage, String dieSound, String flapSound, String hitSound, String pointSound, String rainbowSound, String music, double Tickrate, boolean sound) {
        instance = this;
        logic = new Logic(this);

        movement.init();

        // Initialisiere das Fenster
        setTitle(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocation(utils.centerFrame(this));
        setResizable(resizable);
        setIconImage((utils.reader(icon)));
        setVisible(true);

        // Initialisiere das Config-Panel mit Hintergrund
        final BufferedImage background = utils.reader(backgroundImage);
        final BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        int imageWidth = background.getWidth();
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;

                // Hintergründe zeichnen auf den Buffer
                Graphics2D bufferGraphics = buffer.createGraphics();
                bufferGraphics.setBackground(new Color(0, 0, 0, 0)); // Transparenter Hintergrund
                bufferGraphics.clearRect(0, 0, getWidth(), getHeight());

                int firstX = movement.backgroundResetX % imageWidth;

                if (firstX > 0) {
                    bufferGraphics.drawImage(background, firstX - imageWidth, 0, this);
                }

                for (int x = firstX; x < getWidth() + imageWidth; x += imageWidth) {
                    bufferGraphics.drawImage(background, x, 0, this);
                }

                // Buffer auf das Panel zeichnen
                g2d.drawImage(buffer, 0, 0, this);
            }
        };

        mainPanel.setSize(getWidth(), getHeight());
        mainPanel.setLayout(null);
        mainPanel.setOpaque(false);
        add(mainPanel);

        // Initialisiere den Spieler
        player = new JLabel();
        final ImageIcon playerIcon = utils.createImageIcon(playerImage);
        player.setSize(playerIcon.getIconWidth(), playerIcon.getIconHeight());
        player.setLocation(utils.xPlayerPosition(mainPanel), getHeight() / 2);
        player.setIcon(playerIcon);
        player.setBounds(player.getX(), player.getY(), playerIcon.getIconWidth(), playerIcon.getIconHeight());
        rPlayer = new Rectangle(player.getBounds());
        mainPanel.add(player);

        // Initialisiere die Punkteanzeige
        score = new JLabel();
        int y = getHeight() / 20, x = y * 3;
        score.setSize(x, y);
        score.setLocation(getWidth() - 10 - x, 10);
        score.setFont(new Font("Arial", Font.BOLD, 18));
        score.setForeground(Color.YELLOW);
        score.setText("Score: " + points);
        mainPanel.add(score);

        // Initialisiere das Game Over Bild
        gameOver = new JLabel();
        gameOver.setSize(getWidth(), getHeight());
        gameOver.setLocation(utils.locatePoint(gameOverImage, getWidth(), getHeight()));
        gameOver.setIcon(utils.createImageIcon((gameOverImage)));
        gameOver.setVisible(false);
        mainPanel.add(gameOver);

        // Initialisiere das Pause-Bild
        pauseScreen = new JLabel();
        pauseScreen.setVisible(false);
        ImageIcon pauseScreenIcon = utils.createImageIcon(pauseScreenImage);
        pauseScreen.setSize(pauseScreenIcon.getIconWidth(), pauseScreenIcon.getIconHeight());
        pauseScreen.setLocation(utils.locatePoint(pauseScreenImage, getWidth(), getHeight()));
        pauseScreen.setIcon(pauseScreenIcon);
        mainPanel.add(pauseScreen);

        // Initialisiere den Timer
        tickrate = new Timer((int) Math.round(1000/Tickrate), e -> {
            if (System.getProperty("os.name").equals("linux")) Toolkit.getDefaultToolkit().sync();
            logic.handleTimerTick(utils, movement, height, playerImage, rainbowImage, percentage, verticalGap, obstacleTopImage, obstacleBottomImage, dieSound, hitSound, pointSound, rainbowSound,Tickrate, sound);
            if (logic.developerMode) System.out.println(utils.calculateSystemLatency());
        });

        // Initialisiere die Steuerung
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                // Steuerung
                if (e.getKeyCode() == KeyEvent.VK_SPACE) logic.handleSpaceKeyPress(utils, movement, JumpHeight, flapSound, music, sound);
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) logic.handleGamePause();


                // Konami-Code
                userInput.add(e.getKeyCode());

                // Die Eingabe begrenzen, um Speicherplatz zu sparen
                if (userInput.size() > KONAMI_CODE.length) userInput.remove(0); // Die Eingabe begrenzen, um Speicherplatz zu sparen

                // Prüfen, ob der Konami-Code eingegeben wurde
                if (userInput.size() == KONAMI_CODE.length) {
                    boolean konamiCodeEntered = true;
                    for (int i = 0; i < KONAMI_CODE.length; i++) {
                        if (userInput.get(i) != KONAMI_CODE[i]) {
                            konamiCodeEntered = false;
                            break;
                        }
                    }

                    // Wenn der Konami-Code eingegeben wurde, den Entwickler-Modus umschalten
                    if (konamiCodeEntered) {
                        logic.developerMode = !logic.developerMode;
                        logic.cheatsEnabled = true;
                        System.out.println("Developer-Modus umgeschaltet: " + logic.developerMode);
                        userInput.clear();
                    }
                }
            }
        });

        // Initialisiere die Maussteuerung
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                logic.handleSpaceKeyPress(utils, movement, JumpHeight, flapSound, music, sound);
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

    }

    // Erzeugt Hindernisse basierend auf den übergebenen Parametern
    public void generateObstacles(Utils utils, int percentage, int verticalGap, String obstacleTopImage, String obstacleBottomImage) {

        int minY = ((getHeight() * percentage) / 100);
        int maxY = getHeight() - ((getHeight() * percentage) / 100);

        JLabel obstacleTop = new JLabel(), obstacleBottom = new JLabel();
        mainPanel.add(obstacleTop);
        mainPanel.add(obstacleBottom);

        ImageIcon obstacleTopIcon = utils.createImageIcon((obstacleTopImage));
        ImageIcon obstacleBottomIcon = utils.createImageIcon((obstacleBottomImage));

        obstacleTop.setIcon(obstacleTopIcon);
        obstacleBottom.setIcon(obstacleBottomIcon);

        int obstacleWidth = obstacleTopIcon.getIconWidth(), obstacleHeight = obstacleTopIcon.getIconHeight();
        int yTop = (int) (Math.random() * (maxY - minY + 1) + minY) - obstacleHeight;
        int yBottom = yTop + verticalGap + obstacleHeight;

        obstacleTop.setSize(obstacleWidth, obstacleHeight);
        obstacleTop.setBounds(getWidth(), yTop, obstacleWidth, obstacleHeight);
        obstacleTop.setLocation(getWidth(), yTop);

        obstacleBottom.setSize(obstacleWidth, obstacleHeight);
        obstacleBottom.setBounds(getWidth(), yBottom, obstacleWidth, obstacleHeight);
        obstacleBottom.setLocation(getWidth(), yBottom);

        obstacles.add(obstacleTop);
        obstacles.add(obstacleBottom);

        Rectangle rObstacleTop = new Rectangle(obstacleTop.getBounds());
        Rectangle rObstacleBottom = new Rectangle(obstacleBottom.getBounds());

        rObstacleTop.setBounds(obstacleTop.getBounds());
        rObstacleBottom.setBounds(obstacleBottom.getBounds());

        rObstacles.add(rObstacleTop);
        rObstacles.add(rObstacleBottom);

        Rectangle rectangleBetweenObstacles = new Rectangle(
                obstacleTop.getX() + obstacleWidth,
                obstacleTop.getY() + obstacleHeight,
                obstacleWidth,
                yBottom - (yTop + obstacleHeight)
        );

        greenZones.add(rectangleBetweenObstacles);
    }

    // Entfernt Hindernisse, die außerhalb des Sichtfelds liegen
    public void removeObstacles() {
        Iterator<JLabel> iteratorObstacles = obstacles.iterator();
        while (iteratorObstacles.hasNext()) {
            JLabel component = iteratorObstacles.next();
            int x = component.getX();
            if (x < -64) {
                mainPanel.remove(component);
                iteratorObstacles.remove();
            }
        }

        Iterator<Rectangle> iteratorRectangles = rObstacles.iterator();
        while (iteratorRectangles.hasNext()) {
            Rectangle component = iteratorRectangles.next();
            int x = (int) component.getX();
            if (x < -64) {
                iteratorRectangles.remove();
            }
        }
    }

    // Überprüft Kollisionen mit dem Spieler und anderen Objekten
    public void checkCollision(Utils utils, String dieSound, String hitSound, String pointSound, String rainbowSound, boolean sound) {
        if (!logic.developerMode) {
            if (player.getY() > getWidth()) logic.handleCollision(utils, dieSound, sound);

            for (Rectangle component : rObstacles) {
                if (component != null) {
                    if (rPlayer.intersects(component) && !logic.rainbowMode) {
                        utils.audioPlayer(hitSound, sound, false);
                        logic.handleCollision(utils, dieSound, sound);
                    }
                }
            }
        }

        for (int i = 0; i < greenZones.size(); i++) {
            Rectangle component = greenZones.get(i);
            if (component != null && rPlayer.intersects(component)) {
                logic.handlePoint(utils, pointSound, rainbowSound, sound);
                greenZones.remove(i);
                i--;
            }
        }
    }

    // Überprüft, ob der Spieler sich im Regenbogen-Modus befindet
    public void checkRainbowMode(Utils utils, String playerImage, String rainbowImage) {
        if (logic.rainbowMode && !logic.rainbowModeActive) {
            player.setIcon(utils.createImageIcon((rainbowImage)));
            logic.rainbowModeActive = true;
            } else if (!logic.rainbowMode && logic.rainbowModeActive){
                player.setIcon(utils.createImageIcon((playerImage)));
                logic.rainbowModeActive = false;
        }
    }
}