package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity.Validatable;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.Ui;

public class HotelTravelerInfoOneFragment extends Fragment implements Validatable {

	Traveler mTraveler;
	SectionTravelerInfo mSectionTravelerInfo;

	boolean mAttemptToLeaveMade = false;

	public static HotelTravelerInfoOneFragment newInstance() {
		return new HotelTravelerInfoOneFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
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
					mSectionTravelerInfo.performValidation();
				}
			}
		});

		mSectionTravelerInfo.addInvalidCharacterListener(new InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
			}
		});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsTravelerEditInfo(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		mTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		mSectionTravelerInfo.bind(mTraveler);

		View focused = this.getView().findFocus();
		if (focused == null || !(focused instanceof EditText)) {
			int firstFocusResId = R.id.edit_first_name;
			if (PointOfSale.getPointOfSale().showLastNameFirst()) {
				firstFocusResId = R.id.edit_last_name;
			}
			focused = Ui.findView(mSectionTravelerInfo, firstFocusResId);
		}
		if (focused != null && focused instanceof EditText) {
			FocusViewRunnable.focusView(this, focused);
		}
	}

	@Override
	public boolean validate() {
		mAttemptToLeaveMade = true;
		return mSectionTravelerInfo != null ? mSectionTravelerInfo.performValidation() : false;
	}
}
