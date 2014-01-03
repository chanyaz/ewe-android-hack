package com.expedia.bookings.fragment;

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

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.interfaces.IBackgroundImageReceiver;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ResultsBackgroundImageFragment extends MeasurableFragment implements IBackgroundImageReceiver {

	private String mDestinationCode; //The destination code to use for background images...

	private ImageView mImageView;
	private Bitmap mBgBitmap;//We temporarily store a bitmap here if we have not yet initialized

	public static ResultsBackgroundImageFragment newInstance(String destination) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		fragment.mDestinationCode = destination;
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mImageView = new ImageView(getActivity());
		mImageView.setScaleType(ScaleType.FIT_XY);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImageView.setLayoutParams(params);

		//TODO: make this match mDestinationCode. Will require network call(s)
		mImageView.setImageResource(R.drawable.temporary_paris_backdrop);

		if (mBgBitmap != null) {
			mImageView.setImageBitmap(mBgBitmap);
			mBgBitmap = null;//This is just temprorary storage for adding bitmap before initialization
		}

		return mImageView;
	}

	@Override
	public void bgImageInDbUpdated(int totalRootViewWidth) {
		Bitmap bmap = Db.getBackgroundImage(getActivity(), false);
		if (bmap != null) {
			if (mImageView != null) {
				mImageView.setImageBitmap(bmap);
			}
			else {
				mBgBitmap = bmap;//Will get picked up in onCreateView
			}
		}
	}
}
