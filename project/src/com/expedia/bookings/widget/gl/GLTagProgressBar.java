package com.expedia.bookings.widget.gl;

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

public class GLTagProgressBar extends GLSurfaceView implements SensorEventListener, OnTouchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Private members
	private Context mContext;

	private SensorManager mSensorManager;
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
		setSensorManagerRegistration(getVisibility() == View.VISIBLE);
	}

	@Override
	protected void onDetachedFromWindow() {
		mRenderer.shutdown();
		setSensorManagerRegistration(false);
		
		super.onDetachedFromWindow();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		setSensorManagerRegistration(getVisibility() == View.VISIBLE);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		super.surfaceCreated(holder);
		setSensorManagerRegistration(getVisibility() == View.VISIBLE);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);
		setSensorManagerRegistration(false);
	}

	public void setSensorManagerRegistration(boolean registered) {
		if (registered) {
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
		else {
			mSensorManager.unregisterListener(this);
			mRenderer.reset();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Listener implementations

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mRenderer == null) {
			return;
		}

		final float[] acceleration = event.values.clone();
		switch (mOrientation) {
		case Surface.ROTATION_90: {
			acceleration[1] = event.values[0];
			acceleration[0] = -event.values[1];
			break;
		}
		case Surface.ROTATION_270: {
			acceleration[1] = -event.values[0];
			acceleration[0] = event.values[1];
			break;
		}
		}

		mRenderer.setAcceleration(acceleration[0] * -1, acceleration[1] * -1, acceleration[2] * -1);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public boolean getShowProgress() {
		return mRenderer.getShowProgress();
	}

	public void setShowProgress(boolean showProgress) {
		mRenderer.setShowProgress(showProgress);
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