package com.expedia.bookings.widget.itin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class HotelAttachItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataHotelAttach> {
	public HotelAttachItinContentGenerator(Context context, ItinCardDataHotelAttach itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public View getDetailsView(ViewGroup container) {
		final View view = getLayoutInflater().inflate(R.layout.include_itin_button_hotel_attach, container, false);
		final String buttonText;

		FlightLeg flightLeg = getItinCardData().getFlightLeg();
		if (flightLeg != null && flightLeg.getLastWaypoint() != null
				&& flightLeg.getLastWaypoint().getAirport() != null
				&& !TextUtils.isEmpty(flightLeg.getLastWaypoint().getAirport().mCity)) {

			buttonText = getContext().getString(R.string.add_hotel_TEMPLATE,
					flightLeg.getLastWaypoint().getAirport().mCity);
		}
		else {
			buttonText = getContext().getString(R.string.add_hotel_fallback);
		}

		Ui.setImageResource(view, R.id.action_image_view, R.drawable.ic_hotel_attach);
		Ui.setText(view, R.id.action_text_view, buttonText);

		return view;
	}

	@Override
	public Runnable getOnItemClickRunnable(final Context context) {
		return new Runnable() {
			@Override
			public void run() {
				OmnitureTracking.trackCrossSellItinToHotel(context);

				Db.setSearchParams(getItinCardData().getSearchParams());

				Class<? extends Activity> targetClass = ExpediaBookingApp.useTabletInterface(context) ? SearchResultsFragmentActivity.class
						: PhoneSearchActivity.class;

				Intent intent = new Intent(context, targetClass);
				intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);

				context.startActivity(intent);
			}
		};
	}
}
