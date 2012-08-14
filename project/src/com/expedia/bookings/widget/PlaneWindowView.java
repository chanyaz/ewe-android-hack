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
		Log.v("surfaceChanged()");

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

		// Force the need for a new thread next time the surface is created
		mThread = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Rendering thread

	private class PlaneThread extends Thread implements SensorEventListener {

		// Constants

		private static final boolean LOG_DEBUG_INFO = false;

		private static final boolean SHOW_DEBUG_INFO = false;

		private static final float TEXT_SIZE_FPS = 22;

		private static final double SKY_LOOP_TIME = 45 * 1e9; // Nanoseconds to loop through entire sky animation 

		private static final double ROTATION_PER_NANOSECOND = 10 / 1e9; // Maximum rotation per nanosecond in degrees

		private static final double ROTATION_TOLERANCE = 4; // Min degrees before rotation changes

		private static final double MAX_ROTATION = 15; // Maximum rotation in degrees (in either direction)

		// Vars

		private SurfaceHolder mSurfaceHolder;

		private int mCanvasWidth;
		private int mCanvasHeight;

		private Bitmap mWindowFrameBitmap;
		private Bitmap mWindowShadeBitmap;
		private Bitmap mSkyBitmap;

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

		// For the accelerometer
		private int mDisplayRotation;
		private float mSensorX = 0;
		private double mRotation = 0;
		private double mRotationTarget = 0;
		private float mRotationPivotX;
		private float mRotationPivotY;

		// For calculating time
		private long mPreviousTick;
		private long mCurrentTick;

		// Only render full frame occasionally (usually only a portion is animating)
		private boolean mRenderAll;

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

		public PlaneThread(SurfaceHolder surfaceHolder) {
			mSurfaceHolder = surfaceHolder;

			Resources res = getResources();
			mWindowFrameBitmap = BitmapFactory.decodeResource(res, R.drawable.loading_window_frame);

			if (SHOW_DEBUG_INFO) {
				mDebugTextPaint = new TextPaint();
				mDebugTextPaint.setColor(Color.BLACK);
				mDebugTextPaint.setTextSize(TEXT_SIZE_FPS);

				mDebugBgPaint = new Paint();
				mDebugBgPaint.setColor(Color.WHITE);
			}
		}

		public void setSurfaceSize(int width, int height) {
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

				// Figure out the rotation skybox, which is larger than the actual visible
				// skybox.  This is because if we rotate the image, we want it to still draw
				// the entire space.
				double diagonal = Math.sqrt(Math.pow(mVisibleFrameWidth, 2) + Math.pow(mVisibleFrameHeight, 2)) / 2;
				double padLeftRight = diagonal - (mVisibleFrameWidth / 2);
				double padTopBot = diagonal - (mVisibleFrameHeight / 2);
				mSkyDstFull = new Rect((int) (mVisibleFrameRect.left - padLeftRight),
						(int) (mVisibleFrameRect.top - padTopBot),
						(int) (mVisibleFrameRect.right + padLeftRight),
						(int) (mVisibleFrameRect.bottom + padTopBot));

				// Pre-scale sky bitmap
				mSkyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_repeating_sky);
				int skyWidth = (int) Math.round(mSkyBitmap.getWidth()
						* ((double) mSkyDstFull.height() / (double) mSkyBitmap.getHeight()));
				mSkyBitmap = Bitmap.createScaledBitmap(mSkyBitmap, skyWidth, mSkyDstFull.height(), true);
				mSkyWidth = mSkyBitmap.getWidth();
				mSkyOffsetPerNano = (double) mSkyWidth / SKY_LOOP_TIME;

				// Pre-configure rotation pivot points
				mRotationPivotX = mVisibleFrameRect.left + (mVisibleFrameWidth / 2f);
				mRotationPivotY = mVisibleFrameRect.top + (mVisibleFrameHeight / 2f);

				// Pre-scale shade
				mWindowShadeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_window_shade);
				int shadeHeight = (int) Math.round(mWindowShadeBitmap.getHeight()
						* ((double) mVisibleFrameWidth / (double) mWindowShadeBitmap.getWidth()));
				mWindowShadeBitmap = Bitmap.createScaledBitmap(mWindowShadeBitmap, mVisibleFrameWidth, shadeHeight,
						true);
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
				mRenderAll = true;
			}
		}

		public void setRunning(boolean running) {
			mRun = running;
		}

		public void setRendering(boolean rendering) {
			if (mRendering != rendering) {
				Context context = getContext();
				SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

				if (rendering) {
					Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
					sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

					// Use deprecated getOrientation() because it's exactly
					// the same as getRotation()
					WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
					mDisplayRotation = wm.getDefaultDisplay().getOrientation();

					mRenderAll = true;
				}
				else {
					sm.unregisterListener(this);
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
						if (mRenderAll || SHOW_DEBUG_INFO) {
							c = mSurfaceHolder.lockCanvas(null);
						}
						else {
							c = mSurfaceHolder.lockCanvas(mVisibleFrameRect);
						}

						synchronized (mSurfaceHolder) {
							mPreviousTick = mCurrentTick;
							mCurrentTick = System.nanoTime();
							updateState(mCurrentTick - mPreviousTick);

							doDraw(c);
						}

						if (mRenderAll) {
							mRenderAll = false;

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

			// Adjust sky rotation
			double rotationTarget = mSensorX * 90;
			if (rotationTarget > MAX_ROTATION) {
				rotationTarget = MAX_ROTATION;
			}
			else if (rotationTarget < -MAX_ROTATION) {
				rotationTarget = -MAX_ROTATION;
			}

			// If we're within striking distance of horizontal, cheat and make it horizontal.
			// Feels much more comfortable this way (when trying to get things back to normal)
			if (rotationTarget > -ROTATION_TOLERANCE && rotationTarget < ROTATION_TOLERANCE) {
				mRotationTarget = 0;
			}
			// Don't set a new rotation target unless it's a particular tolerance
			// away from the old one
			else if (Math.abs(mRotationTarget - rotationTarget) > ROTATION_TOLERANCE) {
				mRotationTarget = rotationTarget;
			}

			double currDiff = Math.abs(mRotationTarget - mRotation);
			double rotationChange = diffNanos * ROTATION_PER_NANOSECOND;
			if (currDiff < rotationChange) {
				rotationChange = currDiff;
			}

			if (mRotationTarget < mRotation) {
				mRotation -= rotationChange;
			}
			else {
				mRotation += rotationChange;
			}
		}

		@SuppressWarnings("unused")
		private void doDraw(Canvas canvas) {
			// Fill the background color
			canvas.drawARGB(255, 202, 202, 202);

			// Draw the sky
			canvas.save();

			// Apply clip/rotation for the sky
			canvas.clipRect(mVisibleFrameRect);
			canvas.rotate((float) mRotation, mRotationPivotX, mRotationPivotY);
			canvas.translate((int) -mSkyOffset, 0);

			// Draw two skies, one after another, and let the clipping handle what should be shown
			canvas.drawBitmap(mSkyBitmap, 0, mSkyDstFull.top, null);
			canvas.drawBitmap(mSkyBitmap, mSkyWidth, mSkyDstFull.top, null);

			canvas.restore();

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
						"Rotation: " + mRotation,
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

		//////////////////////////////////////////////////////////////////////
		// SensorEventListener

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// No one cares
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			float sensorX = 0;
			switch (mDisplayRotation) {
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

			mSensorX = sensorX / SensorManager.GRAVITY_EARTH;
		}
	}
}
