package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.utils.Ui;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FlightTravelerInfoOptionsFragment extends Fragment {
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
	Traveler mCurrentPassenger;

	SectionTravelerInfo mPassengerContact;
	SectionTravelerInfo mPassengerPrefs;
	SectionTravelerInfo mPassengerPassportCountry;

	TravelerInfoYoYoListener mListener;

	public static FlightTravelerInfoOptionsFragment newInstance() {
		FlightTravelerInfoOptionsFragment fragment = new FlightTravelerInfoOptionsFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_traveler_info_options, container, false);

		mCurrentPassengerIndex = getActivity().getIntent().getIntExtra(Codes.PASSENGER_INDEX, 0);

		mEditTravelerContainer = Ui.findView(v, R.id.edit_traveler_container);
		mEditTravelerLabel = Ui.findView(v, R.id.edit_traveler_label);
		mEditTravelerLabelDiv = Ui.findView(v, R.id.edit_traveler_label_div);
		mSelectTravelerLabel = Ui.findView(v, R.id.select_traveler_label);
		mSelectTravelerLabelDiv = Ui.findView(v, R.id.select_traveler_label_div);
		mAssociatedTravelersContainer = Ui.findView(v, R.id.associated_travelers_container);
		mInternationalDivider = Ui.findView(v, R.id.current_traveler_passport_country_divider);

		mEnterManuallyBtn = Ui.findView(v, R.id.enter_info_manually_button);
		mEnterManuallyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Db.getFlightPassengers().set(mCurrentPassengerIndex, new Traveler());
				mListener.setMode(YoYoMode.YOYO);
				mListener.displayTravelerEntryOne();
			}
		});

		//Associated Travelers (From Expedia Account)
		mAssociatedTravelersContainer.removeAllViews();
		if (User.isLoggedIn(getActivity())) {
			Resources res = getResources();
			for (int i = 0; i < Db.getUser().getAssociatedTravelers().size(); i++) {
				final Traveler passenger = Db.getUser().getAssociatedTravelers().get(i);
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
						mListener.setMode(YoYoMode.YOYO);
						mListener.displayTravelerEntryOne();
					}
				});

				mAssociatedTravelersContainer.addView(travelerInfo);

				//Add divider
				View divider = new View(getActivity());
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

		mPassengerContact = Ui.findView(v, R.id.current_traveler_contact);
		mPassengerPrefs = Ui.findView(v, R.id.current_traveler_prefs);
		mPassengerPassportCountry = Ui.findView(v, R.id.current_traveler_passport_country);

		mPassengerContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.setMode(YoYoMode.EDIT);
				mListener.displayTravelerEntryOne();
			}
		});

		mPassengerPrefs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.setMode(YoYoMode.EDIT);
				mListener.displayTravelerEntryTwo();
			}
		});

		mPassengerPassportCountry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.setMode(YoYoMode.EDIT);
				mListener.displayTravelerEntryThree();
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof TravelerInfoYoYoListener)) {
			throw new RuntimeException(
					"FlightTravelerInfoOptiosnFragment activity must implement TravelerInfoYoYoListener!");
		}

		mListener = (TravelerInfoYoYoListener) activity;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshCurrentPassenger();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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

	public interface TravelerInfoYoYoListener {
		public void moveForward();

		public void setMode(YoYoMode mode);

		public boolean moveBackwards();

		public void displayOptions();

		public void displayTravelerEntryOne();

		public void displayTravelerEntryTwo();

		public void displayTravelerEntryThree();

		public void displaySaveDialog();

		public void displayCheckout();
	}

}
