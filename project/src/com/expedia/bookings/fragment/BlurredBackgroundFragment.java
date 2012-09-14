package com.expedia.bookings.fragment;

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
import com.expedia.bookings.widget.FadingImageView;
import com.mobiata.android.util.Ui;

public class BlurredBackgroundFragment extends Fragment {

	// Background views
	private ImageView mBackgroundBgView;
	private FadingImageView mBackgroundFgView;

	private Bitmap mHeaderBitmap;
	private Bitmap mBlurredHeaderBitmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_blurred_bg, container, false);

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
			if (mBackgroundBgView != null) {
				mBackgroundBgView.setImageDrawable(new BitmapDrawable(getResources(), mHeaderBitmap));
			}

			if (mBackgroundFgView != null) {
				mBackgroundFgView.setImageDrawable(new BitmapDrawable(getResources(), mBlurredHeaderBitmap));
			}
		}
	}

	public void setFadeRange(int startY, int endY) {
		mBackgroundFgView.setFadeRange(startY, endY);

		// Set this view enabled again
		mBackgroundBgView.setVisibility(View.VISIBLE);
	}

	public void setFadeEnabled(boolean enabled) {
		mBackgroundFgView.setFadeEnabled(enabled);

		// Get rid of a View that's being completely obscured to speed things up
		if (!enabled && mBackgroundBgView != null) {
			mBackgroundBgView.setVisibility(View.GONE);
		}
	}
}
