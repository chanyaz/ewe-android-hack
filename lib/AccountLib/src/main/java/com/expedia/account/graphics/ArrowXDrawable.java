/*
 * Copyright (C) 2014 Chris Renke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.account.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Property;

import static android.graphics.Color.BLACK;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap.BUTT;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.PixelFormat.TRANSLUCENT;

/**
 * A drawable that rotates between a drawer icon and a back arrow based on parameter.
 */
public class ArrowXDrawable extends Drawable {

	/**
	 * Joins two {@link Path}s as if they were one where the first 50% of the path is {@code
	 * PathFirst} and the second 50% of the path is {@code pathSecond}.
	 */
	private static class MeasuredPath {

		private final PathMeasure measure;
		private final float length;

		private MeasuredPath(Path path) {
			measure = new PathMeasure(path, false);
			length = measure.getLength();
		}

		/**
		 * Returns a point on this curve at the given {@code parameter}.
		 * For {@code parameter} values less than .5f, the first path will drive the point.
		 * For {@code parameter} values greater than .5f, the second path will drive the point.
		 * For {@code parameter} equal to .5f, the point will be the point where the two
		 * internal paths connect.
		 */
		private void getPointOnLine(float parameter, float[] coords) {
			measure.getPosTan(length * parameter, coords, null);
		}
	}

	/**
	 * Draws a line between two {@link MeasuredPath}s at distance {@code parameter} along each path.
	 */
	private class BridgingLine {

		private final MeasuredPath pathA;
		private final MeasuredPath pathB;

		private BridgingLine(MeasuredPath pathA, MeasuredPath pathB) {
			this.pathA = pathA;
			this.pathB = pathB;
		}

		/**
		 * Draw a line between the points defined on the paths backing {@code measureA} and
		 * {@code measureB} at the current parameter.
		 */
		private void draw(Canvas canvas) {
			pathA.getPointOnLine(parameter, coordsA);
			pathB.getPointOnLine(parameter, coordsB);
			canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint);
		}
	}

	/**
	 * Path coordinates were generated for a bounding box of this size, 100px.
	 */
	private final static float DIMEN_PX = 100f;

	/**
	 * Paths were generated targeting this stroke width to form the arrowhead properly, modification
	 * may cause the arrow to not for nicely.
	 */
	private final static float STROKE_WIDTH_PX = 8.2f;

	private BridgingLine topLine;
	private BridgingLine middleLine;
	private BridgingLine bottomLine;

	private final boolean useFixedSize;
	private float sizePx = DIMEN_PX;

	private final Paint linePaint;

	private boolean flip;
	private float parameter;

	// Helper fields during drawing calculations.
	private final float coordsA[] = { 0f, 0f };
	private final float coordsB[] = { 0f, 0f };

	/**
	 * Creates a new ArrowXDrawable that will fill its canvasBounds. If
	 * the canvasBounds aren't square, it will be drawn in the upper
	 * left corner of its bounding rect.
	 */
	public ArrowXDrawable() {
		this(0, false);
	}

	/**
	 * Creates a new ArrowXDrawable that will be drawn at a fixed size,
	 * ignoring its bounding rect.
	 *
	 * @param sizePx
	 */
	public ArrowXDrawable(float sizePx) {
		this(sizePx, true);
	}

	private ArrowXDrawable(float sizePx, boolean useFixedSize) {
		linePaint = new Paint(ANTI_ALIAS_FLAG);
		linePaint.setStrokeCap(BUTT);
		linePaint.setColor(BLACK);
		linePaint.setStyle(STROKE);
		this.useFixedSize = useFixedSize;
		this.sizePx = sizePx;
		render();
	}

	private void render() {
		if (sizePx == 0) {
			return;
		}
		float scale = sizePx / DIMEN_PX;
		linePaint.setStrokeWidth(STROKE_WIDTH_PX * scale);

		Path p1, p2;

		// Bottom
		p1 = new Path();
		p1.moveTo(53.25f, 80f);
		p1.lineTo(50f, 87f);
		scalePath(p1, scale);

		p2 = new Path();
		p2.moveTo(20.25f, 47f);
		p2.lineTo(50f, 50f);
		scalePath(p2, scale);

		topLine = new BridgingLine(new MeasuredPath(p1), new MeasuredPath(p2));

		// Middle
		p1 = new Path();
		p1.moveTo(22f, 50f);
		p1.lineTo(13f, 50f);
		scalePath(p1, scale);

		p2 = new Path();
		p2.moveTo(83.3f, 50f);
		p2.lineTo(87f, 50f);
		scalePath(p2, scale);

		middleLine = new BridgingLine(new MeasuredPath(p1), new MeasuredPath(p2));

		// Top
		p1 = new Path();
		p1.moveTo(53.25f, 20f);
		p1.lineTo(50f, 13f);
		scalePath(p1, scale);

		p2 = new Path();
		p2.moveTo(20.25f, 53f);
		p2.lineTo(50f, 50f);
		scalePath(p2, scale);

		bottomLine = new BridgingLine(new MeasuredPath(p1), new MeasuredPath(p2));
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		if (!useFixedSize) {
			sizePx = Math.min(bounds.width(), bounds.height());
			render();
		}
	}

	private Rect canvasBounds = new Rect();

	@Override
	public void draw(Canvas canvas) {
		canvas.save();

		canvas.getClipBounds(canvasBounds);

		canvas.rotate(135f * parameter * (flip ? -1f : 1f), canvasBounds.centerX(), canvasBounds.centerY());
		canvas.translate(canvasBounds.centerX() - sizePx / 2, canvasBounds.centerY() - sizePx / 2);

		topLine.draw(canvas);
		middleLine.draw(canvas);
		bottomLine.draw(canvas);

		canvas.restore();
	}

	@Override
	public void setAlpha(int alpha) {
		linePaint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		linePaint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return TRANSLUCENT;
	}

	public void setStrokeColor(int color) {
		linePaint.setColor(color);
		invalidateSelf();
	}

	/**
	 * Sets the rotation of this drawable based on {@code parameter} between 0 and 1.
	 */
	public void setParameter(float parameter) {
		if (parameter > 1 || parameter < 0) {
			throw new IllegalArgumentException("Value must be between 1 and zero inclusive!");
		}
		this.parameter = parameter;
		invalidateSelf();
	}

	public float getParameter() {
		return this.parameter;
	}

	public static final Property<ArrowXDrawable, Float> PARAMETER = new Property<ArrowXDrawable, Float>(Float.class,
		"parameter") {
		@Override
		public void set(ArrowXDrawable object, Float value) {
			object.setParameter(value);
		}

		@Override
		public Float get(ArrowXDrawable object) {
			return object.getParameter();
		}
	};

	/**
	 * When false, rotates from 3 o'clock to 9 o'clock between a drawer icon and a back arrow.
	 * When true, rotates from 9 o'clock to 3 o'clock between a back arrow and a drawer icon.
	 */
	public void setFlip(boolean flip) {
		this.flip = flip;
		invalidateSelf();
	}

	/**
	 * Scales the paths to the given screen density.
	 */
	private static void scalePath(Path path, float scale) {
		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scale, scale, 0f, 0f);
		path.transform(scaleMatrix);
	}
}