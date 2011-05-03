package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.expedia.bookings.R;

public class TagProgressBar extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener, OnTouchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private Activity mParent;

	private boolean mShowProgress = true;
	private String mText;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private float[] mAcceleration;
	private int mOrientation;
	private float mScaledDensity;

	private boolean mTagGrabbed = false;

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

	//////////////////////////////////////////////////////////////////////////////////
	// Constructors

	public TagProgressBar(Context context) {
		super(context);
		init(context);
	}

	public TagProgressBar(Context context, AttributeSet attrs) {
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

		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

		mDrawingThread = new DrawingThread(getHolder());
		mDrawingThread.setRunning(true);
		mDrawingThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		setKeepScreenOn(false);

		mSensorManager.unregisterListener(this);

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
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			if (isInsideTag(x, y)) {
				mTagGrabbed = true;

				if (mDrawingThread != null) {
					mDrawingThread.setAngleAndVelocityByTouchPoint(x, y);
				}
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			if (mTagGrabbed) {
				if (mDrawingThread != null) {
					mDrawingThread.setAngleAndVelocityByTouchPoint(x, y);
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP: {
			mTagGrabbed = false;
			break;
		}
		}

		return true;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Listener implementations

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		mAcceleration = event.values;
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

	private void init(Context context) {
		setOnTouchListener(this);
		setFocusableInTouchMode(true);

		getHolder().addCallback(this);
		mOrientation = getResources().getConfiguration().orientation;

		mParent = (Activity) context;
		mSensorManager = (SensorManager) mParent.getSystemService(Activity.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		DisplayMetrics metrics = new DisplayMetrics();
		mParent.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		mScaledDensity = metrics.scaledDensity;

		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);

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

	private boolean isInsideTag(float x, float y) {
		if (mDrawingThread != null) {
			final double angle = mDrawingThread.getAngle();
			final double dx = x - mTagCenterX;
			final double dy = y - mTagCenterY;
			final double newX = mTagCenterX - dx * Math.cos(angle) - dy * Math.sin(angle);
			final double newY = mTagCenterX - dx * Math.sin(angle) + dy * Math.cos(angle);

			final Rect tagRect = new Rect(mTagDestRect);
			tagRect.top += mOffsetY;
			tagRect.bottom += mOffsetY;

			return tagRect.contains((int) newX, (int) newY);
		}
		return false;
	}

	private void calculateMeasurements() {
		// NOTE: A few of these measurements are pretty arbitrary, definitely
		// making this view a one time use kind of view.

		mOffsetY = (int) ((float) mHeight * 0.15f);
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
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private classes

	class DrawingThread extends Thread {
		//////////////////////////////////////////////////////////////////////////////
		// Constants

		private final static double GRAVITY = 9.81d;
		private final static double MASS = 0.4d;
		private final static double LENGTH = 0.3d;

		private final static double FRICTION_HANDLE = 0.12d;
		private final static double FRICTION_DOOR = 0.13d;

		private final static double THRESH_DOOR_FRICTION_ANGLE = -8.5d;

		private final static double DEGREES_PER_SECOND = 2 * Math.PI;

		private final static int MAX_ANGULAR_VELOCITY = 150;

		//////////////////////////////////////////////////////////////////////////////
		// Private members

		private SurfaceHolder mSurfaceHolder;

		private boolean mRunning = false;
		private long mLastDrawTime = -1;
		private long mNow;

		private double mAngle;
		private double mAngularVelocity = 0;

		private double mLastAngle;
		private long mLastTagAngleSetByTouchTime;

		//////////////////////////////////////////////////////////////////////////////
		// Constructor

		public DrawingThread(SurfaceHolder surfaceHolder) {
			mSurfaceHolder = surfaceHolder;
		}

		//////////////////////////////////////////////////////////////////////////////
		// Overrides

		@Override
		public void run() {
			Canvas c;
			while (mRunning) {
				c = null;
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
			}
		}

		//////////////////////////////////////////////////////////////////////////////
		// Public methods

		public double getAngle() {
			return mAngle;
		}

		public void setAngleAndVelocityByTouchPoint(float x, float y) {
			long now = System.currentTimeMillis();
			if (mLastTagAngleSetByTouchTime < 0) {
				mLastTagAngleSetByTouchTime = now;
			}
			float delta = (float) (now - mLastTagAngleSetByTouchTime) / 1000f;
			while (delta < 0.01f) {
				try {
					// we need to sleep here if we haven't spend enough time
					// between calculations. If delta is 0 we'll get a divide
					// by zero error.
					sleep(10);

					now = System.currentTimeMillis();
					delta = (float) (now - mLastTagAngleSetByTouchTime) / 1000f;
				}
				catch (InterruptedException e) {
				}
			}

			mLastAngle = mAngle;
			mAngle = Math.atan2(y - mTagCenterY, x - mTagCenterX) - (Math.PI / 2);
			mAngularVelocity = (mAngle - mLastAngle) / delta;

			if (mAngularVelocity < -MAX_ANGULAR_VELOCITY) {
				mAngularVelocity = -MAX_ANGULAR_VELOCITY;
			}
			else if (mAngularVelocity > MAX_ANGULAR_VELOCITY) {
				mAngularVelocity = MAX_ANGULAR_VELOCITY;
			}

			mLastTagAngleSetByTouchTime = now;
		}

		public void setRunning(boolean run) {
			mRunning = run;
		}

		//////////////////////////////////////////////////////////////////////////////
		// Private methods

		private void doDraw(Canvas canvas) {
			if (canvas != null) {
				mNow = System.currentTimeMillis();
				if (mLastDrawTime < 0) {
					mLastDrawTime = mNow;
				}
				final float delta = (float) (mNow - mLastDrawTime) / 1000f;

				if (!mTagGrabbed) {
					updatePhysics(delta);
				}
				draw(canvas);

				mLastDrawTime = mNow;
			}
		}

		//////////////////////////////////////////////////////////////////////////////

		private void updatePhysics(double delta) {
			if (mAcceleration == null) {
				return;
			}

			final float x = mAcceleration[0] * -1;
			final float y = mAcceleration[1] * -1;
			final float z = mAcceleration[2] * -1;

			// !!! Notes taken from Andy's work on the iOS version !!!

			// Find moment of inertia using I = (1/3) * m * L^2  (this is the moment of inertia equation for a rod of length L and mass m, with the axis of rotation at the end of the rod)
			final double inertia = MASS * LENGTH * LENGTH / 3;
			final double magnitude = Math.sqrt(x * x + y * y);

			// Find angle between Position vector and force vector
			// atan2 produces an angle that is positive for counter-clockwise angles (upper half-plane, y > 0), and negative for clockwise angles (lower half-plane, y < 0)
			final double angle = Math.atan2(y, x);
			final double tagAngle = 1.5 * Math.PI - mAngle;
			final double angleDifference = tagAngle - angle;

			// Force from the accelerometer is calculated using F = m * a
			final double force = MASS * magnitude;

			// Calculate Torque about pivot point using t = rFsin(Theta), where r is the position magnitude, F is the force magnitude, and theta is the angle between the 2 vectors
			final double torque = LENGTH * force * Math.sin(angleDifference);

			// Calculate Friction
			double frictionTorque = LENGTH * (FRICTION_HANDLE * magnitude) * Math.sin(angleDifference);
			if ((mAngularVelocity < 0.0 && frictionTorque < 0.0) || (mAngularVelocity > 0.0 && frictionTorque > 0.0)) {
				frictionTorque *= -1.0;
			}

			// Calculate net torque
			final double netTorque = torque + frictionTorque;

			// Find angular acceleration using T = Ia where T is torque, I is moment of inertia, and a is angular acceleration
			final double angularAcceleration = netTorque / inertia;

			mAngularVelocity += angularAcceleration * delta;

			// Apply door friction

			// Get amount of friction by the amount of acceleration on z.
			double frictionZ = FRICTION_DOOR * z / -GRAVITY;
			if (frictionZ > 1) {
				frictionZ = 1;
			}

			// This applies the friction past a certain threshold of angle and only if the friction is positive (lying on its back).
			if (frictionZ > 0 && z < THRESH_DOOR_FRICTION_ANGLE) {
				mAngularVelocity *= (1 - frictionZ);
			}

			// This sets the velocity to zero if it's on its back far enough and there's not enough velocity to overcome friction.
			if (z < THRESH_DOOR_FRICTION_ANGLE && mAngularVelocity < 0.05 && mAngularVelocity > -0.05) {
				mAngularVelocity = 0;
			}

			final double changeInAngle = mAngularVelocity * delta;
			mAngle += changeInAngle;
			mAngle = normalizeAngle(mAngle);

			mLastTagAngleSetByTouchTime = 0;
		}

		private void draw(Canvas canvas) {
			final float ringAngle = (float) normalizeAngle(((double) mNow / 1000) * DEGREES_PER_SECOND);
			final float tagDegrees = (float) (mAngle * 180.0d / Math.PI);
			final float ringDegrees = (float) (ringAngle * 180.0d / Math.PI);

			if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
				//canvas.rotate(90, mTagCenterX, mTagCenterY);
			}

			// CLEAR CANVAS WITH WHITE
			canvas.drawColor(0xFFe4e4e4);

			// DRAW BACKGROUND
			// TOO EXPENSIVE! :(
			//			if (mBackgroundBitmap != null && !mBackgroundBitmap.isRecycled()) {
			//				canvas.drawBitmap(mBackgroundBitmap, mBackgroundSrcRect, mSurfaceRect, mPaint);
			//			}

			// DOOR KNOB BACKGROUND
			canvas.drawBitmap(mKnobBgBitmap, mKnobBgSrcRect, mKnobBgDestRect, mPaint);

			// save canvas
			canvas.save();

			// DRAW TAG =D
			canvas.rotate(tagDegrees, mTagCenterX, mTagCenterY);
			canvas.drawBitmap(mTagBitmap, mTagSrcRect, mTagDestRect, mPaint);

			// DRAW PROGRESS RING
			if (mShowProgress) {
				canvas.drawBitmap(mRingBitmap, mRingSrcRect, mRingDestRect, mPaint);

				canvas.rotate(ringDegrees, mRingFillCenterX, mRingFillCenterY);
				canvas.drawBitmap(mRingFillBitmap, mRingFillSrcRect, mRingFillDestRect, mPaint);
				canvas.rotate(-ringDegrees, mRingFillCenterX, mRingFillCenterY);
			}

			// DRAW DOOR KNOB
			canvas.rotate(-tagDegrees, mTagCenterX, mTagCenterY);
			canvas.drawBitmap(mKnobBitmap, mKnobSrcRect, mKnobDestRect, mPaint);

			// restore saved canvas
			canvas.restore();

			// DRAW TEXT
			StaticLayout layout = new StaticLayout(mText, mTextPaint, (int) (mWidth * 0.9f), Alignment.ALIGN_CENTER, 1,
					0, true);
			final int layoutHeight = layout.getHeight();
			final float dx = mWidth * 0.05f;
			final float dy = mHeight - (((mHeight - (mOffsetY + mTagHeight)) + layoutHeight) / 2);
			canvas.save();
			canvas.translate(dx, dy);
			layout.draw(canvas);
			canvas.restore();
		}

		private double normalizeAngle(double angle) {
			return angle % (2 * Math.PI);
		}
	}
}