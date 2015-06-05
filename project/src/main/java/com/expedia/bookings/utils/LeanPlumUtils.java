package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.leanplum.Leanplum;
import com.leanplum.LeanplumActivityHelper;
import com.leanplum.LeanplumPushNotificationCustomizer;
import com.leanplum.LeanplumPushService;
import com.leanplum.annotations.Parser;
import com.leanplum.callbacks.VariablesChangedCallback;
import com.mobiata.android.Log;

public class LeanPlumUtils {
	public static Map<String, Object> mUserAtrributes = new HashMap<String, Object>();
	public static Context mContext;
	public static final String CAMPAIGN_TEXT_KEY = "campaignText";
	public static final String DEFAULT_CAMPAIGN_TEXT = "leanplum.notification";

	public static void init(ExpediaBookingApp app) {
		mContext = app.getApplicationContext();
		if (BuildConfig.DEBUG) {
			String appId = mContext.getString(R.string.lean_plum_sdk_dev_appid);
			String key = mContext.getString(R.string.lean_plum_sdk_dev_key);
			Leanplum.setAppIdForDevelopmentMode(appId, key);
		}
		else {
			String appId = mContext.getString(R.string.lean_plum_sdk_prod_appid);
			String key = mContext.getString(R.string.lean_plum_sdk_prod_key);
			Leanplum.setAppIdForProductionMode(appId, key);
		}
		String localeIdentifier = PointOfSale.getPointOfSale().getLocaleIdentifier();
		mUserAtrributes.put("PosLocale", localeIdentifier);

		String deviceLocale = Locale.getDefault().toString();
		mUserAtrributes.put("DeviceLocale", deviceLocale);

		String countryCode = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
		mUserAtrributes.put("CountryCode", countryCode);

		String deviceType = ExpediaBookingApp.useTabletInterface(mContext) ? "Tablet" : "Phone";
		mUserAtrributes.put("DeviceType", deviceType);

		LeanplumPushService.setGcmSenderId(PushNotificationUtils.SENDER_ID);
		LeanplumPushService.setCustomizer(new LeanplumPushNotificationCustomizer() {
			@Override
			public void customize(NotificationCompat.Builder builder, Bundle bundle) {
				String campaignText = bundle.getString(CAMPAIGN_TEXT_KEY, DEFAULT_CAMPAIGN_TEXT);
				OmnitureTracking.trackLeanPlumNotification(mContext, campaignText);
				builder.setSmallIcon(R.drawable.ic_stat_expedia);
			}
		});

		Leanplum.setApplicationContext(mContext);
		LeanplumActivityHelper.enableLifecycleCallbacks(app);
		Leanplum.start(mContext, mUserAtrributes);
		updateLoggedInStatus();
		Parser.parseVariablesForClasses(LeanPlumFlags.class);
		Leanplum.addVariablesChangedHandler(flightShareCallback);

	}


	public static void updatePOS() {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			PointOfSale pos = PointOfSale.getPointOfSale();
			mUserAtrributes.put("PosLocale", pos.getLocaleIdentifier());
			mUserAtrributes.put("CountryCode", pos.getTwoLetterCountryCode());

			String deviceLocale = Locale.getDefault().toString();
			mUserAtrributes.put("DeviceLocale", deviceLocale);

			boolean isUserAirAttachQualified = Db.getTripBucket() != null &&
				Db.getTripBucket().isUserAirAttachQualified();
			updateAirAttachState(isUserAirAttachQualified);
		}
	}

	public static void updateAirAttachState(boolean userIsAttachEligible) {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			// Air attach state
			mUserAtrributes.put("airattach_eligible", userIsAttachEligible);
			Leanplum.setUserAttributes(mUserAtrributes);
		}
	}

	public static void updateLoggedInStatus() {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			boolean isUserLoggedIn = User.isLoggedIn(mContext);
			mUserAtrributes.put("isUserLoggedIn", isUserLoggedIn);
			if (isUserLoggedIn) {
				if (Db.getUser() == null) {
					Db.loadUser(mContext);
				}
				if (Db.getUser().getPrimaryTraveler() != null) {
					mUserAtrributes.put("first_name", Db.getUser().getPrimaryTraveler().getFirstName());
				}
				mUserAtrributes.put("membershipTier", User.getLoggedInLoyaltyMembershipTier(mContext).toString());
			}
			boolean isUserAirAttachQualified = Db.getTripBucket() != null &&
				Db.getTripBucket().isUserAirAttachQualified();
			updateAirAttachState(isUserAirAttachQualified);
		}
	}

	public static void tracking(String eventName) {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			Leanplum.track(eventName);
			if (eventName.equalsIgnoreCase("Login")) {
				updateLoggedInStatus();
			}
		}
	}

	private static void tracking(String eventName, HashMap eventParams) {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			Leanplum.track(eventName, eventParams);
		}
	}

	public static void trackHotelBooked(HotelSearchParams params, Property property, String orderNumber, String currency, double totalPrice, double avgPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			String eventName = "Sale Hotel";
			Log.i("LeanPlum hotel booking event currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			Location location = property.getLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
				eventParams.put("Destination", location.getCity());
			}
			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(params.getCheckInDate()));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(params.getCheckOutDate()));
			eventParams.put("b_win", "" + getBookingWindow(params.getCheckInDate()));
			eventParams.put("p_type", "HOTEL");
			eventParams.put("PropertyId", property.getPropertyId());
			eventParams.put("AveragePrice", "" + avgPrice);
			eventParams.put("StayDuration", "" + params.getStayDuration());
			eventParams.put("currency", currency);
			eventParams.put("OrderNumber", orderNumber);
			eventParams.put("TotalPrice", String.valueOf(totalPrice));
			tracking(eventName, eventParams);
		}
	}

	public static void trackFlightBooked(FlightSearch search, String orderId, String currency, double totalPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			FlightSearchParams params = search.getSearchParams();
			String eventName = "Sale Flight";
			Log.i("LeanPlum flight booking event currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
			}
			eventParams.put("DepartureId", params.getDepartureLocation().getDestinationId());
			eventParams.put("ArrivalId", params.getArrivalLocation().getDestinationId());
			eventParams.put("DepartureDate", DateUtils.convertDatetoInt(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				eventParams.put("ReturnDate", DateUtils.convertDatetoInt(params.getReturnDate()));
			}
			eventParams.put("b_win", "" + getBookingWindow(params.getDepartureDate()));
			eventParams.put("p_type", "FLIGHT");
			int numberOfTravelers = params.getNumAdults();
			String productId =
				params.getDepartureLocation().getDestinationId() + "/" + params.getArrivalLocation()
					.getDestinationId();
			eventParams.put("PropertyId", productId);
			eventParams.put("AveragePrice", "" + totalPrice / numberOfTravelers);
			eventParams.put("currency", currency);
			eventParams.put("OrderNumber", orderId);
			eventParams.put("TotalPrice", String.valueOf(totalPrice));
			tracking(eventName, eventParams);

		}
	}

	public static void trackHotelCheckoutStarted(HotelSearchParams params, Property property, String currency,
		double totalPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			String eventName = "Checkout Hotel Started";
			Log.i("LeanPlum hotel checkout started currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			Location location = property.getLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
				eventParams.put("Destination", location.getCity());
			}
			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(params.getCheckInDate()));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(params.getCheckOutDate()));
			eventParams.put("b_win", "" + getBookingWindow(params.getCheckInDate()));
			eventParams.put("p_type", "HOTEL");
			eventParams.put("PropertyId", property.getPropertyId());
			eventParams.put("currency", currency);
			eventParams.put("TotalPrice", totalPrice);
			tracking(eventName, eventParams);
		}
	}

	public static void trackFlightCheckoutStarted(FlightSearch search, String currency, double totalPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {

			String eventName = "Checkout Flight Started";
			Log.i("LeanPlum flight checkout started currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			FlightSearchParams params = search.getSearchParams();

			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
			}
			eventParams.put("DepartureId", params.getDepartureLocation().getDestinationId());
			eventParams.put("ArrivalId", params.getArrivalLocation().getDestinationId());

			eventParams.put("DepartureDate", DateUtils.convertDatetoInt(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				eventParams.put("ReturnDate", DateUtils.convertDatetoInt(params.getReturnDate()));
			}
			eventParams.put("b_win", "" + getBookingWindow(params.getDepartureDate()));
			eventParams.put("p_type", "FLIGHT");
			String productId =
				params.getDepartureLocation().getDestinationId() + "/" + params.getArrivalLocation()
					.getDestinationId();
			eventParams.put("PropertyId", productId);
			eventParams.put("currency", currency);
			eventParams.put("TotalPrice", totalPrice);
			tracking(eventName, eventParams);

		}
	}

	public static void trackHotelSearch() {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			String eventName = "Search Hotel";
			Log.i("LeanPlum hotel search");
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			if (Db.getHotelSearch().getSearchResponse() != null
				&& Db.getHotelSearch().getSearchResponse().getPropertiesCount() > 0) {
				Location location = Db.getHotelSearch().getSearchResponse().getProperty(0).getLocation();
				if (location != null) {
					addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
						location.getCountryCode());
					eventParams.put("Destination", location.getCity());
				}
			}
			if (!TextUtils.isEmpty(params.getRegionId())) {
				eventParams.put("RegionId", params.getRegionId());
			}
			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(params.getCheckInDate()));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(params.getCheckOutDate()));
			eventParams.put("b_win", "" + getBookingWindow(params.getCheckInDate()));
			eventParams.put("p_type", "HOTEL");
			tracking(eventName, eventParams);
		}

	}

	public static void trackFlightSearch() {
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			FlightSearchParams params = Db.getFlightSearch().getSearchParams();
			String destinationAirport = params.getArrivalLocation().getDestinationId();
			String eventName = "Search Flight";
			Log.i("LeanPlum flight search destination=" + destinationAirport);

			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
			}
			eventParams.put("Destination", destinationAirport);
			eventParams.put("DepartureId", params.getDepartureLocation().getDestinationId());
			eventParams.put("ArrivalId", params.getArrivalLocation().getDestinationId());

			eventParams.put("DepartureDate", DateUtils.convertDatetoInt(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				eventParams.put("ReturnDate", DateUtils.convertDatetoInt(params.getReturnDate()));
			}
			eventParams.put("b_win", "" + getBookingWindow(params.getDepartureDate()));
			eventParams.put("p_type", "FLIGHT");

			tracking(eventName, eventParams);

		}
	}

	private static int getBookingWindow(LocalDate time) {
		return JodaUtils.daysBetween(LocalDate.now(), time);
	}

	private static HashMap addCommonProductRetargeting(HashMap eventParams, String city,
		String state, String country) {
		// common except home page view/ itin view
		if (!TextUtils.isEmpty(city)) {
			eventParams.put("fb_city", city);
		}
		if (!TextUtils.isEmpty(state)) {
			eventParams.put("fb_state", state);
		}
		if (!TextUtils.isEmpty(country)) {
			eventParams.put("fb_country", country);
		}
		return eventParams;
	}

	public static VariablesChangedCallback flightShareCallback = new VariablesChangedCallback() {
		@Override
		public void variablesChanged() {
			Log.i("Show Share flight Notification " + LeanPlumFlags.mShowShareFlightNotification);
		}
	};

}
