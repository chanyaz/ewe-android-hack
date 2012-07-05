package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Calendar;

import com.actionbarsherlock.app.SherlockActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionDisplayAddress;
import com.expedia.bookings.section.SectionDisplayCreditCard;
import com.expedia.bookings.section.SectionEditCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class FlightCheckoutActivity extends SherlockActivity {

	public static final String EXTRA_TRIP_KEY = "EXTRA_TRIP_KEY";

	FlightTrip mTrip;
	BillingInfo mBillingInfo;

	ArrayList<SectionTravelerInfo> mTravelerSections = new ArrayList<SectionTravelerInfo>();

	SectionDisplayCreditCard mCreditCardSection;
	SectionDisplayCreditCard mCreditCardSectionButton;
	SectionEditCreditCard mCreditCardSecurityCodeSection;
	SectionDisplayAddress mAddressSection;

	Button mReviewBtn;

	ViewGroup mExpandedPaymentContainer;
	ViewGroup mTravelerContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_checkout);

		mBillingInfo = Db.getBillingInfo();
		mBillingInfo.load(this);

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mCreditCardSectionButton = Ui.findView(this, R.id.creditcard_section_button);
		mCreditCardSection = Ui.findView(this, R.id.creditcard_section);
		mCreditCardSecurityCodeSection = Ui.findView(this, R.id.creditcard_section_security_code);
		mExpandedPaymentContainer = Ui.findView(this, R.id.payment_container_expanded);
		mTravelerContainer = Ui.findView(this, R.id.travelers_container);
		mAddressSection = Ui.findView(this, R.id.address_section);
		mReviewBtn = Ui.findView(this, R.id.review_btn);

		showExpandedPaymentView(false);

		mCreditCardSectionButton.setOnClickListener(creditCardDispatcher);

		mCreditCardSection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent editContact = new Intent(FlightCheckoutActivity.this, FlightPaymentCreditCardActivity.class);
				startActivity(editContact);
			}
		});

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
			String startMonthStr = DateUtils.getMonthString(startCal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
			int startDay = startCal.get(Calendar.DAY_OF_MONTH);
			String startDate = startMonthStr + " " + startDay;

			Flight lastSeg = lastLeg.getSegment(lastLeg.getSegmentCount() - 1);
			Calendar endCal = lastSeg.mDestination.getMostRelevantDateTime();
			String endMonthStr = DateUtils.getMonthString(endCal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
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

		mReviewBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBillingInfo.save(FlightCheckoutActivity.this);
				Intent intent = new Intent(FlightCheckoutActivity.this, FlightBookingActivity.class);
				startActivity(intent);
			}
		});

		ArrayList<FlightPassenger> passengers = Db.getFlightPassengers();
		if (passengers == null) {
			passengers = new ArrayList<FlightPassenger>();
			Db.setFlightPassengers(passengers);
		}

		if (passengers.size() == 0) {
			//TODO:Load from billing info, or from expedia account
			passengers.add(new FlightPassenger());
		}

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < passengers.size(); i++) {
			final int travelerNum = i;
			SectionTravelerInfo traveler = (SectionTravelerInfo) inflater.inflate(
					R.layout.section_display_traveler_info_btn, null);
			traveler.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent editTravelerIntent = new Intent(FlightCheckoutActivity.this,
							FlightTravelerInfoOneActivity.class);
					editTravelerIntent.putExtra(FlightTravelerInfoOneActivity.PASSENGER_INDEX, travelerNum);
					startActivity(editTravelerIntent);
				}
			});
			mTravelerSections.add(traveler);
			mTravelerContainer.addView(traveler);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		bindAll();
	}

	public void bindAll() {
		mCreditCardSection.bind(mBillingInfo);
		mAddressSection.bind(mBillingInfo.getLocation());
		mCreditCardSecurityCodeSection.bind(mBillingInfo);

		ArrayList<FlightPassenger> passengers = Db.getFlightPassengers();
		if (passengers.size() != mTravelerSections.size()) {
			Ui.showToast(this, "Traveler info out of date...");
			Log.e("Traveler info fail... passengers size():" + passengers.size() + " sections:"
					+ mTravelerSections.size());
		}
		else {
			for (int i = 0; i < passengers.size(); i++) {
				mTravelerSections.get(i).bind(passengers.get(i));
			}
		}
	}

	private void showExpandedPaymentView(boolean show) {
		//Here be validation!
		if (show) {
			mCreditCardSectionButton.setVisibility(View.GONE);
			mExpandedPaymentContainer.setVisibility(View.VISIBLE);
		}
		else {
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
			mExpandedPaymentContainer.setVisibility(View.GONE);
		}
	}

	private OnClickListener creditCardDispatcher = new OnClickListener() {
		boolean showing = false;

		@Override
		public void onClick(View v) {
			showing = !showing;
			showExpandedPaymentView(showing);
		}
	};

}