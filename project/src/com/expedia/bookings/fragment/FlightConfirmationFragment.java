package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
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
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.AddFlightsIntentUtils;

// We can assume that if this fragment loaded we successfully booked, so most
// data we need to grab is available.
public class FlightConfirmationFragment extends ConfirmationFragment {

	public static final String TAG = FlightConfirmationFragment.class.getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// This can be invoked when the parent activity finishes itself (when it detects missing data, in the case of
		// a background kill). In this case, lets just return a null view because it won't be used anyway. Prevents NPE.
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();
		String destinationCity = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());

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
		FlightLeg frontmostLeg = trip.getLeg(0);
		FlightLegSummarySection card = (FlightLegSummarySection) inflater.inflate(R.layout.section_flight_leg_summary,
				cardContainer, false);
		card.bind(trip, frontmostLeg, Db.getBillingInfo());
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

		if (frontmostLeg.getDaySpan() > 0) {
			ViewGroup actionContainer = Ui.findView(v, R.id.action_container);
			((MarginLayoutParams) actionContainer.getLayoutParams()).topMargin = res
					.getDimensionPixelSize(R.dimen.flight_card_with_span_mask_offset);
		}

		Ui.findView(v, R.id.action_container).setBackgroundResource(R.drawable.bg_confirmation_mask_flights);

		// Fill out all the actions
		Ui.setText(v, R.id.going_to_text_view, getString(R.string.yay_going_somewhere_TEMPLATE, destinationCity));

		if (PointOfSale.getPointOfSale().showHotelCrossSell()) {
			Ui.setText(v, R.id.hotels_action_text_view, getString(R.string.hotels_in_TEMPLATE, destinationCity));
			Ui.setOnClickListener(v, R.id.hotels_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					searchForHotels();
				}
			});
		}
		else {
			Ui.findView(v, R.id.hotels_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.get_a_room_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.get_a_room_divider).setVisibility(View.GONE);
		}

		Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});

		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity())) {
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

		// Only display an insurance url if it exists. Currently only present for CA POS.
		final String insuranceUrl = PointOfSale.getPointOfSale().getInsuranceUrl();
		if (!TextUtils.isEmpty(insuranceUrl)) {
			Ui.setOnClickListener(v, R.id.ca_insurance_action_text_view, new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					addInsurance(insuranceUrl);
				}
			});
		}
		else {
			Ui.findView(v, R.id.ca_insurance_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.ca_insurance_divider).setVisibility(View.GONE);
		}

		// We need to capitalize in code because the all_caps field isn't until a later API
		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.get_a_room_text_view));
		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.more_actions_text_view));

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// ConfirmationFragment

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_flight_confirmation;
	}

	@Override
	protected int getActionsLayoutId() {
		return R.layout.include_confirmation_actions_flights;
	}

	@Override
	protected String getItinNumber() {
		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();
		Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());
		return itinerary.getItineraryNumber();
	}

	//////////////////////////////////////////////////////////////////////////
	// Search for hotels

	private void searchForHotels() {
		//Where flights meets hotels
		HotelSearchParams sp = new HotelSearchParams();
		sp.setSearchType(SearchType.CITY);

		// Assuming all the travelers are adults, we set the most adults we can
		sp.setNumAdults(Math.min(GuestsPickerUtils.getMaxAdults(0), Db.getFlightSearch().getSearchParams()
				.getNumAdults()));

		int legCount = Db.getFlightSearch().getSelectedFlightTrip().getLegCount();
		FlightLeg firstLeg = Db.getFlightSearch().getSelectedFlightTrip().getLeg(0);
		LocalDate checkInDate = LocalDate.fromCalendarFields(firstLeg.getLastWaypoint().getMostRelevantDateTime());
		sp.setCheckInDate(checkInDate);

		if (legCount > 1) {
			//Round trip
			FlightTrip flightTrip = Db.getFlightSearch().getSelectedFlightTrip();
			FlightLeg lastLeg = flightTrip.getLeg(flightTrip.getLegCount() - 1);

			LocalDate checkOutDate = LocalDate.fromCalendarFields(lastLeg.getFirstWaypoint().getMostRelevantDateTime());

			LocalDate maxCheckOutDate = checkInDate.plusDays(28);
			checkOutDate = checkOutDate.isAfter(maxCheckOutDate) ? maxCheckOutDate : checkOutDate;

			sp.setCheckOutDate(checkOutDate);
		}
		else {
			//One way trip
			sp.setCheckOutDate(checkInDate.plusDays(1));
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

		NavUtils.goToHotels(getActivity(), sp);

		OmnitureTracking.trackCrossSellFlightToHotel(getActivity());
	}

	//////////////////////////////////////////////////////////////////////////
	// Add insurance

	private void addInsurance(String url) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
		builder.setUrl(url);
		builder.setTheme(R.style.FlightTheme);
		builder.setTitle(R.string.insurance);
		builder.setInjectExpediaCookies(true);
		startActivity(builder.getIntent());
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();

		ShareUtils shareUtils = new ShareUtils(getActivity());
		String subject = shareUtils.getFlightShareSubject(trip);
		String body = shareUtils.getFlightShareEmail(trip, Db.getTravelers());

		SocialUtils.email(getActivity(), subject, body);

		OmnitureTracking.trackFlightConfirmationShareEmail(getActivity());
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private void addToCalendar() {
		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		for (int a = 0; a < trip.getLegCount(); a++) {
			Intent intent = generateCalendarInsertIntent(trip.getLeg(a));
			startActivity(intent);
		}

		OmnitureTracking.trackFlightConfirmationAddToCalendar(getActivity());
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
		intent.putExtra(
				Events.EVENT_LOCATION,
				getString(R.string.calendar_flight_location_TEMPLATE, originAirport.mName,
						StrUtils.getWaypointCityOrCode(origin)));

		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.calendar_flight_desc_itinerary_TEMPLATE, itineraryNumber));
		sb.append("\n\n");
		sb.append(getString(R.string.calendar_flight_desc_directions_TEMPLATE, "https://maps.google.com/maps?q="
				+ origin.mAirportCode));
		sb.append("\n\n");
		sb.append(getString(R.string.calendar_flight_desc_support_TEMPLATE, PointOfSale.getPointOfSale()
				.getSupportPhoneNumber()));
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
