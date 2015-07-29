package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataAirAttach;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelCrossSellUtils;

public class AirAttachItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataAirAttach> {

	public AirAttachItinContentGenerator(Context context, ItinCardDataAirAttach itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public Type getType() {
		return Type.FLIGHT;
	}

	@Override
	public View.OnClickListener getOnItemClickListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HotelCrossSellUtils.deepLinkHotels(v.getContext(), getItinCardData().getSearchParams());
				OmnitureTracking.trackAirAttachItinCrossSell();
			}
		};
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
		if (convertView == null) {
			convertView = getLayoutInflater().inflate(R.layout.itin_air_attach_card, container, false);
		}
		return convertView;
	}
}
