package com.expedia.bookings.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioUtils {

	private static final int SAMPLE_RATE = 44100;

	public static AudioTrack genTone(float duration, int frequency) {
		int numSamples = Math.round(duration * SAMPLE_RATE);
		final double[] sample = new double[numSamples];
		byte[] generatedSnd = new byte[2 * numSamples];

		// generate each sample of square wave, fill the array
		for (int i = 0; i < numSamples; ++i) {
			sample[i] = 0.5 * Math.signum(Math.sin(frequency * 2 * Math.PI * i / SAMPLE_RATE));
		}

		int idx = 0;
		for (final double dVal : sample) {
			final short val = (short) ((dVal * 32767));
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
			SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
			AudioTrack.MODE_STATIC);
		audioTrack.write(generatedSnd, 0, generatedSnd.length);
		return audioTrack;
	}
}
