package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

public class TravelerButtonFragment extends LobableFragment {

	public static TravelerButtonFragment newInstance(LineOfBusiness lob, int dbTravelerNumber) {
		TravelerButtonFragment frag = new TravelerButtonFragment();
		frag.setLob(lob);
		frag.setTravelerNumber(dbTravelerNumber);
		return frag;
	}

	public interface ITravelerIsValidProvider {
		public boolean travelerIsValid(int travelerNumber);
	}

	private static final String STATE_TRAVELER_NUMBER = "STATE_TRAVELER_NUMBER";

	private int mTravelerNumber = -1;
	private SectionTravelerInfo mSectionTraveler;
	private ViewGroup mTravelerSectionContainer;
	private ViewGroup mEmptyViewContainer;
	private String mEmptyViewLabel;
	private ITravelerIsValidProvider mValidationProvider;

	private boolean mShowValidMarker = false;

	public void setEnabled(boolean enable) {
		mTravelerSectionContainer.setEnabled(enable);
		mEmptyViewContainer.setEnabled(enable);
		mSectionTraveler.setEnabled(enable);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mValidationProvider = Ui.findFragmentListener(this, ITravelerIsValidProvider.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTravelerNumber = savedInstanceState.getInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
		}

		View rootView = inflater.inflate(R.layout.fragment_checkout_traveler_button, null);
		mTravelerSectionContainer = Ui.findView(rootView, R.id.traveler_section_container);
		mEmptyViewContainer = Ui.findView(rootView, R.id.empty_traveler_container);

		mSectionTraveler = addTravelerSectionToLayout(mTravelerSectionContainer);
		addEmptyTravelerToLayout(mEmptyViewContainer);

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
	}

	public void enableShowValidMarker(boolean enabled) {
		mShowValidMarker = enabled;
	}

	public void setTravelerNumber(int travelerNumber) {
		mTravelerNumber = travelerNumber;
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		//We do everything at bind time
	}

	public void bindToDb() {
		if (mSectionTraveler != null && hasDbTraveler()) {
			if (isValid()) {
				//Valid traveler
				mSectionTraveler.bind(getDbTraveler());
				setShowTravelerView(true);
				setShowValidMarker(mShowValidMarker, true);

			}
			else if (isPartiallyFilled()) {
				//Partially filled button
				mSectionTraveler.bind(getDbTraveler());
				setShowTravelerView(true);
				setShowValidMarker(mShowValidMarker, false);
			}
			else {
				//Empty button
				setShowTravelerView(false);
				setShowValidMarker(mShowValidMarker, false);
			}
		}
	}

	public boolean hasDbTraveler() {
		return Db.getTravelers() != null && mTravelerNumber >= 0 && mTravelerNumber < Db.getTravelers().size();
	}

	public Traveler getDbTraveler() {
		if (hasDbTraveler()) {
			return Db.getTravelers().get(mTravelerNumber);
		}
		return null;
	}

	public boolean isPartiallyFilled() {
		Traveler trav = getDbTraveler();
		if (trav != null) {
			return trav.hasName();
		}
		return false;
	}

	public boolean isValid() {
		return mValidationProvider.travelerIsValid(mTravelerNumber);
	}

	private void setShowValidMarker(boolean showMarker, boolean valid) {
		int visibility = showMarker && valid ? View.VISIBLE : View.GONE;
		Ui.findView(mTravelerSectionContainer, R.id.validation_checkmark).setVisibility(visibility);
	}

	private void setShowTravelerView(boolean showTraveler) {
		if (showTraveler) {
			mEmptyViewContainer.setVisibility(View.GONE);
			mTravelerSectionContainer.setVisibility(View.VISIBLE);
		}
		else {
			mEmptyViewContainer.setVisibility(View.VISIBLE);
			mTravelerSectionContainer.setVisibility(View.GONE);
		}
	}

	/*
	 * THE ACTUAL DISPLAY VIEWS, NOTE THAT THESE ARE EASY TO OVERRIDE
	 */

	public SectionTravelerInfo addTravelerSectionToLayout(ViewGroup group) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		SectionTravelerInfo travSec = null;
		if (getLob() == LineOfBusiness.FLIGHTS) {
			travSec = (SectionTravelerInfo) inflater.inflate(R.layout.section_flight_display_traveler_info_btn, null);
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			travSec = (SectionTravelerInfo) inflater.inflate(
				R.layout.section_hotel_display_traveler_info_btn,
				null);
		}
		if (travSec != null) {
			group.addView(travSec);
		}
		return travSec;
	}

	public void setEmptyViewLabel(String label) {
		mEmptyViewLabel = label;
	}

	public String getEmptyViewLabel() {
		return mEmptyViewLabel;
	}

	public View addEmptyTravelerToLayout(ViewGroup group) {
		View v = View.inflate(getActivity(), R.layout.snippet_booking_overview_traveler, group);
		TextView tv = Ui.findView(v, R.id.traveler_empty_text_view);
		tv.setText(mEmptyViewLabel);
		return v;
	}

}
