package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutPaymentFormFragment extends TabletCheckoutDataFormFragment {

	public static TabletCheckoutPaymentFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutPaymentFormFragment frag = new TabletCheckoutPaymentFormFragment();
		frag.setLob(lob);
		return frag;
	}

	SectionBillingInfo mSectionBillingInfo;
	private ICheckoutDataListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}

	public void bindToDb() {
		if (mSectionBillingInfo != null) {
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(Db.getBillingInfo());
			mSectionBillingInfo.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
		}
		setTopLeftText(getString(R.string.payment_method));
		setTopRightText(getString(R.string.done));
		setTopRightTextOnClick(mTopRightClickListener);
	}

	private OnClickListener mTopRightClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
			mListener.onCheckoutDataUpdated();
			getActivity().onBackPressed();
		}
	};

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
