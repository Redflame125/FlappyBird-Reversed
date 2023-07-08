import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// Klasse für alle Utensiien
public class Utils {
    private final HashMap<String, Clip> HeavyClipCache = new HashMap<>(); // Cache für AudioClips
    private final HashMap<String, BufferedImage> bufferedImageCache = new HashMap<>(); // Cache für BufferedImages
    private final HashMap<String, ImageIcon> imageIconCache = new HashMap<>(); // Cache für ImageIcons
    private final ArrayList<BufferedInputStream> HeavyBufferedInputStreamCache = new ArrayList<>(); // Cache für BufferedInputStreams
    private final ArrayList<AudioInputStream> HeavyAudioInputStreamCache = new ArrayList<>(); // Cache für AudioInputStreams
    private long startTime = System.currentTimeMillis();
    private boolean audioIsStopped;

    // Konstruktor und Multiplikator für die Tickrate
    public Utils() {
    }

    // Berechnet die Flugbahn des Spielers
    public int calculateGravity(int x) { return -2 * x + 4; }

    // Läd Bilddateien
    public BufferedImage reader(String resource) {
        if (!bufferedImageCache.containsKey(resource)) { // Überprüft, ob das Bild bereits geladen wurde
            try {
                if (resource.endsWith(".png")) {
                    bufferedImageCache.put(resource, ImageIO.read(Objects.requireNonNull(getClass().getResource(resource)))); // Läd das Bild
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bufferedImageCache.get(resource); // Gibt das Bild zurück
    }

    // Erstellt ein ImageIcon aus Bildern
    public ImageIcon createImageIcon(String resource) {
        if (!imageIconCache.containsKey(resource)) { // Überprüft, ob der Pfad bereits geladen wurde
            if (resource.endsWith(".png")) {
                imageIconCache.put(resource, new ImageIcon(reader(resource))); // Erstellt ein ImageIcon
            }
            if (resource.endsWith(".gif")) {
                URL imageUrl = getClass().getClassLoader().getResource(resource);
                imageIconCache.put(resource, new ImageIcon(Objects.requireNonNull(imageUrl))); // Erstellt ein ImageIcon
            }
        }
        return imageIconCache.get(resource); // Gibt das ImageIcon zurück
    }

    // Zentriert ein Bild mittig
    public Point locatePoint(String image, int width, int height) {
        BufferedImage img = reader(image);
        return new Point((width -  img.getWidth()) / 2, (height - img.getHeight()) / 2);
    }

    // Läd Musikdateien und spielt sie ab
    public void audioPlayer(String audioFilePath, boolean sound, boolean loop) {
        if (sound && !Logic.instance.gamePaused && !Objects.equals(audioFilePath, "error/empty.wav")) {
            if (!loop) audioIsStopped = false;
            CompletableFuture.runAsync(() -> {
                try {
                    if (HeavyClipCache.get(audioFilePath) != null) {
                        Clip clip = HeavyClipCache.get(audioFilePath);
                        clip.setFramePosition(0);
                        clip.start();
                        return;
                    }
                    ClassLoader classLoader = getClass().getClassLoader();
                    InputStream audioFileInputStream = classLoader.getResourceAsStream(audioFilePath);

                    // Überprüfen, ob die Audiodatei gefunden wurde
                    if (audioFileInputStream == null) throw new IllegalArgumentException("Die Audiodatei wurde nicht gefunden: " + audioFilePath);

                    BufferedInputStream bufferedInputStream = new BufferedInputStream(audioFileInputStream);
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);

                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);

                    // Lange Audiodateien werden in den Cache geladen, um die Ressourcen freizugeben
                    if (clip.getMicrosecondLength() > 1000000 || loop) {
                        HeavyBufferedInputStreamCache.add(bufferedInputStream);
                        HeavyAudioInputStreamCache.add(audioInputStream);
                        HeavyClipCache.put(audioFilePath, clip);
                    }

                    // Hinzufügen eines LineListeners, um die Ressourcen freizugeben, wenn die Wiedergabe beendet ist
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            try {
                                if (loop && Logic.instance.gameOver && !audioIsStopped) audioPlayer(audioFilePath, true, true);
                                if (!HeavyClipCache.containsKey(audioFilePath)) {
                                    clip.close();
                                    audioInputStream.close();
                                    bufferedInputStream.close();
                                }


                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    clip.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // Stoppt die Musik
    public void stopHeavyAudio() {
        CompletableFuture.runAsync(() -> {
            try {
                audioIsStopped = true;
                for (Clip clip : HeavyClipCache.values()) clip.stop();
                for (AudioInputStream audioInputStream : HeavyAudioInputStreamCache) audioInputStream.close();
                for (BufferedInputStream bufferedInputStream : HeavyBufferedInputStreamCache) bufferedInputStream.close();

                HeavyBufferedInputStreamCache.clear();
                HeavyAudioInputStreamCache.clear();
                HeavyClipCache.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Berechnet die Breite des Hintergrunds
    public int getBackgroundWidth(String path) {
        return bufferedImageCache.get(path).getWidth();
    }

    // Zentriert das Fenster mittig auf dem Bildschirm
    public Point centerFrame(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Bildschirmgröße
        return new Point(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2));
    }

    // Zentriert das Fenster mittig auf dem Bildschirm
    public int xPlayerPosition(JPanel frame) {
        int x = frame.getWidth() / 4;
        return Math.min(x, 200);
    }

    // Berechnet die Latenz des Systems
    public long calculateSystemLatency() {
        long currentTime = System.currentTimeMillis(); // Aktuelle Zeit
        long latency = currentTime - startTime; // Latenz
        startTime = currentTime;
        soutLogger("latency-log.txt", String.valueOf(latency));
        return latency;
    }

    // Schreibt Strings in eine Log-Datei
    public void soutLogger(String file, String message) {
        if (Logic.instance.developerMode) {
            CompletableFuture.runAsync(() -> { // Asynchroner Aufruf
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    writer.append(message);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    // Liest eine JSON-Datei aus
    public JsonNode readJson(String path) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Die Ressource konnte nicht gefunden werden: " + path);
            }
            return mapper.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Überprüft ob der 11. September ist
    public JsonNode checkDate(String Default) {
        JsonNode config;
        LocalDate date = LocalDate.now();
        if (date.getMonthValue() == 9 && date.getDayOfMonth() == 11 ) config = readJson("config/911beta.json");
        else config = readJson("config/" + Default + ".json");
        return config;
    }
}
