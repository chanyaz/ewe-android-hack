package com.expedia.bookings.activity;


import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoOptionsActivity extends SherlockFragmentActivity {
	View mOverviewBtn;
	View mEnterManuallyBtn;
	View mInternationalDivider;

	TextView mEditTravelerLabel;
	View mEditTravelerLabelDiv;
	TextView mSelectTravelerLabel;
	View mSelectTravelerLabelDiv;
	ViewGroup mEditTravelerContainer;
	ViewGroup mAssociatedTravelersContainer;

	int mCurrentPassengerIndex;
	FlightPassenger mCurrentPassenger;

	SectionTravelerInfo mPassengerContact;
	SectionTravelerInfo mPassengerPrefs;
	SectionTravelerInfo mPassengerPassportCountry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_traveler_info_options);

		mCurrentPassengerIndex = getIntent().getIntExtra(Codes.PASSENGER_INDEX, 0);

		mEditTravelerContainer = Ui.findView(this, R.id.edit_traveler_container);
		mEditTravelerLabel = Ui.findView(this, R.id.edit_traveler_label);
		mEditTravelerLabelDiv = Ui.findView(this, R.id.edit_traveler_label_div);
		mSelectTravelerLabel = Ui.findView(this, R.id.select_traveler_label);
		mSelectTravelerLabelDiv = Ui.findView(this, R.id.select_traveler_label_div);
		mAssociatedTravelersContainer = Ui.findView(this, R.id.associated_travelers_container);
		mInternationalDivider = Ui.findView(this, R.id.current_traveler_passport_country_divider);

		mEnterManuallyBtn = Ui.findView(this, R.id.enter_info_manually_button);
		mEnterManuallyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Db.getFlightPassengers().set(mCurrentPassengerIndex, new FlightPassenger());
				gotoFirstDataEntryPage();
			}
		});

		//Associated Travelers (From Expedia Account)
		mAssociatedTravelersContainer.removeAllViews();
		if (User.isLoggedIn(this)) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			Resources res = getResources();
			for (int i = 0; i < Db.getUser().getAssociatedTravelers().size(); i++) {
				final FlightPassenger passenger = Db.getUser().getAssociatedTravelers().get(i);
				SectionTravelerInfo travelerInfo = (SectionTravelerInfo) inflater.inflate(
						R.layout.section_display_traveler_info_name, null);
				travelerInfo.bind(passenger);
				travelerInfo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCurrentPassenger = passenger;
						Db.getFlightPassengers().set(mCurrentPassengerIndex, passenger);
						//TODO: In the future we hope that stored travelers will have all the traveler data required
						//At that time we will not need to go to the entry pages at all
						gotoFirstDataEntryPage();
					}
				});

				mAssociatedTravelersContainer.addView(travelerInfo);

				//Add divider
				View divider = new View(this);
				LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_grey_divider_height));
				divLayoutParams.setMargins(0, res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_top), 0,
						res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_bottom));
				divider.setLayoutParams(divLayoutParams);
				divider.setBackgroundColor(res.getColor(R.color.divider_grey));
				mAssociatedTravelersContainer.addView(divider);
			}
		}

		//Selected traveler
		mCurrentPassenger = Db.getFlightPassengers().get(mCurrentPassengerIndex);

		mPassengerContact = Ui.findView(this, R.id.current_traveler_contact);
		mPassengerPrefs = Ui.findView(this, R.id.current_traveler_prefs);
		mPassengerPassportCountry = Ui.findView(this, R.id.current_traveler_passport_country);

		mPassengerContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this,
						FlightTravelerInfoOneActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightTravelerInfoOptionsActivity.class);
				intent.putExtra(Codes.PASSENGER_INDEX, mCurrentPassengerIndex);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});

		mPassengerPrefs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this,
						FlightTravelerInfoTwoActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightTravelerInfoOptionsActivity.class);
				intent.putExtra(Codes.PASSENGER_INDEX, mCurrentPassengerIndex);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});

		mPassengerPassportCountry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this,
						FlightTravelerInfoThreeActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightTravelerInfoOptionsActivity.class);
				intent.putExtra(Codes.PASSENGER_INDEX, mCurrentPassengerIndex);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		refreshCurrentPassenger();
	}

	public void refreshCurrentPassenger() {
		if (!mCurrentPassenger.hasName()) {
			mEditTravelerContainer.setVisibility(View.GONE);
			mEditTravelerLabel.setVisibility(View.GONE);
			mSelectTravelerLabel.setText(getString(R.string.select_a_traveler));
		}
		else {
			mEditTravelerContainer.setVisibility(View.VISIBLE);
			mEditTravelerLabel.setVisibility(View.VISIBLE);
			mSelectTravelerLabel.setText(getString(R.string.select_a_different_traveler));
			if (Db.getFlightSearch().getSelectedFlightTrip().isInternational()) {
				mInternationalDivider.setVisibility(View.VISIBLE);
				mPassengerPassportCountry.setVisibility(View.VISIBLE);
			}
			else {
				mInternationalDivider.setVisibility(View.GONE);
				mPassengerPassportCountry.setVisibility(View.GONE);
			}
		}
		
		mEditTravelerLabelDiv.setVisibility(mEditTravelerLabel.getVisibility());
		mSelectTravelerLabelDiv.setVisibility(mSelectTravelerLabel.getVisibility());

		mPassengerContact.bind(mCurrentPassenger);
		mPassengerPrefs.bind(mCurrentPassenger);
		mPassengerPassportCountry.bind(mCurrentPassenger);
	}


	protected void gotoFirstDataEntryPage() {
		Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this, FlightTravelerInfoOneActivity.class);
		YoYo yoyo = new YoYo();
		yoyo.addYoYoTrick(FlightTravelerInfoTwoActivity.class);
		if (Db.getFlightSearch().getSelectedFlightTrip().isInternational()) {
			yoyo.addYoYoTrick(FlightTravelerInfoThreeActivity.class);
		}
		yoyo.addYoYoTrick(FlightCheckoutActivity.class);
		intent.putExtra(Codes.PASSENGER_INDEX, mCurrentPassengerIndex);
		intent.putExtra(YoYo.TAG_YOYO, yoyo);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_done, menu);
		menu.findItem(R.id.menu_yoyo).getActionView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this, FlightCheckoutActivity.class);
				startActivity(intent);
			}		
		});
		return true;
	}

}
