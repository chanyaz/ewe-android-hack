package com.expedia.bookings.utils;

import java.util.List;

import android.content.Context;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.util.AndroidUtils;

public class AirAttachUtils {

	public static HotelSearchParams generateHotelSearchParamsFromItinData(TripFlight tripFlight,
																		  FlightLeg firstLeg, FlightLeg secondLeg) {
		List<ChildTraveler> childTravelersInTrip = tripFlight.getChildTravelers();
		int numAdults = tripFlight.getTravelers().size() - childTravelersInTrip.size();
		return HotelSearchParams.fromFlightParams(firstLeg, secondLeg, numAdults, childTravelersInTrip);
	}

	public static void launchTabletResultsFromItinCrossSell(final Context context,
		final HotelSearchParams hotelSearchParams) {
		if (AndroidUtils.isTablet(context)) {
			final SearchParams searchParams = SearchParams.fromHotelSearchParams(hotelSearchParams);
			BackgroundDownloader.getInstance().startDownload("itinCrossSellSuggest", new BackgroundDownloader.Download<SuggestionResponse>() {
				@Override
				public SuggestionResponse doDownload() {
					ExpediaServices services = new ExpediaServices(context);
					return services.suggestionsCityNearby(searchParams.getDestination().getLocation().getLatitude(),
						searchParams.getDestination().getLocation().getLongitude());
				}
			}, new BackgroundDownloader.OnDownloadComplete<SuggestionResponse>() {
				@Override
				public void onDownload(SuggestionResponse results) {
					if (results != null && results.getSuggestions().size() > 0) {
						searchParams.setDestination(results.getSuggestions().get(0));
						NavUtils.goToTabletResults(context, searchParams, LineOfBusiness.HOTELS);
					}
				}
			});
		}
		else {
			NavUtils.goToHotels(context, hotelSearchParams);
		}
	}

}
