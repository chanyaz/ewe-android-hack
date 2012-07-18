package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.mobiata.flightlib.data.Flight;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionFlightTrip extends LinearLayout implements ISection<FlightTrip> {

	ArrayList<SectionField<?, FlightTrip>> mFields = new ArrayList<SectionField<?, FlightTrip>>();

	FlightTrip mTrip;
	boolean mIsOutbound = false;

	Context mContext;

	public SectionFlightTrip(Context context) {
		super(context);
		init(context);
	}

	public SectionFlightTrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionFlightTrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		//Display fields
		mFields.add(this.mDisplaySubTotal);
		mFields.add(mDisplayTaxesAndFees);
		mFields.add(mTotalPrice);
		mFields.add(mDestinationCity);
		mFields.add(mTripDuration);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, FlightTrip> field : mFields) {
			field.bindField(this);
		}
	}

	@Override
	public void bind(FlightTrip trip) {
		//Update fields
		mTrip = trip;

		if (mTrip != null) {
			for (SectionField<?, FlightTrip> field : mFields) {
				field.bindData(mTrip);
			}
		}
	}

	//////////////////////////////////////
	////// DISPLAY FIELDS
	//////////////////////////////////////

	SectionField<TextView, FlightTrip> mDisplaySubTotal = new SectionField<TextView, FlightTrip>(
			R.id.display_subtotal) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getBaseFare() != null) ? data.getBaseFare().getFormattedMoney() : "");
		}
	};

	SectionField<TextView, FlightTrip> mDisplayTaxesAndFees = new SectionField<TextView, FlightTrip>(
			R.id.display_taxes_and_fees) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getTaxes() != null) ? data.getTaxes().getFormattedMoney() : "");
		}
	};

	SectionField<TextView, FlightTrip> mTotalPrice = new SectionField<TextView, FlightTrip>(
			R.id.display_total_price) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getTotalFare() != null) ? data.getTotalFare().getFormattedMoney() : "");
		}
	};

	SectionField<TextView, FlightTrip> mTripDuration = new SectionField<TextView, FlightTrip>(
			R.id.display_trip_duration) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			//TODO: More error checking...
			FlightLeg firstLeg = data.getLeg(0);
			FlightLeg lastLeg = data.getLeg(data.getLegCount() - 1);

			Flight firstSeg = firstLeg.getSegment(0);
			Calendar startCal = firstSeg.mOrigin.getMostRelevantDateTime();

			Flight lastSeg = lastLeg.getSegment(lastLeg.getSegmentCount() - 1);
			Calendar endCal = lastSeg.mDestination.getMostRelevantDateTime();

			String durationStr = DateUtils.formatDateRange(mContext, startCal.getTimeInMillis(),
					endCal.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);

			field.setText(durationStr);

		}
	};

	SectionField<TextView, FlightTrip> mDestinationCity = new SectionField<TextView, FlightTrip>(
			R.id.display_destination_city) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			//TODO: More error checking...
			FlightLeg firstLeg = data.getLeg(0);
			String cityName = firstLeg.getSegment(firstLeg.getSegmentCount() - 1).mDestination.getAirport().mCity;
			field.setText(cityName);
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

}
