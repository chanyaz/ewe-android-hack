package com.expedia.bookings.fragment;

import java.security.InvalidParameterException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class BlurredBackgroundFragment extends Fragment {

	// Background views
	private ImageView mBackgroundView;
	private ImageView mBackgroundBgView;
	private ImageView mBackgroundFgView;

	private Bitmap mHeaderBitmap;
	private Bitmap mBlurredHeaderBitmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_blurred_bg, container, false);

		mBackgroundView = Ui.findView(v, R.id.background_view);
		mBackgroundBgView = Ui.findView(v, R.id.background_bg_view);
		mBackgroundFgView = Ui.findView(v, R.id.background_fg_view);

		// TODO: Remove this at some point, let people set it on their own!
		setBitmap(null);

		displayBackground();

		return v;
	}

	public void setBitmap(Bitmap bitmap) {
		// TODO: Actually implement dynamic loading of images/blurring
		mHeaderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.san_francisco);
		mBlurredHeaderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.san_francisco_blurred);

		displayBackground();
	}

	private void displayBackground() {
		if (mHeaderBitmap != null && mBlurredHeaderBitmap != null) {
			if (mBackgroundView != null) {
				mBackgroundView.setImageDrawable(new BitmapDrawable(getResources(), mBlurredHeaderBitmap));
			}

			if (mBackgroundBgView != null) {
				mBackgroundBgView.setImageDrawable(new BitmapDrawable(getResources(), mHeaderBitmap));
			}

			if (mBackgroundFgView != null) {
				mBackgroundFgView.setImageDrawable(new BitmapDrawable(getResources(), mBlurredHeaderBitmap));
			}
		}
	}

	// Goes from 0.0 - 1.0
	public void setBlurAmount(float percent) {
		if (percent < 0 || percent > 1.0) {
			throw new InvalidParameterException("percent must be between 0 and 1, but was input as " + percent);
		}

		if (mBackgroundFgView != null) {
			mBackgroundFgView.setAlpha(percent);
		}
	}

}
