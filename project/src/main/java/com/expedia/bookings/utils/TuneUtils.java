package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.mobileapptracker.MATEvent;
import com.mobileapptracker.MATEventItem;
import com.mobileapptracker.MobileAppTracker;

public class TuneUtils {

	public static MobileAppTracker mobileAppTracker = null;
	private static final int DEEEPLINK_TIMEOUT = 5000;
	private static boolean initialized = false;
	public static Context context;

	public static void init(ExpediaBookingApp app) {
		initialized = true;
		context = app.getApplicationContext();

		String advertiserID = app.getString(R.string.tune_sdk_app_advertiser_id);
		String conversionKey = app.getString(R.string.tune_sdk_app_conversion_key);

		mobileAppTracker = MobileAppTracker.init(app, advertiserID, conversionKey);

		mobileAppTracker.setDebugMode(BuildConfig.DEBUG);
		mobileAppTracker.setDeferredDeeplink(Boolean.TRUE, DEEEPLINK_TIMEOUT);
		mobileAppTracker.setAllowDuplicates(BuildConfig.DEBUG);

		MATEvent launchEvent = new MATEvent("launch")
			.withAttribute1(getTuid())
			.withAttribute3(getMembershipTier())
			.withAttribute2(Boolean.toString(User.isLoggedIn(context)));
		trackEvent(launchEvent);
	}

	public static void startTune(Activity activity) {
		if (initialized) {
			// Get source of open for app re-engagement
			mobileAppTracker.setReferralSources(activity);
			// MAT will not function unless the measureSession call is included
			mobileAppTracker.measureSession();

		}
	}

	public static void trackHomePageView() {
		if (initialized) {
			MATEvent event = new MATEvent("home_view");

			withTuidAndMembership(event)
					.withAttribute2(Boolean.toString(User.isLoggedIn(context)))
				 	.withAttribute4("Mobile:US:OLA:Criteo"); // TODO: Need to verify this String
			trackEvent(event);
		}
	}

	public static void trackHotelInfoSite() {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_infosite");

			withTuidAndMembership(event)
				.withAttribute2(Boolean.toString(User.isLoggedIn(context)))
				.withAttribute4("Mobile:US:OLA:Criteo");
			trackEvent(event);
		}
	}

	public static void trackHotelRateDetails(Property selectedProperty) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_rate_details");
			MATEventItem eventItem = new MATEventItem("hotel_rate_details_item");
			eventItem.withAttribute1(selectedProperty.getLocation().getCity());

			Date checkInDate = getHotelSearchParams().getCheckInDate().toDate();
			Date checkOutDate = getHotelSearchParams().getCheckOutDate().toDate();

			withTuidAndMembership(event)
				.withAttribute2(Boolean.toString(User.isLoggedIn(context)))
				.withAttribute4("ola.us.display.criteo.appremarketing.hotel")
				.withContentType(selectedProperty.getName())
				.withContentId(selectedProperty.getPropertyId())
				.withDate1(checkInDate)
				.withDate2(checkOutDate);

			trackEvent(event);
		}
	}

	public static void trackHotelSearchResults() {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_search_results");
			MATEventItem eventItem = new MATEventItem("hotel_rate_details_item");

			Date checkInDate = getHotelSearchParams().getCheckInDate().toDate();
			Date checkOutDate = getHotelSearchParams().getCheckOutDate().toDate();

			eventItem.withAttribute1(getHotelSearchParams().getCorrespondingAirportCode());
			StringBuilder topFiveHotelIdsBuilder = new StringBuilder();
			StringBuilder sb = new StringBuilder();
			int propertiesCount = Db.getHotelSearch().getSearchResponse().getPropertiesCount();
			if (Db.getHotelSearch().getSearchResponse() != null && propertiesCount >= 0) {
				for (int i = 0; (i < 5 && i < propertiesCount); i++) {
					Property property = Db.getHotelSearch().getSearchResponse().getProperty(i);
					topFiveHotelIdsBuilder.append(property.getPropertyId());
					String hotelId = property.getPropertyId();
					String hotelName = property.getName();
					String price = property.getLowestRate().getDisplayPrice().formattedPrice;
					String currency = property.getLowestRate().getDisplayBasePrice().getCurrency();
					String starRating = Double.toString(property.getHotelRating());
					String miles = Double.toString(property.getDistanceFromUser().getDistance());

					sb.append(
						String.format("%s|%s|%s|%s|%s|%s", hotelId, hotelName, price, currency,
							starRating, miles));
					if (i != 4) {
						sb.append(":");
						topFiveHotelIdsBuilder.append(",");
					}
				}
			}
			eventItem.withAttribute4(topFiveHotelIdsBuilder.toString());
			eventItem.withAttribute5(sb.toString());

			withTuidAndMembership(event)
				.withAttribute2(Boolean.toString(User.isLoggedIn(context)))
				.withAttribute4("ola.us.display.criteo.appretargetting.hotel")
				.withDate1(checkInDate)
				.withDate2(checkOutDate)
				.withEventItems(Arrays.asList(eventItem))
				.withSearchString("Hotel")
				.withLevel(1);

			trackEvent(event);
		}
	}

	public static void trackHotelConfirmation(double revenue, String transactionId, int numberRooms, TripBucketItemHotel hotel) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_confirmation");
			MATEventItem eventItem = new MATEventItem("hotel_confirmation_item");

			eventItem.withQuantity(hotel.getHotelSearchParams().getStayDuration())
				.withAttribute1(hotel.getProperty().getLocation().getCity())
				.withRevenue(revenue);

			Date checkInDate = hotel.getHotelSearchParams().getCheckInDate().toDate();
			Date checkOutDate = hotel.getHotelSearchParams().getCheckOutDate().toDate();

			withTuidAndMembership(event)
				.withAttribute2(Boolean.toString(User.isLoggedIn(context)))
				.withAttribute4("ola.us.display.criteo.appretargetting.hotel")
				.withRevenue(revenue)
				.withAdvertiserRefId(transactionId)
				.withQuantity(numberRooms)
				.withContentType(hotel.getProperty().getName())
				.withContentId(hotel.getProperty().getPropertyId())
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(checkInDate)
				.withDate2(checkOutDate);

			trackEvent(event);
		}
	}

	private static void trackEvent(MATEvent eventName) {
		if (initialized) {
			mobileAppTracker.measureEvent(eventName);
		}
	}

	public static void trackLogin() {
		if (initialized) {
			MATEvent loginEvent = new MATEvent("login");
			loginEvent.withAttribute1(getTuid());
			loginEvent.withAttribute2(getMembershipTier());
			trackEvent(loginEvent);
		}
	}

	//////////
	// Helpers

	private static String getMembershipTier() {
		if (User.isLoggedIn(context)) {
			lazyLoadUser();
			return User.getLoggedInLoyaltyMembershipTier(context).toString();
		}
		return "";
	}

	private static String getTuid() {
		if (User.isLoggedIn(context)) {
			lazyLoadUser();
			return Db.getUser().getTuidString();
		}
		return "";
	}

	private static void lazyLoadUser() {
		if (Db.getUser() == null && User.isLoggedIn(context)) {
			Db.loadUser(context);
		}
	}

	private static HotelSearchParams getHotelSearchParams() {
		return Db.getHotelSearch().getSearchParams();
	}

	private static MATEvent withTuidAndMembership(MATEvent event) {
		return event.withAttribute1(getTuid())
			.withAttribute3(getMembershipTier());
	}
}
