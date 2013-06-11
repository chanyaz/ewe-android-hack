package com.expedia.bookings.section;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;

public class SectionFlightTrip extends LinearLayout implements ISection<FlightTrip> {
	SectionFieldList<FlightTrip> mFields = new SectionFieldList<FlightTrip>();

	FlightTrip mTrip;
	private BillingInfo mBillingInfo;

	Context mContext;

	public SectionFlightTrip(Context context) {
		super(context);
		init(context);
	}

	public SectionFlightTrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@SuppressLint("NewApi")
	public SectionFlightTrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		//Display fields
		mFields.add(mTripTotal);
		mFields.add(mTravelerTotal);
		mFields.add(mTravelerBaseFare);
		mFields.add(mTravelerTaxes);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mFields.bindFieldsAll(this);
	}

	@Override
	public void bind(FlightTrip trip) {
		//Update fields
		mTrip = trip;
		if (mTrip != null) {
			mFields.bindDataAll(mTrip);
		}
	}

	/**
	 * A special bind method that is used to display the price with LCC fees.
	 * @param trip - the FlightTrip to bind the view to
	 * @param billingInfo - the billingInfo which is used in conjunction with FlowState to validate the card
	 */
	public void bind(FlightTrip trip, BillingInfo billingInfo) {
		if (billingInfo == null) {
			trip.setShowFareWithCardFee(false);
		}
		else {
			if (trip.showFareWithCardFee(mContext, Db.getBillingInfo())) {
				mBillingInfo = billingInfo;
			}
		}

		bind(trip);
	}

	//////////////////////////////////////
	////// DISPLAY FIELDS
	//////////////////////////////////////

	SectionField<TextView, FlightTrip> mTripTotal = new SectionField<TextView, FlightTrip>(R.id.trip_total) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTrip data) {
			String text;
			if (data.getBaseFare() == null) {
				text = "";
			}
			else {
				if (mTrip.showFareWithCardFee(mContext, mBillingInfo)) {
					text = data.getTotalFareWithCardFee(mBillingInfo).getFormattedMoney();
				}
				else {
					text = data.getTotalFare().getFormattedMoney();
				}
			}
			field.setText(text);
		}
	};

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
