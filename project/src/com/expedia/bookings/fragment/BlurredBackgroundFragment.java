package com.expedia.bookings.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.BoundedBottomImageView;
import com.expedia.bookings.widget.FadingImageView;
import com.mobiata.android.util.Ui;

public class BlurredBackgroundFragment extends Fragment {

	public static final String TAG = BlurredBackgroundFragment.class.getName();

	// Background views
	private BoundedBottomImageView mBackgroundBgView;
	private FadingImageView mBackgroundFgView;

	private Bitmap mBgBitmap;
	private Bitmap mBlurredBgBitmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_blurred_bg, container, false);

		LayoutUtils.adjustPaddingForOverlayMode(getActivity(), v, false);

		mBackgroundBgView = Ui.findView(v, R.id.background_bg_view);
		mBackgroundFgView = Ui.findView(v, R.id.background_fg_view);

		setBitmap(Db.getBackgroundImage(getActivity(), false), Db.getBackgroundImage(getActivity(), true));

		return v;
	}

	public void setBitmap(Bitmap bgBitmap, Bitmap blurredBgBitmap) {
		mBgBitmap = bgBitmap;
		mBlurredBgBitmap = blurredBgBitmap;
		displayBackground();
	}

	private void displayBackground() {
		if (mBgBitmap != null && mBlurredBgBitmap != null) {
			if (mBackgroundBgView != null) {
				mBackgroundBgView.setImageDrawable(new BitmapDrawable(getResources(), mBgBitmap));
			}

			if (mBackgroundFgView != null) {
				mBackgroundFgView.setImageDrawable(new BitmapDrawable(getResources(), mBlurredBgBitmap));
			}
		}
	}

	public void setFadeRange(int startY, int endY) {
		mBackgroundFgView.setFadeRange(startY, endY);

		// This optimization is only necessary (and, in fact, only works) on the old
		// rendering system.  In the new rendering system, this view is only drawn
		// once anyways (and reused) so it doesn't matter that we're not planning
		// for overdraw.
		if (Build.VERSION.SDK_INT < 11) {
			mBackgroundBgView.setBottomBound(endY);
		}

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
