package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.section.SectionBillingInfo;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutPaymentFormFragment extends Fragment {

	private static final String STATE_LOB = "STATE_LOB";

	private ViewGroup mRootC;
	private ViewGroup mFormContentC;
	private LineOfBusiness mLob = LineOfBusiness.HOTELS;

	private SectionBillingInfo mSectionBillingInfo;

	public static TabletCheckoutPaymentFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutPaymentFormFragment frag = new TabletCheckoutPaymentFormFragment();
		frag.setLob(lob);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LOB)) {
				LineOfBusiness lob = LineOfBusiness.valueOf(savedInstanceState.getString(STATE_LOB));
				if (lob != null) {
					setLob(lob);
				}
			}
		}

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_payment_form, container, false);
		mFormContentC = Ui.findView(mRootC, R.id.content_container);

		setUpFormContent();

		return mRootC;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_LOB, mLob.name());
	}

	public void setLob(LineOfBusiness lob) {
		if (lob != mLob) {
			mLob = lob;
			if (mFormContentC != null) {
				setUpFormContent();
			}
		}

	}

	public void bindToDb() {
		if (mSectionBillingInfo != null) {
			mSectionBillingInfo.bind(Db.getBillingInfo());
		}
	}

	private void setUpFormContent() {
		//This will probably end up having way more moving parts than this...
		mFormContentC.removeAllViews();
		if (mLob == LineOfBusiness.HOTELS) {
			mSectionBillingInfo = (SectionBillingInfo) View.inflate(getActivity(),
					R.layout.section_hotel_edit_creditcard, null);
		}
		else if (mLob == LineOfBusiness.FLIGHTS) {
			mSectionBillingInfo = (SectionBillingInfo) View.inflate(getActivity(), R.layout.section_edit_creditcard,
					null);
		}
		mFormContentC.addView(mSectionBillingInfo);
	}
}
