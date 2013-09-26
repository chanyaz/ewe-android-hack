package com.expedia.bookings.utils;

import android.graphics.Color;

public class ColorUtils {
	// Blending in the HSV color space is less muddy than
	// a weighted average of the RGB color space
	public static int blend(int firstColor, int secondColor, float fraction) {
		float[] xs = new float[3];
		float[] ys = new float[3];

		Color.colorToHSV(firstColor, xs);
		Color.colorToHSV(secondColor, ys);

		for (int i = 0; i < 3; i++) {
			ys[i] = xs[i] + ((ys[i] - xs[i]) * fraction);
		}
		return Color.HSVToColor(ys);
	}
}

