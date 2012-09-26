package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

public class PlaneWindowView extends SurfaceView implements SurfaceHolder.Callback {

	private PlaneThread mThread;

	private PlaneWindowListener mListener;

	public PlaneWindowView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initThread();
	}

	public void setListener(PlaneWindowListener listener) {
		mListener = listener;
	}

	public void setRendering(boolean rendering) {
		if (mThread != null) {
			mThread.setRendering(rendering);
		}
	}

	/**
	 * Sets whether or not the plane is "grounded"
	 * 
	 * Make sure to call before SurfaceView's initialization.
	 */
	public void setGrounded(boolean isGrounded) {
		mThread.mIsGrounded = isGrounded;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mThread != null && mThread.onTouchEvent(event)) {
			return true;
		}

		return super.onTouchEvent(event);
	}

	private void initThread() {
		if (mThread == null) {
			SurfaceHolder surfaceHolder = getHolder();
			surfaceHolder.addCallback(this);

			mThread = new PlaneThread(surfaceHolder);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Callbacks

	public interface PlaneWindowListener {
		public void onFirstRender();
	}

	//////////////////////////////////////////////////////////////////////////
	// SurfaceHolder.Callback

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v("surfaceCreated()");

		initThread();

		mThread.setRunning(true);
		mThread.setRendering(true);
		mThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v("surfaceChanged(" + format + ", " + width + ", " + height + ")");

		mThread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v("surfaceDestroyed()");

		boolean retry = true;
		mThread.setRunning(false);
		while (retry) {
			try {
				mThread.join();
				retry = false;
			}
			catch (InterruptedException e) {
			}
		}

		// disable rendering to unregister SensorEventListener from the SensorManager and allow thread to be GC'd rather
		// than held on to by the system
		mThread.setRendering(false);

		// Force the need for a new thread next time the surface is created
		mThread = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Rendering thread

	@SuppressWarnings("unused")
	private class PlaneThread extends Thread {

		// Constants

		private static final boolean LOG_DEBUG_INFO = false;

		private static final boolean SHOW_DEBUG_INFO = false;

		private static final float TEXT_SIZE_FPS = 22;

		private static final double SKY_LOOP_TIME = 45 * 1e9; // Nanoseconds to loop through entire sky animation 

		private static final double PITCH_PER_NANOSECOND = 1.5 / 1e9; // Maximum pitch rotation per nanosecond in degrees

		private static final double PITCH_TOLERANCE = 3; // Min degrees before pitch changes

		private static final double MAX_PITCH = 6; // Maximum pitch in degrees (in either direction)

		private static final double MAX_TRANSLATION_Y = 75; // Maximum Y-translation of sky in dp (max roll)

		private static final float MAX_SENSOR_Z = .5f; // Translates Z-sensor into MAX_TRANSLATION_Y

		private static final double ROLL_PERCENT_PER_NANOSECOND = .15 / 1e9; // Maximum roll % change per nanosecond

		private static final double ROLL_PERCENT_TOLERANCE = .1; // Min % change in roll before changes kick in

		// Vars

		private SurfaceHolder mSurfaceHolder;

		private int mCanvasWidth;
		private int mCanvasHeight;

		private Bitmap mWindowFrameBitmap;
		private Bitmap mWindowShadeBitmap;
		private Bitmap mSkyBitmap;

		private Paint mSkyPaint;

		private int mFrameLeft;
		private int mFrameTop;
		private Rect mVisibleFrameRect;
		private int mVisibleFrameWidth;
		private int mVisibleFrameHeight;

		private int mSkyWidth;
		private double mSkyOffsetPerNano;
		private Rect mSkyDstFull;

		private int mShadeY;
		private int mShadeMinY;
		private int mShadeHeight;
		private Rect mShadeSrc;
		private Rect mShadeDst;

		private int mInitialTouchY;
		private int mInitialTouchShadeY;
		private int mEdgeSlop;

		private boolean mRun = false;
		private boolean mRendering = false;

		private double mSkyOffset = 0;

		// For the "grounded" look (instead of flying)
		private boolean mIsGrounded;
		private Bitmap mGroundedBitmap;

		// For the accelerometer
		private int mDisplayRotation;
		private float mSensorX = 0;
		private float mSensorZ = 0;
		private double mPitch = 0;
		private double mPitchTarget = 0;
		private float mPitchPivotX;
		private float mPitchPivotY;
		private double mRollPercent = 0;
		private double mRollPercentTarget = 0;
		private double mMaxTranslationY;

		// For calculating time
		private long mPreviousTick;
		private long mCurrentTick;

		// Only render full frame occasionally (usually only a portion is animating)
		private boolean mHasRendered;

		// For calculating FPS
		private int mFPS;
		private int mFrames;
		private long mLastNanos;
		private long mTimeNanos;
		private int mNumFPSSamples = 0;
		private int mAvgTotal = 0;
		private TextPaint mDebugTextPaint;
		private Paint mDebugBgPaint;
		private int mMaxDebugInfoWidth = 0;

		// SensorListenerProxy
		private SensorListenerProxy mSensorListenerProxy;

		public PlaneThread(SurfaceHolder surfaceHolder) {
			mSurfaceHolder = surfaceHolder;

			Resources res = getResources();
			mWindowFrameBitmap = BitmapFactory.decodeResource(res, R.drawable.loading_window_frame);

			mMaxTranslationY = res.getDisplayMetrics().density * MAX_TRANSLATION_Y;

			mSkyPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

			if (SHOW_DEBUG_INFO) {
				mDebugTextPaint = new TextPaint();
				mDebugTextPaint.setColor(Color.BLACK);
				mDebugTextPaint.setTextSize(TEXT_SIZE_FPS);

				mDebugBgPaint = new Paint();
				mDebugBgPaint.setColor(Color.WHITE);
			}
		}

		public void setSurfaceSize(int width, int height) {
			long start = System.nanoTime();

			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				mCanvasWidth = width;
				mCanvasHeight = height;

				// Configure drawing based on new surface size
				int windowFrameWidth = mWindowFrameBitmap.getWidth();
				int windowFrameHeight = mWindowFrameBitmap.getHeight();

				// Calculate where to place the window frame
				mFrameLeft = mCanvasWidth / 2 - (windowFrameWidth / 2);
				mFrameTop = mCanvasHeight / 2 - (windowFrameHeight / 2);

				// Calculate where to place the sky inside of the window frame
				//
				// WARNING: This is hardcoded to the size of the window frame.
				// If the window frame asset ever changes, you may need to
				// modify these values.
				mVisibleFrameWidth = (int) Math.ceil(.576388889 * windowFrameWidth);
				mVisibleFrameHeight = (int) Math.ceil(.564941922 * windowFrameHeight);
				mVisibleFrameRect = new Rect();
				mVisibleFrameRect.left = mCanvasWidth / 2 - (mVisibleFrameWidth / 2);
				mVisibleFrameRect.top = mCanvasHeight / 2 - (mVisibleFrameHeight / 2);
				mVisibleFrameRect.right = mVisibleFrameRect.left + mVisibleFrameWidth;
				mVisibleFrameRect.bottom = mVisibleFrameRect.top + mVisibleFrameHeight;

				// Common options for decoding bitmaps
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inScaled = false;

				// #470: This var was only added in 10+
				if (Build.VERSION.SDK_INT >= 10) {
					opts.inPreferQualityOverSpeed = true;
				}

				if (!mIsGrounded) {
					// Figure out the rotation skybox, which is larger than the actual visible
					// skybox.  This is because if we rotate the image, we want it to still draw
					// the entire space.
					double diagonal = Math.sqrt(Math.pow(mVisibleFrameWidth, 2) + Math.pow(mVisibleFrameHeight, 2)) / 2;
					double padLeftRight = diagonal - (mVisibleFrameWidth / 2);
					double padTopBot = diagonal - (mVisibleFrameHeight / 2);
					mSkyDstFull = new Rect((int) (mVisibleFrameRect.left - padLeftRight),
							(int) (mVisibleFrameRect.top - padTopBot - MAX_TRANSLATION_Y),
							(int) (mVisibleFrameRect.right + padLeftRight),
							(int) (mVisibleFrameRect.bottom + padTopBot + MAX_TRANSLATION_Y));

					// Pre-scale sky bitmap
					mSkyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_repeating_sky, opts);
					int skyWidth = (int) Math.round(mSkyBitmap.getWidth()
							* ((double) mSkyDstFull.height() / (double) mSkyBitmap.getHeight()));
					mSkyBitmap = Bitmap.createScaledBitmap(mSkyBitmap, skyWidth, mSkyDstFull.height(), true);
					mSkyWidth = mSkyBitmap.getWidth();
					mSkyOffsetPerNano = (double) mSkyWidth / SKY_LOOP_TIME;

					// Pre-configure pitch pivot points
					mPitchPivotX = mVisibleFrameRect.left + (mVisibleFrameWidth / 2f);
					mPitchPivotY = mVisibleFrameRect.top + (mVisibleFrameHeight / 2f);
				}
				else {
					// Setup simple image to be drawn, as the plane is grounded
					mGroundedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_grounded, opts);
					mGroundedBitmap = Bitmap.createScaledBitmap(mGroundedBitmap, mVisibleFrameWidth,
							mVisibleFrameHeight, true);
				}

				// Pre-scale shade
				mWindowShadeBitmap = BitmapFactory
						.decodeResource(getResources(), R.drawable.loading_window_shade, opts);
				mWindowShadeBitmap = Bitmap.createScaledBitmap(mWindowShadeBitmap, mVisibleFrameWidth,
						(int) Math.ceil(mVisibleFrameHeight * 1.05), true);
				mShadeHeight = mWindowShadeBitmap.getHeight();
				mShadeMinY = mShadeY = (int) (.17 * mShadeHeight);

				// Pre-configure shade bitmap drawing rects.  The -1s are where we
				// fill in values later, vs. being a static value
				mShadeSrc = new Rect(0, -1, mWindowShadeBitmap.getWidth(), mShadeHeight);
				mShadeDst = new Rect(mVisibleFrameRect.left, mVisibleFrameRect.top, mVisibleFrameRect.right, -1);

				// Slop values for touch interaction
				mEdgeSlop = ViewConfiguration.get(getContext()).getScaledEdgeSlop();

				// Init some values
				mCurrentTick = System.nanoTime();
				mHasRendered = false;
			}

			Log.d("Prepped PlaneWindowView in " + ((System.nanoTime() - start) / 1e6) + " ms");
		}

		public void setRunning(boolean running) {
			mRun = running;
		}

		public void setRendering(boolean rendering) {
			if (mRendering != rendering) {
				Context context = getContext();
				SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

				if (rendering) {
					if (!mIsGrounded) {
						Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
						mSensorListenerProxy = new SensorListenerProxy();
						sm.registerListener(mSensorListenerProxy, accelerometer, SensorManager.SENSOR_DELAY_UI);
					}

					// Use deprecated getOrientation() because it's exactly
					// the same as getRotation()
					WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
					mDisplayRotation = wm.getDefaultDisplay().getOrientation();

					mHasRendered = false;
				}
				else {
					if (!mIsGrounded) {
						sm.unregisterListener(mSensorListenerProxy);
						mSensorListenerProxy = null;
					}
				}
			}

			mRendering = rendering;
		}

		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				// Reject the touch if it doesn't touch the shade area (+/- slop)
				float eventX = event.getX();
				float eventY = event.getY();
				if (eventX < mShadeDst.left - mEdgeSlop || eventX > mShadeDst.right + mEdgeSlop
						|| eventY < mShadeDst.top - mEdgeSlop || eventY > mShadeDst.bottom + mEdgeSlop) {
					return false;
				}

				mInitialTouchY = (int) eventY;
				mInitialTouchShadeY = mShadeY;
				return true;
			}
			case MotionEvent.ACTION_MOVE: {
				int diff = mInitialTouchY - (int) event.getY();
				mShadeY = mInitialTouchShadeY - diff;

				// Constrain to bounds
				if (mShadeY < mShadeMinY) {
					mShadeY = mShadeMinY;
				}
				else if (mShadeY > mShadeHeight) {
					mShadeY = mShadeHeight;
				}

				return true;
			}
			}

			return false;
		}

		@Override
		public void run() {
			while (mRun) {
				if (mRendering) {
					Canvas c = null;
					try {
						c = mSurfaceHolder.lockCanvas(null);

						synchronized (mSurfaceHolder) {
							mPreviousTick = mCurrentTick;
							mCurrentTick = System.nanoTime();

							if (!mIsGrounded) {
								updateState(mCurrentTick - mPreviousTick);
							}

							doDraw(c);
						}

						if (!mHasRendered) {
							mHasRendered = true;

							if (mListener != null) {
								mListener.onFirstRender();
							}
						}
					}
					catch (Exception e) {
						// Only log an error if we were meant to be running
						if (mRun) {
							Log.w("Got an exception in PlaneWindowView.run()", e);
						}
					}
					finally {
						// do this in a finally so that if an exception is thrown
						// during the above, we don't leave the Surface in an
						// inconsistent state
						if (c != null) {
							mSurfaceHolder.unlockCanvasAndPost(c);
						}
					}
				}
			}
		}

		private void updateState(long diffNanos) {
			// Adjust sky offset
			mSkyOffset = (mSkyOffset + (mSkyOffsetPerNano * diffNanos)) % mSkyWidth;

			// Adjust plane pitch
			double pitchTarget = mSensorX * 90;
			if (pitchTarget > MAX_PITCH) {
				pitchTarget = MAX_PITCH;
			}
			else if (pitchTarget < -MAX_PITCH) {
				pitchTarget = -MAX_PITCH;
			}

			// If we're within striking distance of horizontal, cheat and make it horizontal.
			// Feels much more comfortable this way (when trying to get things back to normal)
			if (pitchTarget > -PITCH_TOLERANCE && pitchTarget < PITCH_TOLERANCE) {
				mPitchTarget = 0;
			}
			// Don't set a new pitch target unless it's a particular tolerance
			// away from the old one
			else if (Math.abs(mPitchTarget - pitchTarget) > PITCH_TOLERANCE) {
				mPitchTarget = pitchTarget;
			}

			double currDiff = Math.abs(mPitchTarget - mPitch);
			double pitchChange = diffNanos * PITCH_PER_NANOSECOND;
			if (currDiff < pitchChange) {
				pitchChange = currDiff;
			}

			if (mPitchTarget < mPitch) {
				mPitch -= pitchChange;
			}
			else {
				mPitch += pitchChange;
			}

			// Adjust sky y-translation

			// Translate sensor Z into a range from -100% to 100%
			double targetPercentTranslate = mSensorZ / MAX_SENSOR_Z;
			if (targetPercentTranslate > 1) {
				targetPercentTranslate = 1;
			}
			else if (targetPercentTranslate < -1) {
				targetPercentTranslate = -1;
			}

			// Calculate if we've gone past the threshhold with which to translate
			if (Math.abs(mRollPercentTarget - targetPercentTranslate) > ROLL_PERCENT_TOLERANCE) {
				mRollPercentTarget = targetPercentTranslate;
			}

			// Change the roll % by the amount necessary
			currDiff = Math.abs(mRollPercentTarget - mRollPercent);
			double rollChange = diffNanos * ROLL_PERCENT_PER_NANOSECOND;
			if (currDiff < rollChange) {
				rollChange = currDiff;
			}

			if (mRollPercentTarget < mRollPercent) {
				mRollPercent -= rollChange;
			}
			else {
				mRollPercent += rollChange;
			}
		}

		private void doDraw(Canvas canvas) {
			// Fill the background color
			canvas.drawARGB(255, 202, 202, 202);

			if (!mIsGrounded) {
				// Draw the sky
				canvas.save();

				// Apply clip/rotation for the sky
				canvas.clipRect(mVisibleFrameRect);
				canvas.rotate((float) mPitch, mPitchPivotX, mPitchPivotY);
				canvas.translate((int) -mSkyOffset, (float) (mRollPercent * MAX_TRANSLATION_Y));

				// Draw two skies, one after another, and let the clipping handle what should be shown
				canvas.drawBitmap(mSkyBitmap, 0, mSkyDstFull.top, mSkyPaint);
				canvas.drawBitmap(mSkyBitmap, mSkyWidth, mSkyDstFull.top, mSkyPaint);

				canvas.restore();
			}
			else {
				canvas.drawBitmap(mGroundedBitmap, null, mVisibleFrameRect, null);
			}

			// Draw the shade
			mShadeSrc.top = mShadeHeight - mShadeY;
			mShadeDst.bottom = mVisibleFrameRect.top + mShadeY;
			canvas.drawBitmap(mWindowShadeBitmap, mShadeSrc, mShadeDst, null);

			// Draw the window frame
			canvas.drawBitmap(mWindowFrameBitmap, mFrameLeft, mFrameTop, null);

			if (LOG_DEBUG_INFO || SHOW_DEBUG_INFO) {
				doDebugDraw(canvas);
			}
		}

		public void doDebugDraw(Canvas canvas) {
			// Show debug info if requested
			mFrames++;

			mTimeNanos = System.nanoTime();
			if (mTimeNanos > mLastNanos + 1e9) {
				mFPS = mFrames;
				mFrames = 0;
				mLastNanos = mTimeNanos;

				mNumFPSSamples++;
				mAvgTotal += mFPS;
			}

			if (LOG_DEBUG_INFO) {
				Log.i("FPS: " + mFPS + ", Avg: " + (int) ((double) mAvgTotal / mNumFPSSamples));
			}

			if (SHOW_DEBUG_INFO) {
				// Edit this to add more debug statements
				String[] debugStrings = new String[] {
						"FPS: " + mFPS + " AVG: " + (int) ((double) mAvgTotal / mNumFPSSamples),
						"SensorX: " + mSensorX,
						"SensorZ: " + mSensorZ,
						"Rotation: " + mPitch,
						"Sky Offset: " + mSkyOffset,
				};

				// Measure width necessary to draw debug info
				for (int a = 0; a < debugStrings.length; a++) {
					int width = (int) mDebugTextPaint.measureText(debugStrings[a]);
					if (width > mMaxDebugInfoWidth) {
						mMaxDebugInfoWidth = width;
					}
				}

				// Draw debug info
				canvas.drawRect(0, 0, mMaxDebugInfoWidth + 8, (TEXT_SIZE_FPS * debugStrings.length) + 8, mDebugBgPaint);
				for (int a = 0; a < debugStrings.length; a++) {
					canvas.drawText(debugStrings[a], 0, TEXT_SIZE_FPS * (a + 1), mDebugTextPaint);
				}
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	// SensorEventListener

	/**
	 * Note: The reason that there exists this proxy class for the SensorEventListener is to workaround a known bug
	 * in the Android SDK. SensorManager does not properly unregister SensorEventListeners, and in our case this causes
	 * memory issues. Use this proxy class to leak only the SensorListenerProxy rather than the expensive PlaneThread:
	 *
	 * http://code.google.com/p/android/issues/detail?id=15170
	 */

	private class SensorListenerProxy implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// No one cares
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (mThread != null) {
				float sensorX = 0;
				switch (mThread.mDisplayRotation) {
				case Surface.ROTATION_0:
					sensorX = event.values[0];
					break;
				case Surface.ROTATION_90:
					sensorX = -event.values[1];
					break;
				case Surface.ROTATION_180:
					sensorX = -event.values[0];
					break;
				case Surface.ROTATION_270:
					sensorX = event.values[1];
					break;
				}

				mThread.mSensorX = sensorX / SensorManager.GRAVITY_EARTH;
				mThread.mSensorZ = -event.values[2] / SensorManager.GRAVITY_EARTH;
			}
		}
	}
}
