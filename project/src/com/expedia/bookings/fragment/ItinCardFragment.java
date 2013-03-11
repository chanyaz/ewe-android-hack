package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.dialog.SocialMessageChooserDialogFragment;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.itin.ItinContentGenerator;

/**
 * Standalone ItinCard fragment that can be placed anywhere
 */
public class ItinCardFragment extends Fragment {

	private ViewGroup mItinHeaderContainer;
	private ScrollView mItinCardContainer;

	private ItinCardData mCurrentData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itin_card, container, false);
		mItinHeaderContainer = Ui.findView(view, R.id.itin_header_container);
		mItinCardContainer = Ui.findView(view, R.id.itin_card_container);

		Ui.setOnClickListener(view, R.id.share_image_button, new OnClickListener() {
			@Override
			public void onClick(View v) {
				ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(getActivity(), mCurrentData);

				SocialMessageChooserDialogFragment.newInstance(generator).show(getFragmentManager(), "shareDialog");
			}
		});

		return view;
	}

	public boolean showItinDetails(ItinCardData data) {
		if (data == mCurrentData) {
			return false;
		}

		mCurrentData = data;
		Activity activity = getActivity();

		ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(activity, data);
		if (generator != null && generator.hasDetails()) {
			View headerView = generator.getTitleView(mItinHeaderContainer);
			mItinHeaderContainer.removeAllViews();
			mItinHeaderContainer.addView(headerView);

			View detailView = generator.getDetailsView(mItinCardContainer);
			mItinCardContainer.removeAllViews();
			mItinCardContainer.addView(detailView);

			// Make sure we start fully scrolled up when we load new data
			mItinCardContainer.fullScroll(View.FOCUS_UP);

			return true;
		}
		else {
			return false;
		}
	}
}
