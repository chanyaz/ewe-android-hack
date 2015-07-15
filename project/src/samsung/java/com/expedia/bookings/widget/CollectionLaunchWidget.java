package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.squareup.otto.Subscribe;

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
	private Suggestion location;
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
		params.setQuery(location.shortName);
		params.setSearchType(HotelSearchParams.SearchType.valueOf(location.type));
		params.setRegionId(location.id);
		params.setSearchLatLon(location.latLong.lat, location.latLong.lng);
		LocalDate now = LocalDate.now();
		params.setCheckInDate(now.plusDays(1));
		params.setCheckOutDate(now.plusDays(2));
		params.setNumAdults(2);
		params.setChildren(null);
		NavUtils.goToHotels(getContext(), params, animOptions, 0);
	}

	@OnClick(R.id.button_search_flights)
	void flightClicked() {

	}

	@OnClick(R.id.button_search_activities)
	void activitiesClicked() {
		LXSearchParams params = new LXSearchParams();
		params.location(location.shortName);
		LocalDate now = LocalDate.now();
		params.startDate(now.plusDays(1));
		NavUtils.goToLx(getContext(), animOptions, params, false);
	}

	// Hotel search in collection location
	@Subscribe
	public void onCollectionLocationSelected(Events.LaunchCollectionItemSelected event) {
		location = event.collectionLocation.location;
		animOptions = event.animOptions;
	}

}
