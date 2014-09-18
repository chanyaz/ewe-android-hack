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

	/**
	 * Scale the saturation.
	 * <p/>
	 * The resulting color has saturation = saturation + (percentage * saturation);
	 *
	 * @param percentage
	 * @return
	 */
	public static int scaleSaturationByPercentage(int color, float percentage) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] = hsv[1] + (percentage * hsv[1]);
		return Color.HSVToColor(hsv);
	}

	/**
	 * Scale the opacity.
	 * <p/>
	 * The resulting color has opacity = opacity + (percentage * opacity);
	 *
	 * @param color
	 * @param percentage
	 * @return
	 */
	public static int scaleOpacityByPercentage(int color, float percentage) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = hsv[2] + (percentage * hsv[2]);
		return Color.HSVToColor(hsv);
	}

	/**
	 * This sets the saturation to the defined provided value [0...1];
	 *
	 * @param saturation
	 * @return
	 */
	public static int setSaturation(int color, float saturation) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] = saturation;
		return Color.HSVToColor(hsv);
	}

	/**
	 * This sets the opacity to the defined provided value [0...1];
	 *
	 * @param opacity
	 * @return
	 */
	public static int setOpacity(int color, float opacity) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = opacity;
		return Color.HSVToColor(hsv);
	}
}

