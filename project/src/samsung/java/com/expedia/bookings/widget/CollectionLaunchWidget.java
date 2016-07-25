package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CollectionLaunchWidget extends LinearLayout {

	@InjectView(R.id.collection_description)
	TextView collectionDescription;
	@InjectView(R.id.button_search_hotels)
	Button searchHotels;
	@InjectView(R.id.button_search_flights)
	Button searchFlights;
	@InjectView(R.id.button_search_activities)
	Button searchActivities;
	private CollectionLocation.Location suggestion;
	private Bundle animOptions;

	public CollectionLaunchWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		Events.register(this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		FontCache.setTypeface(collectionDescription, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(searchHotels, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(searchFlights, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(searchActivities, FontCache.Font.ROBOTO_LIGHT);
	}

	@OnClick(R.id.button_search_hotels)
	void hotelClicked() {
		HotelSearchParams params = new HotelSearchParams();

		params.setQuery(suggestion.shortName);
		params.setSearchType(HotelSearchParams.SearchType.valueOf(suggestion.type));
		params.setRegionId(suggestion.id);
		params.setSearchLatLon(suggestion.latLong.lat, suggestion.latLong.lng);

		LocalDate now = LocalDate.now();
		params.setCheckInDate(now.plusDays(1));
		params.setCheckOutDate(now.plusDays(2));

		params.setNumAdults(2);
		params.setChildren(null);

		// Go to hotels
		NavUtils.goToHotels(getContext(), params, animOptions, 0);
	}

	@OnClick(R.id.button_search_flights)
	void flightClicked() {
		FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
		Location departureLocation = flightSearchParams.getDepartureLocation();
		flightSearchParams.reset();

		//Set Departure location to the previous one.
		flightSearchParams.setDepartureLocation(departureLocation);

		flightSearchParams.setArrivalLocation(getDestinationLocation());
		flightSearchParams.setDepartureDate(LocalDate.now().plusDays(1));

		// Go to flights
		NavUtils.goToFlights(getContext(), true);
	}

	@OnClick(R.id.button_search_activities)
	void activitiesClicked() {
		LxSearchParams params = (LxSearchParams) new LxSearchParams.Builder().location(suggestion.shortName)
			.startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(14)).build();

		// Go to Lx
		NavUtils.goToActivities(getContext(), animOptions, params, NavUtils.FLAG_OPEN_SEARCH);
	}

	// Hotel search in collection location
	@Subscribe
	public void onCollectionLocationSelected(Events.LaunchCollectionItemSelected event) {
		suggestion = event.collectionLocation.location;
		animOptions = event.animOptions;
	}

	private Location getDestinationLocation() {
		Location loc = new Location();
		Airport airport = FlightStatsDbUtils.getAirport(suggestion.airportCode);
		String destinationId = suggestion.airportCode;
		if (airport != null && airport.mName != null) {
			destinationId = Phrase.from(getContext().getResources().getString(R.string.airport_code_name_TEMPLATE))
				.put("code", airport.mAirportCode)
				.put("name", airport.mName)
				.format().toString();
		}
		loc.setDestinationId(destinationId);
		return loc;
	}
}
