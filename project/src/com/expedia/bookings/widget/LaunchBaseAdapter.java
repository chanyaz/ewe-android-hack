package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
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

	//////////////////////////////////////////////////////////////////////////
	// Utility

	private int mNumTiles = 0;

	protected int getNumTiles() {
		if (mNumTiles == 0) {
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			int height = wm.getDefaultDisplay().getHeight();
			int tileHeight = getTileHeight();
			mNumTiles = (int) Math.ceil((float) height / (float) tileHeight);
		}
		return mNumTiles;
	}

	// Make sure we have at least mNumTiles views in the cache.  We want to make sure
	// that it's also a multiple of the size, so we repeat intelligently.
	protected int getViewCacheSize(int numResults) {
		int numTiles = getNumTiles();
		if (numResults == 0) {
			return numTiles;
		}
		else {
			if (numResults < numTiles) {
				return numResults * (int) Math.ceil((float) numTiles / (float) numResults);
			}
			else {
				return numResults;
			}
		}
	}

	public abstract int getTileHeight();
}
