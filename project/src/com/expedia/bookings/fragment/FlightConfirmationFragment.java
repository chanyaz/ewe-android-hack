package com.expedia.bookings.fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.*;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.utils.*;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.AddFlightsIntentUtils;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

// We can assume that if this fragment loaded we successfully booked, so most
// data we need to grab is available.
public class FlightConfirmationFragment extends Fragment {

	public static final String TAG = FlightConfirmationFragment.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_confirmation, container, false);

		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();
		String destinationCity = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());
		Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());

		// Format the flight cards
		RelativeLayout cardContainer = Ui.findView(v, R.id.flight_card_container);

		// Only display the animation the first time the page loads
		if (savedInstanceState == null) {
			LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(),
					R.anim.layout_card_slide);
			cardContainer.setLayoutAnimation(controller);
		}

		Resources res = getResources();

		// Measure the frontmost card - we need to know its height to correctly size the fake cards
		FlightLegSummarySection card = (FlightLegSummarySection) inflater.inflate(
				R.layout.section_flight_leg_summary, cardContainer, false);
		card.bind(trip, trip.getLeg(0));
		LayoutUtils.setBackgroundResource(card, R.drawable.bg_flight_conf_row);
		card.measure(MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY));
		int cardHeight = card.getMeasuredHeight();

		float initialOffset = res.getDimension(R.dimen.flight_card_initial_offset);
		float offset = res.getDimension(R.dimen.flight_card_overlap_offset);
		int numLegs = trip.getLegCount();
		for (int a = numLegs - 1; a >= 0; a--) {
			View view;

			if (a == 0) {
				cardContainer.addView(card);
				view = card;
			}
			else {
				view = new View(getActivity());
				cardContainer.addView(view);

				// Set a custom bg
				LayoutUtils.setBackgroundResource(view, R.drawable.bg_flight_conf_row);

				// For some reason, I can't get this layout to work unless I set an explicit
				// height (probably because of a RelativeLayout circular dependency).
				view.getLayoutParams().height = cardHeight + Math.round(offset * a);
			}

			// Each card is offset below the last one
			MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
			params.topMargin = (int) (initialOffset + Math.round(offset * (numLegs - 1 - a)));
		}

		// Fill out all the actions
		setTextAndRobotoFont(v, R.id.going_to_text_view,
				getString(R.string.yay_going_somewhere_TEMPLATE, destinationCity));

		Ui.setText(v, R.id.itinerary_text_view,
				getString(R.string.itinerary_confirmation_TEMPLATE, itinerary.getItineraryNumber()));

		setTextAndRobotoFont(v, R.id.email_text_view, Db.getBillingInfo().getEmail());

		Ui.setText(v, R.id.hotels_action_text_view, getString(R.string.hotels_in_TEMPLATE, destinationCity));
		Ui.setOnClickListener(v, R.id.hotels_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchForHotels();
			}
		});

		Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});

		if (supportsCalendar()) {
			Ui.setOnClickListener(v, R.id.calendar_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					addToCalendar();
				}
			});
		}
		else {
			Ui.findView(v, R.id.calendar_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.calendar_divider).setVisibility(View.GONE);
		}

		if (canTrackWithFlightTrack()) {
			Ui.setOnClickListener(v, R.id.flighttrack_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(getFlightTrackIntent());
				}
			});
		}
		else {
			Ui.findView(v, R.id.flighttrack_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.flighttrack_divider).setVisibility(View.GONE);
		}

		Ui.setOnClickListener(v, R.id.call_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getActivity(), SupportUtils.getFlightSupportNumber(getActivity()));
			}
		});

		// We need to capitalize in code because the all_caps field isn't until a later API
		setTextAndRobotoFont(v, R.id.get_a_room_text_view, getString(R.string.get_a_room).toUpperCase());
		setTextAndRobotoFont(v, R.id.more_actions_text_view, getString(R.string.more_actions).toUpperCase());

		return v;
	}

	private void setTextAndRobotoFont(View root, int resId, CharSequence text) {
		TextView tv = Ui.findView(root, resId);
		tv.setTypeface(FontCache.getTypeface(Font.ROBOTO_LIGHT));
		tv.setText(text);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search for hotels

	private Calendar waypointTimeToHotelTime(Calendar in) {
		Date localTzTime = DateTimeUtils.getTimeInLocalTimeZone(in);
		Calendar tCal = Calendar.getInstance();
		tCal.setTime(localTzTime);
		Calendar retCal = Calendar.getInstance();
		retCal.set(tCal.get(Calendar.YEAR), tCal.get(Calendar.MONTH), tCal.get(Calendar.DAY_OF_MONTH));
		return retCal;
	}

	// If we are comparing days between by using Calendar.after and before, must set hours/minutes/seconds to zero
	private void normalizeForQuickTimeComparison(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	private void searchForHotels() {
		//Where flights meets hotels
		SearchParams sp = new SearchParams();
		sp.setSearchType(SearchType.CITY);
		sp.setNumAdults(Db.getTravelers().size());

		int legCount = Db.getFlightSearch().getSelectedFlightTrip().getLegCount();
		FlightLeg firstLeg = Db.getFlightSearch().getSelectedFlightTrip().getLeg(0);
		Calendar checkinDate = firstLeg.getLastWaypoint().getMostRelevantDateTime();

		// h637 we set the TimeZone to UTC in anticipation of it being called later on in the flow. Originally, the 
		// TimeZone was not being set on construction and then set to UTC after day, month, year were set which in turn
		// caused the Calendar DATE field to be incremented one day forward when accessed via get(FIELD)
		Calendar checkinNormalized = Calendar.getInstance();
		checkinNormalized.setTimeZone(CalendarUtils.getFormatTimeZone());
		checkinNormalized.set(Calendar.DATE, checkinDate.get(Calendar.DATE));
		checkinNormalized.set(Calendar.MONTH, checkinDate.get(Calendar.MONTH));
		checkinNormalized.set(Calendar.YEAR, checkinDate.get(Calendar.YEAR));
		sp.setCheckInDate(checkinNormalized);

		if (legCount > 1) {
			//Round trip
			FlightLeg lastLeg = Db.getFlightSearch().getSelectedFlightTrip()
					.getLeg(Db.getFlightSearch().getSelectedFlightTrip().getLegCount() - 1);
			Calendar checkoutDate = waypointTimeToHotelTime(lastLeg.getFirstWaypoint().getMostRelevantDateTime());
			// Note: waypointTimeToHotelTime returns a copy of the time from Db, so we can modify it here
			normalizeForQuickTimeComparison(checkoutDate);

			Calendar checkoutDateCeiling = Calendar.getInstance();
			checkoutDateCeiling.setTime(checkinDate.getTime());
			checkoutDateCeiling.add(Calendar.DAY_OF_MONTH, 28);
			normalizeForQuickTimeComparison(checkoutDateCeiling);

			// f934 do not kick off a hotel search for a more than 28 day stay
			if (checkoutDate.after(checkoutDateCeiling)) {
				sp.setCheckOutDate(checkoutDateCeiling);
			}
			else {
				sp.setCheckOutDate(checkoutDate);
			}
		}
		else {
			//One way trip
			Calendar checkoutDate = Calendar.getInstance();
			checkoutDate.setTime(checkinDate.getTime());
			checkoutDate.add(Calendar.DAY_OF_MONTH, 1);
			sp.setCheckOutDate(checkoutDate);
		}

		String cityStr = firstLeg.getLastWaypoint().getAirport().mCity;
		if (TextUtils.isEmpty(cityStr)) {
			cityStr = firstLeg.getLastWaypoint().mAirportCode;
		}

		//Because we are adding a lat/lon parameter, it doesn't matter too much if our query isn't perfect
		sp.setUserQuery(cityStr);
		sp.setQuery(cityStr);

		double lat = firstLeg.getLastWaypoint().getAirport().getLatE6() / 1E6;
		double lon = firstLeg.getLastWaypoint().getAirport().getLonE6() / 1E6;

		if (lat == 0 && lon == 0) {
			//We try the origin of the last segment - this isn't great, but in the case of a bus ride, it might be about all we have
			lat = firstLeg.getSegment(firstLeg.getSegmentCount() - 1).mOrigin.getAirport().getLatE6() / 1E6;
			lon = firstLeg.getSegment(firstLeg.getSegmentCount() - 1).mOrigin.getAirport().getLonE6() / 1E6;
		}

		//These should only be zero in rare cases, at which time we just use our cityStr
		if (lat != 0 || lon != 0) {
			sp.setSearchLatLon(lat, lon);
			sp.setSearchLatLonUpToDate();
		}

		//Update the Db object to have our search params (which will be used by hotels search)
		Db.setSearchParams(sp);

		Intent searchHotelsIntent = new Intent(getActivity(), PhoneSearchActivity.class);
		searchHotelsIntent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);
		startActivity(searchHotelsIntent);

		// Finish this activity when navigating in-app to hotels search. The rest of the backstack should already be
		// cleared when launching this activity to account for hitting back from this Activity so the KILL_ACTIVITY
		// broadcast does not need to be sent.
		getActivity().finish();
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
		Context context = getActivity();
		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();
		FlightLeg firstLeg = trip.getLeg(0);
		Traveler traveler = Db.getTravelers().get(0); // Assume first traveler is only traveler
		int numLegs = trip.getLegCount();

		String originCity = StrUtils.getWaypointCityOrCode(firstLeg.getFirstWaypoint());
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		// Construct subject
		long start = DateTimeUtils.getTimeInLocalTimeZone(firstLeg.getFirstWaypoint().getMostRelevantDateTime())
				.getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(
				trip.getLeg(numLegs - 1).getLastWaypoint().getMostRelevantDateTime()).getTime();
		String dateRange = DateUtils.formatDateRange(context, start, end, DateUtils.FORMAT_NUMERIC_DATE
				| DateUtils.FORMAT_SHOW_DATE);
		String subject = getString(R.string.share_flight_title_TEMPLATE, destinationCity, dateRange);

		// Construct the body
		StringBuilder body = new StringBuilder();
		body.append(getString(R.string.share_flight_start));

		body.append("\n\n");

		if (numLegs == 1) {
			body.append(getString(R.string.share_flight_one_way_TEMPLATE, originCity, destinationCity));
		}
		else {
			// Assume round trip for now
			body.append(getString(R.string.share_flight_round_trip_TEMPLATE, originCity, destinationCity));
		}

		body.append("\n\n");

		body.append(getString(R.string.share_flight_itinerary_TEMPLATE, trip.getItineraryNumber()));

		body.append("\n\n");

		body.append(getString(R.string.share_flight_name_TEMPLATE,
				getString(R.string.name_template, traveler.getFirstName(), traveler.getLastName())));

		body.append("\n\n");

		body.append(getString(R.string.share_flight_section_outbound));

		body.append("\n\n");

		addShareLeg(body, firstLeg);

		// Assume only round trips
		if (numLegs == 2) {
			body.append("\n\n");

			body.append(getString(R.string.share_flight_section_return));

			body.append("\n\n");

			addShareLeg(body, trip.getLeg(1));
		}

		body.append("\n\n");

		body.append(getString(R.string.share_flight_ticket_cost_TEMPLATE, trip.getBaseFare().getFormattedMoney()));

		body.append("\n");

		Money taxesAndFees = new Money(trip.getTaxes());
		taxesAndFees.add(trip.getFees());

		body.append(getString(R.string.share_flight_taxes_fees_TEMPLATE, taxesAndFees.getFormattedMoney()));

		body.append("\n\n");

		body.append(getString(R.string.share_flight_airfare_total_TEMPLATE, trip.getTotalFare().getFormattedMoney()));

		body.append("\n\n");

		body.append(getString(R.string.share_flight_additional_fees_TEMPLATE,
				SupportUtils.getBaggageFeeUrl(firstLeg.getFirstWaypoint().mAirportCode,
						firstLeg.getLastWaypoint().mAirportCode)));

		body.append("\n\n");

		body.append(getString(R.string.share_flight_support_TEMPLATE, SupportUtils.getFlightSupportNumber(context)));

		body.append("\n\n");

		body.append(getString(R.string.share_flight_shill_app));

		SocialUtils.email(getActivity(), subject, body.toString());
	}

	private void addShareLeg(StringBuilder sb, FlightLeg flightLeg) {
		Context context = getActivity();
		Resources res = context.getResources();
		int segCount = flightLeg.getSegmentCount();

		for (int a = 0; a < segCount; a++) {
			Flight flight = flightLeg.getSegment(a);

			if (a > 0) {
				Layover layover = new Layover(flightLeg.getSegment(a - 1), flight);
				String duration = DateTimeUtils.formatDuration(res, layover.mDuration);
				String waypoint = StrUtils.formatWaypoint(flight.mOrigin);
				sb.append(Html.fromHtml(getString(R.string.layover_duration_location_TEMPLATE, duration, waypoint)));
				sb.append("\n\n");
			}

			sb.append(getString(R.string.path_template, formatAirport(flight.mOrigin.getAirport()),
					formatAirport(flight.mDestination.getAirport())));
			sb.append("\n");
			long start = DateTimeUtils.getTimeInLocalTimeZone(flight.mOrigin.getMostRelevantDateTime()).getTime();
			sb.append(DateUtils.formatDateTime(context, start, DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY));
			sb.append("\n");
			long end = DateTimeUtils.getTimeInLocalTimeZone(flight.mDestination.getMostRelevantDateTime()).getTime();
			sb.append(DateUtils.formatDateRange(context, start, end, DateUtils.FORMAT_SHOW_TIME));
			sb.append("\n");
			sb.append(FormatUtils.formatFlightNumber(flight, context));

			if (a + 1 != segCount) {
				sb.append("\n\n");
			}
		}
	}

	private String formatAirport(Airport airport) {
		if (!TextUtils.isEmpty(airport.mCity)) {
			return airport.mCity + " (" + airport.mAirportCode + ")";
		}
		else {
			return airport.mAirportCode;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private boolean supportsCalendar() {
		Uri data = null;
		try {
			Class<?> clz = Class.forName("android.provider.CalendarContract");
			for (Class<?> c : clz.getDeclaredClasses()) {
				if (c.getName().equals("android.provider.CalendarContract$Events")) {
					Field f = c.getField("CONTENT_URI");
					data = (Uri) f.get(null);
				}
			}
		}
		catch (Exception e) {
			Log.d("Reflection error trying to look for calendar support", e);
		}
		finally {
			if (data == null) {
				Log.d("Device does not support calendaring.");
				return false;
			}
		}

		Intent dummy = new Intent(Intent.ACTION_INSERT);
		dummy.setData(data);

		PackageManager packageManager = getActivity().getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(dummy, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void addToCalendar() {
		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		for (int a = 0; a < trip.getLegCount(); a++) {
			Intent intent = generateCalendarInsertIntent(trip.getLeg(a));
			startActivity(intent);
		}
	}

	@SuppressLint("NewApi")
	private Intent generateCalendarInsertIntent(FlightLeg leg) {
		Waypoint origin = leg.getFirstWaypoint();
		Airport originAirport = origin.getAirport();
		Waypoint destination = leg.getLastWaypoint();
		String itineraryNumber = Db.getFlightSearch().getSelectedFlightTrip().getItineraryNumber();

		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setData(Events.CONTENT_URI);
		intent.putExtra(Events.TITLE,
				getString(R.string.calendar_flight_title_TEMPLATE, origin.mAirportCode, destination.mAirportCode));
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, origin.getMostRelevantDateTime().getTimeInMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, destination.getMostRelevantDateTime().getTimeInMillis());
		intent.putExtra(Events.EVENT_LOCATION,
				getString(R.string.calendar_flight_location_TEMPLATE, originAirport.mName,
						StrUtils.getWaypointCityOrCode(origin)));

		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.calendar_flight_desc_itinerary_TEMPLATE, itineraryNumber));
		sb.append("\n\n");
		sb.append(getString(R.string.calendar_flight_desc_directions_TEMPLATE, "https://maps.google.com/maps?q="
				+ origin.mAirportCode));
		sb.append("\n\n");
		sb.append(getString(R.string.calendar_flight_desc_support_TEMPLATE,
				SupportUtils.getFlightSupportNumber(getActivity())));
		sb.append("\n\n");
		intent.putExtra(Events.DESCRIPTION, sb.toString());
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightTrack integration

	public Intent getFlightTrackIntent() {
		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		List<Flight> flights = new ArrayList<Flight>();
		for (int a = 0; a < trip.getLegCount(); a++) {
			flights.addAll(trip.getLeg(a).getSegments());
		}
		return AddFlightsIntentUtils.getIntent(flights);
	}

	public boolean canTrackWithFlightTrack() {
		return NavUtils.isIntentAvailable(getActivity(), getFlightTrackIntent());
	}
}
