package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.graphics.ResilientBitmapDrawable;
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

	protected void loadImageForLaunchStream(String url, final ViewGroup row, final ImageView bgView) {
		UrlBitmapDrawable drawable = UrlBitmapDrawable.loadImageView(url, bgView);
		drawable.setOnImageLoadedCallback(new OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				Log.v("ImageLoaded: " + url);

				onLaunchImageLoaded(bitmap, row, bgView);

				ObjectAnimator.ofFloat(row, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
			}

			public void onImageLoadFailed(String url) {
				Log.v("Image load failed: " + url);
			}
		});
	}

	private void onLaunchImageLoaded(Bitmap bitmap, ViewGroup row, ImageView bgView) {
		row.setVisibility(View.VISIBLE);
		bgView.setImageDrawable(new ResilientBitmapDrawable(getContext().getResources(), bitmap));
	}

	private int mNumTiles = 0;

	protected int getNumTiles() {
		if (mNumTiles == 0) {
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			int height = wm.getDefaultDisplay().getHeight();
			int tileHeight = getTileHeight();
			mNumTiles = (int) Math.ceil((float) height / (float) tileHeight) + 1;
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
