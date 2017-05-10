package com.expedia.bookings.bitmaps;

import android.graphics.Matrix;

public class BitmapUtils {

	/**
	 * Creates a matrix to use in a drawable or ImageView to perform a TOP_CROP similar to CENTER_CROP
	 */
	public static Matrix createTopCropMatrix(int bitmapWidth, int bitmapHeight, int vwidth, int vheight) {
		Matrix matrix = new Matrix();

		float scale;
		if (bitmapWidth * vheight > vwidth * bitmapHeight) {
			scale = (float) vheight / (float) bitmapHeight;
		}
		else {
			scale = (float) vwidth / (float) bitmapWidth;
		}

		matrix.setScale(scale, scale);

		return matrix;
	}
}
