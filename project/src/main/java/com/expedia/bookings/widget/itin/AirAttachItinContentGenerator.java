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
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.util.AndroidUtils;

public class AirAttachItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataAirAttach> {

	private SearchParams mSearchParams;

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
				final Context context = v.getContext();
				OmnitureTracking.trackCrossSellItinToHotel(context);
				final String destAirportCode = getItinCardData().getFlightLeg().getLastWaypoint().mAirportCode;
				if (AndroidUtils.isTablet(context)) {
					mSearchParams = SearchParams.fromHotelSearchParams(getItinCardData().getSearchParams());
					BackgroundDownloader.getInstance().startDownload("blah blah", new BackgroundDownloader.Download<SuggestionResponse>() {
						@Override
						public SuggestionResponse doDownload() {
							ExpediaServices services = new ExpediaServices(context);
							return services.suggestions(destAirportCode, 0);
						}
					}, new BackgroundDownloader.OnDownloadComplete<SuggestionResponse>() {
						@Override
						public void onDownload(SuggestionResponse results) {
							if (results != null && results.getSuggestions().size() > 0) {
								mSearchParams.setDestination(results.getSuggestions().get(0));
								NavUtils.goToTabletResults(context, mSearchParams, LineOfBusiness.HOTELS);
							}
							else {
								// Do nothing
							}
						}
					});
				}
				else {
					NavUtils.goToHotels(context, getItinCardData().getSearchParams());
				}
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
