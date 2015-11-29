package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightTravelerInfoOptionsActivity.Validatable;
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

public class FlightTravelerInfoOneFragment extends Fragment implements Validatable {

	Traveler mTraveler;
	SectionTravelerInfo mSectionTravelerInfo;

	// EB48: This warning has this behavior:
	// 1. Always shown whenever the user enters the screen.
	// 2. Disappears on *any* interaction (touch or type)
	private TextView mNameMatchWarningTextView;

	boolean mAttemptToLeaveMade = false;

	private static final String ARG_TRAVELER_INDEX = "ARG_TRAVELER_INDEX";

	public static FlightTravelerInfoOneFragment newInstance(int travelerIndex) {
		Bundle args = new Bundle();
		args.putInt(ARG_TRAVELER_INDEX, travelerIndex);
		FlightTravelerInfoOneFragment frag = new FlightTravelerInfoOneFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mAttemptToLeaveMade = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_traveler_info_step1, container, false);
		mAttemptToLeaveMade = false;
		mSectionTravelerInfo = Ui.findView(v, R.id.traveler_info);
		mNameMatchWarningTextView = Ui.findView(v, R.id.name_match_warning_text_view);

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionTravelerInfo.performValidation();
				}

				onInteraction();
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
		OmnitureTracking.trackPageLoadFlightTravelerEditInfo();
	}

	@Override
	public void onResume() {
		super.onResume();
		mTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		mSectionTravelerInfo.bind(mTraveler, Db.getTripBucket().getFlight().getFlightSearchParams());
		mSectionTravelerInfo.setPhoneFieldsEnabled(getArguments().getInt(ARG_TRAVELER_INDEX));

		// Need to do this after bind() so it isn't hidden through the onChange() listener
		mNameMatchWarningTextView.setVisibility(View.VISIBLE);

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

	public boolean isBirthdateAligned() {
		return mSectionTravelerInfo.isBirthdateAligned();
	}

	public void onInteraction() {
		if (getActivity() != null && mNameMatchWarningTextView.getVisibility() != View.GONE) {
			mNameMatchWarningTextView.setVisibility(View.GONE);
		}
	}
}
