package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutTravelerFormFragment extends TabletCheckoutDataFormFragment {

	private static final String STATE_TRAVELER_NUMBER = "STATE_TRAVELER_NUMBER";

	private int mTravelerNumber = 0;
	private SectionTravelerInfo mSectionTraveler;

	public static TabletCheckoutTravelerFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutTravelerFormFragment frag = new TabletCheckoutTravelerFormFragment();
		frag.setLob(lob);
		return frag;
	}

	private ICheckoutDataListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTravelerNumber = savedInstanceState.getInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
	}

	public void bindToDb(int travelerNumber) {
		mTravelerNumber = travelerNumber;
		if (mSectionTraveler != null && travelerNumber >= 0 && travelerNumber < Db.getTravelers().size()) {
			Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(travelerNumber));
			mSectionTraveler.bind(Db.getWorkingTravelerManager().getWorkingTraveler());
			setTopLeftText(getString(R.string.traveler_num_and_category_TEMPLATE, travelerNumber + 1));
			setTopRightText(getString(R.string.done));
			setTopRightTextOnClick(mTopRightClickListener);
		}
	}

	private OnClickListener mTopRightClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Db.getWorkingTravelerManager().commitWorkingTravelerToDB(mTravelerNumber, getActivity());
			mListener.onCheckoutDataUpdated();
			getActivity().onBackPressed();
		}
	};

	protected void setUpFormContent(ViewGroup formContainer) {
		//This will probably end up having way more moving parts than this...
		formContainer.removeAllViews();
		if (getLob() == LineOfBusiness.HOTELS) {
			mSectionTraveler = (SectionTravelerInfo) View.inflate(getActivity(),
					R.layout.section_hotel_edit_traveler_pt1, null);
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mSectionTraveler = (SectionTravelerInfo) View.inflate(getActivity(), R.layout.section_edit_traveler_pt1,
					null);
		}
		formContainer.addView(mSectionTraveler);
	}
}
