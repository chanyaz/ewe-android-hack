package com.expedia.bookings.fragment;

import com.expedia.bookings.activity.TabletResultsActivity.IBackgroundImageReceiver;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.app.Activity;
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
public class ResultsBackgroundImageFragment extends MeasurableFragment implements IBackgroundImageReceiver {

	public interface IBackgroundImageReceiverRegistrar {
		public void registerBgImageReceiver(IBackgroundImageReceiver receiver);

		public void unRegisterBgImageReceiver(IBackgroundImageReceiver receiver);
	}

	private ImageView mImageView;
	private Bitmap mBlurredBmap;
	private IBackgroundImageReceiverRegistrar mBgProvider;

	public static ResultsBackgroundImageFragment newInstance(String destination) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mBgProvider = Ui.findFragmentListener(this, IBackgroundImageReceiverRegistrar.class, true);
		mBgProvider.registerBgImageReceiver(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mBgProvider.unRegisterBgImageReceiver(this);
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

	@Override
	public void bgImageInDbUpdated(int totalRootViewWidth, int totalRootViewHeight) {
		Bitmap bmap = Db.getBackgroundImage(getActivity(), false);
		if (mImageView != null) {
			mImageView.setImageBitmap(bmap);
		}
		else {
			mBlurredBmap = bmap;//Will get picked up in onCreateView
		}
	}
}
