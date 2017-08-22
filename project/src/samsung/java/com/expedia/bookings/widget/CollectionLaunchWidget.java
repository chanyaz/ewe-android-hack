package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXNavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.navigation.FlightNavUtils;
import com.expedia.bookings.utils.navigation.HotelNavUtils;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import org.joda.time.LocalDate;

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
	public void hotelClicked() {
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
		HotelNavUtils.goToHotels(getContext(), params, animOptions, 0);
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
		FlightNavUtils.goToFlights(getContext());
	}

	@OnClick(R.id.button_search_activities)
	void activitiesClicked() {
		LxSearchParams params = (LxSearchParams) new LxSearchParams.Builder().location(suggestion.shortName)
			.startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(14)).build();

		// Go to Lx
		LXNavUtils.goToActivities(getContext(), animOptions, params, NavUtils.FLAG_OPEN_SEARCH);
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

	public void updateWidget(CollectionLocation collectionLocation, String collectionUrl) {
		collectionDescription.setText(collectionLocation.description);

		// Set LOB buttons visibility depending on the POS support.
		searchFlights.setVisibility(PointOfSale.getPointOfSale().supports(LineOfBusiness.FLIGHTS) ? View.VISIBLE : View.GONE);
		searchActivities.setVisibility(PointOfSale.getPointOfSale().supports(LineOfBusiness.LX) ? View.VISIBLE : View.GONE);

		HeaderBitmapDrawable drawable = Images
			.makeCollectionBitmapDrawable(getContext(), null, collectionUrl, "Collection_Details");

		LayerDrawable layerDraw = new LayerDrawable(new Drawable[] {
			drawable, getResources().getDrawable(R.drawable.collection_screen_gradient_overlay)
		});

		Ui.setViewBackground(this, layerDraw);
	}
}
