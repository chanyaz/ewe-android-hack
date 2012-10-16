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
		mFields.add(mDisplayTaxes);
		mFields.add(mTotalPrice);
		mFields.add(mDisplayFees);
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

	SectionField<TextView, FlightTrip> mDisplayTaxes = new SectionField<TextView, FlightTrip>(
			R.id.display_taxes) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getTaxes() != null) ? data.getTaxes().getFormattedMoney() : "");
		}
	};
	
	SectionField<TextView, FlightTrip> mDisplayFees = new SectionField<TextView, FlightTrip>(
			R.id.display_fees) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getFees() != null) ? data.getFees().getFormattedMoney() : "");
		}
	};

	SectionField<TextView, FlightTrip> mTotalPrice = new SectionField<TextView, FlightTrip>(
			R.id.display_total_price) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			field.setText((data.getTotalFare() != null) ? data.getTotalFare().getFormattedMoney() : "");
		}
	};
	
	
	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

}
