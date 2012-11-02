package com.expedia.bookings.tracking;

import java.util.Calendar;

import android.app.Application;
import android.content.Context;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.Log;
import com.omniture.AppMeasurement;

/**
 * The spec behind this class can be found here: http://confluence/display/Omniture/Mobile+App+Flight+Tracking
 * 
 * The basic premise behind this class is to encapsulate the tracking logic as much possible such that tracking events
 * can be inserted into the business logic as cleanly as possible. The events rely on Db.java to populate values when
 * needed, and exceptions are made to accommodate the events that require extra parameters to be sent. This is why there
 * exist so many methods, one for each event that is being tracked.
 * 
 */

public class OmnitureTracking {
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// STANDARD PAGE NAME CONSTANTS

	private static final String LAUNCH_SCREEN = "App.LaunchScreen";
	private static final String FLIGHT_SEARCH = "App.Flight.Search";
	private static final String FLIGHT_SEARCH_INTERSTITIAL = "App.Flight.Search.Interstitial";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT = "App.Flight.Search.Roundtrip.Out";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS = "App.Flight.Search.Roundtrip.Out.Details";
	private static final String FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.Out.BaggageFee";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN = "App.Flight.Search.Roundtrip.In";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS = "App.Flight.Search.Roundtrip.In.Details";
	private static final String FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.In.BaggageFee";
	private static final String FLIGHT_RATE_DETAILS = "App.Flight.RateDetails";
	private static final String FLIGHT_CHECKOUT_INFO = "App.Flight.Checkout.Info";
	private static final String FLIGHT_CHECKOUT_LOGIN = "App.Flight.Checkout.Login";
	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT = "App.Flight.Checkout.Traveler.Select";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO = "App.Flight.Checkout.Traveler.Edit.Info";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS = "App.Flight.Checkout.Traveler.Edit.Details";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT = "App.Flight.Checkout.Traveler.Edit.Passport";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE = "App.Flight.Checkout.Traveler.Edit.Save";
	private static final String FLIGHT_CHECKOUT_WARSAW = "App.Flight.Checkout.Warsaw";
	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT = "App.Flight.Checkout.Payment.Select";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS = "App.Flight.Checkout.Payment.Edit.Address";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD = "App.Flight.Checkout.Payment.Edit.Card";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE = "App.Flight.Checkout.Payment.Edit.Save";
	private static final String FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE = "App.Flight.Checkout.SlideToPurchase";
	private static final String FLIGHT_CHECKOUT_PAYMENT_CID = "App.Flight.Checkout.Payment.CID";
	private static final String FLIGHT_CHECKOUT_CONFIRMATION = "App.Flight.Checkout.Confirmation";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ONEWAY PAGE NAME AND TRACK LINK CONSTANTS

	private static final String FLIGHT_SEARCH_RESULTS_ONE_WAY = "App.Flight.Search.OneWay";
	private static final String PREFIX_FLIGHT_SEARCH_ONE_WAY_SELECT = "App.Flight.Search.OneWay.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ONE_WAY_SORT = "App.Flight.Search.OneWay.Sort";
	private static final String FLIGHT_SEARCH_ONE_WAY_REFINE = "App.Flight.Search.OneWay.RefineSearch";
	private static final String FLIGHT_SEARCH_ONE_WAY_DETAILS = "App.Flight.Search.OneWay.Details";
	private static final String FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE = "App.Flight.Search.OneWay.BaggageFee";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ERROR PAGE NAME CONSTANTS

	private static final String FLIGHT_ERROR_NOT_YET_AVAILABLE = "App.Flight.Error.NotYetAvailable";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET = "App.Flight.Error.Checkout.Payment.PriceChange.Ticket";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED = "App.Flight.Error.Checkout.Payment.Failed";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV = "App.Flight.Error.Checkout.Payment.CVV";
	private static final String FLIGHT_ERROR_SOLD_OUT = "App.Flight.Error.SoldOut";
	private static final String FLIGHT_ERROR_SEARCH_EXPIRED = "App.Flight.Error.Search.Expired";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// TRACK LINK RFFR ACTION ID CONSTANTS

	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT = "App.Flight.Search.Roundtrip.Out.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT = "App.Flight.Search.Roundtrip.Out.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE = "App.Flight.Search.Roundtrip.Out.RefineSearch";

	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT = "App.Flight.Search.Roundtrip.In.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT = "App.Flight.Search.Roundtrip.In.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE = "App.Flight.Search.Roundtrip.In.RefineSearch";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REMOVE_OUT = "App.Flight.Search.Roundtrip.In.RemoveOut";

	private static final String FLIGHT_RATE_DETAILS_REMOVE_OUT = "App.Flight.RateDetails.RemoveOut";
	private static final String FLIGHT_RATE_DETAILS_REMOVE_IN = "App.Flight.RateDetails.RemoveIn";

	private static final String FLIGHT_CHECKOUT_LOGIN_SUCCESS = "App.Flight.Checkout.Login.Success";
	private static final String FLIGHT_CHECKOUT_LOGIN_CANCEL = "App.Flight.Checkout.Login.Cancel";
	private static final String FLIGHT_CHECKOUT_LOGIN_FORGOT = "App.Flight.Checkout.Login.Forgot";

	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING = "App.Flight.Checkout.Traveler.Select.Existing";
	private static final String FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY = "App.Flight.Checkout.Traveler.EnterManually";
	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING = "App.Flight.Checkout.Payment.Select.Existing";
	private static final String FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY = "App.Flight.Checkout.Payment.EnterManually";

	private static final String EMAIL_HASH_KEY = "email_hash_flights";
	private static final String NO_EMAIL = "NO EMAIL PROVIDED FLIGHTS";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC TRACK LINK METHODS

	public static void trackLinkLaunchScreenToHotels(Context context) {
		String link = LAUNCH_SCREEN + "." + "Hotel";
		internalTrackLink(context, link);
	}

	public static void trackLinkLaunchScreenToFlights(Context context) {
		String link = LAUNCH_SCREEN + "." + "Flight";
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightSearchSelect(Context context, int selectPos, int legPos) {
		String prefix = "";

		if (legPos == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT;
			}
			else {
				prefix = PREFIX_FLIGHT_SEARCH_ONE_WAY_SELECT;
			}
		}
		else if (legPos == 1) {
			prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT;
		}

		FlightFilter filter = Db.getFlightSearch().getFilter(legPos);
		String link = prefix + "." + filter.getSort().name() + "." + Integer.toString(selectPos);

		internalTrackLink(context, link);
	}

	public static void trackLinkFlightRefine(Context context, int legPosition) {
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE);
			}
			else {
				internalTrackLink(context, FLIGHT_SEARCH_ONE_WAY_REFINE);
			}
		}
		else if (legPosition == 1) {
			internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE);
		}
	}

	public static void trackLinkFlightSort(Context context, String sortType, int legPosition) {
		String prefix = "";

		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT;
			}
			else {
				prefix = PREFIX_FLIGHT_SEARCH_ONE_WAY_SORT;
			}
		}
		else if (legPosition == 1) {
			prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT;
		}

		String link = prefix + "." + sortType;
		internalTrackLink(context, link);
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

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Flights tracking events

	public static void trackPageLoadFlightCheckoutConfirmation(Context context) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + FLIGHT_CHECKOUT_CONFIRMATION + "\" pageLoad");
		AppMeasurement s = createTrackPageLoadEventBase(context, FLIGHT_CHECKOUT_CONFIRMATION);
		addVars25And26LobAsConfirmer(s);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// products variable, described here: http://confluence/display/Omniture/Product+string+format
		String airlineCode = trip.getLeg(0).getPrimaryAirlines().iterator().next();
		String tripType = getOmnitureStringCodeRepresentingTripTypeByNumLegs(trip.getLegCount());
		String numTravelers = "1"; // TODO: note this hardcoded as 1 for the time being as it is always one now
		String price = trip.getTotalFare().getFormattedMoney();

		s.products = "Flight;AgencyFlight:" + airlineCode + ":" + tripType + ";" + numTravelers + ";" + price;

		s.currencyCode = trip.getTotalFare().getCurrency();
		s.events = "purchase";

		// order number with an "onum" prefix, described here: http://confluence/pages/viewpage.action?pageId=419913476
		final String orderId = Db.getFlightCheckout().getOrderId();
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

	public static void trackPageLoadFlightCheckoutPaymentEditCard(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditAddress(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS);
	}

	public static void trackPageLoadFlightCheckoutPaymentSelect(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_SELECT);
	}

	public static void trackPageLoadFlightCheckoutWarsaw(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_WARSAW);
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

	public static void trackPageLoadFlightCheckoutInfo(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_INFO);
	}

	public static void trackPageLoadFlightRateDetailsOverview(Context context) {
		internalTrackPageLoadEventPriceChangeAsShopper(context, FLIGHT_RATE_DETAILS);
	}

	private static boolean mTrackNewSearchResultSet;

	/**
	 * This method is used in the client and internally as bookkeeping to ensure that the special events, outbound list
	 * and oneway list of search results get tracked once and only once for a given set of search parameters. Clients
	 * will use this method with 'true' when performing a new search, and internally this method is used with 'false' to
	 * disallow additional tracking events after the first event has been tracked (until explicitly set true by client).
	 * 
	 * Most importantly, this keeps the silly Omniture tracking details (mostly) out of client code and in this class.
	 * 
	 * @param markTrackNewSearchResultSet
	 */
	public static void markTrackNewSearchResultSet(boolean markTrackNewSearchResultSet) {
		mTrackNewSearchResultSet = markTrackNewSearchResultSet;
	}

	public static void trackPageLoadFlightSearchResults(Context context, int legPosition) {
		if (legPosition == 0) {
			// Note: according the spec we want only to track the FlightSearchResults if it represents a new set of data
			if (mTrackNewSearchResultSet) {
				if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
					OmnitureTracking.trackPageLoadFlightSearchResultsOutboundList(context);
				}
				else {
					OmnitureTracking.trackPageLoadFlightSearchResultsOneWay(context);
				}
			}
		}

		// According to spec, we want to track the inbound list as many times as the user rotates device, etc...
		else if (legPosition == 1) {
			OmnitureTracking.trackPageLoadFlightSearchResultsInboundList(context);
		}
	}

	private static void trackPageLoadFlightSearchResultsOutboundList(Context context) {
		markTrackNewSearchResultSet(false);

		Log.d("ExpediaBookingsTracking", "Tracking \"" + FLIGHT_SEARCH_ROUNDTRIP_OUT + "\" pageLoad");

		AppMeasurement s = createTrackPageLoadEventStandardAsShopper(context, FLIGHT_SEARCH_ROUNDTRIP_OUT);

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

		// Pipe delimited list of LOB, flight search type (OW, RT, MD), # of Adults, and # of Children)
		// e.g. FLT|RT|A2|C1
		// TODO this will need to be changed once we support multiple travelers
		s.eVar47 = "FLT|RT|A1|C0";

		// Success event for 'Search'
		s.events = "event30";

		s.track();
	}

	private static void trackPageLoadFlightSearchResultsInboundList(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN);
	}

	private static void trackPageLoadFlightSearchResultsOneWay(Context context) {
		markTrackNewSearchResultSet(false);

		Log.d("ExpediaBookingsTracking", "Tracking \"" + FLIGHT_SEARCH_RESULTS_ONE_WAY + "\" pageLoad");

		AppMeasurement s = createTrackPageLoadEventStandardAsShopper(context, FLIGHT_SEARCH_RESULTS_ONE_WAY);

		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

		// Search Type: value always 'Flight'
		s.eVar2 = s.prop2 = "Flight";

		// Search Origin: 3 letter airport code of origin
		s.eVar3 = s.prop3 = searchParams.getDepartureLocation().getDestinationId();

		// Search Destination: 3 letter airport code of destination
		s.eVar4 = s.prop4 = searchParams.getArrivalLocation().getDestinationId();

		// day computation date, TODO test this stuff
		final Calendar departureDate = searchParams.getDepartureDate().getCalendar();
		final Calendar now = Calendar.getInstance();

		// num days between current day (now) and flight departure date
		s.eVar5 = s.prop5 = Long.toString(CalendarUtils.getDaysBetween(now, departureDate));

		// Pipe delimited list of LOB, flight search type (OW, RT, MD), # of Adults, and # of Children)
		// e.g. FLT|RT|A2|C1
		// TODO this will need to be changed once we support multiple travelers
		s.eVar47 = "FLT|OW|A1|C0";

		// Success event for 'Search'
		s.events = "event30";

		s.track();
	}

	public static void trackPageLoadFlightBaggageFee(Context context, int legPosition) {
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE);
			}
			else {
				internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE);
			}
		}
		else if (legPosition == 1) {
			internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE);
		}
	}

	public static void trackPageLoadFlightSearchResultsDetails(Context context, int legPosition) {
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS);
			}
			else {
				internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ONE_WAY_DETAILS);
			}
		}
		else if (legPosition == 1) {
			internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS);

		}
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

	private static void internalTrackPageLoadEventPriceChangeAsShopper(Context context, String pageName) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventPriceChangeAsShopper(context, pageName).track();
	}

	private static void internalTrackLink(Context context, String link) {
		Log.d("ExpediaBookingsTracking", "Tracking \"" + link + "\" linkClick");
		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		TrackingUtils.addStandardFields(context, s);

		// link
		s.eVar28 = s.prop16 = link;

		s.trackLink(null, "o", s.eVar28);
	}

	// Note: a lot of this is taken from TrackingUtils. Eventually the code should be unified. Avoiding now in interest
	// of time and priorities (and not wanting to break Hotels tracking)
	private static AppMeasurement createTrackPageLoadEventBase(Context context, String pageName) {
		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		// set the pageName
		s.pageName = s.eVar18 = pageName;

		TrackingUtils.addStandardFields(context, s);

		return s;
	}

	private static AppMeasurement createTrackPageLoadEventStandardAsShopper(Context context, String pageName) {
		AppMeasurement s = createTrackPageLoadEventBase(context, pageName);
		addVars25And26LobAsShopper(s);
		return s;
	}

	private static AppMeasurement createTrackPageLoadEventPriceChangeAsShopper(Context context, String pageName) {
		AppMeasurement s = createTrackPageLoadEventPriceChange(context, pageName);
		addVars25And26LobAsShopper(s);
		return s;
	}

	private static AppMeasurement createTrackPageLoadEventPriceChange(Context context, String pageName) {
		AppMeasurement s = createTrackPageLoadEventBase(context, pageName);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// This is only to be included when there is a price change shown on the page. This should be the % increase or
		// decrease in price. Round to whole integers.
		String priceChange = trip.computePercentagePriceChangeForOmnitureTracking();
		if (priceChange != null) {
			s.events = "event62";
			s.prop9 = priceChange;
		}

		return s;
	}

	// Note: The following addVars methods are intended to be used only from within the private, internal event create
	// methods found above, NOT to be used from within the public methods used in the business logic. If you find
	// yourself wanting to use these methods in a new public event method, think about creating an internal method that
	// uses these methods.

	private static AppMeasurement addVars25And26LobAsShopper(AppMeasurement s) {
		s.eVar25 = s.prop25 = "Shopper";
		s.eVar26 = s.prop26 = "FLT Shopper";
		return s;
	}

	private static AppMeasurement addVars25And26LobAsConfirmer(AppMeasurement s) {
		s.eVar25 = s.prop25 = "Confirmer";
		s.eVar26 = s.prop26 = "CKO Shopper";
		return s;
	}

	// This method will eventually become more useful when we support multi-destination flights
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
