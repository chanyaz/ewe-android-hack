package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.data.trips.ItinShareInfo.ItinSharable;
import com.expedia.bookings.utils.FlightClickAbleSpan;
import com.google.android.gms.maps.model.LatLng;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

public class ItinCardDataFlight extends ItinCardData implements ConfirmationNumberable, ItinSharable {

	private int mLegNumber;
	private DateTime mEndDate;
	private DateTime mStartDate;
	private boolean mShowAirAttach;
	private FlightLeg mNextFlightLeg;

	public ItinCardDataFlight(TripFlight parent, int leg) {
		super(parent);

		mLegNumber = leg;

		setId(parent.getUniqueId() + ":" + mLegNumber);
	}


	public DateTime getTripStartDate() {
		return (getTripComponent()).getParentTrip().getStartDate();
	}

	public DateTime getTripEndDate() {
		return (getTripComponent()).getParentTrip().getEndDate();
	}

	public String getBaggageInfoUrl() {
		return ((TripFlight) getTripComponent()).getAdditionalAirlineFees();
	}
	public int getLegCount() {
		return ((TripFlight) getTripComponent()) .getFlightTrip().getLegCount();
	}

	public boolean isSplitTicket() {
		return ((TripFlight) getTripComponent()) .getFlightTrip().isSplitTicket();
	}

	public String getOrderNumber() {
		return ((TripFlight) getTripComponent()).getOrderNumber();
	}

	public int getLegNumber() {
		return mLegNumber;
	}

	public FlightLeg getFlightLeg() {
		return ((TripFlight) getTripComponent()).getFlightTrip().getLeg(mLegNumber);
	}

	// the most relevant flight segment is the currently-active segment if it has not yet landed,
	// otherwise, it is the next segment to take off
	// UNLESS there is a later flight that has been canceled
	public Flight getMostRelevantFlightSegment() {
		DateTime now = DateTime.now();
		List<Flight> segments = getFlightLeg().getSegments();
		Flight relevantSegment = null;
		for (Flight segment : segments) {
			if (relevantSegment == null) {
				if (segment.getOriginWaypoint().getMostRelevantDateTime().isAfter(now)
					|| segment.getArrivalWaypoint().getMostRelevantDateTime().isAfter(now)) {
					relevantSegment = segment;
				}
			}
			else if (Flight.STATUS_CANCELLED.equals(segment.mStatusCode)) {
				return segment;
			}
		}

		if (relevantSegment != null) {
			return relevantSegment;
		}
		else {
			return segments.get(segments.size() - 1);
		}
	}

	@Override
	public DateTime getStartDate() {
		if (mStartDate == null) {
			DateTime startCal = getStartCalFromFlightLeg();
			if (startCal != null) {
				mStartDate = new DateTime(startCal);
			}
			else {
				return super.getStartDate();
			}
		}

		return mStartDate;
	}

	@Override
	public DateTime getEndDate() {
		if (mEndDate == null) {
			DateTime endCal = getEndCalFromFlightLeg();
			if (endCal != null) {
				mEndDate = new DateTime(endCal);
			}
			else {
				return super.getEndDate();
			}
		}

		return mEndDate;
	}

	@Override
	public boolean hasConfirmationNumber() {
		return getTripComponent() != null && ((TripFlight) getTripComponent()).getConfirmations() != null
			&& ((TripFlight) getTripComponent()).getConfirmations().size() > 0;
	}

	@Override
	public String getFormattedConfirmationNumbers() {
		List<FlightConfirmation> confirmationNumbers = ((TripFlight) getTripComponent()).getConfirmations();
		if (confirmationNumbers != null) {
			return TextUtils.join(",  ", confirmationNumbers.toArray());
		}
		return null;
	}

	private List<String> getSpannedListOfConfirmationNumbers() {
		if (hasConfirmationNumber() && getConfirmationStatus() == TicketingStatus.COMPLETE
			&& (getTripComponent()).getBookingStatus() == BookingStatus.BOOKED) {
			List<FlightConfirmation> confirmationNumbers = ((TripFlight) getTripComponent()).getConfirmations();
			List<String> list = new ArrayList<>();
			for (FlightConfirmation confirmation : confirmationNumbers) {
				list.add(confirmation.getConfirmationCode());
			}
			return list;
		}
		return null;
	}

	@Override
	public int getConfirmationNumberLabelResId() {
		return R.string.flight_confirmation_code_label;
	}

	@Override
	public LatLng getLocation() {
		long now = DateTime.now().getMillis();
		Flight flight = getMostRelevantFlightSegment();
		Waypoint waypoint = flight.getOriginWaypoint().getMostRelevantDateTime().getMillis() > now ?
			flight.getOriginWaypoint() :
			flight.getArrivalWaypoint();
		Airport airport = waypoint.getAirport();

		if (airport != null) {
			return new LatLng(airport.getLatitude(), airport.getLongitude());
		}

		return super.getLocation();
	}

	// Don't trust FlightStats' stats.  Just go off of start/end time.
	public boolean isEnRoute() {
		DateTime now = DateTime.now();
		DateTime start = getStartCalFromFlightLeg();
		DateTime end = getEndCalFromFlightLeg();
		if (start != null && end != null) {
			return now.isAfter(start) && now.isBefore(end);
		}
		else {
			return false;
		}
	}

	private DateTime getStartCalFromFlightLeg() {
		FlightLeg leg = getFlightLeg();
		if (leg != null && leg.getFirstWaypoint() != null) {
			return leg.getFirstWaypoint().getMostRelevantDateTime();
		}
		return null;
	}

	private DateTime getEndCalFromFlightLeg() {
		FlightLeg leg = getFlightLeg();
		if (leg != null && leg.getLastWaypoint() != null) {
			return leg.getLastWaypoint().getMostRelevantDateTime();
		}
		return null;
	}

	public boolean showAirAttach() {
		return mShowAirAttach;
	}

	public void setShowAirAttach(boolean show) {
		mShowAirAttach = show;
	}

	public FlightLeg getNextFlightLeg() {
		return mNextFlightLeg;
	}

	public void setNextFlightLeg(FlightLeg nextLeg) {
		mNextFlightLeg = nextLeg;
	}

	public TicketingStatus getConfirmationStatus() {
		return ((TripFlight) getTripComponent()).getTicketingStatus();
	}

	@Override
	public String getSharableDetailsUrl() {
		if (getFlightLeg().getShareInfo().hasSharableUrl()) {
			return getFlightLeg().getShareInfo().getSharableUrl();
		}
		return super.getSharableDetailsUrl();
	}

	@Override
	public ItinShareInfo getShareInfo() {
		return getFlightLeg().getShareInfo();
	}

	@Override
	public boolean getSharingEnabled() {
		return true;
	}

	public String getTravelerFirstAndLastNames() {
		List<Traveler> travelerList = ((TripFlight) getTripComponent()).getTravelers();
		List<String> stringList = new ArrayList();
		for (Traveler traveler : travelerList) {
			stringList.add(traveler.getFirstAndLastName());
		}
		return TextUtils.join(", ", stringList);
	}

	public CharSequence getSpannedConfirmationNumbers(Context context) {
		if (getSpannedListOfConfirmationNumbers() != null) {
			List<String> confirmationNumbers = getSpannedListOfConfirmationNumbers();
			SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(getSpannedString(context, confirmationNumbers.get(0)));
			for (int i = 1; i < confirmationNumbers.size(); i++) {
				spannableBuilder.append(" , ");
				spannableBuilder.append(getSpannedString(context, confirmationNumbers.get(i)));
			}
			return spannableBuilder;
		}
		return "";
	}

	@NonNull
	private SpannableString getSpannedString(Context context, String confirmationNumber) {
		FlightClickAbleSpan flightClickAble = new FlightClickAbleSpan(context);
		SpannableString confirmation = new SpannableString(confirmationNumber);
		confirmation.setSpan(flightClickAble, 0, confirmation.length(), 0);
		return confirmation;
	}
}
