package com.expedia.bookings.section;

import org.joda.time.DateTime;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;

public class FlightInfoBarSection extends LinearLayout {

	private static final int SHOW_URGENCY_CUTOFF = 5;

	private TextView mLeftTextView;
	private TextView mRightTextView;

	public FlightInfoBarSection(Context context) {
		super(context);
	}

	public FlightInfoBarSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FlightInfoBarSection(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mLeftTextView = Ui.findView(this, R.id.left_text_view);
		mRightTextView = Ui.findView(this, R.id.right_text_view);

		// Configure font
		Typeface font = FontCache.getTypeface(Font.ROBOTO_LIGHT);
		mLeftTextView.setTypeface(font);
		mRightTextView.setTypeface(font);
	}

	public void bindFlightDetails(FlightTrip trip, FlightLeg leg) {
		Context context = getContext();

		// Bind left label (distance)
		String duration = FlightUtils.formatDuration(context, leg);
		int distanceInMiles = leg.getDistanceInMiles();
		if (distanceInMiles <= 0) {
			mLeftTextView.setText(Html.fromHtml(context.getString(R.string.bold_template, duration)));
		}
		else {
			String distance = FlightUtils.formatDistance(context, leg, false);
			mLeftTextView.setText(Html.fromHtml(context.getString(R.string.time_distance_TEMPLATE, duration,
				distance)));
		}

		// Bind right label (booking price)
		String fare = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ? trip.getAverageTotalFare().getFormattedMoney(
			Money.F_NO_DECIMAL) : trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL);
		int seatsRemaining = trip.getSeatsRemaining();
		if (!TextUtils.isEmpty(trip.getFareName())) {
			int templateID = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ?
					R.string.fare_name_and_price_min_TEMPLATE :
					R.string.fare_name_and_price_TEMPLATE;
			String fareStr = getResources().getString(templateID, trip.getFareName(), fare);
			mRightTextView.setText(fareStr);
		}
		else if (seatsRemaining > 0 && seatsRemaining <= SHOW_URGENCY_CUTOFF) {
			int templateID = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ?
					R.plurals.urgency_book_min_TEMPLATE : R.plurals.urgency_book_TEMPLATE;
			String urgencyStr = getResources().getQuantityString(templateID, seatsRemaining,
					seatsRemaining, fare);
			mRightTextView.setText(Html.fromHtml(urgencyStr));
		}
		else {
			int bookNowResId;
			if (trip.getLegCount() == 1) {
				bookNowResId = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ?
						R.string.one_way_price_min_TEMPLATE : R.string.one_way_price_TEMPLATE;
			}
			else {
				if (trip.getLeg(0).equals(leg)) {
					bookNowResId = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ?
							R.string.round_trip_price_min_TEMPLATE :
							R.string.round_trip_price_TEMPLATE;
				}
				else {
					bookNowResId = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ?
							R.string.book_now_price_min_TEMPLATE : R.string.book_now_price_TEMPLATE;
				}
			}
			mRightTextView.setText(Html.fromHtml(context.getString(bookNowResId, fare)));
		}
	}

	public void bindTripOverview(FlightTrip trip, int numTravelers) {
		// Bind left label (trip dates)
		DateTime depDate = trip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime().toLocalDateTime().toDateTime();
		DateTime retDate = trip.getLeg(trip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime()
			.toLocalDateTime().toDateTime();

		long start = depDate == null ? 0 : depDate.getMillis();
		long end = retDate == null ? 0 : retDate.getMillis();

		String dateRange = DateUtils.formatDateRange(getContext(), start, end,
			DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY
				| DateUtils.FORMAT_ABBREV_MONTH);
		mLeftTextView.setText(dateRange);

		// Bind right label (# travelers)
		mRightTextView.setText(getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE,
			numTravelers, numTravelers));
	}
}
