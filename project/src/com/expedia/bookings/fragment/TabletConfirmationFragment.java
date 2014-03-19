package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;

public abstract class TabletConfirmationFragment extends Fragment {

	protected abstract int getLayoutId();

	protected abstract int getActionsLayoutId();

	protected abstract String getItinNumber();

	protected abstract String getConfirmationSummaryText();

	protected abstract void shareItinerary();

	protected abstract void addItineraryToCalendar();

	private ViewGroup mBookNextContainer;
	private ViewGroup mDoneBookingContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(getLayoutId(), container, false);

		Ui.setText(v, R.id.confirmation_summary_text, getConfirmationSummaryText());

		Ui.setText(v, R.id.confirmation_itinerary_text_view, getString(R.string.tablet_itinerary_confirmation_TEMPLATE, getItinNumber(), Db.getBillingInfo().getEmail()));

		// Inflate the custom actions layout id
		ViewGroup actionContainer = Ui.findView(v, R.id.custom_actions_container);
		inflater.inflate(getActionsLayoutId(), actionContainer, true);

		mBookNextContainer = Ui.findView(v, R.id.confirmation_book_next_container);
		mBookNextContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Events.post(new Events.BookingConfirmationBookNext());
			}
		});

		if (Db.getTripBucket().getFlight() == null) {
			mBookNextContainer.setVisibility(View.GONE);
			Ui.findView(v, R.id.confirmation_booking_bar_separator).setVisibility(View.GONE);
		}

		mDoneBookingContainer = Ui.findView(v, R.id.confirmation_done_booking_container);
		mDoneBookingContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NavUtils.goToItin(getActivity());
				getActivity().finish();
			}
		});

		Ui.setOnClickListener(v, R.id.call_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getActivity(), PointOfSale.getPointOfSale().getSupportPhoneNumber());
			}
		});

		Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareItinerary();
			}
		});

		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity())) {
			Ui.setOnClickListener(v, R.id.calendar_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					addItineraryToCalendar();
				}
			});
		}
		else {
			Ui.findView(v, R.id.calendar_action_text_view).setVisibility(View.GONE);
		}

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

}
