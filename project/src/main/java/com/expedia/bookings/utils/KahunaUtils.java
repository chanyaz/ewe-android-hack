package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.os.Build;

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
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.kahuna.sdk.KahunaAnalytics;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

/**
 * Created by mohsharma on 4/10/15.
 */
public class KahunaUtils {

	public static Map<String, String> mUserAttributes = new HashMap<String, String>();
	public static final String PROD_APP_KEY = "46adc9b9151f47d888be8bbf43d99af5";
	public static final String QA_APP_KEY = "f97ca9992ee14ba596695c6bd42892f3";
	public static Context mContext;

	public static void init(ExpediaBookingApp app) {
		mContext = app;
		if (BuildConfig.DEBUG) {
			KahunaAnalytics.onAppCreate(app, QA_APP_KEY, PushNotificationUtils.SENDER_ID);
			KahunaAnalytics.setDebugMode(true);
		}
		else {
			KahunaAnalytics.onAppCreate(app, PROD_APP_KEY, PushNotificationUtils.SENDER_ID);
		}

		mUserAttributes.put("point_of_sale", getPointOfSale());

		String deviceLocale = Locale.getDefault().toString();
		mUserAttributes.put("device_locale", deviceLocale);

		String countryCode = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
		mUserAttributes.put("country_code", countryCode);

		String deviceType = ExpediaBookingApp.useTabletInterface(mContext) ? "Tablet" : "Phone";
		mUserAttributes.put("device_type", deviceType);

		String appVersion = AndroidUtils.getAppVersion(mContext);
		mUserAttributes.put("app_short_version", appVersion);

		String osVersion = Build.VERSION.RELEASE;
		mUserAttributes.put("os_version", osVersion);

		updateLoggedInStatus();

		//Set up the icons to show on push notifications
		KahunaAnalytics.setIconResourceId(R.drawable.ic_stat_expedia);

		KahunaAnalytics.enablePush();

		KahunaAnalytics.setUserAttributes(mUserAttributes);
	}

	public static void startKahunaTracking() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			KahunaAnalytics.start();
		}
	}

	public static void stopKahunaTracking() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			KahunaAnalytics.stop();
		}
	}

	public static void updatePOS() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			mUserAttributes.put("point_of_sale", getPointOfSale());
			mUserAttributes.put("country_code", PointOfSale.getPointOfSale().getTwoLetterCountryCode());

			String deviceLocale = Locale.getDefault().toString();
			mUserAttributes.put("device_locale", deviceLocale);

			KahunaAnalytics.setUserAttributes(mUserAttributes);
		}
	}

	public static void updateLoggedInStatus() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			boolean isUserLoggedIn = User.isLoggedIn(mContext);
			mUserAttributes.put("logged_in", Boolean.toString(isUserLoggedIn));

			if (isUserLoggedIn) {

				if (Db.getUser() == null) {
					Db.loadUser(mContext);
				}

				if (Db.getUser().getPrimaryTraveler() != null) {
					KahunaAnalytics.setUserCredential("username", Db.getUser().getExpediaUserId());
					mUserAttributes.put("first_name", Db.getUser().getPrimaryTraveler().getFirstName());
				}

				mUserAttributes.put("rewards_member", User.getLoggedInLoyaltyMembershipTier(mContext).toString());
				mUserAttributes.put("exp_user_id", Db.getUser().getExpediaUserId());
				mUserAttributes.put("tuid", Db.getUser().getTuidString());

			}
			KahunaAnalytics.setUserAttributes(mUserAttributes);
		}
	}

	public static void tracking(String eventName) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			KahunaAnalytics.trackEvent(eventName);
			if (eventName.equalsIgnoreCase("Login")) {
				updateLoggedInStatus();
			}
			else if (eventName.equalsIgnoreCase("Logout")) {
				trackSignOutUser();
			}
		}
	}

	public static void trackSignOutUser() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			mUserAttributes.put("logged_in", "false");
			KahunaAnalytics.setUserAttributes(mUserAttributes);
			KahunaAnalytics.logout();
		}
	}

	private static String getPointOfSale() {
		String pointOfSale = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		if (PointOfSale.getPointOfSale().getEAPID() != PointOfSale.INVALID_EAPID) {
			pointOfSale = PointOfSale.getPointOfSale().getTpid() + "-" + PointOfSale.getPointOfSale().getEAPID();
		}
		return pointOfSale;
	}

	public static void trackHotelSearch() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			String eventName = "hotel_search";
			tracking(eventName);
			Log.i("kahuna hotel search");
			HashMap<String, String> eventParams = new HashMap<String, String>();

			if (Db.getHotelSearch().getSearchResponse() != null
				&& Db.getHotelSearch().getSearchResponse().getPropertiesCount() > 0) {
				Location location = Db.getHotelSearch().getSearchResponse().getProperty(0).getLocation();
				if (location != null) {
					eventParams.put("hs_city", location.getCity());
					eventParams.put("hs_state", location.getStateCode());
					eventParams.put("hs_country", location.getCountryCode());
					eventParams.put("hs_location", location.getCity() + ", " + location.getStateCode());
				}
			}

			eventParams.put("hs_number_of_guests", String.valueOf(params.getNumTravelers()));
			eventParams.put("hs_check_in_date", DateUtils.localDateToyyyyMMdd(params.getCheckInDate()));
			eventParams.put("hs_check_out_date", DateUtils.localDateToyyyyMMdd(params.getCheckOutDate()));
			eventParams
				.put("hs_number_of_nights", String.valueOf(params.getStayDuration()));

			KahunaAnalytics.setUserAttributes(eventParams);
		}

	}

	public static void trackFlightSearch() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "flight_search";
			tracking(eventName);
			Log.i("kahuna flight search");
			FlightSearchParams params = Db.getFlightSearch().getSearchParams();
			Map<String, String> attributes = new HashMap();
			attributes.put("fs_destination", params.getArrivalLocation().getCity());
			attributes.put("fs_origin", params.getDepartureLocation().getCity());
			attributes.put("fs_departure_date", DateUtils.localDateToyyyyMMdd(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				attributes.put("fs_return_date", DateUtils.localDateToyyyyMMdd(params.getReturnDate()));
				attributes.put("fs_type", "round trip flight");
			}
			else {
				attributes.put("fs_type", "one way flight");
			}
			attributes.put("fs_number_of_passengers", String.valueOf(params.getNumTravelers()));
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackHotelInfoSite() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "hotel_info_site";
			tracking(eventName);
			Log.i("kahuna hotel info site");
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			Property property = Db.getHotelSearch().getSelectedProperty();

			Map<String, String> attributes = new HashMap();
			attributes.put("his_hotel_id", property.getPropertyId());
			attributes.put("his_hotel_friendly_name", property.getName());
			attributes.put("his_city", property.getLocation().getCity());
			attributes.put("his_state", property.getLocation().getStateCode());
			attributes.put("his_check_in_date", DateUtils.localDateToyyyyMMdd(params.getCheckInDate()));
			attributes.put("his_check_out_date", DateUtils.localDateToyyyyMMdd(params.getCheckOutDate()));
			attributes.put("his_country", property.getLocation().getCountryCode());
			attributes.put("his_number_of_guests", String.valueOf(params.getNumTravelers()));
			attributes
				.put("his_number_of_nights", String.valueOf(params.getStayDuration()));
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackHotelCheckoutStarted(HotelSearchParams params, Property property, String currency,
		double totalPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "hotel_checkout";
			tracking(eventName);

			Map<String, String> attributes = new HashMap();
			attributes.put("hc_hotel_id", property.getPropertyId());
			attributes.put("hc_hotel_friendly_name", property.getName());
			attributes.put("hc_city", property.getLocation().getCity());
			attributes.put("hc_state", property.getLocation().getStateCode());
			attributes.put("hc_country", property.getLocation().getCountryCode());
			attributes.put("hc_check_in_date", DateUtils.localDateToyyyyMMdd(params.getCheckInDate()));
			attributes.put("hc_check_out_date", DateUtils.localDateToyyyyMMdd(params.getCheckOutDate()));
			attributes.put("hc_guests", String.valueOf(params.getNumTravelers()));
			attributes.put("hc_nights", String.valueOf(params.getStayDuration()));
			attributes.put("hc_price", String.valueOf(totalPrice));
			attributes.put("hc_currency", currency);

			if (property.hasEtpOffer()) {
				attributes.put("hc_etp", String.valueOf(Db.getTripBucket().getHotel().getRate().isPayLater()));
			}
			else {
				attributes.put("hc_etp", "no etp offer");
			}
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackFlightCheckoutStarted(FlightSearch search, String currency, double totalPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "flight_checkout";
			tracking(eventName);

			Map<String, String> attributes = new HashMap();
			FlightSearchParams params = search.getSearchParams();
			attributes.put("fc_origin", params.getDepartureLocation().getCity());
			attributes.put("fc_destination", params.getArrivalLocation().getCity());
			attributes.put("fc_departure_date", DateUtils.localDateToyyyyMMdd(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				attributes.put("fc_return_date", DateUtils.localDateToyyyyMMdd(params.getReturnDate()));
				attributes.put("fc_type", "round trip flight");
			}
			else {
				attributes.put("fc_type", "one way flight");
			}
			attributes.put("fc_total_passengers", String.valueOf(params.getNumTravelers()));
			attributes.put("fc_trip_total", String.valueOf(totalPrice));
			attributes.put("fc_currency", currency);
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackHotelBooked(HotelSearchParams params, Property property, String orderNumber,
		String currency, double totalPrice, double avgPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "hotel_confirmation";
			tracking(eventName);

			Map<String, String> attributes = new HashMap();
			attributes.put("confirmed_hotel_id", property.getPropertyId());
			attributes.put("confirmed_hotel_friendly_name", property.getName());
			attributes.put("confirmed_hotel_city", property.getLocation().getCity());
			attributes.put("confirmed_hotel_state", property.getLocation().getStateCode());
			attributes.put("confirmed_hotel_country", property.getLocation().getCountryCode());
			attributes.put("confirmed_hotel_check_in_date", DateUtils.localDateToyyyyMMdd(params.getCheckInDate()));
			attributes.put("confirmed_hotel_check_out_date", DateUtils.localDateToyyyyMMdd(params.getCheckOutDate()));
			attributes.put("confirmed_hotel_guests", String.valueOf(params.getNumTravelers()));
			attributes
				.put("confirmed_hotel_nights", String.valueOf(params.getStayDuration()));
			if (property.hasEtpOffer()) {
				attributes
					.put("confirmed_hotel_etp", String.valueOf(Db.getTripBucket().getHotel().getRate().isPayLater()));
			}
			else {
				attributes.put("confirmed_hotel_etp", "no etp offer");
			}
			attributes.put("confirmed_hotel_price", String.valueOf(totalPrice));
			attributes.put("confirmed_hotel_currency", currency);
			attributes.put("confirmed_hotel_order_number", orderNumber);
			attributes.put("confirmed_hotel_avg_price", String.valueOf(avgPrice));
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackFlightBooked(FlightSearch search, String orderId, String currency, double totalPrice) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "flight_confirmation";
			tracking(eventName);

			FlightSearchParams params = search.getSearchParams();
			Map<String, String> attributes = new HashMap();
			attributes.put("confirmed_flight_origin", params.getDepartureLocation().getCity());
			attributes.put("confirmed_flight_destination", params.getArrivalLocation().getCity());
			attributes.put("confirmed_flight_departure_date", DateUtils.localDateToyyyyMMdd(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				attributes.put("confirmed_flight_return_date", DateUtils.localDateToyyyyMMdd(params.getReturnDate()));
				attributes.put("confirmed_flight_type", "round trip flight");
			}
			else {
				attributes.put("confirmed_flight_type", "one way flight");
			}
			attributes.put("confirmed_flight_total_passengers", String.valueOf(params.getNumTravelers()));
			attributes.put("confirmed_flight_total", String.valueOf(totalPrice));
			attributes.put("confirmed_flight_currency", currency);
			attributes.put("confirmed_flight_order_number", orderId);

			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackCarSearch(CarSearchParams params) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "car_search";
			tracking(eventName);

			Map<String, String> attributes = new HashMap();
			attributes.put("cs_departure_date", DateUtils.localDateToyyyyMMdd(params.startDateTime.toLocalDate()));
			attributes.put("cs_return_date", DateUtils.localDateToyyyyMMdd(params.endDateTime.toLocalDate()));
			attributes.put("cs_origin", params.originDescription);
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackCarCheckoutStarted(CreateTripCarOffer carOffer) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "car_checkout";
			tracking(eventName);

			Map<String, String> attributes = new HashMap();
			attributes.put("cc_departure_date", DateUtils.localDateToyyyyMMdd(carOffer.getPickupTime().toLocalDate()));
			attributes.put("cc_return_date", DateUtils.localDateToyyyyMMdd(carOffer.getDropOffTime().toLocalDate()));
			attributes.put("cc_origin", carOffer.pickUpLocation.locationDescription);
			attributes.put("cc_currency", carOffer.detailedFare.grandTotal.getCurrency());
			attributes.put("cc_total", carOffer.detailedFare.grandTotal.formattedPrice);
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

	public static void trackCarBooked(CarCheckoutResponse carCheckoutResponse) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			String eventName = "car_confirmation";
			tracking(eventName);

			Map<String, String> attributes = new HashMap();
			CreateTripCarOffer carOffer = carCheckoutResponse.newCarProduct;
			attributes.put("confirmed_car_return_date",
				DateUtils.localDateToyyyyMMdd(carOffer.getDropOffTime().toLocalDate()));
			attributes.put("confirmed_car_departure_date",
				DateUtils.localDateToyyyyMMdd(carOffer.getPickupTime().toLocalDate()));
			attributes.put("confirmed_car_origin", carOffer.pickUpLocation.locationDescription);
			attributes.put("confirmed_car_currency", carOffer.detailedFare.grandTotal.getCurrency());
			attributes.put("confirmed_car_total", carOffer.detailedFare.grandTotal.formattedPrice);
			attributes.put("confirmed_car_order_number", carCheckoutResponse.orderId);
			KahunaAnalytics.setUserAttributes(attributes);
		}
	}

}
