/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.expedia.bookings.bitmaps;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;

/**
 * A custom implementation of BitmapDrawable
 * <p/>
 * There are a few changes from the official source's BitmapDrawable:
 * <p/>
 * 1. You can call setBitmap()
 * 2. You cannot inflate this version of BitmapDrawable.
 * 3. It does not use the RTL libraries.  This should not matter, as I doubt
 * we will ever want to switch the directionality of a URL-loaded image.
 */
public class BitmapDrawable extends Drawable {

	private static final int DEFAULT_PAINT_FLAGS =
		Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;
	private BitmapState mBitmapState;
	private Bitmap mBitmap;
	private int mTargetDensity;

	private final Rect mDstRect = new Rect();   // Gravity.apply() sets this

	private boolean mApplyGravity;
	private boolean mMutated;

	// These are scaled to match the target density.
	private int mBitmapWidth;
	private int mBitmapHeight;

	/**
	 * Create an empty drawable, not dealing with density.
	 *
	 * @deprecated Use {@link #BitmapDrawable(Resources)} to ensure
	 * that the drawable has correctly set its target density.
	 */
	@Deprecated
	public BitmapDrawable() {
		mBitmapState = new BitmapState((Bitmap) null);
	}

	/**
	 * Create an empty drawable, setting initial target density based on
	 * the display metrics of the resources.
	 */
	public BitmapDrawable(Resources res) {
		mBitmapState = new BitmapState((Bitmap) null);
		mBitmapState.mTargetDensity = mTargetDensity;
	}

	/**
	 * Create drawable from a bitmap, not dealing with density.
	 *
	 * @deprecated Use {@link #BitmapDrawable(Resources, Bitmap)} to ensure
	 * that the drawable has correctly set its target density.
	 */
	@Deprecated
	public BitmapDrawable(Bitmap bitmap) {
		this(new BitmapState(bitmap), null);
	}

	/**
	 * Create drawable from a bitmap, setting initial target density based on
	 * the display metrics of the resources.
	 */
	public BitmapDrawable(Resources res, Bitmap bitmap) {
		this(new BitmapState(bitmap), res);
		mBitmapState.mTargetDensity = mTargetDensity;
	}

	/**
	 * Create a drawable by opening a given file path and decoding the bitmap.
	 *
	 * @deprecated Use {@link #BitmapDrawable(Resources, String)} to ensure
	 * that the drawable has correctly set its target density.
	 */
	@Deprecated
	public BitmapDrawable(String filepath) {
		this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
		if (mBitmap == null) {
			android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
		}
	}

	/**
	 * Create a drawable by opening a given file path and decoding the bitmap.
	 */
	public BitmapDrawable(Resources res, String filepath) {
		this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
		mBitmapState.mTargetDensity = mTargetDensity;
		if (mBitmap == null) {
			android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
		}
	}

	/**
	 * Create a drawable by decoding a bitmap from the given input stream.
	 *
	 * @deprecated Use {@link #BitmapDrawable(Resources, java.io.InputStream)} to ensure
	 * that the drawable has correctly set its target density.
	 */
	@Deprecated
	public BitmapDrawable(java.io.InputStream is) {
		this(new BitmapState(BitmapFactory.decodeStream(is)), null);
		if (mBitmap == null) {
			android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
		}
	}

	/**
	 * Create a drawable by decoding a bitmap from the given input stream.
	 */
	public BitmapDrawable(Resources res, java.io.InputStream is) {
		this(new BitmapState(BitmapFactory.decodeStream(is)), null);
		mBitmapState.mTargetDensity = mTargetDensity;
		if (mBitmap == null) {
			android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
		}
	}

	/**
	 * Returns the paint used to render this drawable.
	 */
	public final Paint getPaint() {
		return mBitmapState.mPaint;
	}

	/**
	 * Returns the bitmap used by this drawable to render. May be null.
	 */
	public final Bitmap getBitmap() {
		return mBitmap;
	}

	private void computeBitmapSize() {
		mBitmapWidth = mBitmap.getScaledWidth(mTargetDensity);
		mBitmapHeight = mBitmap.getScaledHeight(mTargetDensity);
	}

	public void setBitmap(Bitmap bitmap) {
		if (bitmap != mBitmap) {
			mBitmap = bitmap;
			mBitmapState.mBitmap = bitmap;
			if (bitmap != null) {
				computeBitmapSize();
			}
			else {
				mBitmapWidth = mBitmapHeight = -1;
			}
			invalidateSelf();
		}
	}

	/**
	 * Set the density scale at which this drawable will be rendered. This
	 * method assumes the drawable will be rendered at the same density as the
	 * specified canvas.
	 *
	 * @param canvas The Canvas from which the density scale must be obtained.
	 * @see Bitmap#setDensity(int)
	 * @see Bitmap#getDensity()
	 */
	public void setTargetDensity(Canvas canvas) {
		setTargetDensity(canvas.getDensity());
	}

	/**
	 * Set the density scale at which this drawable will be rendered.
	 *
	 * @param metrics The DisplayMetrics indicating the density scale for this drawable.
	 * @see Bitmap#setDensity(int)
	 * @see Bitmap#getDensity()
	 */
	public void setTargetDensity(DisplayMetrics metrics) {
		setTargetDensity(metrics.densityDpi);
	}

	/**
	 * Set the density at which this drawable will be rendered.
	 *
	 * @param density The density scale for this drawable.
	 * @see Bitmap#setDensity(int)
	 * @see Bitmap#getDensity()
	 */
	public void setTargetDensity(int density) {
		if (mTargetDensity != density) {
			mTargetDensity = density == 0 ? DisplayMetrics.DENSITY_DEFAULT : density;
			if (mBitmap != null) {
				computeBitmapSize();
			}
			invalidateSelf();
		}
	}

	/**
	 * Get the gravity used to position/stretch the bitmap within its bounds.
	 * See android.view.Gravity
	 *
	 * @return the gravity applied to the bitmap
	 */
	public int getGravity() {
		return mBitmapState.mGravity;
	}

	/**
	 * Set the gravity used to position/stretch the bitmap within its bounds.
	 * See android.view.Gravity
	 *
	 * @param gravity the gravity
	 */
	public void setGravity(int gravity) {
		if (mBitmapState.mGravity != gravity) {
			mBitmapState.mGravity = gravity;
			mApplyGravity = true;
			invalidateSelf();
		}
	}

	/**
	 * Enables or disables anti-aliasing for this drawable. Anti-aliasing affects
	 * the edges of the bitmap only so it applies only when the drawable is rotated.
	 *
	 * @param aa True if the bitmap should be anti-aliased, false otherwise.
	 */
	public void setAntiAlias(boolean aa) {
		mBitmapState.mPaint.setAntiAlias(aa);
		invalidateSelf();
	}

	@Override
	public void setFilterBitmap(boolean filter) {
		mBitmapState.mPaint.setFilterBitmap(filter);
		invalidateSelf();
	}

	@Override
	public void setDither(boolean dither) {
		mBitmapState.mPaint.setDither(dither);
		invalidateSelf();
	}

	/**
	 * Indicates the repeat behavior of this drawable on the X axis.
	 *
	 * @return {@link Shader.TileMode#CLAMP} if the bitmap does not repeat,
	 * {@link Shader.TileMode#REPEAT} or {@link Shader.TileMode#MIRROR} otherwise.
	 */
	public Shader.TileMode getTileModeX() {
		return mBitmapState.mTileModeX;
	}

	/**
	 * Indicates the repeat behavior of this drawable on the Y axis.
	 *
	 * @return {@link Shader.TileMode#CLAMP} if the bitmap does not repeat,
	 * {@link Shader.TileMode#REPEAT} or {@link Shader.TileMode#MIRROR} otherwise.
	 */
	public Shader.TileMode getTileModeY() {
		return mBitmapState.mTileModeY;
	}

	/**
	 * Sets the repeat behavior of this drawable on the X axis. By default, the drawable
	 * does not repeat its bitmap. Using {@link Shader.TileMode#REPEAT} or
	 * {@link Shader.TileMode#MIRROR} the bitmap can be repeated (or tiled) if the bitmap
	 * is smaller than this drawable.
	 *
	 * @param mode The repeat mode for this drawable.
	 * @see #setTileModeY(Shader.TileMode)
	 * @see #setTileModeXY(Shader.TileMode, Shader.TileMode)
	 */
	public void setTileModeX(Shader.TileMode mode) {
		setTileModeXY(mode, mBitmapState.mTileModeY);
	}

	/**
	 * Sets the repeat behavior of this drawable on the Y axis. By default, the drawable
	 * does not repeat its bitmap. Using {@link Shader.TileMode#REPEAT} or
	 * {@link Shader.TileMode#MIRROR} the bitmap can be repeated (or tiled) if the bitmap
	 * is smaller than this drawable.
	 *
	 * @param mode The repeat mode for this drawable.
	 * @see #setTileModeX(Shader.TileMode)
	 * @see #setTileModeXY(Shader.TileMode, Shader.TileMode)
	 */
	public final void setTileModeY(Shader.TileMode mode) {
		setTileModeXY(mBitmapState.mTileModeX, mode);
	}

	/**
	 * Sets the repeat behavior of this drawable on both axis. By default, the drawable
	 * does not repeat its bitmap. Using {@link Shader.TileMode#REPEAT} or
	 * {@link Shader.TileMode#MIRROR} the bitmap can be repeated (or tiled) if the bitmap
	 * is smaller than this drawable.
	 *
	 * @param xmode The X repeat mode for this drawable.
	 * @param ymode The Y repeat mode for this drawable.
	 * @see #setTileModeX(Shader.TileMode)
	 * @see #setTileModeY(Shader.TileMode)
	 */
	public void setTileModeXY(Shader.TileMode xmode, Shader.TileMode ymode) {
		final BitmapState state = mBitmapState;
		if (state.mTileModeX != xmode || state.mTileModeY != ymode) {
			state.mTileModeX = xmode;
			state.mTileModeY = ymode;
			state.mRebuildShader = true;
			invalidateSelf();
		}
	}

	@Override
	public int getChangingConfigurations() {
		return super.getChangingConfigurations() | mBitmapState.mChangingConfigurations;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mApplyGravity = true;
	}

	@Override
	public void draw(Canvas canvas) {
		Bitmap bitmap = mBitmap;
		if (bitmap != null) {
			final BitmapState state = mBitmapState;
			if (state.mRebuildShader) {
				Shader.TileMode tmx = state.mTileModeX;
				Shader.TileMode tmy = state.mTileModeY;

				if (tmx == null && tmy == null) {
					state.mPaint.setShader(null);
				}
				else {
					state.mPaint.setShader(new BitmapShader(bitmap,
						tmx == null ? Shader.TileMode.CLAMP : tmx,
						tmy == null ? Shader.TileMode.CLAMP : tmy));
				}
				state.mRebuildShader = false;
				copyBounds(mDstRect);
			}

			Shader shader = state.mPaint.getShader();
			if (shader == null) {
				if (mApplyGravity) {
					Gravity.apply(state.mGravity, mBitmapWidth, mBitmapHeight,
						getBounds(), mDstRect);
					mApplyGravity = false;
				}
				canvas.drawBitmap(bitmap, null, mDstRect, state.mPaint);
			}
			else {
				if (mApplyGravity) {
					copyBounds(mDstRect);
					mApplyGravity = false;
				}
				canvas.drawRect(mDstRect, state.mPaint);
			}
		}
	}

	@Override
	public void setAlpha(int alpha) {
		int oldAlpha = mBitmapState.mPaint.getAlpha();
		if (alpha != oldAlpha) {
			mBitmapState.mPaint.setAlpha(alpha);
			invalidateSelf();
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mBitmapState.mPaint.setColorFilter(cf);
		invalidateSelf();
	}

	/**
	 * A mutable BitmapDrawable still shares its Bitmap with any other Drawable
	 * that comes from the same resource.
	 *
	 * @return This drawable.
	 */
	@Override
	public Drawable mutate() {
		if (!mMutated && super.mutate() == this) {
			mBitmapState = new BitmapState(mBitmapState);
			mMutated = true;
		}
		return this;
	}

	@Override
	public int getIntrinsicWidth() {
		return mBitmapWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return mBitmapHeight;
	}

	@Override
	public int getOpacity() {
		if (mBitmapState.mGravity != Gravity.FILL) {
			return PixelFormat.TRANSLUCENT;
		}
		Bitmap bm = mBitmap;
		return (bm == null || bm.hasAlpha() || mBitmapState.mPaint.getAlpha() < 255) ?
			PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
	}

	@Override
	public final ConstantState getConstantState() {
		mBitmapState.mChangingConfigurations = getChangingConfigurations();
		return mBitmapState;
	}

	final static class BitmapState extends ConstantState {
		Bitmap mBitmap;
		int mChangingConfigurations;
		int mGravity = Gravity.FILL;
		Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);
		Shader.TileMode mTileModeX = null;
		Shader.TileMode mTileModeY = null;
		int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
		boolean mRebuildShader;

		BitmapState(Bitmap bitmap) {
			mBitmap = bitmap;
		}

		BitmapState(BitmapState bitmapState) {
			this(bitmapState.mBitmap);
			mChangingConfigurations = bitmapState.mChangingConfigurations;
			mGravity = bitmapState.mGravity;
			mTileModeX = bitmapState.mTileModeX;
			mTileModeY = bitmapState.mTileModeY;
			mTargetDensity = bitmapState.mTargetDensity;
			mPaint = new Paint(bitmapState.mPaint);
			mRebuildShader = bitmapState.mRebuildShader;
		}

		@Override
		public Drawable newDrawable() {
			return new BitmapDrawable(this, null);
		}

		@Override
		public Drawable newDrawable(Resources res) {
			return new BitmapDrawable(this, res);
		}

		@Override
		public int getChangingConfigurations() {
			return mChangingConfigurations;
		}
	}

	private BitmapDrawable(BitmapState state, Resources res) {
		mBitmapState = state;
		if (res != null) {
			mTargetDensity = res.getDisplayMetrics().densityDpi;
		}
		else {
			mTargetDensity = state.mTargetDensity;
		}
		setBitmap(state != null ? state.mBitmap : null);
	}
}
