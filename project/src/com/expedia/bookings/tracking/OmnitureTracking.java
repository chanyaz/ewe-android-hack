package com.expedia.bookings.tracking;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import com.expedia.bookings.data.*;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.omniture.AppMeasurement;

import java.util.Calendar;

/**
 * The spec behind this class can be found here: http://confluence/display/Omniture/Mobile+App+Flight+Tracking
 */

public class OmnitureTracking {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// STANDARD PAGE NAME CONSTANTS

	private static final String LAUNCH_SCREEN = "App.LaunchScreen";
	private static final String FLIGHT_SEARCH = "App.Flight.Search";
	private static final String FLIGHT_SEARCH_INTERSTITIAL = "App.Flight.Search.Interstitial";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT = "App.Flight.Search.Roundtrip.Out";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS = "App.Flight.Search.Roundtrip.Out.Details";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN = "App.Flight.Search.Roundtrip.In";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS = "App.Flight.Search.Roundtrip.In.Details";
	private static final String FLIGHT_RATE_DETAILS = "App.Flight.RateDetails";
	private static final String FLIGHT_CHECKOUT_INFO = "App.Flight.Checkout.Info";
	private static final String FLIGHT_CHECKOUT_LOGIN = "App.Flight.Checkout.Login";
	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT = "App.Flight.Checkout.Traveler.Select";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO = "App.Flight.Checkout.Traveler.Edit.Info";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS = "App.Flight.Checkout.Traveler.Edit.Details";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT = "App.Flight.Checkout.Traveler.Edit.Passport";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE = "App.Flight.Checkout.Traveler.Edit.Save";
	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT = "App.Flight.Checkout.Payment.Select";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS = "App.Flight.Checkout.Payment.Edit.Address";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD = "App.Flight.Checkout.Payment.Edit.Card";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_EMAIL = "App.Flight.Checkout.Payment.Edit.Email";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE = "App.Flight.Checkout.Payment.Edit.Save";
	private static final String FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE = "App.Flight.Checkout.SlideToPurchase";
	private static final String FLIGHT_CHECKOUT_PAYMENT_CID = "App.Flight.Checkout.Payment.CID";
	private static final String FLIGHT_CHECKOUT_CONFIRMATION = "App.Flight.Checkout.Confirmation";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ERROR PAGE NAME CONSTANTS

	private static final String FLIGHT_ERROR_NOT_YET_AVAILABLE = "App.Flight.Error.NotYetAvailable";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET = "App.Flight.Error.Checkout.Payment.PriceChange.Ticket";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_FEE = "App.Flight.Error.Checkout.Payment.PriceChange.Fee";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED = "App.Flight.Error.Checkout.Payment.Failed";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV = "App.Flight.Error.Checkout.Payment.CVV";
	private static final String FLIGHT_ERROR_SOLD_OUT = "App.Flight.Error.SoldOut";
	private static final String FLIGHT_ERROR_SEARCH_EXPIRED = "App.Flight.Error.Search.Expired";
	private static final String FLIGHT_ERROR_CHECKOUT_TRAVELER_INFO_MISSING = "App.Flight.Error.Checkout.Traveler.InfoMissing";
	private static final String FLIGHT_ERROR_CHECKOUT_TRAVELER_UNACCOMPANIED_MINOR = "App.Flight.Error.Checkout.Traveler.UnaccompaniedMinor";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// TRACK LINK RFFR ACTION ID CONSTANTS

	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT = "App.Flight.Search.Roundtrip.Out.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT = "App.Flight.Search.Roundtrip.Out.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE = "App.Flight.Search.Roundtrip.Out.RefineSearch";

	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT = "App.Flight.Search.Roundtrip.In.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT = "App.Flight.Search.Roundtrip.Out.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE = "App.Flight.Search.Roundtrip.In.RefineSearch";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REMOVE_OUT = "App.Flight.Search.Roundtrip.In.RemoveOut";

	private static final String FLIGHT_RATE_DETAILS_REMOVE_OUT = "App.Flight.RateDetails.RemoveOut";
	private static final String FLIGHT_RATE_DETAILS_REMOVE_IN = "App.Flight.RateDetails.RemoveIn";

	private static final String FLIGHT_CHECKOUT_LOGIN_SUCCESS = "App.Flight.Checkout.Login.Success";
	private static final String FLIGHT_CHECKOUT_LOGIN_CANCEL = "App.Flight.Checkout.Login.Cancel";
	private static final String FLIGHT_CHECKOUT_LOGIN_FORGOT = "App.Flight.Checkout.Login.Forgot";

	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING = "App.Flight.Checkout.Traveler.Select.Existing";
	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT_FROM_CONTACTS = "App.Flight.Checkout.Traveler.Select.FromContacts";
	private static final String FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY = "App.Flight.Checkout.Traveler.EnterManually";

	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING = "App.Flight.Checkout.Payment.Select.Existing";
	private static final String FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY = "App.Flight.Checkout.Payment.EnterManually";

	public static void trackLinkLaunchScreenToNextScreen(Context context, String flightOrHotelString) {
		String link = LAUNCH_SCREEN + "." + flightOrHotelString;
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightOutboundSelect(Context context, String sortType, int position) {
		String link = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT + "." + sortType + "." + Integer.toString(position);
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightOutboundSort(Context context, String sortType) {
		String link = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT + "." + sortType;
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightOutboundRefine(Context context) {
		internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE);
	}

	public static void trackLinkFlightInboundSelect(Context context, String sortType, int position) {
		String link = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT + "." + sortType + "." + Integer.toString(position);
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightInboundSort(Context context, String sortType) {
		String link = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT + "." + sortType;
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightInboundRefine(Context context) {
		internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE);
	}

	public static void trackLinkFlightRemoveOutboundSelection(Context context) {
		internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_IN_REMOVE_OUT);
	}

	public static void trackLinkFlightRateDetailsRemoveOut(Context context) {
		internalTrackLink(context, FLIGHT_RATE_DETAILS_REMOVE_OUT);
	}

	public static void trackLinkFlightRateDetailsRemoveIn(Context context) {
		internalTrackLink(context, FLIGHT_RATE_DETAILS_REMOVE_IN);
	}

	public static void trackLinkFlightCheckoutLoginSuccess(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_LOGIN_SUCCESS);
	}

	public static void trackLinkFlightCheckoutLoginCancel(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_LOGIN_CANCEL);
	}

	public static void trackLinkFlightCheckoutLoginForgot(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_LOGIN_FORGOT);
	}

	public static void trackLinkFlightCheckoutTravelerSelectExisting(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING);
	}

	// DOES NOT EXIST ON ANDROID
	//	public static void trackLinkFlightCheckoutTravelerSelectFromContacts(Context context) {
	//		internalTrackLink(context, FLIGHT_CHECKOUT_TRAVELER_SELECT_FROM_CONTACTS);
	//	}

	public static void trackLinkFlightCheckoutTravelerEnterManually(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY);
	}

	public static void trackLinkFlightCheckoutPaymentSelectExisting(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING);
	}

	public static void trackLinkFlightCheckoutPaymentEnterManually(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY);
	}

	public static void trackErrorPageLoadFlightUnsupportedPOS(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_NOT_YET_AVAILABLE);
	}

	public static void trackErrorPageLoadFlightPriceChangeTicket(Context context) {
		internalTrackPageLoadEventPriceChange(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET);
	}

	public static void trackErrorPageLoadFlightPriceChangeFree(Context context) {
		internalTrackPageLoadEventPriceChange(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_FEE);
	}

	public static void trackErrorPageLoadFlightPaymentFailed(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED);
	}

	public static void trackErrorPageLoadFlightIncorrectCVV(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV);
	}

	public static void trackErrorPageLoadFlightSoldOut(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_SOLD_OUT);
	}

	public static void trackErrorPageLoadFlightSearchExpired(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_SEARCH_EXPIRED);
	}

	public static void trackErrorPageLoadFlightTravelerInfoMissing(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_TRAVELER_INFO_MISSING);
	}

	public static void trackErrorPageLoadFlightTravelerUnaccompaniedMinor(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_TRAVELER_UNACCOMPANIED_MINOR);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Flights tracking events

	// Note: this method looks slightly different than the rest, as it has an extra parameter - orderId. We need to send
	// this parameter in with the method call because we do not save FlightCheckout to the Db. All of the other methods
	// rely on Db.java to grab the information it desires
	public static void trackPageLoadFlightCheckoutConfirmation(Context context, final String orderId) {
		AppMeasurement s = createTrackPageLoadEventBase(context, FLIGHT_CHECKOUT_CONFIRMATION);
		addVars25And26LobAsConfirmer(s);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// products variable, described here: http://confluence/display/Omniture/Product+string+format
		String airlineCode = trip.getLeg(0).getOperatingAirlines().iterator().next();
		String tripType = getOmnitureStringCodeRepresentingTripTypeByNumLegs(trip.getLegCount());
		String numTravelers = "1"; // TODO: note this hardcoded as 1 for the time being as it is always one now
		String price = trip.getTotalFare().getFormattedMoney();

		s.products = "Flight;AgencyFlight:" + airlineCode + ":" + tripType + ";" + numTravelers + ";" + price;

		s.currencyCode = trip.getTotalFare().getCurrency();
		s.events = "purchase";

		// order number with an "onum" prefix, described here: http://confluence/pages/viewpage.action?pageId=419913476
		s.purchaseID = "onum" + orderId;

		// TRL
		Itinerary itin = Db.getItinerary(trip.getItineraryNumber());
		s.prop71 = itin.getTravelRecordLocator();

		// order #
		s.prop72 = orderId;

		s.track();
	}

	public static void trackPageLoadFlightCheckoutPaymentCid(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_CID);
	}

	public static void trackPageLoadFlightCheckoutSlideToPurchase(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE);
	}

	// TODO: does not exist
	//public static void trackPageLoadFlightCheckoutPaymentEditEmail(Context context) {
	//	internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_EMAIL);
	//}

	public static void trackPageLoadFlightCheckoutPaymentEditCard(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditAddress(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS);
	}

	public static void trackPageLoadFlightCheckoutPaymentSelect(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_SELECT);
	}

	public static void trackPageLoadFlightTravelerEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE);
	}

	public static void trackPageLoadFlightTravelerEditPassport(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT);
	}

	public static void trackPageLoadFlightTravelerEditDetails(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS);
	}

	public static void trackPageLoadFlightTravelerEditInfo(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO);
	}

	public static void trackPageLoadFlightTravelerSelect(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_SELECT);
	}

	public static void trackPageLoadFlightLogin(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_LOGIN);
	}

	public static void trackPageLoadFlightCheckout(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_INFO);
	}

	public static void trackPageLoadFlightOverview(Context context) {
		createTrackPageLoadEventPriceChangeAsShopper(context, FLIGHT_RATE_DETAILS).track();
	}

	public static void trackPageLoadFlightSearchResultsInboundDetails(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS);
	}

	public static void trackPageLoadFlightSearchResultsInboundList(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN);
	}

	public static void trackPageLoadFlightSearchResultsOutboundDetail(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS);
	}

	public static void trackPageLoadFlightSearchResultsOutboundList(Context context) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + FLIGHT_SEARCH_ROUNDTRIP_OUT + "\" pageLoad");

		AppMeasurement s = createTrackPageLoadEventBase(context, FLIGHT_SEARCH_ROUNDTRIP_OUT);

		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

		// Search Type: value always 'Flight'
		s.eVar2 = s.prop2 = "Flight";

		// Search Origin: 3 letter airport code of origin
		s.eVar3 = s.prop3 = searchParams.getDepartureLocation().getDestinationId();

		// Search Destination: 3 letter airport code of destination
		s.eVar4 = s.prop4 = searchParams.getArrivalLocation().getDestinationId();

		// day computation date, TODO test this stuff
		final Calendar departureDate = searchParams.getDepartureDate().getCalendar();
		final Calendar returnDate = searchParams.getReturnDate() == null ? null : searchParams.getReturnDate()
				.getCalendar();
		final Calendar now = Calendar.getInstance();

		// num days between current day (now) and flight departure date
		s.eVar5 = s.prop5 = Long.toString(CalendarUtils.getDaysBetween(now, departureDate));

		// num days between departure and return dates
		s.eVar6 = s.prop6 = Long.toString(CalendarUtils.getDaysBetween(departureDate, returnDate));

		_addVars25And26LobAsShopper(s);

		// Pipe delimited list of LOB, flight search type (OW, RT, MD), # of Adults, and # of Children)
		// e.g. FLT|RT|A2|C1
		// TODO this will need to be changed once we support multiple travelers
		s.eVar47 = "FLT|RT|A1|C0";

		// Success event for 'Search'
		s.events = "event30";

		s.track();
	}

	public static void trackPageLoadFlightSearchResultsPlaneLoadingFragment(Context context) {
		internalTrackPageLoadEventStandardNoVars25And25LobShopper(context, FLIGHT_SEARCH_INTERSTITIAL);
	}

	public static void trackPageLoadFlightSearch(Context context) {
		internalTrackPageLoadEventStandardNoVars25And25LobShopper(context, FLIGHT_SEARCH);
	}

	public static void trackPageLoadLaunchScreen(Context context) {
		internalTrackPageLoadEventStandardNoVars25And25LobShopper(context, LAUNCH_SCREEN);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Private helper methods

	private static void internalTrackPageLoadEventStandard(Context context, String pageName) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventStandardAsShopper(context, pageName).track();
	}

	private static void internalTrackPageLoadEventStandardNoVars25And25LobShopper(Context context, String pageName) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventBase(context, pageName).track();
	}

	private static void internalTrackPageLoadEventPriceChange(Context context, String pageName) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventPriceChange(context, pageName).track();
	}

	private static void internalTrackLink(Context context, String link) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + link + "\" linkClick");
		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		// TPID
		s.prop7 = LocaleUtils.getTPID(context);

		// link
		s.eVar28 = s.prop16 = link;

		s.trackLink(null, "o", s.eVar28);
	}

	private static AppMeasurement createTrackPageLoadEventBase(Context context, String pageName) {
		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		// set the pageName
		s.pageName = s.eVar18 = pageName;

		// TPID
		s.prop7 = LocaleUtils.getTPID(context);

		// TUID
		if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null) {
			long tuid = Db.getUser().getPrimaryTraveler().getTuid();
			if (tuid != 0) {
				s.prop11 = Long.toString(tuid);
			}
		}

		// App version
		s.prop35 = AndroidUtils.getAppVersion(context);

		// Screen orientation
		Configuration config = context.getResources().getConfiguration();
		switch (config.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			s.prop39 = "landscape";
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			s.prop39 = "portrait";
			break;
		case Configuration.ORIENTATION_SQUARE:
			s.prop39 = "square";
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			s.prop39 = "undefined";
			break;
		}

		// Experience segmentation TODO remove the hardcoded value
		s.eVar50 = "app.phone.android";

		// TODO verify this is needed

		// Add debugging flag if not release
		if (!AndroidUtils.isRelease(context) || DebugUtils.isLogEnablerInstalled(context)) {
			s.debugTracking = true;
		}

		// Add offline tracking, so user doesn't have to be online to be tracked
		s.trackOffline = true;

		// Server
		s.trackingServer = "om.expedia.com";
		s.trackingServerSecure = "oms.expedia.com";

		s.account = "expedia1androidcom";
		if (!AndroidUtils.isRelease(context)) {
			s.account += "dev";
		}

		return s;
	}

	private static AppMeasurement createTrackPageLoadEventStandardAsShopper(Context context, String pageName) {
		AppMeasurement s = createTrackPageLoadEventBase(context, pageName);
		_addVars25And26LobAsShopper(s);
		return s;
	}

	private static AppMeasurement createTrackPageLoadEventPriceChangeAsShopper(Context context, String pageName) {
		AppMeasurement s = createTrackPageLoadEventPriceChange(context, pageName);
		_addVars25And26LobAsShopper(s);
		return s;
	}

	private static AppMeasurement createTrackPageLoadEventPriceChange(Context context, String pageName) {
		AppMeasurement s = createTrackPageLoadEventBase(context, pageName);
		addEventPriceChange(s);
		return s;
	}

	private static AppMeasurement _addVars25And26LobAsShopper(AppMeasurement s) {
		s.eVar25 = s.prop25 = "Shopper";
		s.eVar26 = s.prop26 = "FLT Shopper";
		return s;
	}

	private static AppMeasurement addVars25And26LobAsConfirmer(AppMeasurement s) {
		s.eVar25 = s.prop25 = "Confirmer";
		s.eVar26 = s.prop26 = "CKO Shopper";
		return s;
	}

	private static AppMeasurement addEventPriceChange(AppMeasurement s) {
		// flag notifying price change occurred
		s.events = "event62";

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// This is only to be included when there is a price change shown on the page. This should be the % increase or 
		// decrease in price. Round to whole integers.
		String priceChange = trip.computePercentagePriceChangeForOmnitureTracking();
		if (priceChange != null) {
			s.prop9 = priceChange;
		}

		return s;
	}

	private static String getOmnitureStringCodeRepresentingTripTypeByNumLegs(final int numLegs) {
		switch (numLegs) {
		case 1:
			return "OW";
		case 2:
			return "RT";
		default:
			return "MD";
		}
	}
}
