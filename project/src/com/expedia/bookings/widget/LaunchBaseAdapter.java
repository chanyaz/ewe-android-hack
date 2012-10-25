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

	private Context mContext;
	private boolean mIsMeasuring = false;

	public LaunchBaseAdapter(Context context, int resId) {
		super(context, resId);
		mContext = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Private methods and stuff

	protected boolean loadImageForLaunchStream(String url, final RelativeLayout layout) {
		String key = layout.toString();
		Log.v("Loading RelativeLayout bg " + key + " with " + url);

		// Begin a load on the ImageView
		ImageCache.OnImageLoaded callback = new ImageCache.OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				Log.v("ImageLoaded: " + url);

				layout.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
				layout.setVisibility(View.VISIBLE);
				ObjectAnimator.ofFloat(layout, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
			}

			public void onImageLoadFailed(String url) {
				Log.v("Image load failed: " + url);
			}
		};

		return ImageCache.loadImage(key, url, callback);
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
