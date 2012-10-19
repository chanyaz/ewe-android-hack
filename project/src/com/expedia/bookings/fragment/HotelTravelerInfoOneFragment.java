package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity.Validatable;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionHotelTravelerInfo;
import com.expedia.bookings.utils.Ui;

public class HotelTravelerInfoOneFragment extends Fragment implements Validatable {

	Traveler mTraveler;
	SectionHotelTravelerInfo mSectionTravelerInfo;
	int mTravelerIndex = -1;

	boolean mAttemptToLeaveMade = false;

	public static HotelTravelerInfoOneFragment newInstance() {
		HotelTravelerInfoOneFragment fragment = new HotelTravelerInfoOneFragment();
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
		View v = inflater.inflate(R.layout.fragment_hotel_traveler_info_step1, container, false);
		mAttemptToLeaveMade = false;
		mSectionTravelerInfo = Ui.findView(v, R.id.traveler_info);

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionTravelerInfo.hasValidInput();
				}
				//We attempt a save on change
				Db.getWorkingTravelerManager().attemptWorkingTravelerSave(getActivity(), false);
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
		mTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		mSectionTravelerInfo.bind(mTraveler);

		View focused = this.getView().findFocus();
		if (focused == null || !(focused instanceof EditText)) {
			focused = Ui.findView(mSectionTravelerInfo, R.id.edit_first_name);
		}
		final View finalFocused = focused;
		if (finalFocused != null && finalFocused instanceof EditText) {
			finalFocused.postDelayed(new Runnable() {
				@Override
				public void run() {
					//Dumb but effective - show the keyboard by emulating a click on the view
					finalFocused.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
					finalFocused.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
				}
			}, 200);
		}
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
