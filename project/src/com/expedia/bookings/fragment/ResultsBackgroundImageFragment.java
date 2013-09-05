package com.expedia.bookings.fragment;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ResultsBackgroundImageFragment extends MeasurableFragment {

	private ImageView mImageView;
	private Bitmap mBlurredBmap;

	public static ResultsBackgroundImageFragment newInstance(String destination) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mImageView = new ImageView(getActivity());
		mImageView.setScaleType(ScaleType.FIT_XY);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImageView.setLayoutParams(params);

		if (mBlurredBmap != null) {
			mImageView.setImageBitmap(mBlurredBmap);
			mBlurredBmap = null;//This is just temprorary storage for adding bitmap before initialization
		}

		return mImageView;
	}

	/**
	 * Load up that sweet sweet background bitmap from Db.getBackgroundImageCache
	 */
	public void loadBitmapFromDb() {
		Bitmap bmap = Db.getBackgroundImage(getActivity(), false);
		if (mImageView != null) {
			mImageView.setImageBitmap(bmap);
		}
		else {
			mBlurredBmap = bmap;//Will get picked up in onCreateView
		}
	}
}
