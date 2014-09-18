package com.expedia.bookings.widget.gl;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

import com.expedia.bookings.widget.gl.GLTagProgressBarRenderer.OnDrawStartedListener;
import com.mobiata.android.Log;

public class GLTagProgressBar extends GLSurfaceView implements OnTouchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Private members
	private Context mContext;

	private SensorManager mSensorManager;
	private SensorListenerProxy mSensorListenerProxy;
	private Sensor mAccelerometer;
	private int mOrientation;

	GLTagProgressBarRenderer mRenderer;

	//////////////////////////////////////////////////////////////////////////////////
	// Constructors

	public GLTagProgressBar(Context context) {
		super(context);
		init(context);
	}

	public GLTagProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	public void onPause() {
		super.onPause();
		registerSensorListener(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerSensorListener(true);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (mRenderer == null) {
			return true;
		}

		mRenderer.onTouch(view, event);

		return true;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		registerSensorListener(getVisibility() == View.VISIBLE);
	}

	@Override
	protected void onDetachedFromWindow() {
		mRenderer.shutdown();
		registerSensorListener(false);

		super.onDetachedFromWindow();
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (visibility != View.VISIBLE) {
			mRenderer.pause();
			reset();
		}
		else {
			mRenderer.resume();
		}

		registerSensorListener(visibility == View.VISIBLE);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		registerSensorListener(getVisibility() == View.VISIBLE);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		super.surfaceCreated(holder);
		registerSensorListener(getVisibility() == View.VISIBLE);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);
		registerSensorListener(false);
	}

	private void registerSensorListener(boolean registered) {
		if (registered) {
			Log.v("GLTagProgressBar SensorListenerProxy registered");
			if (mSensorListenerProxy == null) {
				mSensorListenerProxy = new SensorListenerProxy(mRenderer, mOrientation);
				mSensorManager.registerListener(mSensorListenerProxy, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			}
			else {
				Log.v("GLTagProgressBar: Double register detected, ignored");
			}
		}
		else {
			Log.v("GLTagProgressBar SensorListenerProxy unregistered");
			if (mSensorListenerProxy != null) {
				mSensorManager.unregisterListener(mSensorListenerProxy);
				mSensorListenerProxy = null;
			}
			else {
				Log.v("GLTagProgressBar: Double unregister detected, ignored");
			}
			reset();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Listener implementations

	/**
	 * Note: The reason that there exists this proxy class for the SensorEventListener is to workaround a known bug
	 * in the Android SDK. SensorManager does not properly unregister SensorEventListeners, and in our case this causes
	 * memory issues. Use this proxy class to leak only the SensorListenerProxy.
	 *
	 * http://code.google.com/p/android/issues/detail?id=15170
	 */

	private static class SensorListenerProxy implements SensorEventListener {
		private WeakReference<GLTagProgressBarRenderer> mTarget;
		private final int mOrientation;
		private boolean mLostTarget = false; // Just so there isn't infinite logging

		public SensorListenerProxy(GLTagProgressBarRenderer renderer, int orientation) {
			mTarget = new WeakReference<GLTagProgressBarRenderer>(renderer);
			mOrientation = orientation;
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			GLTagProgressBarRenderer renderer = mTarget.get();
			if (renderer == null) {
				if (!mLostTarget) {
					Log.d("SensorListenerProxy renderer no longer exists. Either not unregistered or leaked.");
					mLostTarget = true;
				}
				return;
			}

			final float[] acceleration = event.values.clone();
			switch (mOrientation) {
				case Surface.ROTATION_90: {
					acceleration[0] = -event.values[1];
					acceleration[1] = event.values[0];
					break;
				}
				case Surface.ROTATION_180: {
					acceleration[0] = -event.values[0];
					acceleration[1] = -event.values[1];
					break;
				}
				case Surface.ROTATION_270: {
					acceleration[0] = event.values[1];
					acceleration[1] = -event.values[0];
					break;
				}
			}

			renderer.setAcceleration(acceleration[0] * -1, acceleration[1] * -1, acceleration[2] * -1);
		}

	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public boolean getShowProgress() {
		return mRenderer.getShowProgress();
	}

	public void setShowProgress(boolean showProgress) {
		mRenderer.setShowProgress(showProgress);
	}

	public void reset() {
		if (mRenderer != null) {
			mRenderer.postReset();
		}
	}

	// Listeners

	public void addOnDrawStartedListener(OnDrawStartedListener onDrawStartedListener) {
		mRenderer.addOnDrawStartedListener(onDrawStartedListener);
	}

	public void removeOnDrawStartedListener(OnDrawStartedListener onDrawStartedListener) {
		mRenderer.removeOnDrawStartedListener(onDrawStartedListener);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	private void init(Context context) {
		mContext = (Activity) context;
		mOrientation = ((Activity) context).getWindowManager().getDefaultDisplay().getOrientation();

		mRenderer = new GLTagProgressBarRenderer(context, this);
		mRenderer.setOrientation(mOrientation);

		setRenderer(mRenderer);
		setOnTouchListener(this);
		setFocusableInTouchMode(true);

		mSensorManager = (SensorManager) mContext.getSystemService(Activity.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
}
