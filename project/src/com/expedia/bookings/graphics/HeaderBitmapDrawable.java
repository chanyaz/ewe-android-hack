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

import com.mobiata.android.Log;
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
 * Possible TODO: Add ScaleType so that we can center images differently
 */
public class HeaderBitmapDrawable extends Drawable implements OnImageLoaded {

	public enum CornerMode {
		ALL,
		TOP,
		NONE
	}

	// This represents where the current bitmap shader came from
	// It helps distinguish when we're upgrading from one source
	// to another (e.g., null to placeholder, or placeholder to bitmap)
	private enum Source {
		BITMAP,
		PLACEHOLDER
	}

	private CornerMode mCornerMode = CornerMode.NONE;
	private Source mSource = null;

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
	private float mDx;
	private float mDy;
	private int mBitmapWidth;
	private int mBitmapHeight;

	// Cached for draw speed
	private final RectF mRect = new RectF();

	// If you want to use an underlying UrlBitmapDrawable to drive image loading functionality
	private UrlBitmapDrawable mUrlBitmapDrawable;

	// If you want to use an underlying Drawable to drive placeholder iamges
	private Drawable mPlaceholderDrawable;

	public HeaderBitmapDrawable() {
		mBitmapPaint = new Paint();
		mBitmapPaint.setAntiAlias(true);

		mGradientPaint = new Paint();
		mGradientPaint.setAntiAlias(true);
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

	private void clearState() {
		if (mUrlBitmapDrawable != null) {
			mUrlBitmapDrawable.setOnImageLoadedCallback(null);
		}

		mUrlBitmapDrawable = null;
		mPlaceholderDrawable = null;
		mBitmapShader = null;
		mBitmapPaint.setShader(null);
		mSource = null;
	}

	public void setBitmap(Bitmap bitmap) {
		setBitmap(bitmap, true);
	}

	private void setBitmap(Bitmap bitmap, boolean invalidateSelf) {
		configureBitmap(bitmap);
		configureMatrix(getBounds());

		if (invalidateSelf) {
			invalidateSelf();
		}
	}

	public void setUrlBitmapDrawable(UrlBitmapDrawable urlBitmapDrawable) {
		clearState();

		mUrlBitmapDrawable = urlBitmapDrawable;
		mUrlBitmapDrawable.setOnImageLoadedCallback(this);

		invalidateSelf();
	}

	private void configureBitmap(Bitmap bitmap) {
		mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		mBitmapWidth = bitmap.getWidth();
		mBitmapHeight = bitmap.getHeight();

		mBitmapPaint.setShader(mBitmapShader);
		mSource = Source.BITMAP;
	}

	public void setPlaceholderDrawable(Drawable placeholderDrawable) {
		clearState();

		mPlaceholderDrawable = placeholderDrawable;

		invalidateSelf();
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

	public void setMatrixTranslation(float dX, float dY) {
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

		if (mUrlBitmapDrawable != null) {
			mUrlBitmapDrawable.setBounds(bounds);
		}

		mRect.set(bounds);

		configureOverlayDrawableBounds(bounds);

		configureGradient(bounds);

		configureMatrix(bounds);
	}

	@Override
	public void draw(Canvas canvas) {
		if (mUrlBitmapDrawable != null) {
			boolean hasLoadedBitmap = mUrlBitmapDrawable.hasLoadedBitmap();
			if (hasLoadedBitmap && mSource != Source.BITMAP) {
				setBitmap(mUrlBitmapDrawable.getBitmap(), false);
				mSource = Source.BITMAP;
			}
			else if (!hasLoadedBitmap && mSource == Source.BITMAP) {
				mBitmapPaint.setShader(null);
				mSource = null;
			}
		}

		// If we have nothing, try to upgrade to placeholder
		if (mSource == null) {
			Drawable drawable = null;
			if (mUrlBitmapDrawable != null) {
				drawable = mUrlBitmapDrawable;
			}
			else if (mPlaceholderDrawable != null) {
				drawable = mPlaceholderDrawable;
			}

			if (drawable != null) {
				int width = drawable.getIntrinsicWidth();
				int height = drawable.getIntrinsicHeight();

				if (width <= 0) {
					width = (int) mRect.width();
				}
				if (height <= 0) {
					height = (int) mRect.height();
				}

				Bitmap placeholderBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				Canvas placeholderCanvas = new Canvas(placeholderBitmap);
				drawable.setBounds(0, 0, width, height);
				drawable.draw(placeholderCanvas);
				setBitmap(placeholderBitmap, false);

				mSource = Source.PLACEHOLDER;
			}
		}

		if (mSource != null) {
			paintWithPossiblyRoundedCorners(canvas, mBitmapPaint);
		}

		// Draw the gradient (if set)
		if (mGradientPaint.getShader() != null) {
			paintWithPossiblyRoundedCorners(canvas, mGradientPaint);
		}

		// Draw overlay (if set)
		if (mOverlayDrawable != null) {
			mOverlayDrawable.draw(canvas);
		}
	}

	private void paintWithPossiblyRoundedCorners(Canvas canvas, Paint paint) {
		if (mCornerMode == CornerMode.NONE) {
			canvas.drawRect(mRect, paint);
		}
		else {
			canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, paint);

			if (mCornerMode == CornerMode.TOP) {
				float width = mRect.width();
				float height = mRect.height();

				// Overdraw bottom left corner (without rounding)
				canvas.drawRect(0, height - mCornerRadius, mCornerRadius, height, paint);

				// Overdraw bottom right corner (without rounding)
				canvas.drawRect(width - mCornerRadius, height - mCornerRadius, width, height, paint);
			}
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

			if (dX == 0) {
				dX = (vwidth - bitmapWidth * scale) * 0.5f;
			}
		}
		else {
			scale = (float) vwidth / (float) bitmapWidth;

			if (dY == 0) {
				dY = (vheight - bitmapHeight * scale) * 0.5f;
			}
		}

		matrix.setScale(scale, scale);

		matrix.postTranslate((int) (dX + 0.5f), (int) (dY + 0.5f));

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
