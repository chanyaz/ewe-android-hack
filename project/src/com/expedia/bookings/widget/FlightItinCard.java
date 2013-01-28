package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.mobiata.flightlib.data.Waypoint;

public class FlightItinCard extends ItinCard {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private FlightTrip mFlightTrip;
	private Waypoint mDestination;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public FlightItinCard(Context context) {
		this(context, null);
	}

	public FlightItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_flight;
	}

	@Override
	public Type getType() {
		return Type.FLIGHT;
	}

	@Override
	public void bind(TripComponent tripComponent) {
		mFlightTrip = ((TripFlight) tripComponent).getFlightTrip();

		if (mFlightTrip != null && mFlightTrip.getLegCount() > 0) {
			mDestination = mFlightTrip.getLeg(mFlightTrip.getLegCount() - 1).getLastWaypoint();
			super.bind(tripComponent);
		}
	}

	@Override
	protected String getHeaderImageUrl(TripComponent tripComponent) {
		return ((TripFlight) tripComponent).getDestinationImageUrl();
	}

	@Override
	protected String getHeaderText(TripComponent tripComponent) {
		if (mDestination != null) {
			return mDestination.getAirport().mCity;
		}

		return "Flight Card";
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		View view = inflater.inflate(R.layout.include_itin_card_flight, container, false);

		return view;
	}
}