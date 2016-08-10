package com.expedia.bookings.section;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.pos.PointOfSale;

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
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mFields.bindFieldsAll(this);

		TextView totalPriceLabel = (TextView) findViewById(R.id.total_price_label);
		totalPriceLabel.setText(PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ? R.string.total_price_min_label : R.string.total_price_label);
		TextView taxesFeesLabel = (TextView) findViewById(R.id.taxes_fees_label);
		taxesFeesLabel.setText(PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() ? R.string.includes_taxes : R.string.includes_taxes_and_fees);
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
	 * @param billingInfo - the billingInfo which will be used in conjunction with FlowState to validate the card
	 */
	public void bind(final FlightTrip trip, BillingInfo billingInfo) {
		mBillingInfo = billingInfo;
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
				if (mTrip.showFareWithCardFee(mContext, mBillingInfo) && Db.getTripBucket().getFlight() != null) {
					text = data.getTotalFareWithCardFee(mBillingInfo, Db.getTripBucket().getFlight()).getFormattedMoney();
				}
				else {
					text = data.getTotalPrice().getFormattedMoney();
				}
			}
			field.setText(text);
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

}
