package com.expedia.bookings.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.BoundedBottomImageView;
import com.expedia.bookings.widget.FadingImageView;

public class BlurredBackgroundFragment extends Fragment {

	public static final String TAG = BlurredBackgroundFragment.class.getName();

	// Background views
	private BoundedBottomImageView mBackgroundBgView;
	private FadingImageView mBackgroundFgView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_blurred_bg, container, false);

		mBackgroundBgView = Ui.findView(v, R.id.background_bg_view);
		mBackgroundFgView = Ui.findView(v, R.id.background_fg_view);

		loadBitmapFromCache();

		return v;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mBackgroundBgView = null;
		mBackgroundFgView = null;
	}

	public void loadBitmapFromCache() {
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return;
		}

		Point portrait = Ui.getPortraitScreenSize(getActivity());
		final String code = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();
		final String url = new Akeakamai(Images.getFlightDestination(code)) //
			.resizeExactly(portrait.x, portrait.y) //
			.build();

		new PicassoHelper.Builder(mBackgroundFgView).setPlaceholder(R.drawable.default_flights_background_blurred)
			.applyBlurTransformation(true).build().load(url);
		new PicassoHelper.Builder(mBackgroundBgView).setPlaceholder(R.drawable.default_flights_background).build()
			.load(url);
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
