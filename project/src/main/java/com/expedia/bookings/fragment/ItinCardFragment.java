package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ScrollView;

import com.dgmltn.shareeverywhere.ShareView;
import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ItinActionsSection;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.util.CalendarAPIUtils;

import java.util.List;

/**
 * Standalone ItinCard fragment that can be placed anywhere
 */
public class ItinCardFragment extends Fragment implements PopupMenu.OnMenuItemClickListener,
		ShareView.OnShareTargetSelectedListener {

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

		// Show itin Share overflow image only if sharing is supported.
		if (ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
			Ui.setOnClickListener(view, R.id.itin_overflow_image_button, new OnClickListener() {
				@Override
				public void onClick(View v) {
					mShareView.setVisibility(View.VISIBLE);
					onOverflowButtonClicked(v);
				}
			});
		}
		else {
			Ui.findView(view, R.id.itin_overflow_image_button).setVisibility(View.GONE);
			mShareView.setVisibility(View.GONE);
		}

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

	private void onOverflowButtonClicked(View anchorView) {
		PopupMenu popup = new PopupMenu(getActivity(), anchorView);
		popup.setOnMenuItemClickListener(ItinCardFragment.this);
		popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu popupMenu) {
				mShareView.setVisibility(View.INVISIBLE);
			}
		});

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
			mShareView.setOnShareTargetSelectedListener(this);
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
	public void onShareTargetSelected(ShareView view, Intent intent) {
		OmnitureTracking.trackItinShareNew(mCurrentData.getTripComponentType(), intent);
	}

}
