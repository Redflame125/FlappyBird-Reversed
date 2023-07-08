import java.util.Objects;

public class Config {
    // Alle Variablen und Assets für die Spielkonfiguration
    private final String Title; // Titel des Spiels
    private final String Background; // Dateipfad für den Hintergrund
    private final String Player ; // Dateipfad für das Spielerbild
    private final String Rainbow; // Dateipfad für das Regenbogenbild
    private final String ObstacleTop; // Dateipfad für das Hindernis von oben
    private final String ObstacleBottom; // Dateipfad für das Hindernis von unten
    private final String Icon; // Dateipfad für das Spielsymbol
    private final String GameOver; // Dateipfad für das Game Over-Bild
    private final String Pause; // Dateipfad für das Pause-Bild
    private final String dieSound; // Dateipfad für den Sterbesound
    private final String flapSound; // Dateipfad für den Flügelschlag-Sound
    private final String hitSound; // Dateipfad für den Aufprall-Sound
    private final String pointSound; // Dateipfad für den Punkte-Sound
    private final String RainbowSound; // Dateipfad für den Regenbogen-Sound
    private final String Music; // Dateipfad für die Hintergrundmusik
    private final int WindowSizeX; // Fensterbreite
    private final int WindowSizeY; // Fensterhöhe
    private final boolean Resizeable; // Gibt an, ob das Fenster in der Größe verändert werden kann
    private final int Percentage; // Prozentzahl, die die Größe des Hindernisses von der Fensterhöhe ausmacht
    private final int Gap; // Vertikaler Abstand zwischen den Hindernissen

    // Konstruktor zum Initialisieren der Variablen
    public Config(Utils utils, int JumpHeight, int Percentage, int Gap, int TPS, String title, int windowSizeX, int windowSizeY, boolean resizeable, String background, String player, String rainbow, String obstacleTop, String obstacleBottom, String icon, String gameOver, String pause, String dieSound, String flapSound, String hitSound, String pointSound, String rainbowSound, String music) {
        this.Gap = Gap;
        this.Percentage = Percentage;
        Title = title;
        WindowSizeX = windowSizeX;
        WindowSizeY = windowSizeY;
        Resizeable = resizeable;

        if (Objects.equals(background, "")) background = "error/empty.png";
        if (Objects.equals(player, "")) player = "error/empty.png";
        if (Objects.equals(rainbow, "")) rainbow = player;
        if (Objects.equals(obstacleTop, "")) obstacleTop = "error/empty.png";
        if (Objects.equals(obstacleBottom, "")) obstacleBottom = "error/empty.png";
        if (Objects.equals(icon, "")) icon = "error/empty.png";
        if (Objects.equals(gameOver, "")) gameOver = "error/empty.png";
        if (Objects.equals(pause, "")) pause = "error/empty.png";
        if (Objects.equals(dieSound, "")) dieSound = "error/empty.wav";
        if (Objects.equals(flapSound, "")) flapSound = "error/empty.wav";
        if (Objects.equals(hitSound, "")) hitSound = "error/empty.wav";
        if (Objects.equals(pointSound, "")) pointSound = "error/empty.wav";
        if (Objects.equals(rainbowSound, "")) rainbowSound = "error/empty.wav";
        if (Objects.equals(music, "")) music = "error/empty.wav";

        Background = background;
        Player = player;
        Rainbow = rainbow;
        ObstacleTop = obstacleTop;
        ObstacleBottom = obstacleBottom;
        Icon = icon;
        GameOver = gameOver;
        Pause = pause;
        this.dieSound = dieSound;
        this.flapSound = flapSound;
        this.hitSound = hitSound;
        this.pointSound = pointSound;
        RainbowSound = rainbowSound;
        Music = music;

        // Starte die Bewegungslogik mit den angegebenen Parametern
        new Movement(utils, Background, JumpHeight, TPS, true, this);
    }

    // Methode zum Starten des Spiels
    public void run(Utils utils, Movement movement, int JumpHeight, double Tickrate, boolean sound) {

        // Starte die Spiellogik mit den angegebenen Parametern
        new GameUI(
                    utils,
                    movement,
                    WindowSizeX,
                    WindowSizeY,
                    Title,
                    Icon,
                    Resizeable,
                    Background,
                    Player,
                    Rainbow,
                    JumpHeight,
                    Percentage,
                    Gap,
                    ObstacleTop,
                    ObstacleBottom,
                    GameOver,
                    Pause,
                    dieSound,
                    flapSound,
                    hitSound,
                    pointSound,
                    RainbowSound,
                    Music,
                    Tickrate,
                    sound
        );
    }
}