package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;

public class HotelConfirmationFragment extends ConfirmationFragment {

	public static final String TAG = HotelConfirmationFragment.class.getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// This can be invoked when the parent activity finishes itself (when it detects missing data, in the case of
		// a background kill). In this case, lets just return a null view because it won't be used anyway. Prevents NPE.
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		PointOfSale pos = PointOfSale.getPointOfSale();
		if (pos.showHotelCrossSell() && pos.supportsFlights()) {
			ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.get_there_text_view));

			String city = Db.getSelectedProperty().getLocation().getCity();
			Ui.setText(v, R.id.flights_action_text_view, getString(R.string.flights_to_TEMPLATE, city));
			Ui.setOnClickListener(v, R.id.flights_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					searchForFlights();
				}
			});
		}
		else {
			Ui.findView(v, R.id.get_there_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.get_there_text_divider).setVisibility(View.GONE);
			Ui.findView(v, R.id.flights_action_text_view).setVisibility(View.GONE);
		}

		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.more_actions_text_view));

		Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});

		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity())) {
			Ui.setOnClickListener(v, R.id.calendar_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					addToCalendar();
				}
			});
		}
		else {
			Ui.findView(v, R.id.calendar_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.calendar_divider).setVisibility(View.GONE);
		}

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// ConfirmationFragment

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_hotel_confirmation;
	}

	@Override
	protected int getActionsLayoutId() {
		return R.layout.include_confirmation_actions_hotels;
	}

	@Override
	protected String getItinNumber() {
		return Db.getBookingResponse().getItineraryId();
	}

	//////////////////////////////////////////////////////////////////////////
	// Cross-sell

	private void searchForFlights() {
		// TODO
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
		// TODO
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private void addToCalendar() {
		// TODO
	}
}
