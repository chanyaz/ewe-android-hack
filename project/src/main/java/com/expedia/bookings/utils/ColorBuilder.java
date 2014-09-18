package com.expedia.bookings.utils;

import android.graphics.Color;

/**
 * This builder class is for manipulating colors in a chained fashion.
 * <p/>
 * e.g. If we want to darken a color, and then increase saturation, and then set alpha we would do the following.
 * (new ColorBuilder(#123456)).darkenBy(0.5f).scaleSaturation(0.2f).setAlpha(200).build();
 */
public class ColorBuilder {

	private final int mBaseColor;//This is just for reference
	private int mCurrentColor;//This color changes with each operation.

	/**
	 * Constructor
	 *
	 * @param baseColor - This is the base color on which all other color operations will be performed.
	 */
	public ColorBuilder(int baseColor) {
		mBaseColor = baseColor;
		mCurrentColor = baseColor;
	}

	/**
	 * Darken the color by blending it with BLACK.
	 *
	 * @param percentage
	 * @return
	 */
	public ColorBuilder darkenBy(float percentage) {
		return blend(Color.BLACK, percentage);
	}

	/**
	 * Lighten the color by blending it with WHITE.
	 *
	 * @param percentage
	 * @return
	 */
	public ColorBuilder lightenBy(float percentage) {
		return blend(Color.WHITE, percentage);
	}

	/**
	 * Scale the saturation.
	 * <p/>
	 * E.g. if I want a color to have 20% more saturation I call scaleSaturation(0.2f).
	 * This will result in color saturation = saturation + (0.2f * saturation);
	 *
	 * @param percentage
	 * @return
	 */
	public ColorBuilder scaleSaturation(float percentage) {
		mCurrentColor = ColorUtils.scaleSaturationByPercentage(mCurrentColor, percentage);
		return this;
	}

	/**
	 * Scale the opacity.
	 * <p/>
	 * E.g. if I want a color to have 20% more opacity I call scaleOpacity(0.2f).
	 * This will result in color opacity = opacity + (0.2f * opacity);
	 *
	 * @param percentage
	 * @return
	 */
	public ColorBuilder scaleOpacity(float percentage) {
		mCurrentColor = ColorUtils.scaleOpacityByPercentage(mCurrentColor, percentage);
		return this;
	}

	/**
	 * This sets the saturation to the defined provided value [0...1];
	 *
	 * @param saturation
	 * @return
	 */
	public ColorBuilder setSaturation(float saturation) {
		mCurrentColor = ColorUtils.setSaturation(mCurrentColor, saturation);
		return this;
	}

	/**
	 * This sets the opacity to the defined provided value [0...1];
	 *
	 * @param opacity
	 * @return
	 */
	public ColorBuilder setOpacity(float opacity) {
		mCurrentColor = ColorUtils.setOpacity(mCurrentColor, opacity);
		return this;
	}

	/**
	 * Blend the active color with the provided color.
	 *
	 * @param color
	 * @param percentage
	 * @return
	 */
	public ColorBuilder blend(int color, float percentage) {
		mCurrentColor = ColorUtils.blend(mCurrentColor, color, percentage);
		return this;
	}

	/**
	 * Set the alpha chanel [0...255]
	 *
	 * @param alpha
	 * @return
	 */
	public ColorBuilder setAlpha(int alpha) {
		mCurrentColor = Color
			.argb(alpha, Color.red(mCurrentColor), Color.green(mCurrentColor), Color.blue(mCurrentColor));
		return this;
	}

	/**
	 * Set the alpha channel [0...1f]
	 *
	 * @param alphaPercentage
	 * @return
	 */
	public ColorBuilder setAlpha(float alphaPercentage) {
		int alphaNum = (int) (alphaPercentage * 255);
		setAlpha(alphaNum);
		return this;
	}

	/**
	 * Build the color.
	 *
	 * @return
	 */
	public int build() {
		return mCurrentColor;
	}
}
