package com.expedia.bookings.fragment;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Waypoint;

// We can assume that if this fragment loaded we successfully booked, so most
// data we need to grab is available.
public class FlightConfirmationFragment extends Fragment {

	public static final String TAG = FlightConfirmationFragment.class.getName();

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

		float initialOffset = res.getDimension(R.dimen.flight_card_initial_offset);
		float offset = res.getDimension(R.dimen.flight_card_overlap_offset);
		int numLegs = trip.getLegCount();
		for (int a = 0; a < numLegs; a++) {
			FlightLegSummarySection card = (FlightLegSummarySection) inflater.inflate(
					R.layout.section_flight_leg_summary, cardContainer, false);

			// Each card is offset below the last one
			MarginLayoutParams params = (MarginLayoutParams) card.getLayoutParams();
			params.topMargin = (int) (initialOffset + Math.round(offset * a));

			// Set a custom bg
			card.setBackgroundResource(R.drawable.bg_flight_row);

			// Bind data
			if (a + 1 == numLegs) {
				card.bind(trip, trip.getLeg(a));
			}
			if (a + 1 != numLegs) {
				card.bind(null, trip.getLeg(a));

				// We can't arbitrarily make views more transparent until API 11,
				// which is what actually looks best.  So before that, we just
				// make everything in the card invisible (so it doesn't look like
				// some overlapping mess).
				if (Build.VERSION.SDK_INT >= 11) {
					card.setAlpha(.5f);
				}
				else {
					card.makeInvisible();
				}
			}

			// Add card to view
			cardContainer.addView(card);
		}

		// Fill out all the actions
		Ui.setText(v, R.id.going_to_text_view,
				getString(R.string.yay_going_somewhere_TEMPLATE, destinationCity));

		Ui.setText(v, R.id.itinerary_text_view,
				Html.fromHtml(getString(R.string.itinerary_confirmation_TEMPLATE, itinerary.getItineraryNumber(),
						Db.getBillingInfo().getEmail())));

		Ui.setText(v, R.id.hotels_action_text_view, getString(R.string.hotels_in_TEMPLATE, destinationCity));
		Ui.setOnClickListener(v, R.id.hotels_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ui.showToast(getActivity(), "TODO: Search for hotels");
			}
		});

		Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ui.showToast(getActivity(), "TODO: Share booking");
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

		Ui.setOnClickListener(v, R.id.flighttrack_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ui.showToast(getActivity(), "TODO: Track on FT");
			}
		});

		Ui.setOnClickListener(v, R.id.call_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ui.showToast(getActivity(), "TODO: Call Expedia");
			}
		});

		// We need to capitalize in code because the all_caps field isn't until a later API
		Ui.setText(v, R.id.get_a_room_text_view, getString(R.string.get_a_room).toUpperCase());
		Ui.setText(v, R.id.more_actions_text_view, getString(R.string.more_actions).toUpperCase());

		return v;
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
		sb.append(getString(R.string.calendar_flight_desc_directions_TEMPLATE, "http://url-to-directions.com"));
		sb.append("\n\n");
		sb.append(getString(R.string.calendar_flight_desc_support_TEMPLATE, "<insert phone # here, once we know it>"));
		sb.append("\n\n");
		intent.putExtra(Events.DESCRIPTION, sb.toString());
		return intent;
	}
}
