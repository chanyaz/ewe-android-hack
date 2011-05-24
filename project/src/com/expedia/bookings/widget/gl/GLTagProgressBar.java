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

	private boolean mShowProgress = true;
	private String mText;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int mOrientation;

	private boolean mTagGrabbed = false;

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
	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mTagGrabbed = true;
			mRenderer.setAngleAndVelocityByTouchPoint(event.getX(), event.getY());

			break;
		}
		case MotionEvent.ACTION_MOVE: {
			if (mTagGrabbed) {
				mRenderer.setAngleAndVelocityByTouchPoint(event.getX(), event.getY());
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

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		super.surfaceCreated(holder);
		
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);
		
		mSensorManager.unregisterListener(this);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Listener implementations
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
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
		return mShowProgress;
	}

	public String getText() {
		return mText;
	}

	public void setShowProgress(boolean showProgress) {
		mShowProgress = showProgress;
	}

	public void setText(int resId) {
		setText(mContext.getString(resId));
	}

	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		mText = text;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	private void init(Context context) {
		mContext = (Activity) context;
		mSensorManager = (SensorManager) mContext.getSystemService(Activity.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mOrientation = ((Activity) context).getWindowManager().getDefaultDisplay().getOrientation();

		setOnTouchListener(this);
		setFocusableInTouchMode(true);

		mRenderer = new GLTagProgressBarRenderer(context);
		mRenderer.setOrientation(mOrientation);

		setRenderer(mRenderer);
	}
}