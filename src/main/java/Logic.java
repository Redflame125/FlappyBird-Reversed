import static java.lang.Thread.sleep;


public class Logic {
    public static Logic instance;
    private final GameUI gameUI;
    public boolean gamePaused = false, rainbowMode = false, rainbowModeActive = false, developerMode = false, cheatsEnabled = false, gameState = false, gameOver = false;


    public Logic(GameUI gameUI) {
        this.gameUI = gameUI;
        instance = this;
    }

    // Handler für die Leertaste
    public void handleSpaceKeyPress(Utils utils, Movement movement, int JumpHeight, String flapSound, String music, boolean sound) {

        // Wenn das Spiel nicht läuft und nicht beendet ist
        if (!(gameUI.tickrate.isRunning() || gameState || gameOver)) {
            utils.audioPlayer(music, sound, true); // Musik abspielen
            gameUI.tickrate.start(); // Timer starten
            gameUI.gameOver.setVisible(false);
            gameState = true;
            gamePaused = false;
        }

        // Wenn das Spiel nicht läuft und beendet ist
        if (!gameUI.tickrate.isRunning() && !gameState && gameOver) {
            System.exit(0); // Programm beenden
            gameUI.dispose(); // Aktuelles Fenster schließen
        }

        // Wenn das Spiel nicht beendet ist, Sprung ausführen
        if (!gameOver) handleBounce(utils, movement, JumpHeight, flapSound, sound);
    }

    // Handler für den Timer Tick
    public void handleTimerTick(Utils utils, Movement movement, int height, String playerImage, String rainbowImage,
                                int percentage, int verticalGap, String obstacleTopImage, String obstacleBottomImage, String dieSound,
                                String hitSound, String pointSound, String rainbowSound, double Tickrate, boolean sound) {

        if (!gamePaused) {

            if (gameUI.player.getY() >= height && gameOver && !gameState) gameUI.tickrate.stop(); // Stop the timer
            movement.movePlayer(utils, Tickrate); // Move the player


            if (gameState && !gameOver) {
                movement.moveObstacles(utils, percentage, verticalGap, obstacleTopImage, obstacleBottomImage, Tickrate, gameUI.points); // Move the obstacles
                movement.moveBackground(utils, Tickrate); // Move the background

                gameUI.removeObstacles(); // Remove non-visible obstacles
                gameUI.checkCollision(utils, dieSound, hitSound, pointSound, rainbowSound, sound); // Check for collisions
                gameUI.checkRainbowMode(utils, playerImage, rainbowImage);
            }
        }
    }

    // Handler für die Kollision
    public void handleCollision(Utils utils, String dieSound, boolean sound) {
        utils.stopHeavyAudio();
        utils.audioPlayer(dieSound, sound, false);

        gameOver = true;
        gameState = false;

        gameUI.gameOver.setVisible(true);
    }

    // Handler für den Jump
    public void handleBounce(Utils utils, Movement movement, int JumpHeight, String flapSound, boolean sound) {
        utils.audioPlayer(flapSound, sound, false);
        if (gameUI.player.getY() > 32) movement.xPosition = - JumpHeight; // Spieler nach oben bewegen
    }

    // Handler für die Punkte
    public void handlePoint(Utils utils, String pointSound, String rainbowSound, boolean sound) {
        utils.audioPlayer(pointSound, sound, false);
        gameUI.points++;
        if (gameUI.points > 0 && gameUI.points % 5 == 0 && (int) (Math.random() * 6 + 1) == 3) handleRainbowMode(utils, rainbowSound, sound);
        gameUI.score.setText("Score: " + gameUI.points);
    }

    // Handler für die Pause
    public void handleGamePause() {
        if (gamePaused) {
            gameUI.pauseScreen.setVisible(false);
            gamePaused = false;
        } else {
            if (!rainbowMode && !gameOver) {
                gameUI.pauseScreen.setVisible(true);
                gamePaused = true;
            }
        }
    }

    // Handler für den Rainbow Mode
    private void handleRainbowMode(Utils utils, String rainbowSound, boolean sound) {
        Thread rainbow = new Thread(() -> {
            try {
                rainbowMode = true;
                utils.audioPlayer(rainbowSound, sound, false);
                sleep(7000);
                rainbowMode = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long startTime = System.currentTimeMillis();
        rainbow.start();
        if (System.currentTimeMillis() > startTime + 7000) {
            rainbowMode = false;
            rainbow.interrupt();
        }
    }
}