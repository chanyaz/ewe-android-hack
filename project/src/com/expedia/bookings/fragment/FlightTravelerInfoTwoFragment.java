package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightTravelerInfoOptionsActivity.Validatable;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class FlightTravelerInfoTwoFragment extends Fragment implements Validatable {

	Traveler mTraveler;
	SectionTravelerInfo mSectionTravelerInfo;
	int mTravelerIndex = -1;

	boolean mAttemptToLeaveMade = false;

	public static FlightTravelerInfoTwoFragment newInstance() {
		FlightTravelerInfoTwoFragment fragment = new FlightTravelerInfoTwoFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			OmnitureTracking.trackPageLoadFlightTravelerEditDetails(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_traveler_info_step2, container, false);
		mAttemptToLeaveMade = false;
		mSectionTravelerInfo = Ui.findView(v, R.id.traveler_info);

		mTravelerIndex = getActivity().getIntent().getIntExtra(Codes.TRAVELER_INDEX, -1);
		if (mTravelerIndex >= 0) {
			mTraveler = Db.getTravelers().get(mTravelerIndex);
		}

		//We don't need to show redress number field if we aren't passing through the US
		if (!Db.getFlightSearch().getSelectedFlightTrip().passesThroughCountry("US")) {
			View redressEntry = Ui.findView(mSectionTravelerInfo, R.id.edit_redress_number);
			if (redressEntry != null) {
				redressEntry.setVisibility(View.GONE);
			}
		}

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionTravelerInfo.hasValidInput();
				}
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		mSectionTravelerInfo.bind(mTraveler);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean validate() {
		mAttemptToLeaveMade = true;
		return mSectionTravelerInfo != null ? mSectionTravelerInfo.hasValidInput() : false;
	}
}
