package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dgmltn.shareeverywhere.ShareView;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AbsPopupMenu;
import com.expedia.bookings.widget.ItinActionsSection;
import com.expedia.bookings.widget.PopupMenu;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.util.CalendarAPIUtils;

/**
 * Standalone ItinCard fragment that can be placed anywhere
 */
public class ItinCardFragment extends Fragment implements AbsPopupMenu.OnMenuItemClickListener,
		ShareView.ShareViewListener {

	private ViewGroup mItinHeaderContainer;
	private ScrollView mItinCardContainer;
	private ViewGroup mItinSummaryContainer;
	private ViewGroup mItinDetailsContainer;
	private ItinActionsSection mActionButtons;

	private ItinCardData mCurrentData;

	private ShareView mShareView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itin_card, container, false);
		mItinHeaderContainer = Ui.findView(view, R.id.itin_header_container);
		mItinCardContainer = Ui.findView(view, R.id.itin_card_container);
		mItinSummaryContainer = Ui.findView(view, R.id.summary_layout);
		mItinDetailsContainer = Ui.findView(view, R.id.itin_details_container);
		mActionButtons = Ui.findView(view, R.id.action_button_layout);
		mShareView = Ui.findView(view, R.id.itin_share_view);

		Ui.setOnClickListener(view, R.id.itin_overflow_image_button, new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOverflowButtonClicked(v);
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

			//Remove share and add to calendar for travelocity
			if(ExpediaBookingApp.IS_TRAVELOCITY) {
				Ui.findView(activity, R.id.fragment_title_menu_layout).setVisibility(View.GONE);
			}

			return true;
		}
		else {
			return false;
		}
	}

	private void onOverflowButtonClicked(View anchorView) {
		PopupMenu popup = new PopupMenu(getActivity(), anchorView);
		popup.setOnMenuItemClickListener(ItinCardFragment.this);

		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_itin_expanded_overflow, popup.getMenu());

		// Only show add to calendar on devices and card types that are supported
		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity())) {
			ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(getActivity(), mCurrentData);
			List<Intent> intents = generator.getAddToCalendarIntents();
			if (intents.isEmpty()) {
				popup.getMenu().removeItem(R.id.itin_card_add_to_calendar);
			}
		}
		else {
			popup.getMenu().removeItem(R.id.itin_card_add_to_calendar);
		}

		popup.show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		ItinContentGenerator<?> generator = ItinContentGenerator.createGenerator(getActivity(), mCurrentData);
		switch (item.getItemId()) {
		case R.id.itin_card_share:
			ShareUtils shareUtils = new ShareUtils(getActivity());
			mShareView.setShareIntent(shareUtils.getShareIntents(generator));
			mShareView.setListener(this);
			mShareView.showPopup();
			return true;
		case R.id.itin_card_add_to_calendar:
			for (Intent intent : generator.getAddToCalendarIntents()) {
				startActivity(intent);
			}
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onShareAppSelected(Intent intent) {
		OmnitureTracking.trackItinShareNew(getActivity(), mCurrentData.getTripComponentType(), intent);
	}

}
