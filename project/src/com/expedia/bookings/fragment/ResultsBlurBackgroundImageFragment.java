package com.expedia.bookings.fragment;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.base.MeasurableFragment;

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

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class ResultsBlurBackgroundImageFragment extends MeasurableFragment {

	public static ResultsBlurBackgroundImageFragment newInstance() {
		ResultsBlurBackgroundImageFragment fragment = new ResultsBlurBackgroundImageFragment();
		return fragment;
	}

	private ImageView mImageView;
	private Bitmap mBlurredBmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LayoutParams frameParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LayoutParams imageParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		FrameLayout frame = new FrameLayout(getActivity());
		frame.setBackgroundColor(Color.argb(235, 10, 10, 10));
		frame.setLayoutParams(frameParams);

		mImageView = new ImageView(getActivity());
		mImageView.setScaleType(ScaleType.FIT_XY);
		mImageView.setLayoutParams(imageParams);
		if (mBlurredBmap != null) {
			mImageView.setImageBitmap(mBlurredBmap);
			mBlurredBmap = null;//This is just temprorary storage for adding bitmap before initialization
		}

		frame.addView(mImageView);
		return frame;
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

}
