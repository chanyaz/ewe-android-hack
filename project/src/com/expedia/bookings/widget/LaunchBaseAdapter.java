package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.RelativeLayout;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.nineoldandroids.animation.ObjectAnimator;

public abstract class LaunchBaseAdapter<T> extends CircularArrayAdapter<T> implements OnMeasureListener {

	protected static final int DURATION_FADE_MS = 700;

	private boolean mIsMeasuring = false;

	public LaunchBaseAdapter(Context context, int resId) {
		super(context, resId);
	}

	protected boolean isMeasuring() {
		return mIsMeasuring;
	}

	//////////////////////////////////////////////////////////////////////////
	// OnMeasureListener

	@Override
	public void onStartMeasure() {
		mIsMeasuring = true;
	}

	@Override
	public void onStopMeasure() {
		mIsMeasuring = false;
	}

}
