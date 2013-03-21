package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.expedia.bookings.R;

public class ItinHeaderImageView extends OptimizedImageView {
	public static final int MODE_MINI = 0;
	public static final int MODE_SUMMARY = 1;
	public static final int MODE_FULL = 2;

	private int mMode = MODE_MINI;
	private int mRadius = 10;

	private Rect mBounds;
	private Bitmap mCompositeBitmap;
	private Canvas mCompositeCanvas;

	private Paint mMaskPaint;
	private Bitmap mTLMaskBitmap;
	private Bitmap mTRMaskBitmap;
	private Bitmap mBLMaskBitmap;
	private Bitmap mBRMaskBitmap;
	private Canvas mTLMaskCanvas;
	private Canvas mTRMaskCanvas;
	private Canvas mBLMaskCanvas;
	private Canvas mBRMaskCanvas;

	private Drawable mHighlightDrawable;

	public ItinHeaderImageView(Context context) {
		super(context);
		init(context, null, 0);
	}

	public ItinHeaderImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public ItinHeaderImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		mBounds = new Rect();
		mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaskPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));

		mHighlightDrawable = context.getResources().getDrawable(R.drawable.card_top_lighting);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ItinHeaderImageView);
		setMode(a.getInteger(R.styleable.ItinHeaderImageView_mode, mMode));
		setRadius(a.getDimensionPixelSize(R.styleable.ItinHeaderImageView_radius, mRadius));
		a.recycle();
	}

	public void setMode(int mode) {
		if (mode != mMode) {
			mMode = mode;
			invalidate();
		}
	}

	public void setRadius(int radius) {
		if (radius != mRadius) {
			mRadius = radius;
			createMasks();
			invalidate();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mBounds.set(0, 0, w, h);
		mHighlightDrawable.setBounds(mBounds);

		// Only create a new bitmap if it needs to be bigger
		if (mCompositeBitmap == null || w > oldw) {
			mCompositeBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			mCompositeCanvas = new Canvas(mCompositeBitmap);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Use native drawing if we're not altering the image
		if (mMode == MODE_FULL || mCompositeBitmap == null || getDrawable() == null) {
			super.onDraw(canvas);
			return;
		}

		// Draw image
		super.onDraw(mCompositeCanvas);

		// Draw masks
		mCompositeCanvas.drawBitmap(mTLMaskBitmap, 0, 0, mMaskPaint);
		mCompositeCanvas.drawBitmap(mTRMaskBitmap, mBounds.right - mRadius, 0, mMaskPaint);

		if (mMode == MODE_MINI) {
			mCompositeCanvas.drawBitmap(mBLMaskBitmap, 0, mBounds.bottom - mRadius, mMaskPaint);
			mCompositeCanvas.drawBitmap(mBRMaskBitmap, mBounds.right - mRadius, mBounds.bottom - mRadius, mMaskPaint);
		}

		// Draw highlight
		mHighlightDrawable.draw(mCompositeCanvas);

		// Draw composite (must use composite bitmap because original bitmap may not be ARGB_8888)
		canvas.drawBitmap(mCompositeBitmap, 0, 0, null);
	}

	private void createMasks() {
		// Here we create the mask bitmaps. We use four different bitmaps so we don't hold all that
		// transparent image in memory. Also this means we don't have to create a new mask bitmap
		// every time the view is resized, but rather reposition the corners in onDraw().

		// Top left mask
		final RectF rect = new RectF(0, 0, mRadius * 2, mRadius * 2);

		mTLMaskBitmap = Bitmap.createBitmap(mRadius, mRadius, Config.ARGB_8888);
		mTLMaskCanvas = new Canvas(mTLMaskBitmap);
		mTLMaskCanvas.drawColor(Color.BLACK);
		mTLMaskCanvas.drawRoundRect(rect, mRadius, mRadius, mMaskPaint);

		// Top right mask
		rect.set(-mRadius, 0, mRadius, mRadius * 2);

		mTRMaskBitmap = Bitmap.createBitmap(mRadius, mRadius, Config.ARGB_8888);
		mTRMaskCanvas = new Canvas(mTRMaskBitmap);
		mTRMaskCanvas.drawColor(Color.BLACK);
		mTRMaskCanvas.drawRoundRect(rect, mRadius, mRadius, mMaskPaint);

		// Bottom left mask
		rect.set(0, -mRadius, mRadius * 2, mRadius);

		mBLMaskBitmap = Bitmap.createBitmap(mRadius, mRadius, Config.ARGB_8888);
		mBLMaskCanvas = new Canvas(mBLMaskBitmap);
		mBLMaskCanvas.drawColor(Color.BLACK);
		mBLMaskCanvas.drawRoundRect(rect, mRadius, mRadius, mMaskPaint);

		// Bottom right mask
		rect.set(-mRadius, -mRadius, mRadius, mRadius);

		mBRMaskBitmap = Bitmap.createBitmap(mRadius, mRadius, Config.ARGB_8888);
		mBRMaskCanvas = new Canvas(mBRMaskBitmap);
		mBRMaskCanvas.drawColor(Color.BLACK);
		mBRMaskCanvas.drawRoundRect(rect, mRadius, mRadius, mMaskPaint);
	}
}