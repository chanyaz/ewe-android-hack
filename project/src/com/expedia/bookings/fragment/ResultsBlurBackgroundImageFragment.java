package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
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

import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.interfaces.IBackgroundImageReceiver;

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
	private int mPrevTotalWidth = -1;
	private int mWidth = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LayoutParams frameParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LayoutParams imageParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		mFrameLayout = new FrameLayout(getActivity()) {
			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh) {
				super.onSizeChanged(w, h, oldw, oldh);
				mWidth = w;
				if (mImageView != null && mPrevTotalWidth >= 0) {
					bgImageInDbUpdated(mPrevTotalWidth);
				}
			}
		};
		mFrameLayout.setBackgroundColor(Color.argb(235, 10, 10, 10));
		mFrameLayout.setLayoutParams(frameParams);

		mImageView = new ImageView(getActivity());
		mImageView.setScaleType(ScaleType.FIT_XY);
		mImageView.setLayoutParams(imageParams);
		if (mPrevTotalWidth >= 0) {
			bgImageInDbUpdated(mPrevTotalWidth);
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
	private void loadBitmapFromDb(int blurredWidth, int totalWidth, Bitmap bmap) {
		if (mImageView != null && bmap != null && blurredWidth > 0 && totalWidth > 0) {

			//Aww math, god damn it
			float bmapWidth = bmap.getWidth();
			float theirWidth = totalWidth;
			float ourWidth = blurredWidth;
			float widthRatio = ourWidth / theirWidth;

			int widthPix = (int) (bmapWidth * widthRatio);
			int heightPix = bmap.getHeight();
			int x = (int) Math.max(0, bmapWidth - widthPix);

			if (widthPix > 0) {
				Bitmap bmapClipped = Bitmap.createBitmap(bmap, x, 0, widthPix, heightPix);
				mImageView.setImageBitmap(bmapClipped);
			}
		}
	}

	@Override
	public void bgImageInDbUpdated(int totalRootViewWidth) {
		mPrevTotalWidth = totalRootViewWidth;
		Bitmap bmap = Db.getBackgroundImage(getActivity(), true);
		loadBitmapFromDb(mWidth, totalRootViewWidth, bmap);
	}

}
