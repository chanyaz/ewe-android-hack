package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.expedia.bookings.R;

/**
 * A FrameLayout that can apply rounded corners to its children, with a couple caveats:
 * <p/>
 * 1. Do not use this if you simply want an image with rounded corners. In that case, use a
 * (more performant) BitmapShader.
 * <p/>
 * 2. This does not perform antialiasing on the rounded corners, due to API limitations. So make
 * sure they look ok.
 * <p/>
 * This works quite nicely in other cases.
 *
 * @author doug
 * @see { http://stackoverflow.com/questions/12358350/rendering-rounded-corners-for-imageview-on-android }
 */
public class RoundedCornerFrameLayout extends FrameLayout {

	private Path roundedPath;
	private float mRadius;

	public RoundedCornerFrameLayout(Context context) {
		this(context, null);
	}

	public RoundedCornerFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundedCornerFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedCornerFrameLayout);
			// Set the rounded rect radius (default 0)
			mRadius = a.getDimension(R.styleable.RoundedCornerFrameLayout_radius, 0);
			a.recycle();
		}

		setRadius(mRadius);

		// If the application is hardware accelerated,
		// must disable it for this view.
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw || h != oldh) {
			roundedPath = new Path();
			roundedPath.addRoundRect(new RectF(0, 0, w, h),
					mRadius, mRadius, Path.Direction.CW);
		}
	}

	public void setRadius(float radius) {
		mRadius = radius;
		roundedPath = new Path();
		roundedPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()),
				radius, radius, Path.Direction.CW);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		//Apply the clip
		if (mRadius != 0) {
			canvas.clipPath(roundedPath);
		}
		//Let the view draw as normal
		super.dispatchDraw(canvas);
	}
}
