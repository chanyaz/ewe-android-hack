package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.LaunchListAdapter;
import com.expedia.bookings.widget.LaunchListWidget;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class LuanchScreenTest {
	private CollectionLocation collectionLocation = new CollectionLocation();
	private Hotel hotel = new Hotel();
	@Before
	public void before() {
		hotel.name = "Hotel";
		HotelRate rate = new HotelRate();
		rate.maxNightlyRate = 1;
		rate.averageRate = 1;
		rate.surchargeTotal = 1;
		rate.surchargeTotalForEntireStay = 1;
		rate.averageBaseRate = 1;
		rate.nightlyRateTotal = 1;
		rate.discountPercent = 1;
		rate.total = 1;
		rate.currencyCode = "USD";
		rate.currencySymbol = "USD";
		rate.discountMessage = "";
		rate.priceToShowUsers = 1;
		rate.strikethroughPriceToShowUsers = 1;
		rate.strikethroughPriceWithTaxesAndFeesToShowUsers = 1;
		rate.totalMandatoryFees = 1;
		rate.totalPriceMandatoryFees = 1;
		rate.formattedTotalPriceMandatoryFees = "1";
		rate.userPriceType = "";
		rate.checkoutPriceType = "";
		rate.roomTypeCode = "";
		rate.ratePlanCode = "";

		hotel.lowRateInfo = rate;
		hotel.largeThumbnailUrl = "";
		hotel.hotelGuestRating = 5f;
		collectionLocation.id = "1";
		collectionLocation.title = "San Francisco";
		collectionLocation.subtitle = "California";
		collectionLocation.imageCode = "image";
		collectionLocation.description = "Place";
	}

	@Test
	public void testListDisplaysCollection() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		View v = LayoutInflater.from(activity).inflate(R.layout.launch_screen_test, null);
		LaunchListWidget launchListWidget = (LaunchListWidget) v.findViewById(R.id.launch_list_widget);
		launchListWidget.setHeaderPaddingTop(10);
		launchListWidget.showListLoadingAnimation();

		Collection collection = new Collection();
		collection.title = "Title";
		collection.locations = new ArrayList<>();
		collection.locations.add(collectionLocation);
		collection.locations.add(collectionLocation);
		collection.locations.add(collectionLocation);
		Events.CollectionDownloadComplete event = new Events.CollectionDownloadComplete(collection);
		launchListWidget.onCollectionDownloadComplete(event);
		launchListWidget.measure(0, 0);
		launchListWidget.layout(0, 0, 100, 10000);

		assertEquals(LaunchListAdapter.HeaderViewHolder.class, launchListWidget.findViewHolderForPosition(0).getClass());
		assertEquals(LaunchListAdapter.CollectionViewHolder.class, launchListWidget.findViewHolderForPosition(1).getClass());

	}

	@Test
	public void testListDisplaysHotels() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		View v = LayoutInflater.from(activity).inflate(R.layout.launch_screen_test, null);
		LaunchListWidget launchListWidget = (LaunchListWidget) v.findViewById(R.id.launch_list_widget);
		launchListWidget.setHeaderPaddingTop(10);
		launchListWidget.showListLoadingAnimation();

		List<Hotel> hotels = new ArrayList<>();
		hotels.add(hotel);
		hotels.add(hotel);
		hotels.add(hotel);
		hotels.add(hotel);
		Events.LaunchHotelSearchResponse event = new Events.LaunchHotelSearchResponse(hotels);
		launchListWidget.onNearbyHotelsSearchResults(event);
		launchListWidget.measure(0, 0);
		launchListWidget.layout(0, 0, 100, 10000);

		assertEquals(LaunchListAdapter.HeaderViewHolder.class, launchListWidget.findViewHolderForPosition(0).getClass());
		assertEquals(LaunchListAdapter.HotelViewHolder.class, launchListWidget.findViewHolderForPosition(1).getClass());

	}
}
