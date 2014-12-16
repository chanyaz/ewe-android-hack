package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletLaunchActivity;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.trips.ItinCardDataAirAttach;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AirAttachUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.BackgroundDownloader;
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
				AirAttachUtils.launchTabletResultsFromItinCrossSell(v.getContext(), getItinCardData().getSearchParams(),
					getItinCardData().getFlightLeg().getLastWaypoint().mAirportCode);
				OmnitureTracking.trackAirAttachItinCrossSell(v.getContext());
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
