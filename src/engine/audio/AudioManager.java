package engine.audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioManager {
    private Clip bgmClip;

    private Clip loadClip(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be null or blank.");
        }
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path))) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new IllegalArgumentException("Failed to load audio: " + path, e);
        }
    }

    public void playSe(String path) {
        Clip clip = loadClip(path);
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();
            }
        });
        clip.setFramePosition(0);
        clip.start();
    }

    public void playBgm(String path) {
        stopBgm();
        bgmClip = loadClip(path);
        bgmClip.setFramePosition(0);
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        bgmClip.start();
    }

    public void stopBgm() {
        if (bgmClip == null) {
            return;
        }
        bgmClip.stop();
        bgmClip.close();
        bgmClip = null;
    }

    public void close() {
        stopBgm();
    }
}
