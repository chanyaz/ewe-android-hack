package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

/**
 * Used for creating drawables with some special formatting for headers
 * 
 * Can be used to:
 * 
 * - Create rounded corners (on top, all, or none)
 * - Add a gradient
 * - Add an overlay Drawable
 * - Center/translate the Bitmap
 * 
 * TODO: ADD SCALETYPE
 */
public class HeaderBitmapDrawable extends Drawable implements OnImageLoaded {

	public enum CornerMode {
		ALL,
		TOP,
		NONE
	}

	private CornerMode mCornerMode = CornerMode.NONE;

	// For drawing the bitmap
	private BitmapShader mBitmapShader;
	private Paint mBitmapPaint;

	// For the rounded corners
	private int mCornerRadius;

	// Overlay drawable
	private Drawable mOverlayDrawable;

	// For a built-in gradient
	private int[] mColors = null;
	private float[] mPositions = null;
	private Paint mGradientPaint;

	// Used to create matrix (if requested)
	private boolean mMatrixEnabled;
	private float mDx;
	private float mDy;
	private int mBitmapWidth;
	private int mBitmapHeight;

	// Cached for draw speed
	private final RectF mRect = new RectF();

	// If you want to use an underlying UrlBitmapDrawable to drive image loading functionality
	private UrlBitmapDrawable mUrlBitmapDrawable;

	public HeaderBitmapDrawable() {
		mBitmapPaint = new Paint();
		mBitmapPaint.setAntiAlias(true);

		mGradientPaint = new Paint();
	}

	public HeaderBitmapDrawable(Bitmap bitmap) {
		this();

		setBitmap(bitmap);
	}

	public HeaderBitmapDrawable(UrlBitmapDrawable urlBitmapDrawable) {
		this();

		setUrlBitmapDrawable(urlBitmapDrawable);
	}

	//////////////////////////////////////////////////////////////////////////
	// Configuration

	public void setBitmap(Bitmap bitmap) {
		configureBitmap(bitmap);
		configureMatrix(getBounds());

		invalidateSelf();
	}

	public void setUrlBitmapDrawable(UrlBitmapDrawable urlBitmapDrawable) {
		mUrlBitmapDrawable = urlBitmapDrawable;
		mUrlBitmapDrawable.setOnImageLoadedCallback(this);

		mBitmapPaint.setShader(null);

		invalidateSelf();
	}

	private void configureBitmap(Bitmap bitmap) {
		mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		mBitmapWidth = bitmap.getWidth();
		mBitmapHeight = bitmap.getHeight();

		mBitmapPaint.setShader(mBitmapShader);
	}

	public void setCornerMode(CornerMode cornerMode) {
		mCornerMode = cornerMode;

		invalidateSelf();
	}

	public void setCornerRadius(int cornerRadius) {
		mCornerRadius = cornerRadius;

		invalidateSelf();
	}

	public void setOverlayDrawable(Drawable drawable) {
		mOverlayDrawable = drawable;

		configureOverlayDrawableBounds(getBounds());
		invalidateSelf();
	}

	private void configureOverlayDrawableBounds(Rect bounds) {
		if (mOverlayDrawable != null) {
			mOverlayDrawable.setBounds(bounds);
		}
	}

	/**
	 * Sets a gradient for the image.  Does not apply gradient to the overlay
	 *
	 * If colors is null, it cancels the gradient
	 *
	 * @param colors The colors to be distributed along the gradient line
	 * @param positions May be null. The relative positions [0..1] of each corresponding color in the colors array.
	 * 		If this is null, the the colors are distributed evenly along the gradient line.
	 */
	public void setGradient(int[] colors, float[] positions) {
		mColors = colors;
		mPositions = positions;

		configureGradient(getBounds());
		invalidateSelf();
	}

	private void configureGradient(Rect bounds) {
		int height = bounds.height();
		if (mColors != null && height > 0) {
			mGradientPaint.setShader(new LinearGradient(0, 0, 0, height, mColors, mPositions,
					Shader.TileMode.CLAMP));
		}
		else {
			mGradientPaint.setShader(null);
		}
	}

	public void setMatrixEnabled(boolean enabled, float dX, float dY) {
		mMatrixEnabled = enabled;
		mDx = dX;
		mDy = dY;

		configureMatrix(getBounds());
		invalidateSelf();
	}

	private void configureMatrix(Rect bounds) {
		if (mBitmapShader != null) {
			Matrix matrix = createCenterCropMatrix(mDx, mDy, mBitmapWidth, mBitmapHeight, bounds);
			mBitmapShader.setLocalMatrix(matrix);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Drawable implementation

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		mRect.set(bounds);

		configureOverlayDrawableBounds(bounds);

		configureGradient(bounds);

		configureMatrix(bounds);
	}

	@Override
	public void draw(Canvas canvas) {
		boolean hasBitmap = mBitmapPaint.getShader() != null;

		if (mUrlBitmapDrawable != null) {
			boolean hasLoadedBitmap = mUrlBitmapDrawable.hasLoadedBitmap();
			if (hasLoadedBitmap && !hasBitmap) {
				setBitmap(mUrlBitmapDrawable.getBitmap());
				hasBitmap = true;
			}
			else if (!hasLoadedBitmap && hasBitmap) {
				mBitmapPaint.setShader(null);
				hasBitmap = false;
			}
		}

		if (hasBitmap) {
			// Draw the bitmap (possibly with rounded corners)
			if (mCornerMode == CornerMode.NONE) {
				canvas.drawRect(mRect, mBitmapPaint);
			}
			else {
				canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mBitmapPaint);

				if (mCornerMode == CornerMode.TOP) {
					float width = mRect.width();
					float height = mRect.height();

					// Overdraw bottom left corner (without rounding)
					canvas.drawRect(0, height - mCornerRadius, mCornerRadius, height, mBitmapPaint);

					// Overdraw bottom right corner (without rounding)
					canvas.drawRect(width - mCornerRadius, height - mCornerRadius, width, height, mBitmapPaint);
				}
			}
		}
		else {
			// If we don't have a bitmap, rely on UrlBitmapDrawable to render (and kick off a load)
			mUrlBitmapDrawable.draw(canvas);
		}

		// Draw the gradient (if set)
		if (mGradientPaint.getShader() != null) {
			canvas.drawRect(mRect, mGradientPaint);
		}

		// Draw overlay (if set)
		if (mOverlayDrawable != null) {
			mOverlayDrawable.draw(canvas);
		}
	}

	@Override
	public int getOpacity() {
		if (mCornerMode == CornerMode.NONE) {
			return PixelFormat.OPAQUE;
		}
		else {
			return PixelFormat.TRANSLUCENT;
		}
	}

	@Override
	public void setAlpha(int alpha) {
		mBitmapPaint.setAlpha(alpha);

		if (mOverlayDrawable != null) {
			mOverlayDrawable.setAlpha(alpha);
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mBitmapPaint.setColorFilter(cf);

		if (mOverlayDrawable != null) {
			mOverlayDrawable.setColorFilter(cf);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility

	private Matrix createCenterCropMatrix(float dX, float dY, int bitmapWidth, int bitmapHeight, Rect bounds) {
		Matrix matrix = new Matrix();

		int vwidth = bounds.width();
		int vheight = bounds.height();

		float scale;
		if (bitmapWidth * vheight > vwidth * bitmapHeight) {
			scale = (float) vheight / (float) bitmapHeight;
			dX = (vwidth - bitmapWidth * scale) * 0.5f;
		}
		else {
			scale = (float) vwidth / (float) bitmapWidth;
		}

		matrix.setScale(scale, scale);
		matrix.postTranslate((int) (dX + 0.5f), (int) dY);

		return matrix;
	}

	//////////////////////////////////////////////////////////////////////////
	// OnImageLoaded

	@Override
	public void onImageLoaded(String url, Bitmap bitmap) {
		setBitmap(bitmap);
	}

	@Override
	public void onImageLoadFailed(String url) {
		invalidateSelf();
	}

}
