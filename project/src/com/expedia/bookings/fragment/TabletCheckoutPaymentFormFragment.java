package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.section.SectionBillingInfo;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutPaymentFormFragment extends TabletCheckoutDataFormFragment {

	SectionBillingInfo mSectionBillingInfo;

	public static TabletCheckoutPaymentFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutPaymentFormFragment frag = new TabletCheckoutPaymentFormFragment();
		frag.setLob(lob);
		return frag;
	}

	public void bindToDb() {
		if (mSectionBillingInfo != null) {
			mSectionBillingInfo.bind(Db.getBillingInfo());
		}
		setTopLeftText(getString(R.string.payment_method));
		setTopRightText(getString(R.string.done));
	}

	protected void setUpFormContent(ViewGroup formContainer) {
		//This will probably end up having way more moving parts than this...
		formContainer.removeAllViews();
		if (getLob() == LineOfBusiness.HOTELS) {
			mSectionBillingInfo = (SectionBillingInfo) View.inflate(getActivity(),
					R.layout.section_hotel_edit_creditcard, null);
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mSectionBillingInfo = (SectionBillingInfo) View.inflate(getActivity(), R.layout.section_edit_creditcard,
					null);
		}
		formContainer.addView(mSectionBillingInfo);
	}
}
