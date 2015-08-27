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

@RunWith(RobolectricRunner.class)
public class LaunchScreenTest {
	private CollectionLocation collectionLocation = new CollectionLocation();
	private Hotel hotel = new Hotel();
	private Hotel hotelNoRating = new Hotel();
	@Before
	public void before() {
		HotelRate rate = new HotelRate();
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
		rate.totalMandatoryFees = 1;
		rate.totalPriceWithMandatoryFees = 1;
		rate.userPriceType = "";
		rate.checkoutPriceType = "";
		rate.roomTypeCode = "";
		rate.ratePlanCode = "";

		hotel.localizedName = "Hotel";
		hotel.lowRateInfo = rate;
		hotel.largeThumbnailUrl = "";
		hotel.hotelGuestRating = 5f;

		hotelNoRating.localizedName = "Hotel No Rating";
		hotelNoRating.lowRateInfo = rate;
		hotelNoRating.largeThumbnailUrl = "";
		hotelNoRating.hotelGuestRating = 0f;

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

		assertEquals(LaunchListAdapter.HeaderViewHolder.class, launchListWidget.findViewHolderForAdapterPosition(0).getClass());
		assertEquals(LaunchListAdapter.CollectionViewHolder.class, launchListWidget.findViewHolderForAdapterPosition(1).getClass());

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

		assertEquals(LaunchListAdapter.HeaderViewHolder.class, launchListWidget.findViewHolderForAdapterPosition(0).getClass());
		assertEquals(LaunchListAdapter.HotelViewHolder.class, launchListWidget.findViewHolderForAdapterPosition(1).getClass());

	}

	@Test
	public void testZeroRating() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		View v = LayoutInflater.from(activity).inflate(R.layout.launch_screen_test, null);
		LaunchListWidget launchListWidget = (LaunchListWidget) v.findViewById(R.id.launch_list_widget);
		launchListWidget.setHeaderPaddingTop(10);
		launchListWidget.showListLoadingAnimation();

		List<Hotel> hotels = new ArrayList<>();
		hotels.add(hotel);
		hotels.add(hotelNoRating);
		hotels.add(hotelNoRating);
		hotels.add(hotelNoRating);
		hotels.add(hotelNoRating);
		hotels.add(hotelNoRating);
		Events.LaunchHotelSearchResponse event = new Events.LaunchHotelSearchResponse(hotels);
		launchListWidget.onNearbyHotelsSearchResults(event);
		launchListWidget.measure(0, 0);
		launchListWidget.layout(0, 0, 100, 10000);


		LaunchListAdapter.HotelViewHolder h1 = (LaunchListAdapter.HotelViewHolder) launchListWidget.findViewHolderForAdapterPosition(1);
		LaunchListAdapter.HotelViewHolder h2 = (LaunchListAdapter.HotelViewHolder) launchListWidget.findViewHolderForAdapterPosition(2);
		LaunchListAdapter.HotelViewHolder h3 = (LaunchListAdapter.HotelViewHolder) launchListWidget.findViewHolderForAdapterPosition(3);
		LaunchListAdapter.HotelViewHolder h4 = (LaunchListAdapter.HotelViewHolder) launchListWidget.findViewHolderForAdapterPosition(4);
		LaunchListAdapter.HotelViewHolder h5 = (LaunchListAdapter.HotelViewHolder) launchListWidget.findViewHolderForAdapterPosition(5);
		LaunchListAdapter.HotelViewHolder h6 = (LaunchListAdapter.HotelViewHolder) launchListWidget.findViewHolderForAdapterPosition(6);

		assertEquals("5.0", h1.rating.getText());
		assertEquals(View.VISIBLE, h1.ratingText.getVisibility());

		assertEquals(View.INVISIBLE, h2.ratingInfo.getVisibility());
		assertEquals(View.GONE, h2.ratingText.getVisibility());

		assertEquals(View.INVISIBLE, h3.ratingInfo.getVisibility());
		assertEquals(View.GONE, h3.ratingText.getVisibility());

		assertEquals(View.INVISIBLE, h4.ratingInfo.getVisibility());
		assertEquals(View.GONE, h4.ratingText.getVisibility());

		assertEquals(View.INVISIBLE, h5.ratingInfo.getVisibility());
		assertEquals(View.GONE, h5.ratingText.getVisibility());

		assertEquals("Not Rated", h6.noRatingText.getText());
		assertEquals(View.VISIBLE, h6.noRatingText.getVisibility());
		assertEquals(View.GONE, h6.ratingInfo.getVisibility());
	}
}
