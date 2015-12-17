package com.expedia.bookings.widget.gl;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

@SuppressWarnings("unused")
public class GLTagProgressBarRenderer implements GLSurfaceView.Renderer {
	public interface OnDrawStartedListener {
		void onDrawStarted();
	}

	//////////////////////////////////////////////////////////////////////////////
	// Constants

	static final boolean LOG_FPS = false;

	private final static double GRAVITY = 9.81d;
	private final static double MASS = 0.4d;
	private final static double LENGTH = 0.3d;
	private final static double FRICTION_HANDLE = 0.12d;
	private final static double FRICTION_DOOR = 0.13d;
	private final static double THRESH_DOOR_FRICTION_ANGLE = -8.5d;
	private final static double DEGREES_PER_SECOND = 2 * Math.PI;
	private final static int MAX_ANGULAR_VELOCITY = 150;

	private final static int SIZE_TAG_WIDTH = 97;
	private final static int SIZE_TAG_HEIGHT = 245;
	private final static int SIZE_KNOB_BG_WIDTH = 85;
	private final static int SIZE_KNOB_BG_HEIGHT = 85;
	private final static int SIZE_KNOB_WIDTH = 75;
	private final static int SIZE_KNOB_HEIGHT = 77;
	private final static int SIZE_RING_WIDTH = 65;
	private final static int SIZE_RING_HEIGHT = 65;
	private final static int SIZE_RING_FILL_WIDTH = 65;
	private final static int SIZE_RING_FILL_HEIGHT = 65;

	//////////////////////////////////////////////////////////////////////////////
	// Private members

	private Context mContext;
	private View mParent;

	private GL10 mGL;

	private List<OnDrawStartedListener> mOnDrawStartedListners;

	private long mLastDrawTime = -1;
	private long mNow;

	private boolean mShowProgress = true;
	private boolean mTagGrabbed = false;
	private boolean mIsPaused = false;
	private boolean mDoReset = true;
	private boolean mDrawingStarted = false;

	private double mAngle;
	private double mAngularVelocity = 0;

	private float mAccelX;
	private float mAccelY;
	private float mAccelZ;

	private Handler mTouchHandler;

	private double mLastAngle;
	private long mLastTagAngleSetByTouchTime;

	private double mInertia;
	private double mMagnitude;
	private double mMedianAngle;
	private double mTagAngle;
	private double mAngleDifference;
	private double mForce;
	private double mTorque;
	private double mFrictionTorque;
	private double mNetTorque;
	private double mAngularAcceleration;
	private double mFrictionZ;
	private double mChangeInAngle;

	float mRingAngle;
	float mTagDegrees;
	float mRingDegrees;

	private int mOrientation;
	private float mScaledDensity;

	private float mWidth;
	private float mHeight;

	private RectF mTagSrcRect;
	private RectF mKnobSrcRect;
	private RectF mKnobBgSrcRect;
	private RectF mRingSrcRect;
	private RectF mRingFillSrcRect;

	private RectF mTagDestRect;
	private RectF mKnobDestRect;
	private RectF mKnobBgDestRect;
	private RectF mRingDestRect;
	private RectF mRingFillDestRect;

	private float mTagWidth;
	private float mTagHeight;
	private float mKnobBgWidth;
	private float mKnobBgHeight;
	private float mKnobWidth;
	private float mKnobHeight;
	private float mRingWidth;
	private float mRingHeight;
	private float mRingFillWidth;
	private float mRingFillHeight;

	private float mOffsetY;
	private float mRingMargin;
	private float mRingLeftOffset;

	private float mTagCenterX;
	private float mTagCenterY;
	private float mRingFillCenterX;
	private float mRingFillCenterY;

	private static BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

	private GLSprite[] mSprites;
	private GLSprite mTagSprite;
	private GLSprite mRingFillSprite;
	private GLSprite mKnobSprite;
	private GLSprite mKnobBgSprite;

	private int[] mTextureNameWorkspace;
	private int[] mCropWorkspace;
	private boolean mUseVerts = true;
	private boolean mUseHardwareBuffers = false;

	public GLTagProgressBarRenderer(Context context, View view) {
		mContext = context;
		mParent = view;

		mTextureNameWorkspace = new int[1];
		mCropWorkspace = new int[4];
		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

		mTagSprite = new GLSprite(Ui.obtainThemeResID((Activity)mContext, R.attr.skin_hangTagProgressDrawable));
		mRingFillSprite = new GLSprite(Ui.obtainThemeResID((Activity)mContext, R.attr.skin_hangTagProgressRingFillDrawable));
		mKnobSprite = new GLSprite(Ui.obtainThemeResID((Activity)mContext, R.attr.skin_hangTagKnobDrawable));
		mKnobBgSprite = new GLSprite(Ui.obtainThemeResID((Activity)mContext, R.attr.skin_hangTagKnobBackgroundDrawable));

		// Setup sprites
		mSprites = new GLSprite[4];
		mSprites[0] = mKnobBgSprite;
		mSprites[1] = mTagSprite;
		mSprites[2] = mRingFillSprite;
		mSprites[3] = mKnobSprite;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		final boolean visible = (mParent.getVisibility() == View.VISIBLE);

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		mNow = System.currentTimeMillis();
		if (mLastDrawTime < 0) {
			mLastDrawTime = mNow;
		}
		final float delta = (float) (mNow - mLastDrawTime) / 1000f;
		if (LOG_FPS && visible) {
			Log.t("FPS: %d", (int) (1f / delta));
		}

		if (mDoReset) {
			reset();
		}

		if (visible && !mIsPaused) {
			if (!mTagGrabbed) {
				updatePhysics(delta);
			}
			updateSpritePositions();
		}
		drawFrame(gl);

		if (visible) {
			mLastDrawTime = System.currentTimeMillis();
		}
		else {
			mLastDrawTime = -1;
		}

		if (!mDrawingStarted && !mIsPaused && mOnDrawStartedListners != null) {
			for (OnDrawStartedListener onDrawStartedListener : mOnDrawStartedListners) {
				onDrawStartedListener.onDrawStarted();
			}
			mDrawingStarted = true;
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glClearColor(0.894117647f, 0.894117647f, 0.894117647f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		mGL = gl;

		mWidth = (float) width;
		mHeight = (float) height;

		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScaledDensity = metrics.scaledDensity;

		calculateMeasurements();

		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, width, 0.0f, height, 0.0f, 1.0f);

		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
		gl.glEnable(GL10.GL_TEXTURE_2D);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.894117647f, 0.894117647f, 0.894117647f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		/*
		* Some one-time OpenGL initialization can be made here probably based
		* on features of this particular context
		*/
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		//gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		//gl.glDisable(GL10.GL_DITHER);
		gl.glDisable(GL10.GL_LIGHTING);

		// Custom settings
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_MULTISAMPLE);

		if (mSprites != null) {

			// If we are using hardware buffers and the screen lost context
			// then the buffer indexes that we recorded previously are now
			// invalid.  Forget them here and recreate them below.
			if (mUseHardwareBuffers) {
				for (int x = 0; x < mSprites.length; x++) {
					// Ditch old buffer indexes.
					mSprites[x].getGrid().invalidateHardwareBuffers();
				}
			}

			// Load our texture and set its texture name on all sprites.

			// To keep this sample simple we will assume that sprites that share
			// the same texture are grouped together in our sprite list. A real
			// app would probably have another level of texture management,
			// like a texture hash.

			int lastLoadedResource = -1;
			int lastTextureId = -1;

			for (int x = 0; x < mSprites.length; x++) {
				int resource = mSprites[x].getResourceId();
				if (resource != lastLoadedResource) {
					int[] retVal = loadBitmap(mContext, gl, resource);
					lastTextureId = retVal[0];
					lastLoadedResource = resource;

					mSprites[x].setTextureName(retVal[0]);
					mSprites[x].width = retVal[1];
					mSprites[x].height = retVal[2];
				}
				mSprites[x].setTextureName(lastTextureId);
				if (mUseHardwareBuffers) {
					Grid currentGrid = mSprites[x].getGrid();
					if (!currentGrid.usingHardwareBuffers()) {
						currentGrid.generateHardwareBuffers(gl);
					}
				}
			}

			calculateMeasurements();
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Public methods

	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			if (isInsideTag(event.getX(), event.getY())) {
				mTagGrabbed = true;
				setAngleAndVelocityByTouchPoint(event.getX(), event.getY());
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			if (mTagGrabbed) {
				setAngleAndVelocityByTouchPoint(event.getX(), event.getY());
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

	public int[] getConfigSpec() {
		// We don't need a depth buffer, and don't care about our
		// color depth.
		int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
		return configSpec;
	}

	public boolean isInsideTag(float x, float y) {
		if (mTagDestRect == null) {
			return false;
		}

		if (mOrientation == Surface.ROTATION_90 || mOrientation == Surface.ROTATION_270) {
			mAngle -= Math.PI;
		}

		final double dx = x - mTagCenterX;
		final double dy = y - mTagCenterY;
		final double newX = mTagCenterX - dx * Math.cos(mAngle) - dy * Math.sin(mAngle);
		final double newY = mTagCenterX - dx * Math.sin(mAngle) + dy * Math.cos(mAngle);

		final RectF tagRect = new RectF(mTagDestRect);
		tagRect.top += mOffsetY;
		tagRect.bottom += mOffsetY;

		return tagRect.contains((float) newX, (float) newY);
	}

	public void postReset() {
		mDoReset = true;
		mDrawingStarted = false;
	}

	private void reset() {
		mAngle = 0;
		mAngularVelocity = 0;

		mAccelX = 0;
		mAccelY = 0;
		mAccelZ = 0;

		mLastAngle = 0;
		mLastTagAngleSetByTouchTime = 0;

		mInertia = 0;
		mMagnitude = 0;
		mMedianAngle = 0;
		mTagAngle = 0;
		mAngleDifference = 0;
		mForce = 0;
		mTorque = 0;
		mFrictionTorque = 0;
		mNetTorque = 0;
		mAngularAcceleration = 0;
		mFrictionZ = 0;
		mChangeInAngle = 0;

		mDoReset = false;
	}

	public synchronized void setAcceleration(float x, float y, float z) {
		mAccelX = x;
		mAccelY = y;
		mAccelZ = z;
	}

	public synchronized void setAngleAndVelocityByTouchPoint(float x, float y) {
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
				Thread.sleep(10);

				now = System.currentTimeMillis();
				delta = (float) (now - mLastTagAngleSetByTouchTime) / 1000f;
			}
			catch (InterruptedException e) {
				// ignore
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

	public void setVertMode(boolean useVerts, boolean useHardwareBuffers) {
		mUseVerts = useVerts;
		mUseHardwareBuffers = useVerts ? useHardwareBuffers : false;
	}

	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}

	public boolean getShowProgress() {
		return mShowProgress;
	}

	public void setShowProgress(boolean showProgress) {
		mShowProgress = showProgress;
		mRingFillSprite.visible = showProgress;
	}

	// Listeners

	public void addOnDrawStartedListener(OnDrawStartedListener onDrawStartedListener) {
		if (mOnDrawStartedListners == null) {
			mOnDrawStartedListners = new ArrayList<OnDrawStartedListener>();
		}

		mOnDrawStartedListners.add(onDrawStartedListener);
	}

	public void removeOnDrawStartedListener(OnDrawStartedListener onDrawStartedListener) {
		if (mOnDrawStartedListners != null) {
			mOnDrawStartedListners.remove(onDrawStartedListener);
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Private methods

	private boolean updateScaledDensity() {
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float newDensity = metrics.scaledDensity;

		if (newDensity != mScaledDensity) {
			mScaledDensity = newDensity;
			mTagWidth = SIZE_TAG_WIDTH * mScaledDensity;
			mTagHeight = SIZE_TAG_HEIGHT * mScaledDensity;
			mKnobBgWidth = SIZE_KNOB_BG_WIDTH * mScaledDensity;
			mKnobBgHeight = SIZE_KNOB_BG_HEIGHT * mScaledDensity;
			mKnobWidth = SIZE_KNOB_WIDTH * mScaledDensity;
			mKnobHeight = SIZE_KNOB_HEIGHT * mScaledDensity;
			mRingWidth = SIZE_RING_WIDTH * mScaledDensity;
			mRingHeight = SIZE_RING_HEIGHT * mScaledDensity;
			mRingFillWidth = SIZE_RING_FILL_WIDTH * mScaledDensity;
			mRingFillHeight = SIZE_RING_FILL_HEIGHT * mScaledDensity;
			return true;
		}
		else {
			return false;
		}
	}

	private void calculateMeasurements() {
		// NOTE: A few of these measurements are pretty arbitrary, definitely
		// making this view a one time use kind of view.

		boolean changed = updateScaledDensity();
		float newOffsetY;
		if (mOrientation == Surface.ROTATION_90 || mOrientation == Surface.ROTATION_270) {
			newOffsetY = mHeight * 0.25f;
		}
		else {
			newOffsetY = mHeight * 0.15f;
		}

		if (newOffsetY != mOffsetY) {
			mOffsetY = newOffsetY;
			changed = true;
		}

		if (!changed) {
			return;
		}

		mRingMargin = (mTagWidth - mRingWidth) / 2;
		mRingLeftOffset = mTagWidth * 0.028f;

		mTagCenterX = mWidth / 2;
		mTagCenterY = mOffsetY + (mTagWidth / 2);

		final float knobTopOffset = (mKnobHeight * 0.10f);

		// DEST RECTS
		mTagDestRect = new RectF();
		mTagDestRect.top = mTagCenterY - (mTagHeight * 0.16f);
		mTagDestRect.bottom = mTagDestRect.top + mTagHeight;
		mTagDestRect.left = (mTagCenterX - (mTagWidth / 2));
		mTagDestRect.right = mTagDestRect.left + mTagWidth;

		mKnobBgDestRect = new RectF();
		mKnobBgDestRect.top = mTagCenterY - (mKnobBgHeight / 2);
		mKnobBgDestRect.bottom = mKnobBgDestRect.top + mKnobBgHeight;
		mKnobBgDestRect.left = (mTagCenterX - (mKnobBgWidth / 2));
		mKnobBgDestRect.right = mKnobBgDestRect.left + mKnobBgWidth;

		mKnobDestRect = new RectF();
		mKnobDestRect.top = mTagCenterY - (mKnobHeight / 2) + knobTopOffset;
		mKnobDestRect.bottom = mKnobDestRect.top + mKnobHeight;
		mKnobDestRect.left = (mTagCenterX - (mKnobWidth / 2));
		mKnobDestRect.right = mKnobDestRect.left + mKnobWidth;

		mRingDestRect = new RectF();
		mRingDestRect.top = mTagDestRect.bottom - mRingHeight - mRingMargin;
		mRingDestRect.bottom = mRingDestRect.top + mRingHeight;
		mRingDestRect.left = (mTagCenterX - (mRingWidth / 2)) + mRingLeftOffset;
		mRingDestRect.right = mRingDestRect.left + mRingWidth;

		mRingFillDestRect = new RectF();
		mRingFillDestRect.top = mRingDestRect.top + ((mRingHeight - mRingFillHeight) / 2);
		mRingFillDestRect.bottom = mRingFillDestRect.top + mRingFillHeight;
		mRingFillDestRect.left = (mTagCenterX - (mRingFillWidth / 2)) + mRingLeftOffset;
		mRingFillDestRect.right = mRingFillDestRect.left + mRingFillWidth;

		mRingFillCenterX = mRingFillDestRect.left + (mRingFillWidth / 2);
		mRingFillCenterY = mRingFillDestRect.top + (mRingFillHeight / 2);

		//calculateTextLayout();

		mTagSprite.x = mTagDestRect.left;
		mTagSprite.y = mHeight - mTagDestRect.bottom;
		mTagSprite.rotationX = mTagWidth / 2;
		mTagSprite.rotationY = mTagHeight - (mTagCenterY - mTagDestRect.top);

		mKnobBgSprite.x = mKnobBgDestRect.left;
		mKnobBgSprite.y = mHeight - mKnobBgDestRect.bottom;

		mKnobSprite.x = mKnobDestRect.left;
		mKnobSprite.y = mHeight - mKnobDestRect.bottom;

		mKnobSprite.x = mKnobDestRect.left;
		mKnobSprite.y = mHeight - mKnobDestRect.bottom;

		mRingFillSprite.x = mRingFillDestRect.left;
		mRingFillSprite.y = mHeight - mRingFillDestRect.bottom;
		mRingFillSprite.rotationX = mRingFillWidth / 2;
		mRingFillSprite.rotationY = mRingFillHeight / 2;
	}

	private void drawFrame(GL10 gl) {
		if (mSprites != null) {

			gl.glMatrixMode(GL10.GL_MODELVIEW);

			if (mUseVerts) {
				Grid.beginDrawing(gl, true, false);
			}

			for (int x = 0; x < mSprites.length; x++) {
				mSprites[x].draw(gl);
			}

			if (mUseVerts) {
				Grid.endDrawing(gl);
			}
		}
	}

	private double normalizeAngle(double angle) {
		return angle % (2 * Math.PI);
	}

	private synchronized void updatePhysics(double delta) {
		// !!! Notes taken from Andy's work on the iOS version !!!

		// Find moment of inertia using I = (1/3) * m * L^2  (this is the moment of inertia equation for a rod of length L and mass m, with the axis of rotation at the end of the rod)
		mInertia = MASS * LENGTH * LENGTH / 3;
		mMagnitude = Math.sqrt(mAccelX * mAccelX + mAccelY * mAccelY);

		// Find angle between Position vector and force vector
		// atan2 produces an angle that is positive for counter-clockwise angles (upper half-plane, y > 0), and negative for clockwise angles (lower half-plane, y < 0)
		mMedianAngle = Math.atan2(mAccelY, mAccelX);
		mTagAngle = 1.5 * Math.PI - mAngle;
		mAngleDifference = mTagAngle - mMedianAngle;

		// Force from the accelerometer is calculated using F = m * a
		mForce = MASS * mMagnitude;

		// Calculate Torque about pivot point using t = rFsin(Theta), where r is the position magnitude, F is the force magnitude, and theta is the angle between the 2 vectors
		mTorque = LENGTH * mForce * Math.sin(mAngleDifference);

		// Calculate Friction
		mFrictionTorque = LENGTH * (FRICTION_HANDLE * mMagnitude) * Math.sin(mAngleDifference);
		if ((mAngularVelocity < 0.0 && mFrictionTorque < 0.0) || (mAngularVelocity > 0.0 && mFrictionTorque > 0.0)) {
			mFrictionTorque *= -1.0;
		}

		// Calculate net torque
		mNetTorque = mTorque + mFrictionTorque;

		// Find angular acceleration using T = Ia where T is torque, I is moment of inertia, and a is angular acceleration
		mAngularAcceleration = mNetTorque / mInertia;
		mAngularVelocity += mAngularAcceleration * delta;

		// Apply door friction

		// Get amount of friction by the amount of acceleration on z.
		mFrictionZ = FRICTION_DOOR * mAccelZ / -GRAVITY;
		if (mFrictionZ > 1) {
			mFrictionZ = 1;
		}

		// This applies the friction past a certain threshold of angle and only if the friction is positive (lying on its back).
		if (mFrictionZ > 0 && mAccelZ < THRESH_DOOR_FRICTION_ANGLE) {
			mAngularVelocity *= (1 - mFrictionZ);
		}

		// This sets the velocity to zero if it's on its back far enough and there's not enough velocity to overcome friction.
		if (mAccelZ < THRESH_DOOR_FRICTION_ANGLE && mAngularVelocity < 0.05 && mAngularVelocity > -0.05) {
			mAngularVelocity = 0;
		}

		mChangeInAngle = mAngularVelocity * delta;
		mAngle += mChangeInAngle;
		mAngle = normalizeAngle(mAngle);

		mLastTagAngleSetByTouchTime = 0;
	}

	private void updateSpritePositions() {
		final double angle = mAngle * 180 / Math.PI;
		mTagSprite.rotation = -angle;

		final double length = mRingFillDestRect.centerY() - mTagCenterY;
		final double progressAngle = normalizeAngle(((double) mNow / 1000) * DEGREES_PER_SECOND) * 180 / Math.PI;
		final double ringAngle = (float) (angle + progressAngle);

		final double glAdjustedAngle = -mAngle - (Math.PI / 2);
		final double offsetX = Math.cos(glAdjustedAngle) * length;
		final double offsetY = (Math.sin(glAdjustedAngle) * length) + length;

		mRingFillSprite.x = mRingFillDestRect.left + (float) offsetX;
		mRingFillSprite.y = mHeight - mRingFillDestRect.bottom + (float) offsetY;
		mRingFillSprite.rotation = -ringAngle;
	}

	/**
	 * Called when the rendering thread shuts down.  This is a good place to
	 * release OpenGL ES resources.
	 * @param gl
	 */
	public void shutdown() {
		Log.t("GL shutdown called.");

		// Clear out the reference so we don't hold onto the opengl context when we shutdown
		mGL = null;
	}

	protected int[] loadBitmap(Context context, GL10 gl, int resourceId) {
		int textureName = -1;
		int[] retVal = new int[3];
		retVal[0] = textureName;

		if (context != null && gl != null) {
			gl.glGenTextures(1, mTextureNameWorkspace, 0);

			textureName = mTextureNameWorkspace[0];
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

			gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

			// use decodeResource since we use scaledDensity everywhere for calculations
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, sBitmapOptions);

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

			mCropWorkspace[0] = 0;
			mCropWorkspace[1] = bitmap.getHeight();
			mCropWorkspace[2] = bitmap.getWidth();
			mCropWorkspace[3] = -bitmap.getHeight();

			retVal[0] = textureName;
			retVal[1] = bitmap.getWidth();
			retVal[2] = bitmap.getHeight();

			bitmap.recycle();

			((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, mCropWorkspace, 0);

			int error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.e("Texture Load GLError: " + error);
			}

		}

		return retVal;
	}

	public void pause() {
		mIsPaused = true;
	}

	public void resume() {
		mIsPaused = false;
	}
}
