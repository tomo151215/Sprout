package engine.audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class AudioManager implements AutoCloseable {
    private Clip bgmClip;

    public void playSe(String path) {
        Clip clip = loadClip(path);

        try {
            clip.addLineListener(event -> closeWhenPlaybackStops(event, clip));
            clip.setFramePosition(0);
            clip.start();
        } catch (RuntimeException e) {
            closeClip(clip);
            throw e;
        }
    }

    public synchronized void playBgm(String path) {
        stopBgm();

        Clip clip = loadClip(path);
        try {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            bgmClip = clip;
        } catch (RuntimeException e) {
            closeClip(clip);
            throw e;
        }
    }

    public synchronized void stopBgm() {
        Clip clip = bgmClip;
        bgmClip = null;

        if (clip == null) {
            return;
        }

        clip.stop();
        closeClip(clip);
    }

    @Override
    public void close() {
        stopBgm();
    }

    private Clip loadClip(String path) {
        validatePath(path);

        Clip clip = null;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path))) {
            clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            if (clip != null) {
                try {
                    closeClip(clip);
                } catch (RuntimeException closeError) {
                    e.addSuppressed(closeError);
                }
            }
            throw new IllegalArgumentException("Failed to load audio: " + path, e);
        }
    }

    private void closeWhenPlaybackStops(LineEvent event, Clip clip) {
        if (event.getType() == LineEvent.Type.STOP) {
            closeClip(clip);
        }
    }

    private void closeClip(Clip clip) {
        if (clip.isOpen()) {
            clip.close();
        }
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be null or blank.");
        }
    }
}
