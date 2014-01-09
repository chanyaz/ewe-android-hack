package com.expedia.bookings.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.larvalabs.svgandroid.SVG;
import com.mobiata.android.Log;

public class SvgDrawable extends Drawable {
	private SVG mSvg;
	private Matrix mMatrix;

	public SvgDrawable(SVG svg, Matrix matrix) {
		mSvg = svg;
		mMatrix = matrix;
	}

	@Override
	public void draw(Canvas canvas) {
		final Canvas finalCanvas = canvas;
		Canvas interceptingCanvas = new Canvas() {
			@Override
			public void drawPath(Path path, Paint paint) {
				Path scaledPath = new Path();

				// We do not want to alter the given paths in the SVG
				// or bother inverting the transformation since we are
				// not guarenteed to be able to invert the supplied
				// 2d homogeneous matrix we are given
				path.transform(getMatrix(), scaledPath);
				finalCanvas.drawPath(scaledPath, paint);
			}
		};

		// The SVG groups set matrices to do relative positioning so we need to leverage that
		interceptingCanvas.setMatrix(mMatrix);
		finalCanvas.save();

		// Respect the bounds
		finalCanvas.clipRect(getBounds());

		mSvg.getRoot().render(interceptingCanvas, null, null);
		finalCanvas.restore();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// ignore
	}

	@Override
	public void setAlpha(int alpha) {
		// ignore
	}
}
