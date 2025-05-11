package main;

import java.net.URL;
import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;

public class Sound {
    private Clip musicClip;
    private URL[] url = new URL[10];
    private long clipPosition = 0;
    private boolean isPaused = false;

    public Sound() {
        try {
            url[0] = getClass().getResource("/Original Tetris theme (Tetris Soundtrack).wav");
            url[1] = getClass().getResource("/delete line.wav");
            url[2] = getClass().getResource("/gameover.wav");
            url[3] = getClass().getResource("/rotation.wav");
            url[4] = getClass().getResource("/touch floor.wav");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play(int i, boolean isMusic) {
        try {
            if (isMusic) {
                if (musicClip != null) {
                    musicClip.close();
                }
                
                AudioInputStream ais = AudioSystem.getAudioInputStream(url[i]);
                musicClip = AudioSystem.getClip();
                musicClip.open(ais);
                
                musicClip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                        if (event.getType() == Type.STOP && !isPaused) {
                            musicClip.close();
                        }
                    }
                });
                
                ais.close();
                musicClip.start();
                isPaused = false;
            } else {
                // For sound effects
                AudioInputStream ais = AudioSystem.getAudioInputStream(url[i]);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                
                clip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                        if (event.getType() == Type.STOP) {
                            clip.close();
                        }
                    }
                });
                
                clip.start();
                ais.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setVolume(float volume) { // volume between 0.0 and 1.0
        if (musicClip != null) {
            FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    public void pause() {
        if (musicClip != null && musicClip.isRunning()) {
            clipPosition = musicClip.getMicrosecondPosition();
            musicClip.stop();
            isPaused = true;
        }
    }

    public void resume() {
        if (musicClip != null && isPaused) {
            try {
                if (!musicClip.isOpen()) {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(url[0]);
                    musicClip.open(ais);
                    ais.close();
                }
                musicClip.setMicrosecondPosition(clipPosition);
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPaused = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loop() {
        if (musicClip != null && musicClip.isOpen()) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            clipPosition = 0;
            isPaused = false;
        }
    }
}