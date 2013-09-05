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
import com.expedia.bookings.widget.ItinActionsSection;
import com.expedia.bookings.widget.itin.ItinContentGenerator;

/**
 * Standalone ItinCard fragment that can be placed anywhere
 */
public class ItinCardFragment extends Fragment {

	private ViewGroup mItinHeaderContainer;
	private ScrollView mItinCardContainer;
	private ViewGroup mItinSummaryContainer;
	private ViewGroup mItinDetailsContainer;
	private ItinActionsSection mActionButtons;

	private ItinCardData mCurrentData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itin_card, container, false);
		mItinHeaderContainer = Ui.findView(view, R.id.itin_header_container);
		mItinCardContainer = Ui.findView(view, R.id.itin_card_container);
		mItinSummaryContainer = Ui.findView(view, R.id.summary_layout);
		mItinDetailsContainer = Ui.findView(view, R.id.itin_details_container);
		mActionButtons = Ui.findView(view, R.id.action_button_layout);

		Ui.setOnClickListener(view, R.id.itin_overflow_image_button, new OnClickListener() {
			@Override
			public void onClick(View v) {
				ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(getActivity(), mCurrentData);

				SocialMessageChooserDialogFragment.newInstance(generator).show(getFragmentManager(), "shareDialog");
			}
		});

		return view;
	}

	public ItinCardData getItinCardData() {
		return mCurrentData;
	}

	public boolean showItinDetails(ItinCardData data) {
		return showItinDetails(data, true);
	}

	public boolean showItinDetails(ItinCardData data, boolean scrollToTop) {
		if (data == mCurrentData) {
			return false;
		}

		mCurrentData = data;
		Activity activity = getActivity();

		ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(activity, data);
		if (generator != null && generator.hasDetails()) {
			View headerView = generator.getTitleView(null, mItinHeaderContainer);
			mItinHeaderContainer.removeAllViews();
			mItinHeaderContainer.addView(headerView);

			View summaryView = generator.getSummaryView(null, mItinSummaryContainer);
			mItinSummaryContainer.removeAllViews();
			mItinSummaryContainer.addView(summaryView);

			View detailView = generator.getDetailsView(null, mItinDetailsContainer);
			mItinDetailsContainer.removeAllViews();
			mItinDetailsContainer.addView(detailView);

			mActionButtons.bind(generator.getSummaryLeftButton(), generator.getSummaryRightButton());

			if (scrollToTop) {
				mItinCardContainer.fullScroll(View.FOCUS_UP);
			}

			return true;
		}
		else {
			return false;
		}
	}
}
