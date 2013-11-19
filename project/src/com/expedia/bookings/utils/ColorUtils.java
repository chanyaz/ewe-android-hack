package com.expedia.bookings.utils;

import android.graphics.Color;

public class ColorUtils {
	public static int blend(int c0, int c1, float fraction) {
		double totalAlpha = Color.alpha(c0) + Color.alpha(c1);
		double weight0 = 1.0 - fraction;
		double weight1 = fraction;

		double r = weight0 * Color.red(c0) + weight1 * Color.red(c1);
		double g = weight0 * Color.green(c0) + weight1 * Color.green(c1);
		double b = weight0 * Color.blue(c0) + weight1 * Color.blue(c1);
		double a = Math.max(Color.alpha(c0), Color.alpha(c1));

		return Color.argb((int) a, (int) r, (int) g, (int) b);
	}
}

