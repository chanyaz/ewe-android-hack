package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelCrossSellUtils;
import com.expedia.bookings.utils.Ui;

public class HotelAttachItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataHotelAttach> {

	public HotelAttachItinContentGenerator(Context context, ItinCardDataHotelAttach itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public Type getType() {
		return Type.FLIGHT;
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
		if (convertView == null) {
			convertView = getLayoutInflater().inflate(R.layout.include_itin_button_hotel_attach, container, false);
		}

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

		Ui.setText(convertView, R.id.action_text_view, buttonText);

		return convertView;
	}

	@Override
	public View.OnClickListener getOnItemClickListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HotelCrossSellUtils.deepLinkHotels(v.getContext(), getItinCardData().getSearchParams());
				OmnitureTracking.trackCrossSellItinToHotel();
			}
		};
	}

}
