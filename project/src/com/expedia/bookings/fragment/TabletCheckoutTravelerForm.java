package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutTravelerForm extends Fragment {

	private ViewGroup mRootC;

	private LineOfBusiness mLob = LineOfBusiness.HOTELS;
	private int mTravelerNumber = 0;

	public static TabletCheckoutTravelerForm newInstance() {
		TabletCheckoutTravelerForm frag = new TabletCheckoutTravelerForm();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_traveler_form, container, false);
		return mRootC;
	}

	public void bindToDb(int travelerNumber, LineOfBusiness lob) {
		mLob = lob;

		//TODO: Bind stuff with Db.getTravelers().get(travelerNumber);
	}
}
