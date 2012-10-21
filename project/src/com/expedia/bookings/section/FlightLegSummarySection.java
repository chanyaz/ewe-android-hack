package com.expedia.bookings.section;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.widget.FlightTripView;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

/**
 * Note: This is somewhat overloaded to be able to represent either an entire
 * leg or just one segment inside of a leg, depending on what data is bound
 * to it.
 */
public class FlightLegSummarySection extends RelativeLayout {

	private static final DecimalFormat sDaySpanFormatter = new DecimalFormat("#");

	static {
		// TODO: Should this be localized in some way?
		sDaySpanFormatter.setPositivePrefix("+");
	}

	private FlightLegSummarySectionListener mListener;

	private ImageView mCancelView;
	private TextView mAirlineTextView;
	private TextView mOperatingCarrierTextView;
	private TextView mPriceTextView;
	private TextView mDepartureTimeTextView;
	private TextView mArrivalTimeTextView;
	private TextView mMultiDayTextView;
	private FlightTripView mFlightTripView;

	public FlightLegSummarySection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Setting a listener implicitly sets this section as deselectable, 
	 * showing the "X" button instead of showing the price.
	 */
	public void setListener(FlightLegSummarySectionListener listener) {
		mListener = listener;
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mCancelView = Ui.findView(this, R.id.cancel_button);
		mAirlineTextView = Ui.findView(this, R.id.airline_text_view);
		mOperatingCarrierTextView = Ui.findView(this, R.id.operating_carrier_text_view);
		mPriceTextView = Ui.findView(this, R.id.price_text_view);
		mDepartureTimeTextView = Ui.findView(this, R.id.departure_time_text_view);
		mArrivalTimeTextView = Ui.findView(this, R.id.arrival_time_text_view);
		mMultiDayTextView = Ui.findView(this, R.id.multi_day_text_view);
		mFlightTripView = Ui.findView(this, R.id.flight_trip_view);
	}

	public void bindFlight(Flight flight, Calendar minTime, Calendar maxTime) {
		// Fake a flight leg
		FlightLeg pseudoLeg = new FlightLeg();
		pseudoLeg.addSegment(flight);

		bind(null, pseudoLeg, minTime, maxTime, true);
	}

	public void bind(FlightTrip trip, FlightLeg leg) {
		bind(trip, leg, null, null, false);
	}

	public void bind(FlightTrip trip, final FlightLeg leg, Calendar minTime, Calendar maxTime) {
		bind(trip, leg, minTime, maxTime, false);
	}

	public void bind(FlightTrip trip, final FlightLeg leg, Calendar minTime, Calendar maxTime,
			boolean isIndividualFlight) {
		Context context = getContext();

		// Don't lie to me!
		Flight firstFlight = leg.getSegment(0);
		if (isIndividualFlight && leg.getSegmentCount() != 1) {
			isIndividualFlight = false;
		}

		if (mAirlineTextView != null) {
			if (isIndividualFlight) {
				mAirlineTextView.setText(FormatUtils.formatFlightNumber(firstFlight, context));
			}
			else {
				mAirlineTextView.setText(leg.getAirlinesFormatted());
			}
		}

		int belowTarget;
		if (isIndividualFlight && !firstFlight.getPrimaryFlightCode().equals(firstFlight.getOperatingFlightCode())) {
			Airline airline = Db.getAirline(firstFlight.getOperatingFlightCode().mAirlineCode);
			mOperatingCarrierTextView.setText(context.getString(R.string.operated_by_TEMPLATE,
					FormatUtils.formatAirline(airline, context)));

			mOperatingCarrierTextView.setVisibility(View.VISIBLE);

			belowTarget = mOperatingCarrierTextView.getId();
		}
		else {
			belowTarget = mAirlineTextView.getId();
		}

		// Adjust rules depending on what we need to be below
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDepartureTimeTextView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);
		params = (RelativeLayout.LayoutParams) mArrivalTimeTextView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);
		params = (RelativeLayout.LayoutParams) mFlightTripView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);

		if (mDepartureTimeTextView != null) {
			mDepartureTimeTextView.setText(formatTime(leg.getFirstWaypoint().getMostRelevantDateTime()));
		}

		if (mArrivalTimeTextView != null) {
			mArrivalTimeTextView.setText(formatTime(leg.getLastWaypoint().getMostRelevantDateTime()));
		}

		int airlineLeftOfId = 0;
		if (mPriceTextView != null) {
			if (trip == null || mListener != null) {
				mPriceTextView.setVisibility(View.GONE);
			}
			else if (trip.hasPricing()) {
				mPriceTextView.setText(trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
				airlineLeftOfId = mPriceTextView.getId();
			}
			else {
				mPriceTextView.setText(null);
			}
		}

		if (mListener != null) {
			mCancelView.setVisibility(View.VISIBLE);
			airlineLeftOfId = mCancelView.getId();
			mCancelView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onDeselect(leg);
				}
			});
		}
		else {
			mCancelView.setVisibility(View.GONE);
		}

		// F794: We need the airline text view to not overlap
		// either the price or the cancel button, whichever one is showing.
		params = (RelativeLayout.LayoutParams) mAirlineTextView.getLayoutParams();
		params.addRule(RelativeLayout.LEFT_OF, airlineLeftOfId);

		int daySpan = leg.getDaySpan();
		if (daySpan != 0) {
			mMultiDayTextView.setVisibility(View.VISIBLE);
			String daySpanStr = sDaySpanFormatter.format(daySpan);
			mMultiDayTextView.setText(getResources().getQuantityString(R.plurals.day_span, daySpan, daySpanStr));
		}
		else {
			mMultiDayTextView.setVisibility(View.INVISIBLE);
		}

		mFlightTripView.setUp(leg, minTime, maxTime);
	}

	// Makes the card invisible - good for laying cards on top of each other
	public void makeInvisible() {
		mAirlineTextView.setVisibility(View.INVISIBLE);
		mPriceTextView.setVisibility(View.INVISIBLE);
		mDepartureTimeTextView.setVisibility(View.INVISIBLE);
		mArrivalTimeTextView.setVisibility(View.INVISIBLE);
		mMultiDayTextView.setVisibility(View.INVISIBLE);
		mFlightTripView.setVisibility(View.INVISIBLE);
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(getContext());
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface FlightLegSummarySectionListener {
		public void onDeselect(FlightLeg flightLeg);
	}
}
