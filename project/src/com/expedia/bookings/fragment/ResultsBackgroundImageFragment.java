package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ResultsBackgroundImageFragment extends MeasurableFragment implements L2ImageCache.OnBitmapLoaded {

	private static final int HALF_FADE_IN_TIME = 500;

	private static final String ARG_DEST_CODE = "ARG_DEST_CODE";
	private static final String ARG_BLUR = "ARG_BLUR";

	private String mDestinationCode;
	private boolean mBlur;

	private int mWidth = -1;
	private int mHeight = -1;

	private ImageView mImageView;

	private Bitmap mBgBitmap; // We temporarily store a bitmap here if we have not yet initialized

	public static ResultsBackgroundImageFragment newInstance(String destination, boolean blur) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		Bundle args = new Bundle();
		args.putString(ARG_DEST_CODE, destination);
		args.putBoolean(ARG_BLUR, blur);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Fragment arguments
		Bundle args = getArguments();
		mDestinationCode = args.getString(ARG_DEST_CODE);
		mBlur = args.getBoolean(ARG_BLUR);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mImageView = new ImageView(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImageView.setLayoutParams(params);

		final ExpediaImageManager imageManager = ExpediaImageManager.getInstance();

		// Check to see if bitmap was retrieved before onCreateView
		if (mBgBitmap != null) {
			handleBitmap(mBgBitmap, false);
			mBgBitmap = null;
		}
		else if (!imageManager.isDownloadingDestinationImage(mBlur)) {
			mImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
					mWidth = mImageView.getWidth();
					mHeight = mImageView.getHeight();

					imageManager.loadDestinationBitmap(makeImageParams(), ResultsBackgroundImageFragment.this);
					return true;
				}
			});
		}

		return mImageView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Sp.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Sp.getBus().unregister(this);
	}

	///////////////////////////////////////////////////////////////
	// Otto

	@Subscribe
	public void onSpChange(Sp.SpUpdateEvent event) {
		String newCode = Sp.getParams().getDestination().getAirportCode();
		if (!TextUtils.isEmpty(newCode) && !newCode.equals(mDestinationCode)) {
			getArguments().putString(ARG_DEST_CODE, newCode);
			mDestinationCode = newCode;

			// Start new dl
			ExpediaImageManager.getInstance().cancelDownloadingDestinationImage(mBlur);
			if (hasDimensions()) {
				ExpediaImageManager.getInstance().loadDestinationBitmap(makeImageParams(), this);
			}
			else {
				if (mImageView != null) {
					mImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
						@Override
						public boolean onPreDraw() {
							mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
							mWidth = mImageView.getWidth();
							mHeight = mImageView.getHeight();

							ExpediaImageManager.getInstance().loadDestinationBitmap(makeImageParams(), ResultsBackgroundImageFragment.this);
							return true;
						}
					});
				}
			}
		}
	}

	private boolean hasDimensions() {
		return mWidth != -1 && mHeight != - 1;
	}

	private ExpediaImageManager.ImageParams makeImageParams() {
		return new ExpediaImageManager.ImageParams().setBlur(mBlur).setWidth(mWidth).setHeight(mHeight);
	}

	///////////////////////////////////////////////////////////////
	// OnBitmapLoaded

	@Override
	public void onBitmapLoaded(String url, Bitmap bitmap) {
		handleBitmap(bitmap, true);
	}

	@Override
	public void onBitmapLoadFailed(String url) {
		Log.e("ResultsBackgroundImageFragment - onBitmapLoadFailed");
	}

	private void handleBitmap(final Bitmap bitmap, boolean fade) {
		if (bitmap != null) {
			if (mImageView != null) {
				if (fade) {
					// Use ViewPropertyAnimator to run a simple fade in + fade out animation to update the
					// ImageView
					mImageView.animate()
						.alpha(0f)
						.setDuration(mImageView.getDrawable() == null ? 0 : HALF_FADE_IN_TIME)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mImageView.setImageBitmap(bitmap);
								mImageView.animate()
									.alpha(1f)
									.setDuration(HALF_FADE_IN_TIME)
									.setListener(null);
							}
						});
				}
				else {
					mImageView.setImageBitmap(bitmap);
				}
			}
			else {
				mBgBitmap = bitmap; // Store the Bitmap to get picked up in onCreateView
			}
		}
		else {
			Log.v("bitmap null null null");
		}
	}

}
