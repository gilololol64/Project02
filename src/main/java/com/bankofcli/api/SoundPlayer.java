package com.bankofcli.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundPlayer implements SoundPlayerInterface{
	static URL Success = SoundPlayer.class.getResource("/Sounds/success2.wav");
	static URL Failure = SoundPlayer.class.getResource("/Sounds/denied.wav");
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");
	
	private final Set<Clip> activeClips = ConcurrentHashMap.newKeySet();

	public void playSound(URL soundUrl) {
	    try {
	        AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl);
	        Clip clip = AudioSystem.getClip();
	        activeClips.add(clip);

	        clip.open(audioStream);
	        clip.addLineListener(event -> {
	            if (event.getType() == LineEvent.Type.STOP) {
	                clip.close();
	                activeClips.remove(clip);
	                try {
	                    audioStream.close();
	                } catch (IOException e) {
	                    errorLogger.error("Failed to close audio stream", e);
	                }
	            }
	        });
	        clip.start();

	    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
	        errorLogger.error("Failed to play sound from {}: {}", soundUrl, e.getMessage());
	    }
	}
	public void playFailure() { playSound(Failure); }
	public void playSuccess() { playSound(Success); }
}
