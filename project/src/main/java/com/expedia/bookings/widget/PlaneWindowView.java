package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Parcel;
import android.os.Parcelable;
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

	private boolean mIsGrounded;

	private double mShadePercentClosed = 0;

	public PlaneWindowView(Context context, AttributeSet attrs) {
		super(context, attrs);

		getHolder().setFormat(PixelFormat.RGBA_8888);

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
	 * Make sure to call before SurfaceView starts rendering,
	 * but after the thread has been initialized
	 */
	public void setGrounded(boolean isGrounded) {
		mIsGrounded = isGrounded;
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
	// Save state
	//
	// Inspiration from http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes/3542895#3542895

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);

		ss.mShadePercentClosed = mShadePercentClosed;

		return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof SavedState) {
			SavedState ss = (SavedState) state;
			super.onRestoreInstanceState(ss.getSuperState());
			mShadePercentClosed = ss.mShadePercentClosed;
		}
		else {
			super.onRestoreInstanceState(state);
		}
	}

	private static class SavedState extends BaseSavedState {
		private double mShadePercentClosed;

		private SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);

			mShadePercentClosed = in.readDouble();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);

			out.writeDouble(mShadePercentClosed);
		}

		// Required field that makes Parcelables from a Parcel
		@SuppressWarnings("unused")
		public static final Creator<SavedState> CREATOR =
				new Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
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
				// ignore
			}
		}

		// disable rendering to unregister SensorEventListener from the SensorManager and allow thread to be GC'd rather
		// than held on to by the system
		mThread.setRendering(false);

		// Reduce, reuse, recycle
		mThread.recycle();

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

		private static final double PITCH_PER_NANOSECOND = 7 / 1e9; // Maximum pitch rotation per nanosecond in degrees

		private static final double PITCH_TOLERANCE = 3; // Min degrees before pitch changes

		private static final double MAX_PITCH = 30; // Maximum pitch in degrees (in either direction)

		private static final double PITCH_PADDING_Y = 3; // Extra padding for sky to account for pitch changes (in dp)

		private static final double MAX_TRANSLATION_Y = 45; // Maximum Y-translation of sky in dp (max roll)

		private static final float MAX_SENSOR_Z = .5f; // Translates Z-sensor into MAX_TRANSLATION_Y

		private static final double ROLL_PERCENT_PER_NANOSECOND = .15 / 1e9; // Maximum roll % change per nanosecond

		private static final double ROLL_PERCENT_TOLERANCE = .1; // Min % change in roll before changes kick in

		// Vars

		private SurfaceHolder mSurfaceHolder;

		private int mCanvasWidth;
		private int mCanvasHeight;

		private int mBgColor;

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
		private Bitmap mGroundedBitmap;

		// For the accelerometer
		private int mDisplayRotation;
		private float mSensorX = 0;
		private float mSensorZ = 0;
		private double mPitch = 0;
		private double mPitchTarget = 0;
		private double mPitchPaddingY;
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
			float density = res.getDisplayMetrics().density;
			mPitchPaddingY = density * PITCH_PADDING_Y;
			mMaxTranslationY = density * MAX_TRANSLATION_Y;
			mBgColor = res.getColor(R.color.plane_window_background);

			mSkyPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

			if (SHOW_DEBUG_INFO) {
				mDebugTextPaint = new TextPaint();
				mDebugTextPaint.setColor(Color.BLACK);
				mDebugTextPaint.setTextSize(TEXT_SIZE_FPS);

				mDebugBgPaint = new Paint();
				mDebugBgPaint.setColor(Color.WHITE);
			}
		}

		// Helps clear up memory when the view is no longer rendering
		public void recycle() {
			if (mWindowFrameBitmap != null) {
				mWindowFrameBitmap.recycle();
			}
			if (mWindowShadeBitmap != null) {
				mWindowShadeBitmap.recycle();
			}
			if (mSkyBitmap != null) {
				mSkyBitmap.recycle();
			}
			if (mGroundedBitmap != null) {
				mGroundedBitmap.recycle();
			}
		}

		public void setSurfaceSize(int width, int height) {
			long start = System.nanoTime();

			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				Resources res = getResources();

				// Common options for decoding bitmaps
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inScaled = false;
				opts.inPreferQualityOverSpeed = true;

				mCanvasWidth = width;
				mCanvasHeight = height;

				// Resize the window frame to make it match the maximum dimension for the surface
				BitmapFactory.Options windowFrameBounds = decodeBounds(res, R.drawable.loading_window_frame);
				int windowFrameWidth = windowFrameBounds.outWidth;
				int windowFrameHeight = windowFrameBounds.outHeight;
				Log.v("Window frame orig width,height: " + windowFrameWidth + ", " + windowFrameHeight);
				float scale;
				if (windowFrameWidth * mCanvasHeight > windowFrameHeight * mCanvasWidth) {
					scale = (float) mCanvasWidth / (float) windowFrameWidth;
				}
				else {
					scale = (float) mCanvasHeight / (float) windowFrameHeight;
				}
				windowFrameWidth *= scale;
				windowFrameHeight *= scale;
				Log.v("Window frame target width,height: " + windowFrameWidth + ", " + windowFrameHeight);
				opts.inSampleSize = calculateInSampleSize(windowFrameBounds, windowFrameWidth, windowFrameHeight);
				Log.v("Window frame sample size: " + opts.inSampleSize);
				Bitmap origWindowFrameBitmap = BitmapFactory.decodeResource(res, R.drawable.loading_window_frame, opts);
				Log.v("Window frame (sampled) width,height: " + origWindowFrameBitmap.getWidth() + ", "
						+ origWindowFrameBitmap.getHeight());
				mWindowFrameBitmap = scaleBitmap(origWindowFrameBitmap, windowFrameWidth, windowFrameHeight);
				Log.v("Window frame (final) width,height: " + mWindowFrameBitmap.getWidth() + ", "
						+ mWindowFrameBitmap.getHeight());

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

				if (!mIsGrounded) {
					// We need to expand the skybox to handle rotation.  Right now we've got a hardcoded
					// extra padding, but at some point we might want to do the actual math (so we don't
					// have to keep updating it whenever we change MAX_PITCH).
					mSkyDstFull = new Rect(mVisibleFrameRect);
					mSkyDstFull.top -= (mPitchPaddingY + mMaxTranslationY);
					mSkyDstFull.bottom += (mPitchPaddingY + mMaxTranslationY);

					// Pre-scale sky bitmap
					BitmapFactory.Options skyBounds = decodeBounds(res, R.drawable.loading_repeating_sky);
					Log.v("Sky orig width,height: " + skyBounds.outWidth + ", "
							+ skyBounds.outHeight);
					int skyWidth = (int) Math.round(skyBounds.outWidth
							* ((double) mSkyDstFull.height() / (double) skyBounds.outHeight));
					int skyHeight = mSkyDstFull.height();
					Log.v("Sky target width,height: " + skyWidth + ", " + skyHeight);
					opts.inSampleSize = calculateInSampleSize(skyBounds, skyWidth, skyHeight);
					Log.v("Sky sample size: " + opts.inSampleSize);
					Bitmap origSkyBitmap = BitmapFactory.decodeResource(res, R.drawable.loading_repeating_sky, opts);
					Log.v("Sky (sampled) width,height: " + origSkyBitmap.getWidth() + ", " + origSkyBitmap.getHeight());
					mSkyBitmap = scaleBitmap(origSkyBitmap, skyWidth, skyHeight);
					Log.v("Sky final width,height: " + mSkyBitmap.getWidth() + ", " + mSkyBitmap.getHeight());
					mSkyWidth = mSkyBitmap.getWidth();
					mSkyOffsetPerNano = (double) mSkyWidth / SKY_LOOP_TIME;

					// Pre-configure pitch pivot points
					mPitchPivotX = mVisibleFrameRect.left + (mVisibleFrameWidth / 2f);
					mPitchPivotY = mVisibleFrameRect.top + (mVisibleFrameHeight / 2f);
				}
				else {
					// Setup simple image to be drawn, as the plane is grounded
					Log.v("Grounded target width,height: " + mVisibleFrameWidth + ", " + mVisibleFrameHeight);
					BitmapFactory.Options groundedBounds = decodeBounds(res, R.drawable.loading_grounded);
					Log.v("Grounded orig width,height: " + groundedBounds.outWidth + ", "
							+ groundedBounds.outHeight);
					opts.inSampleSize = calculateInSampleSize(groundedBounds, mVisibleFrameWidth, mVisibleFrameHeight);
					Log.v("Grounded sample size: " + opts.inSampleSize);
					Bitmap origGroundedBitmap = BitmapFactory.decodeResource(res, R.drawable.loading_grounded, opts);
					Log.v("Grounded (sampled) width,height: " + origGroundedBitmap.getWidth() + ", "
							+ origGroundedBitmap.getHeight());
					mGroundedBitmap = scaleBitmap(origGroundedBitmap, mVisibleFrameWidth, mVisibleFrameHeight);
					Log.v("Grounded (final) width,height: " + mGroundedBitmap.getWidth() + ", "
							+ mGroundedBitmap.getHeight());
				}

				// Pre-scale shade
				int windowShadeWidth = mVisibleFrameWidth;
				int windowShadeHeight = (int) Math.ceil(mVisibleFrameHeight * 1.05);
				Log.v("Window shade target width,height: " + windowShadeWidth + ", "
						+ windowShadeHeight);
				BitmapFactory.Options windowShadeBounds = decodeBounds(res, R.drawable.loading_window_shade);
				Log.v("Window shade orig width,height: " + windowShadeBounds.outWidth + ", "
						+ windowShadeBounds.outHeight);
				opts.inSampleSize = calculateInSampleSize(windowShadeBounds, windowShadeWidth, windowShadeHeight);
				Log.v("Window shade sample size: " + opts.inSampleSize);
				Bitmap origWindowShadeBitmap = BitmapFactory.decodeResource(res, R.drawable.loading_window_shade, opts);
				Log.v("Window shade (sampled) width,height: " + origWindowShadeBitmap.getWidth() + ", "
						+ origWindowShadeBitmap.getHeight());
				mWindowShadeBitmap = scaleBitmap(origWindowShadeBitmap, windowShadeWidth, windowShadeHeight);
				Log.v("Window shade (final) width,height: " + mWindowShadeBitmap.getWidth() + ", "
						+ mWindowShadeBitmap.getHeight());
				mShadeHeight = mWindowShadeBitmap.getHeight();
				mShadeMinY = (int) (.17 * mShadeHeight);

				// Restore the shade Y; if we're not restoring, it's == mShadeMinY
				mShadeY = (int) Math.round((mShadeHeight - mShadeMinY) * mShadePercentClosed) + mShadeMinY;

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

		// Either returns the same Bitmap (if no scaling needed) or scales it and recycles the old one
		private Bitmap scaleBitmap(Bitmap origBitmap, int desiredWidth, int desiredHeight) {
			if (origBitmap.getWidth() == desiredWidth && origBitmap.getHeight() == desiredHeight) {
				return origBitmap;
			}
			else {
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(origBitmap, desiredWidth, desiredHeight, true);
				origBitmap.recycle();
				return scaledBitmap;
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
					if (!mIsGrounded) {
						Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
						mSensorListenerProxy = new SensorListenerProxy(mThread);
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
				if (mShadeDst == null || eventX < mShadeDst.left - mEdgeSlop || eventX > mShadeDst.right + mEdgeSlop
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

				// Update the shade percent closed, for save state purposes
				mShadePercentClosed = (double) (mShadeY - mShadeMinY) / (double) (mShadeHeight - mShadeMinY);

				return true;
			}
			}

			return false;
		}

		@Override
		public void run() {
			while (mRun) {
				try {
					sleep(100);
				}
				catch (InterruptedException e) {
					Log.w("Got an exception in PlaneWindowView.run()", e);
				}
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
			canvas.drawColor(mBgColor);

			if (!mIsGrounded) {
				// Draw the sky
				canvas.save();

				// Apply clip/rotation for the sky
				canvas.clipRect(mVisibleFrameRect);
				canvas.rotate((float) mPitch, mPitchPivotX, mPitchPivotY);
				canvas.translate((int) -mSkyOffset, (float) (mRollPercent * mMaxTranslationY));

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

	private static class SensorListenerProxy implements SensorEventListener {
		private WeakReference<PlaneThread> mTarget;
		private boolean mLostTarget = false; // Just so there isn't infinite logging

		public SensorListenerProxy(PlaneThread thread) {
			mTarget = new WeakReference<PlaneThread>(thread);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// No one cares
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			PlaneThread thread = mTarget.get();
			if (thread != null) {
				float sensorX = 0;
				switch (thread.mDisplayRotation) {
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

				thread.mSensorX = sensorX / SensorManager.GRAVITY_EARTH;
				thread.mSensorZ = -event.values[2] / SensorManager.GRAVITY_EARTH;
			}
			else {
				if (!mLostTarget) {
					Log.d("PlaneWindowView SensorListenerProxy PlaneThread was null. Either forgot to unregister or leaked");
					mLostTarget = true;
				}
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	// Bitmap loading utility

	public static BitmapFactory.Options decodeBounds(Resources res, int resId) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		return options;
	}

	// From http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	// From http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = (int) Math.floor((float) height / (float) reqHeight);
			}
			else {
				inSampleSize = (int) Math.floor((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}
}
