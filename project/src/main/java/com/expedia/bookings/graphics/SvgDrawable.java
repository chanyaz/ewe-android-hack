package com.expedia.bookings.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;

import com.larvalabs.svgandroid.SVG;

public class SvgDrawable extends Drawable {
	private final SVG mSvg;
	private final Matrix mMatrix;

	public SvgDrawable(SVG svg, Matrix matrix) {
		mSvg = svg;
		mMatrix = matrix;
	}

	@Override
	public void draw(Canvas canvas) {
		final Canvas finalCanvas = canvas;
		final Rect bounds = getBounds();
		Canvas interceptingCanvas = new Canvas() {
			private final RectF pathBounds = new RectF();
			private Path scaledPath = new Path();
			private final Path clippedPath = new Path();
			private final Region region = new Region();
			private final Region clipRegion = new Region(bounds);

			final private Matrix identity = new Matrix();

			@Override
			public void drawPath(Path path, Paint paint) {
				// We do not want to alter the given paths in the SVG
				// or bother inverting the transformation since we are
				// not guarenteed to be able to invert the supplied
				// 2d homogeneous matrix we are given
				path.transform(getMatrix(), scaledPath);
				scaledPath.computeBounds(pathBounds, false);

				// Check and skip drawing if path is offscreen
				if (pathBounds.intersects(bounds.left, bounds.top, bounds.right, bounds.bottom)) {
					if (pathBounds.width() > bounds.width() || pathBounds.height() > bounds.height()) {
						// Path is too large, we need to clip it
						region.setPath(scaledPath, clipRegion);
						scaledPath = region.getBoundaryPath();
						scaledPath.transform(identity, clippedPath);
						finalCanvas.drawPath(clippedPath, paint);
					}
					else {
						// No clipping required
						finalCanvas.drawPath(scaledPath, paint);
					}
				}
			}
		};

		// The SVG groups set matrices to do relative positioning so we need to leverage that
		interceptingCanvas.setMatrix(mMatrix);
		finalCanvas.save();

		// Respect the bounds
		finalCanvas.clipRect(bounds);

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
