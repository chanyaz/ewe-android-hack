package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.expedia.bookings.R;

public class DumbTagProgressBar extends SurfaceView implements SurfaceHolder.Callback {
	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private Activity mParent;

	private boolean mShowProgress = true;
	private String mText;

	private int mOrientation;
	private float mScaledDensity;

	private DrawingThread mDrawingThread;
	private Paint mPaint;

	private Bitmap mBackgroundBitmap;
	private Bitmap mTagBitmap;
	private Bitmap mKnobBitmap;
	private Bitmap mKnobBgBitmap;
	private Bitmap mRingBitmap;
	private Bitmap mRingFillBitmap;

	private Rect mSurfaceRect;

	private Rect mBackgroundSrcRect;
	private Rect mTagSrcRect;
	private Rect mKnobSrcRect;
	private Rect mKnobBgSrcRect;
	private Rect mRingSrcRect;
	private Rect mRingFillSrcRect;

	private Rect mTagDestRect;
	private Rect mKnobDestRect;
	private Rect mKnobBgDestRect;
	private Rect mRingDestRect;
	private Rect mRingFillDestRect;

	private int mTagWidth;
	private int mTagHeight;
	private int mKnobBgWidth;
	private int mKnobBgHeight;
	private int mKnobWidth;
	private int mKnobHeight;
	private int mRingWidth;
	private int mRingHeight;
	private int mRingFillWidth;
	private int mRingFillHeight;

	private int mOffsetY;
	private int mRingMargin;
	private int mRingLeftOffset;

	private int mTagCenterX;
	private int mTagCenterY;
	private int mRingFillCenterX;
	private int mRingFillCenterY;

	private int mWidth;
	private int mHeight;

	private TextPaint mTextPaint;
	private StaticLayout mTextLayout;
	private float mTextLayoutDx;
	private float mTextLayoutDy;

	//////////////////////////////////////////////////////////////////////////////////
	// Constructors

	public DumbTagProgressBar(Context context) {
		super(context);
		init(context);
	}

	public DumbTagProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);

		setText(attrs.getAttributeValue("android", "text"));
		setTextColor(attrs.getAttributeIntValue("android", "textColor", 0xFF555555));
		setTextSize(attrs.getAttributeIntValue("android", "textSize", 16));
		setTextStyle(attrs.getAttributeIntValue("android", "textStyle", Typeface.NORMAL));
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mWidth = width;
		mHeight = height;
		mSurfaceRect = new Rect(0, 0, width, height);

		calculateMeasurements();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setKeepScreenOn(true);
		loadResources();

		mDrawingThread = new DrawingThread(getHolder());
		mDrawingThread.setRunning(true);
		mDrawingThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		setKeepScreenOn(false);

		boolean retry = true;
		mDrawingThread.setRunning(false);
		while (retry) {
			try {
				mDrawingThread.join();
				retry = false;
			}
			catch (InterruptedException e) {
			}
		}

		recycleResources();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public boolean getShowProgress() {
		return mShowProgress;
	}

	public String getText() {
		return mText;
	}

	public void setBackgroundBitmap(Bitmap bitmap) {
		if (mBackgroundBitmap != null) {
			mBackgroundBitmap.recycle();
		}

		mBackgroundBitmap = bitmap;
		mBackgroundSrcRect = new Rect(0, 0, mBackgroundBitmap.getWidth(), mBackgroundBitmap.getHeight());
	}

	public void setBackgroundResource(int resId) {
		setBackgroundBitmap(BitmapFactory.decodeResource(getResources(), resId));
	}

	public void setShowProgress(boolean showProgress) {
		mShowProgress = showProgress;
	}

	public void setText(int resId) {
		setText(mParent.getString(resId));
	}

	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		mText = text;

		calculateTextLayout();
	}

	private void calculateTextLayout() {
		mTextLayout = new StaticLayout(mText, mTextPaint, (int) (mWidth * 0.9f), Alignment.ALIGN_CENTER, 1, 0, true);
		mTextLayoutDx = mWidth * 0.05f;
		mTextLayoutDy = mHeight - (((mHeight - (mOffsetY + mTagHeight)) + mTextLayout.getHeight()) / 2);

		if (mOrientation == Surface.ROTATION_90 || mOrientation == Surface.ROTATION_270) {
			mTextLayoutDy = (mOffsetY) / 2;
		}
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
		mTextPaint.setShadowLayer(0.1f * mScaledDensity, 0, 1 * mScaledDensity, 0x88FFFFFF);
	}

	public void setTextSize(int textSize) {
		mTextPaint.setTextSize(textSize * mScaledDensity);
	}

	public void setTextStyle(int style) {
		setTypeface(Typeface.defaultFromStyle(style));
	}

	public void setTypeface(Typeface typeface) {
		mTextPaint.setTypeface(typeface);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	private void calculateMeasurements() {
		// NOTE: A few of these measurements are pretty arbitrary, definitely
		// making this view a one time use kind of view.

		mOffsetY = (int) ((float) mHeight * 0.15f);
		if (mOrientation == Surface.ROTATION_90 || mOrientation == Surface.ROTATION_270) {
			mOffsetY = (int) ((float) mHeight * 0.25f);
		}

		mRingMargin = (mTagWidth - mRingWidth) / 2;
		mRingLeftOffset = (int) (mTagWidth * 0.028f);

		mTagCenterX = mWidth / 2;
		mTagCenterY = mOffsetY + (int) (mTagWidth / 2);

		final int knobTopOffset = (int) (mKnobHeight * 0.09f);

		// DEST RECTS
		mTagDestRect = new Rect();
		mTagDestRect.top = mTagCenterY - (int) (mTagWidth * 0.38);
		mTagDestRect.bottom = mTagDestRect.top + mTagHeight;
		mTagDestRect.left = (int) (mTagCenterX - (mTagWidth / 2));
		mTagDestRect.right = mTagDestRect.left + mTagWidth;

		mKnobBgDestRect = new Rect();
		mKnobBgDestRect.top = mTagCenterY - (mKnobBgHeight / 2);
		mKnobBgDestRect.bottom = mKnobBgDestRect.top + mKnobBgHeight;
		mKnobBgDestRect.left = (int) (mTagCenterX - (mKnobBgWidth / 2));
		mKnobBgDestRect.right = mKnobBgDestRect.left + mKnobBgWidth;

		mKnobDestRect = new Rect();
		mKnobDestRect.top = mTagCenterY - (mKnobHeight / 2) + knobTopOffset;
		mKnobDestRect.bottom = mKnobDestRect.top + mKnobHeight;
		mKnobDestRect.left = (int) (mTagCenterX - (mKnobWidth / 2));
		mKnobDestRect.right = mKnobDestRect.left + mKnobWidth;

		mRingDestRect = new Rect();
		mRingDestRect.top = mTagDestRect.bottom - mRingHeight - mRingMargin;
		mRingDestRect.bottom = mRingDestRect.top + mRingHeight;
		mRingDestRect.left = (int) (mTagCenterX - (mRingWidth / 2)) + mRingLeftOffset;
		mRingDestRect.right = mRingDestRect.left + mRingWidth;

		mRingFillDestRect = new Rect();
		mRingFillDestRect.top = mRingDestRect.top + ((mRingHeight - mRingFillHeight) / 2);
		mRingFillDestRect.bottom = mRingFillDestRect.top + mRingFillHeight;
		mRingFillDestRect.left = (int) (mTagCenterX - (mRingFillWidth / 2)) + mRingLeftOffset;
		mRingFillDestRect.right = mRingFillDestRect.left + mRingFillWidth;

		mRingFillCenterX = mRingFillDestRect.left + (mRingFillWidth / 2);
		mRingFillCenterY = mRingFillDestRect.top + (mRingFillHeight / 2);

		calculateTextLayout();
	}

	private void init(Context context) {
		setFocusableInTouchMode(true);

		getHolder().addCallback(this);

		mParent = (Activity) context;
		mOrientation = mParent.getWindowManager().getDefaultDisplay().getOrientation();

		DisplayMetrics metrics = new DisplayMetrics();
		mParent.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		mScaledDensity = metrics.scaledDensity;

		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
	}

	private void loadResources() {
		mTagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progress_tag);
		mTagWidth = mTagBitmap.getWidth();
		mTagHeight = mTagBitmap.getHeight();
		mTagSrcRect = new Rect(0, 0, mTagWidth, mTagHeight);

		mKnobBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progress_knob);
		mKnobWidth = mKnobBitmap.getWidth();
		mKnobHeight = mKnobBitmap.getHeight();
		mKnobSrcRect = new Rect(0, 0, mKnobWidth, mKnobHeight);

		mKnobBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progress_knob_bg);
		mKnobBgWidth = mKnobBgBitmap.getWidth();
		mKnobBgHeight = mKnobBgBitmap.getHeight();
		mKnobBgSrcRect = new Rect(0, 0, mKnobBgWidth, mKnobBgHeight);

		mRingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progress_ring);
		mRingWidth = mRingBitmap.getWidth();
		mRingHeight = mRingBitmap.getHeight();
		mRingSrcRect = new Rect(0, 0, mRingWidth, mRingHeight);

		mRingFillBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progress_ring_fill);
		mRingFillWidth = mRingFillBitmap.getWidth();
		mRingFillHeight = mRingFillBitmap.getHeight();
		mRingFillSrcRect = new Rect(0, 0, mRingFillWidth, mRingFillHeight);
	}

	private void recycleResources() {
		if (mBackgroundBitmap != null) {
			mBackgroundBitmap.recycle();
		}
		if (mTagBitmap != null) {
			mTagBitmap.recycle();
		}
		if (mKnobBitmap != null) {
			mKnobBitmap.recycle();
		}
		if (mKnobBgBitmap != null) {
			mKnobBgBitmap.recycle();
		}
		if (mKnobBgBitmap != null) {
			mRingBitmap.recycle();
		}
		if (mRingFillBitmap != null) {
			mRingFillBitmap.recycle();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private classes

	class DrawingThread extends Thread {
		//////////////////////////////////////////////////////////////////////////////
		// Constants

		private final static double DEGREES_PER_SECOND = 2 * Math.PI;

		//////////////////////////////////////////////////////////////////////////////
		// Private members

		private SurfaceHolder mSurfaceHolder;

		private boolean mRunning = false;

		float mRingAngle;
		float mTagDegrees;
		float mRingDegrees;

		//////////////////////////////////////////////////////////////////////////////
		// Constructor

		public DrawingThread(SurfaceHolder surfaceHolder) {
			mSurfaceHolder = surfaceHolder;
		}

		//////////////////////////////////////////////////////////////////////////////
		// Overrides

		@Override
		public void run() {
			Canvas c = null;
			try {
				c = mSurfaceHolder.lockCanvas(null);
				synchronized (mSurfaceHolder) {
					doDraw(c);
				}
			}
			finally {
				if (c != null) {
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}

			if (mRunning) {
				postDelayed(mDrawingThread, 50);
			}
		}

		//////////////////////////////////////////////////////////////////////////////
		// Public methods

		public void setRunning(boolean run) {
			mRunning = run;
		}

		//////////////////////////////////////////////////////////////////////////////
		// Private methods

		private void doDraw(Canvas canvas) {
			if (canvas != null) {
				draw(canvas);
			}
		}

		private void draw(Canvas canvas) {
			mRingAngle = (float) normalizeAngle(((double) System.currentTimeMillis() / 1000) * DEGREES_PER_SECOND);
			mRingDegrees = (float) (mRingAngle * 180.0d / Math.PI);

			// CLEAR CANVAS WITH WHITE
			canvas.drawColor(0xFFe4e4e4);

			// DRAW TEXT
			canvas.save();
			canvas.translate(mTextLayoutDx, mTextLayoutDy);
			mTextLayout.draw(canvas);
			canvas.restore();

			// DRAW BACKGROUND
			// TOO EXPENSIVE! :(
			//			if (mBackgroundBitmap != null && !mBackgroundBitmap.isRecycled()) {
			//				canvas.drawBitmap(mBackgroundBitmap, mBackgroundSrcRect, mSurfaceRect, mPaint);
			//			}

			// DOOR KNOB BACKGROUND
			canvas.drawBitmap(mKnobBgBitmap, mKnobBgSrcRect, mKnobBgDestRect, mPaint);

			// DRAW TAG =D
			canvas.drawBitmap(mTagBitmap, mTagSrcRect, mTagDestRect, mPaint);

			// DRAW PROGRESS RING
			if (mShowProgress) {
				canvas.drawBitmap(mRingBitmap, mRingSrcRect, mRingDestRect, mPaint);

				canvas.rotate(mRingDegrees, mRingFillCenterX, mRingFillCenterY);
				canvas.drawBitmap(mRingFillBitmap, mRingFillSrcRect, mRingFillDestRect, mPaint);
				canvas.rotate(-mRingDegrees, mRingFillCenterX, mRingFillCenterY);
			}

			// DRAW DOOR KNOB
			canvas.rotate(-mTagDegrees, mTagCenterX, mTagCenterY);
			canvas.drawBitmap(mKnobBitmap, mKnobSrcRect, mKnobDestRect, mPaint);
		}

		private double normalizeAngle(double angle) {
			return angle % (2 * Math.PI);
		}
	}
}