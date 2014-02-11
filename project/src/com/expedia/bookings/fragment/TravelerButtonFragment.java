package com.expedia.bookings.fragment;

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
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.model.TravelerFlowState;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

public class TravelerButtonFragment extends LobableFragment {

	public static TravelerButtonFragment newInstance(LineOfBusiness lob, int dbTravelerNumber) {
		TravelerButtonFragment frag = new TravelerButtonFragment();
		frag.setLob(lob);
		frag.setTravelerNumber(dbTravelerNumber);
		return frag;
	}

	private static final String STATE_TRAVELER_NUMBER = "STATE_TRAVELER_NUMBER";

	private int mTravelerNumber = -1;
	private SectionTravelerInfo mSectionTraveler;
	private ViewGroup mTravelerSectionContainer;
	private ViewGroup mEmptyViewContainer;

	private boolean mShowValidMarker = false;

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
		Traveler trav = getDbTraveler();
		if (trav != null) {
			if (getLob() == LineOfBusiness.FLIGHTS) {
				//TODO: THIS WAS BLOWING UP, BUT WE SHOULD NOT BE ASSUMING DOMESTIC
				boolean international = Db.getFlightSearch() != null
					&& Db.getFlightSearch().getSelectedFlightTrip() != null
					? Db.getFlightSearch().getSelectedFlightTrip().isInternational()
					: false;
				return TravelerFlowState.getInstance(getActivity()).allTravelerInfoValid(trav, international);
			}
			else {
				return HotelTravelerFlowState.getInstance(getActivity()).hasValidTraveler(trav);
			}
		}
		return false;
	}

	private void setShowValidMarker(boolean showMarker, boolean valid) {

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
			travSec = (SectionTravelerInfo) inflater.inflate(R.layout.section_display_traveler_info_btn, null);
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

	public View addEmptyTravelerToLayout(ViewGroup group) {
		if (getLob() == LineOfBusiness.FLIGHTS) {
			View v = View.inflate(getActivity(), R.layout.snippet_booking_overview_traveler, group);
			TextView tv = Ui.findView(v, R.id.traveler_empty_text_view);
			if (Db.getTravelers().size() == 1) {
				tv.setText(R.string.traveler_details);
			}
			else {
				tv.setText(getString(R.string.add_traveler_number_TEMPLATE, mTravelerNumber + 1)); // no zero index for users
			}
			return v;
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			return View.inflate(getActivity(), R.layout.snippet_booking_overview_traveler, group);
		}
		return null;
	}

	//TODO: WE NEED TO FLESH THESE OUT

	public View addValidIndicator(ViewGroup group) {
		return null;
	}

	public View addInvalidIndicator(ViewGroup group) {
		return null;
	}

}
