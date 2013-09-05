package com.expedia.bookings.fragment;

import com.expedia.bookings.activity.TabletResultsActivity.IBackgroundImageReceiver;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment.IBackgroundImageReceiverRegistrar;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout.LayoutParams;

/**
 * ResultsBackgroundImageFragment: designed for tablet results 2013
 * Used to act as a background for the trip overview fragment...
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class ResultsBlurBackgroundImageFragment extends MeasurableFragment implements IBackgroundImageReceiver {

	public static ResultsBlurBackgroundImageFragment newInstance() {
		ResultsBlurBackgroundImageFragment fragment = new ResultsBlurBackgroundImageFragment();
		return fragment;
	}

	private FrameLayout mFrameLayout;
	private ImageView mImageView;
	private Bitmap mBlurredBmap;

	private int mPrevTotalWidth = -1;
	private int mPrevTotalHeight = -1;

	private IBackgroundImageReceiverRegistrar mBgProvider;

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
		LayoutParams frameParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LayoutParams imageParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		mFrameLayout = new FrameLayout(getActivity()) {
			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh) {
				super.onSizeChanged(w, h, oldw, oldh);
				if (mImageView != null && mImageView.getDrawable() != null && mPrevTotalWidth >= 0
						&& mPrevTotalHeight >= 0) {
					loadBitmapFromDb(w, h, mPrevTotalWidth, mPrevTotalHeight);
				}
			}
		};
		mFrameLayout.setBackgroundColor(Color.argb(235, 10, 10, 10));
		mFrameLayout.setLayoutParams(frameParams);

		mImageView = new ImageView(getActivity());
		mImageView.setScaleType(ScaleType.FIT_XY);
		mImageView.setLayoutParams(imageParams);
		if (mBlurredBmap != null) {
			mImageView.setImageBitmap(mBlurredBmap);
			mBlurredBmap = null;//This is just temprorary storage for adding bitmap before initialization
		}

		mFrameLayout.addView(mImageView);
		return mFrameLayout;
	}

	/**
	 * Load up that sweet sweet blurred bitmap from Db.getBackgroundImageCache
	 * and clip it to the appropriate size.
	 * 
	 * @param blurredWidth - how wide is our blurred container *supposed to be*
	 * @param blurredHeight - how tall is our blurred container *supposed to be*
	 * @param totalWidth - how big is the full background that we are laying this over
	 * @param totalHeight - how tall is the full background that we are laying this over
	 */
	public void loadBitmapFromDb(int blurredWidth, int blurredHeight, int totalWidth, int totalHeight) {
		Bitmap bmap = Db.getBackgroundImage(getActivity(), true);

		//Aww math, god damn it
		float bw = bmap.getWidth();
		float bh = bmap.getHeight();
		float tw = totalWidth;
		float ow = blurredWidth;
		float th = totalHeight;
		float oh = blurredHeight;

		float ratioW = ow / tw;
		float ratioH = oh / th;

		int widthPix = (int) (bw * ratioW);
		int heightPix = (int) (bh * ratioH);

		Bitmap bmapClipped = Bitmap.createBitmap(bmap, (int) (bw - widthPix), 0, widthPix, heightPix);
		if (mImageView != null) {
			mImageView.setImageBitmap(bmapClipped);
		}
		else {
			mBlurredBmap = bmapClipped;//Will get picked up in onCreateView
		}
	}

	@Override
	public void bgImageInDbUpdated(int totalRootViewWidth, int totalRootViewHeight) {
		if (totalRootViewWidth > 0 && totalRootViewHeight > 0) {
			mPrevTotalWidth = totalRootViewWidth;
			mPrevTotalHeight = totalRootViewHeight;
			loadBitmapFromDb(mFrameLayout.getWidth(), mFrameLayout.getHeight(), totalRootViewWidth, totalRootViewHeight);
		}
	}

}
