package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;

/**
 * A view that draws a mask color everywhere except over the specified View. For instance, we'll
 * use it to highlight the CVV field on the checkout screen. This works best on layouts where views
 * can be stacked on top of each other (like FrameLayout or RelativeLayout). This view should be
 * a sibling of the view to be exposed.
 *
 *   <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *       xmlns:app="http://schemas.android.com/apk/res-auto"
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent" >
 *
 *       ....
 *
 *       <com.expedia.bookings.widget.MaskView
 *           android:layout_width="match_parent"
 *           android:layout_height="match_parent"
 *           app:exposedView="@id/exposed_view"
 *           app:maskColor="#66000000"
 *           app:exposedPadding="2dp" />
 *
 *   </RelativeLayout>
 */
public class MaskView extends View {

	private static final int DEFAULT_EXPOSED_VIEW_RES_ID = View.NO_ID;
	private static final int DEFAULT_MASK_COLOR = 0x66000000;

	private int mExposedViewResId = DEFAULT_EXPOSED_VIEW_RES_ID;
	private int mMaskColor = DEFAULT_MASK_COLOR;
	private int mExposedPadding = 0;

	private Paint mPlainPaint;
	private Paint mShadePaint;

	// Pre-allocate for rendering
	private Rect mClipBounds;
	private final int[] mThisLocation = new int[2];
	private final int[] mExposedLocation = new int[2];

	public MaskView(Context context) {
		super(context);
		init(context, null);
	}

	public MaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public MaskView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attr) {
		if (attr != null) {
			TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.MaskView, 0, 0);
			mExposedViewResId = ta.getResourceId(R.styleable.MaskView_exposedView, DEFAULT_EXPOSED_VIEW_RES_ID);
			mExposedPadding = ta.getDimensionPixelSize(R.styleable.MaskView_exposedPadding, 0);
			mMaskColor = ta.getColor(R.styleable.MaskView_maskColor, DEFAULT_MASK_COLOR);
			ta.recycle();
		}

		mPlainPaint = new Paint();

		mShadePaint = new Paint();
		mShadePaint.setColor(mMaskColor);
		mShadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

		mClipBounds = new Rect();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw the shaded portion everywhere except the
		canvas.getClipBounds(mClipBounds);

		canvas.saveLayer(mClipBounds.left, mClipBounds.top, mClipBounds.right, mClipBounds.bottom, null,
			Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);

		// Draw knocked-out portion
		if (getParent() instanceof View) {
			View exposedView = ((View) getParent()).findViewById(mExposedViewResId);
			if (exposedView != null) {
				exposedView.getLocationInWindow(mExposedLocation);
				getLocationInWindow(mThisLocation);
				canvas.drawRect(
					mExposedLocation[0] - mExposedPadding - mThisLocation[0],
					mExposedLocation[1] - mExposedPadding - mThisLocation[1],
					mExposedLocation[0] + mExposedPadding - mThisLocation[0] + exposedView.getWidth(),
					mExposedLocation[1] + mExposedPadding - mThisLocation[1] + exposedView.getHeight(),
					mPlainPaint);
			}
		}

		// Fill rest of rectangle
		canvas.drawRect(mClipBounds, mShadePaint);

		canvas.restore();
	}
}
