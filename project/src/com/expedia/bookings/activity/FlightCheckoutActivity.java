package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionDisplayAddress;
import com.expedia.bookings.section.SectionDisplayContactInfo;
import com.expedia.bookings.section.SectionDisplayCreditCard;
import com.expedia.bookings.section.SectionEditCreditCard;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlightCheckoutActivity extends SherlockActivity {

	public static final String EXTRA_TRIP_KEY = "EXTRA_TRIP_KEY";

	FlightTrip mTrip;
	BillingInfo mBillingInfo;

	SectionDisplayContactInfo mContactSection;
	SectionDisplayCreditCard mCreditCardSection;
	SectionEditCreditCard mCreditCardSecurityCodeSection;
	SectionDisplayAddress mAddressSection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_checkout);

		mBillingInfo = Db.getBillingInfo();
		//mBillingInfo.load(this);

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mCreditCardSection = Ui.findView(this, R.id.creditcard_section);
		mCreditCardSection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent editContact = new Intent(FlightCheckoutActivity.this, FlightPaymentCreditCardActivity.class);
				startActivity(editContact);
			}
		});

		mCreditCardSecurityCodeSection = Ui.findView(this, R.id.creditcard_section_security_code);

		mContactSection = Ui.findView(this, R.id.contact_info_section);
		mContactSection.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent editContact = new Intent(FlightCheckoutActivity.this, FlightPaymentContactActivity.class);
				startActivity(editContact);
			}
		});

		mAddressSection = Ui.findView(this, R.id.address_section);
		mAddressSection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent editAddress = new Intent(FlightCheckoutActivity.this, FlightPaymentAddressActivity.class);
				startActivity(editAddress);
			}
		});

		String tripKey = getIntent().getStringExtra(EXTRA_TRIP_KEY);
		if (tripKey != null) {
			mTrip = Db.getFlightSearch().getFlightTrip(tripKey);

			FlightLeg firstLeg = mTrip.getLeg(0);
			FlightLeg lastLeg = mTrip.getLeg(mTrip.getLegCount() - 1);

			String cityName = firstLeg.getSegment(firstLeg.getSegmentCount() - 1).mDestination.getAirport().mCity;

			Flight firstSeg = firstLeg.getSegment(0);
			Calendar startCal = firstSeg.mOrigin.getMostRelevantDateTime();
			String startMonthStr = startCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
			int startDay = startCal.get(Calendar.DAY_OF_MONTH);
			String startDate = startMonthStr + " " + startDay;

			Flight lastSeg = lastLeg.getSegment(lastLeg.getSegmentCount() - 1);
			Calendar endCal = lastSeg.mDestination.getMostRelevantDateTime();
			String endMonthStr = endCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
			int endDay = endCal.get(Calendar.DAY_OF_MONTH);
			String endDate = "";

			if (startMonthStr.compareTo(endMonthStr) == 0) {
				//Same month
				endDate = "" + endDay;
			}
			else {
				//Dif months
				endDate = endMonthStr + " " + endDay;
			}

			Ui.setText(this, R.id.city_and_dates, String.format(
					getResources().getString(R.string.checkout_heading_city_and_dates_TEMPLATE), cityName, startDate,
					endDate));
		}

		Button reviewBtn = Ui.findView(this, R.id.review_btn);
		reviewBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightCheckoutActivity.this, FlightBookingActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		bindAll();
	}

	public void bindAll() {
		mCreditCardSection.bind(mBillingInfo);
		mContactSection.bind(mBillingInfo);
		mAddressSection.bind(mBillingInfo.getLocation());
		mCreditCardSecurityCodeSection.bind(mBillingInfo);
	}

}