package com.expedia.account;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * A custom ImageView that scales its image to the height of the *screen*,
 * and adds a .setPan() method to reveal a different portion of the image.
 *
 * Created by doug on 5/8/15.
 */
public class PanningImageView extends ImageView {

	private float mScale = 1f;
	private float mPan = 0f;
	private float mPanFrom = 0f;
	private float mPanTo = 1f;
	private float mPanScale = 0f;
	private Matrix mMatrix;

	public PanningImageView(Context context) {
		super(context);
		init(context, null);
	}

	public PanningImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public PanningImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@TargetApi(21)
	public PanningImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setScaleType(ScaleType.MATRIX);

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.acct__PanningImageView);
			mPan = a.getFloat(R.styleable.acct__PanningImageView_acct__piv_pan, mPan);
			mPanFrom = a.getFloat(R.styleable.acct__PanningImageView_acct__piv_panFrom, mPanFrom);
			mPanTo = a.getFloat(R.styleable.acct__PanningImageView_acct__piv_panTo, mPanTo);
			a.recycle();
		}

		calculateScale();
		updateMatrix();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		calculateScale();
		updateMatrix();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		calculateScale();
		updateMatrix();
	}

	/**
	 * Sets the percentage by which this view should be panned.
	 * 0.0 = all the way to the left, 1.0 = all the way to the right.
	 * @param percent
	 */
	public void setPan(float percent) {
		mPan = percent;
		updateMatrix();
	}

	public float getPan() {
		return mPan;
	}

	/**
	 * Obscures the image by multiplying it with a dim ColorMatrix. The
	 * dim color is hard-coded as 0x2770c6. This could be abstracted to
	 * a view attribute some day.
	 * @param percent
	 */
	public void setObscure(float percent) {
		// Multiplying with 0xffffff is an identity color filter,
		// And we want to gradually fade to multiplying by 0x2770c6,
		// so the color values must change thusly:
		// Red: 0xff -> 0x27
		// Green: 0xff -> 0x70
		// Blue: 0xff -> 0xc6
		int red = (int)((0x27 - 0xff) * percent + 0xff);
		int green = (int)((0x70 - 0xff) * percent + 0xff);
		int blue = (int)((0xc6 - 0xff) * percent + 0xff);
		int color = Color.argb(0xff, red, green, blue);

		// We're stuck creating a new ColorFilter here. The .setColor method is not visible.
		ColorFilter cf = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
		setColorFilter(cf);
	}

	private void calculateScale() {
		Drawable drawable = getDrawable();
		if (drawable == null) {
			mScale = 1f;
			mPanScale = 0f;
			return;
		}

		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point screenSize = new Point();
		display.getSize(screenSize);

		float drawableAspect = (float) drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
		float screenAspect = (float) screenSize.x / screenSize.y;

		if (screenAspect < drawableAspect) {
			// How much by which to scale the bitmap to make it fit the height of the screen.
			// Based on the ratio of the aspect ratios, we know that the bitmap is wide enough
			// to fill the width of the screen when scaled.
			mScale = (float) screenSize.y / drawable.getIntrinsicHeight();

			// Factor by which to scale the pan percent into pixels to reveal the entire bitmap.
			mPanScale = screenSize.x - drawable.getIntrinsicWidth() * mScale;
		}
		else {
			// We can't do much except scale the picture up to match the width of the screen.
			mScale = (float) screenSize.x / drawable.getIntrinsicWidth();
			mPanScale = 0f;
		}
	}

	private void updateMatrix() {
		if (mMatrix == null) {
			mMatrix = new Matrix();
		}
		else {
			mMatrix.reset();
		}
		mMatrix.postScale(mScale, mScale);
		mMatrix.postTranslate((mPan * (mPanTo - mPanFrom) + mPanFrom) * mPanScale, 0);
		setImageMatrix(mMatrix);
	}
}
