package com.expedia.bookings.widget.gl;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.Surface;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

@SuppressWarnings("unused")
class GLTagProgressBarRenderer implements GLSurfaceView.Renderer {
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

	//////////////////////////////////////////////////////////////////////////////
	// Private members

	private Context mContext;

	private long mLastDrawTime = -1;
	private long mNow;

	private double mAngle;
	private double mAngularVelocity = 0;

	private float mAccelX;
	private float mAccelY;
	private float mAccelZ;

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

	int mWidth;
	int mHeight;

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

	private static BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

	private GLSprite[] mSprites;
	private GLSprite mTagSprite;
	private GLSprite mRingSprite;
	private GLSprite mRingFillSprite;
	private GLSprite mKnobSprite;
	private GLSprite mKnobBgSprite;

	private int[] mTextureNameWorkspace;
	private int[] mCropWorkspace;
	private boolean mUseVerts = true;
	private boolean mUseHardwareBuffers = false;

	public GLTagProgressBarRenderer(Context context) {
		mContext = context;

		mTextureNameWorkspace = new int[1];
		mCropWorkspace = new int[4];
		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

		mTagSprite = new GLSprite(R.drawable.gl_progress_tag);
		mRingSprite = new GLSprite(R.drawable.gl_progress_ring);
		mRingFillSprite = new GLSprite(R.drawable.gl_progress_ring_fill);
		mKnobSprite = new GLSprite(R.drawable.gl_progress_knob);
		mKnobBgSprite = new GLSprite(R.drawable.gl_progress_knob_bg);

		// Setup sprites
		mSprites = new GLSprite[5];
		mSprites[0] = mKnobBgSprite;
		mSprites[1] = mTagSprite;
		mSprites[2] = mRingSprite;
		mSprites[3] = mRingFillSprite;
		mSprites[4] = mKnobSprite;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		mNow = System.currentTimeMillis();
		if (mLastDrawTime < 0) {
			mLastDrawTime = mNow;
		}
		final float delta = (float) (mNow - mLastDrawTime) / 1000f;
		if (LOG_FPS) {
			Log.t("FPS: %d", (int) (1f / delta));
		}

		updatePhysics(delta);
		updateSpritePositions();
		drawFrame(gl);
		
		mLastDrawTime = System.currentTimeMillis();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mWidth = width;
		mHeight = height;

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
		/*
		 * Some one-time OpenGL initialization can be made here probably based
		 * on features of this particular context
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glClearColor(1, 1, 1, 1);
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		gl.glDisable(GL10.GL_DITHER);
		gl.glDisable(GL10.GL_LIGHTING);

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

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
					//mSprites[x].getGrid().generateHardwareBuffers(gl);
				}
			}

			calculateMeasurements();
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Public methods

	public int[] getConfigSpec() {
		// We don't need a depth buffer, and don't care about our
		// color depth.
		int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
		return configSpec;
	}

	public synchronized void setAcceleration(float x, float y, float z) {
		mAccelX = x;
		mAccelY = y;
		mAccelZ = z;
	}

	public synchronized void setAngleAndVelocityByTouchPoint(float x, float y) {
		if (!isInsideTag(x, y)) {
			return;
		}

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

	//////////////////////////////////////////////////////////////////////////////
	// Private methods

	void calculateMeasurements() {
		mTagWidth = (int) mTagSprite.width;
		mTagHeight = (int) mTagSprite.height;
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
		mTagDestRect.top = mOffsetY;
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

		//calculateTextLayout();
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

	private boolean isInsideTag(float x, float y) {
		if (mOrientation == Surface.ROTATION_90 || mOrientation == Surface.ROTATION_270) {
			mAngle -= Math.PI;
		}

		final double dx = x - mTagCenterX;
		final double dy = y - mTagCenterY;
		final double newX = mTagCenterX - dx * Math.cos(mAngle) - dy * Math.sin(mAngle);
		final double newY = mTagCenterX - dx * Math.sin(mAngle) + dy * Math.cos(mAngle);

		final Rect tagRect = new Rect(mTagDestRect);
		tagRect.top += mOffsetY;
		tagRect.bottom += mOffsetY;

		return tagRect.contains((int) newX, (int) newY);
	}

	private double normalizeAngle(double angle) {
		return angle % (2 * Math.PI);
	}

	private void updatePhysics(double delta) {
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
		mTagSprite.x = mTagDestRect.left;
		mTagSprite.y = mTagDestRect.top;
		mTagSprite.rotation = mAngle;
	}

	/**
	 * Called when the rendering thread shuts down.  This is a good place to
	 * release OpenGL ES resources.
	 * @param gl
	 */
	public void shutdown(GL10 gl) {
		if (mSprites != null) {

			int lastFreedResource = -1;
			int[] textureToDelete = new int[1];

			for (int x = 0; x < mSprites.length; x++) {
				int resource = mSprites[x].getResourceId();
				if (resource != lastFreedResource) {
					textureToDelete[0] = mSprites[x].getTextureName();
					gl.glDeleteTextures(1, textureToDelete, 0);
					mSprites[x].setTextureName(0);
				}
				if (mUseHardwareBuffers) {
					mSprites[x].getGrid().releaseHardwareBuffers(gl);
				}
			}
		}
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

			InputStream is = context.getResources().openRawResource(resourceId);
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(is, null, sBitmapOptions);
			}
			finally {
				try {
					is.close();
				}
				catch (IOException e) {
					// Ignore.
				}
			}

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
}