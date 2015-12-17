package com.expedia.bookings.section;

import java.text.DecimalFormat;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightTripView;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
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

	private LinearLayout mAirlineContainer;
	private TextView mAirlineTextView;
	private TextView mAirlineAndCitiesTextView;
	private ImageView mOperatingCarrierImageView;
	private TextView mOperatingCarrierTextView;
	private TextView mPriceTextView;
	private TextView mFlightTimeTextView;
	private TextView mDepartureTimeTextView;
	private TextView mArrivalTimeTextView;
	private TextView mMultiDayTextView;
	private FlightTripView mFlightTripView;

	public FlightLegSummarySection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mAirlineContainer = Ui.findView(this, R.id.airline_container);
		mAirlineTextView = Ui.findView(this, R.id.airline_text_view);
		mAirlineAndCitiesTextView = Ui.findView(this, R.id.airline_and_cities_text_view);
		mOperatingCarrierImageView = Ui.findView(this, R.id.operating_carrier_image_view);
		mOperatingCarrierTextView = Ui.findView(this, R.id.operating_carrier_text_view);
		mPriceTextView = Ui.findView(this, R.id.price_text_view);
		mFlightTimeTextView = Ui.findView(this, R.id.flight_time_text_view);
		mDepartureTimeTextView = Ui.findView(this, R.id.departure_time_text_view);
		mArrivalTimeTextView = Ui.findView(this, R.id.arrival_time_text_view);
		mMultiDayTextView = Ui.findView(this, R.id.multi_day_text_view);
		mFlightTripView = Ui.findView(this, R.id.flight_trip_view);
	}

	public void bindFlight(Flight flight, DateTime minTime, DateTime maxTime) {
		// Fake a flight leg
		FlightLeg pseudoLeg = new FlightLeg();
		pseudoLeg.addSegment(flight);

		bind(null, pseudoLeg, minTime, maxTime, true);
	}

	public void bind(FlightSearch flightSearch, int legNumber) {
		FlightTripQuery query = flightSearch.queryTrips(legNumber);
		DateTime minTime = new DateTime(query.getMinTime());
		DateTime maxTime = new DateTime(query.getMaxTime());

		FlightTripLeg flightTripLeg = flightSearch.getSelectedLegs()[legNumber];

		FlightTrip trip = flightTripLeg.getFlightTrip();
		FlightLeg leg = flightTripLeg.getFlightLeg();

		bind(trip, leg, minTime, maxTime);
	}

	public void bind(FlightTrip trip, FlightLeg leg) {
		bind(trip, leg, null, null, false);
	}

	public void bind(FlightTrip trip, FlightLeg leg, BillingInfo billingInfo,
					 TripBucketItemFlight tripBucketItemFlight) {
		bind(trip, leg, null, null, null, false, billingInfo, tripBucketItemFlight);
	}

	public void bind(FlightTrip trip, final FlightLeg leg, DateTime minTime, DateTime maxTime) {
		bind(trip, leg, minTime, maxTime, false);
	}

	public void bind(FlightTrip trip, final FlightLeg leg, DateTime minTime, DateTime maxTime,
					 boolean isIndividualFlight) {
		bind(trip, leg, null, minTime, maxTime, isIndividualFlight, null, null);
	}

	public void bind(FlightTrip trip, final FlightLeg leg, final FlightLeg legTwo, DateTime minTime,
		DateTime maxTime, boolean isIndividualFlight, BillingInfo billingInfo,
					 TripBucketItemFlight tripBucketItemFlight) {
		Context context = getContext();
		Resources res = getResources();

		// Don't lie to me!
		Flight firstFlight = leg.getSegment(0);
		if (isIndividualFlight && leg.getSegmentCount() != 1) {
			isIndividualFlight = false;
		}

		adjustLayout(leg, isIndividualFlight);

		if (mAirlineTextView != null) {
			mAirlineTextView.setText(getAirlinesStr(context, firstFlight, leg, legTwo, isIndividualFlight));

			if (trip != null) {
				if (trip.hasBagFee()) {
					mAirlineTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						getBagWithXDrawableResId(), 0);
				}
				else {
					mAirlineTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				}
			}
		}

		if (mAirlineAndCitiesTextView != null) {
			SpannableBuilder sb = new SpannableBuilder();
			String airlinesStr = getAirlinesStr(context, firstFlight, leg, legTwo, isIndividualFlight);
			sb.append(airlinesStr, FontCache.getSpan(FontCache.Font.ROBOTO_MEDIUM));

			String depCity = leg.getAirport(true).mCity;
			String arrCity = leg.getAirport(false).mCity;
			String text = " - " + res.getString(R.string.flight_cities_TEMPLATE, depCity, arrCity);
			sb.append(text, FontCache.getSpan(FontCache.Font.ROBOTO_LIGHT));

			mAirlineAndCitiesTextView.setText(sb.build());
		}

		if (mFlightTimeTextView != null && legTwo == null) {
			SpannableStringBuilder builder = new SpannableStringBuilder();

			// 10:05 AM to 2:20 PM
			String departure = formatTime(leg.getFirstWaypoint().getBestSearchDateTime());
			String arrival = formatTime(leg.getLastWaypoint().getBestSearchDateTime());
			String time = res.getString(R.string.date_range_TEMPLATE, departure, arrival);
			builder.append(time);

			mFlightTimeTextView.setText(builder);
		}
		else if (mFlightTimeTextView != null && legTwo != null) {
			mFlightTimeTextView.setText(context.getString(R.string.round_trip));
		}

		if (mDepartureTimeTextView != null) {
			mDepartureTimeTextView.setText(formatTime(leg.getFirstWaypoint().getBestSearchDateTime()));
		}

		if (mArrivalTimeTextView != null) {
			mArrivalTimeTextView.setText(formatTime(leg.getLastWaypoint().getBestSearchDateTime()));
		}

		if (mMultiDayTextView != null) {
			int daySpan = leg.getDaySpan();
			if (daySpan != 0) {
				mMultiDayTextView.setVisibility(View.VISIBLE);
				String daySpanStr = sDaySpanFormatter.format(daySpan);
				mMultiDayTextView.setText(res.getQuantityString(R.plurals.day_span, Math.abs(daySpan), daySpanStr));
			}
			else {
				mMultiDayTextView.setVisibility(View.INVISIBLE);
			}
		}

		if (mPriceTextView != null) {
			if (trip != null && trip.hasPricing()) {
				Money money;
				if (tripBucketItemFlight != null && billingInfo != null) {
					money = trip.getTotalFareWithCardFee(billingInfo, tripBucketItemFlight);
				}
				else {
					money = trip.getAverageTotalFare();
				}
				mPriceTextView.setText(money.getFormattedMoney(Money.F_NO_DECIMAL));
			}
			else {
				mPriceTextView.setVisibility(View.GONE);
			}
		}

		if (legTwo != null) {
			mFlightTripView.setUpRoundTrip(leg, legTwo);
		}
		else {
			mFlightTripView.setUp(leg, minTime, maxTime);
		}
	}

	private static String getAirlinesStr(Context context, Flight flight, FlightLeg leg, FlightLeg legTwo,
										 boolean isIndividualFlight) {
		if (isIndividualFlight) {
			return FormatUtils.formatFlightNumber(flight, context);
		}
		else {
			return getCombinedAirlinesFormatted(leg, legTwo);
		}
	}

	private static String getCombinedAirlinesFormatted(FlightLeg leg, FlightLeg legTwo) {
		if (legTwo != null) {
			String firstAirlines = leg.getPrimaryAirlineNamesFormatted();
			String secondAirlines = legTwo.getPrimaryAirlineNamesFormatted();
			return firstAirlines + ", " + secondAirlines;
		}
		else {
			return leg.getPrimaryAirlineNamesFormatted();
		}
	}

	protected int getBagWithXDrawableResId() {
		return Ui.obtainThemeResID(getContext(), R.attr.skin_icSuitCaseBaggage);
	}

	/**
	 * Perform any layout adjustments on this section, based on the data in this FlightLeg,
	 * whether this is a summary card or an itin card, etc.
	 * <p/>
	 * This is broken into its own method, so that descendent classes can make different
	 * layout adjustments (looking at you, FlightLegSummarySectionTablet).
	 */
	protected void adjustLayout(final FlightLeg leg, boolean isIndividualFlight) {
		Context context = getContext();
		Flight firstFlight = leg.getSegment(0);

		int belowTarget;
		FlightCode opFlightCode = firstFlight.getOperatingFlightCode();
		if (isIndividualFlight && !firstFlight.getPrimaryFlightCode().equals(opFlightCode)) {
			String operatedBy;
			if (!TextUtils.isEmpty(opFlightCode.mAirlineCode)) {
				Airline airline = Db.getAirline(opFlightCode.mAirlineCode);
				operatedBy = FormatUtils.formatAirline(airline, context);
			}
			else {
				operatedBy = opFlightCode.mAirlineName;
			}

			mOperatingCarrierTextView.setText(context.getString(R.string.operated_by_TEMPLATE, operatedBy));

			mOperatingCarrierImageView.setVisibility(View.VISIBLE);
			mOperatingCarrierTextView.setVisibility(View.VISIBLE);

			belowTarget = mOperatingCarrierTextView.getId();
		}
		else {
			// section_flight_leg_summary.xml and section_flight_leg_summary_itin.xml are similar but different layouts...
			// The container has first order of precedence if present for proper laying out on section_flight_leg_summary
			// section_flight_leg_summary_itin.xml does not require the container, so default to the TextView in that case
			if (mAirlineContainer != null) {
				belowTarget = mAirlineContainer.getId();
			}
			else if (mAirlineAndCitiesTextView != null) {
				belowTarget = mAirlineAndCitiesTextView.getId();
			}
			else {
				belowTarget = mAirlineTextView.getId();
			}
		}

		// Adjust rules depending on what we need to be below
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDepartureTimeTextView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);
		params = (RelativeLayout.LayoutParams) mArrivalTimeTextView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);
		params = (RelativeLayout.LayoutParams) mFlightTripView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);

		if (mMultiDayTextView != null) {
			// 104 - modify card size depending on whether or not this is string is displayed to prevent overlap
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMultiDayTextView.getLayoutParams();
			Resources res = getResources();
			int rightMargin = res.getDimensionPixelSize(R.dimen.flight_leg_day_textview_margin_right);
			int topMargin = res.getDimensionPixelSize(leg.getDaySpan() == 0
				? R.dimen.flight_leg_day_textview_margin_top
				: R.dimen.flight_leg_day_textview_margin_top_no_overlap);
			lp.setMargins(0, topMargin, rightMargin, 0);
		}
	}

	private String formatTime(DateTime cal) {
		String dateFormat = DateTimeUtils.getDeviceTimeFormat(getContext());
		return JodaUtils.format(cal, dateFormat);
	}
}
