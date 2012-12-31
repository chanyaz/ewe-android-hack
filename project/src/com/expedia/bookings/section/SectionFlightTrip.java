package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightTrip;

public class SectionFlightTrip extends LinearLayout implements ISection<FlightTrip> {

	ArrayList<SectionField<?, FlightTrip>> mFields = new ArrayList<SectionField<?, FlightTrip>>();

	FlightTrip mTrip;

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
		mFields.add(mTravelerTotal);
		mFields.add(mTravelerBaseFare);
		mFields.add(mTravelerTaxes);
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

	SectionField<TextView, FlightTrip> mTravelerTotal = new SectionField<TextView, FlightTrip>(R.id.traveler_total) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getBaseFare() != null) ? data.getTotalFare().getFormattedMoneyPerTraveler() : "");
		}
	};

	SectionField<TextView, FlightTrip> mTravelerBaseFare = new SectionField<TextView, FlightTrip>(
			R.id.traveler_base_fare) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getBaseFare() != null) ? data.getBaseFare().getFormattedMoneyPerTraveler() : "");
		}
	};

	SectionField<TextView, FlightTrip> mTravelerTaxes = new SectionField<TextView, FlightTrip>(
			R.id.traveler_taxes) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getTaxes() != null) ? data.getTaxes().getFormattedMoneyPerTraveler() : "");
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

}
