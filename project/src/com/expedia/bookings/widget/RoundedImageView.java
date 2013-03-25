package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.expedia.bookings.R;

public class RoundedImageView extends OptimizedImageView {
	private Rect mBounds;
	private Bitmap mCompositeBitmap;
	private Canvas mCompositeCanvas;

	private int mTLRadius = -1;
	private int mTRRadius = -1;
	private int mBLRadius = -1;
	private int mBRRadius = -1;

	private Paint mMaskPaint;
	private Bitmap mTLMaskBitmap;
	private Bitmap mTRMaskBitmap;
	private Bitmap mBLMaskBitmap;
	private Bitmap mBRMaskBitmap;
	private Canvas mTLMaskCanvas;
	private Canvas mTRMaskCanvas;
	private Canvas mBLMaskCanvas;
	private Canvas mBRMaskCanvas;

	public RoundedImageView(Context context) {
		super(context);
		init(context, null, 0);
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		mBounds = new Rect();
		mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaskPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);

		int tl = a.getDimensionPixelSize(R.styleable.RoundedImageView_radiusTL, 0);
		int tr = a.getDimensionPixelSize(R.styleable.RoundedImageView_radiusTR, 0);
		int bl = a.getDimensionPixelSize(R.styleable.RoundedImageView_radiusBL, 0);
		int br = a.getDimensionPixelSize(R.styleable.RoundedImageView_radiusBR, 0);
		a.recycle();

		setRadius(tl, tr, bl, br);
	}

	public void setRadius(int tl, int tr, int bl, int br) {
		boolean changed = false;

		if (mTLRadius != tl) {
			mTLRadius = tl;
			changed = true;
		}

		if (mTRRadius != tr) {
			mTRRadius = tr;
			changed = true;
		}

		if (mBLRadius != bl) {
			mBLRadius = bl;
			changed = true;
		}

		if (mBRRadius != br) {
			mBRRadius = br;
			changed = true;
		}

		if (changed) {
			createMasks();
			invalidate();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mBounds.set(0, 0, w, h);

		// Only create a new bitmap if it needs to be bigger
		if (mCompositeBitmap == null || w > oldw) {
			mCompositeBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			mCompositeCanvas = new Canvas(mCompositeBitmap);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw image
		super.onDraw(mCompositeCanvas);

		// Draw masks
		if (mTLRadius > 0) {
			mCompositeCanvas.drawBitmap(mTLMaskBitmap, 0, 0, mMaskPaint);
		}

		if (mTRRadius > 0) {
			mCompositeCanvas.drawBitmap(mTRMaskBitmap, mBounds.right - mTRRadius, 0, mMaskPaint);
		}

		if (mBLRadius > 0) {
			mCompositeCanvas.drawBitmap(mBLMaskBitmap, 0, mBounds.bottom - mBLRadius, mMaskPaint);
		}

		if (mBRRadius > 0) {
			mCompositeCanvas.drawBitmap(mBRMaskBitmap, mBounds.right - mBRRadius, mBounds.bottom - mBRRadius, mMaskPaint);
		}

		// Draw composite (must use composite bitmap because original bitmap may not be ARGB_8888)
		canvas.drawBitmap(mCompositeBitmap, 0, 0, null);
	}

	private void createMasks() {
		// Here we create the mask bitmaps. We use four different bitmaps so we don't hold all that
		// transparent image in memory. Also this means we don't have to create a new mask bitmap
		// every time the view is resized, but rather reposition the corners in onDraw().

		final RectF rect = new RectF(0, 0, 0, 0);

		// Top left mask
		int radius = mTLRadius;
		if (radius > 0) {
			rect.set(0, 0, radius * 2, radius * 2);

			mTLMaskBitmap = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
			mTLMaskCanvas = new Canvas(mTLMaskBitmap);
			mTLMaskCanvas.drawColor(Color.BLACK);
			mTLMaskCanvas.drawRoundRect(rect, radius, radius, mMaskPaint);
		}

		// Top right mask
		radius = mTRRadius;
		if (radius > 0) {
			rect.set(-radius, 0, radius, radius * 2);

			mTRMaskBitmap = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
			mTRMaskCanvas = new Canvas(mTRMaskBitmap);
			mTRMaskCanvas.drawColor(Color.BLACK);
			mTRMaskCanvas.drawRoundRect(rect, radius, radius, mMaskPaint);
		}

		// Bottom left mask
		radius = mBLRadius;
		if (radius > 0) {
			rect.set(0, -radius, radius * 2, radius);

			mBLMaskBitmap = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
			mBLMaskCanvas = new Canvas(mBLMaskBitmap);
			mBLMaskCanvas.drawColor(Color.BLACK);
			mBLMaskCanvas.drawRoundRect(rect, radius, radius, mMaskPaint);
		}

		// Bottom right mask
		radius = mBRRadius;
		if (radius > 0) {
			rect.set(-radius, -radius, radius, radius);

			mBRMaskBitmap = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
			mBRMaskCanvas = new Canvas(mBRMaskBitmap);
			mBRMaskCanvas.drawColor(Color.BLACK);
			mBRMaskCanvas.drawRoundRect(rect, radius, radius, mMaskPaint);
		}
	}
}
