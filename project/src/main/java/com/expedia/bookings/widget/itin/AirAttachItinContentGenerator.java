package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataAirAttach;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.util.AndroidUtils;

public class AirAttachItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataAirAttach> {
	public AirAttachItinContentGenerator(Context context, ItinCardDataAirAttach itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public Type getType() {
		return Type.AIR_ATTACH;
	}

	@Override
	public View.OnClickListener getOnItemClickListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startSearchActivity(v.getContext());
			}
		};
	}

	private void startSearchActivity(Context context) {
		OmnitureTracking.trackCrossSellItinToHotel(context);
		if (AndroidUtils.isTablet(context)) {
			Toast.makeText(context, "We're launching a hotel search on a tablet!", Toast.LENGTH_SHORT).show();
		}
		else {
			NavUtils.goToHotels(context, getItinCardData().getSearchParams());
		}
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
		if (convertView == null) {
			convertView = getLayoutInflater().inflate(R.layout.itin_air_attach_card, container, false);
		}
		return convertView;
	}
}
