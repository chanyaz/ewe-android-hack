package com.expedia.bookings.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.picasso.Picasso;

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

		PicassoTarget callback = new PicassoTarget() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				super.onBitmapLoaded(bitmap, from);
				bgView.setImageBitmap(bitmap);
				row.setVisibility(View.VISIBLE);
				if (from != Picasso.LoadedFrom.NETWORK) {
					ObjectAnimator.ofFloat(row, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
				}
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				super.onBitmapFailed(errorDrawable);
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {
				super.onPrepareLoad(placeHolderDrawable);
			}
		};
		bgView.setTag(callback);
		new PicassoHelper.Builder(bgView.getContext()).setTarget(callback).build().load(url);
	}

	private int mNumTiles = 0;

	protected int getNumTiles() {
		if (mNumTiles == 0) {
			int height = AndroidUtils.getScreenSize(getContext()).y;
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
