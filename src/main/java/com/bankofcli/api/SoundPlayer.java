package com.bankofcli.api;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public class SoundPlayer{
	static URL Success = SoundPlayer.class.getResource("/Sounds/success2.wav");
	static URL Failure = SoundPlayer.class.getResource("/Sounds/denied.wav");
	
	public static void main(String[] args) {
		playSucess();
		playFailure();
	}
	
	public static void playSound(URL sound) {
		CountDownLatch latch = new CountDownLatch(1);
		
		try(AudioInputStream audioStream = AudioSystem.getAudioInputStream(sound);
			Clip clip =AudioSystem.getClip();
				) {
			clip.open(audioStream);
			clip.addLineListener(event -> {
				if(event.getType() == LineEvent.Type.STOP) {
					latch.countDown();
				}
			});
			clip.start();

			latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void playSucess() {
		CountDownLatch latch = new CountDownLatch(1);
		
		try(AudioInputStream audioStream = AudioSystem.getAudioInputStream(Success);
			Clip clip =AudioSystem.getClip();
				) {
			clip.open(audioStream);
			clip.addLineListener(event -> {
				if(event.getType() == LineEvent.Type.STOP) {
					latch.countDown();
				}
			});
			clip.start();

			latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void playFailure() {
		CountDownLatch latch = new CountDownLatch(1);
		
		try(AudioInputStream audioStream = AudioSystem.getAudioInputStream(Failure);
			Clip clip =AudioSystem.getClip();
				) {
			clip.open(audioStream);
			clip.addLineListener(event -> {
				if(event.getType() == LineEvent.Type.STOP) {
					latch.countDown();
				}
			});
			clip.start();

			latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
