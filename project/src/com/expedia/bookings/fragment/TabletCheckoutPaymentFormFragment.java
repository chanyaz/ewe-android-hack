package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutPaymentFormFragment extends TabletCheckoutDataFormFragment {

	public static TabletCheckoutPaymentFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutPaymentFormFragment frag = new TabletCheckoutPaymentFormFragment();
		frag.setLob(lob);
		return frag;
	}

	SectionBillingInfo mSectionBillingInfo;
	SectionLocation mSectionLocation;
	private ICheckoutDataListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}

	@Override
	public void onResume() {
		super.onResume();
		bindToDb();
	}

	public void bindToDb() {
		if (mSectionBillingInfo != null) {
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(Db.getBillingInfo());
			mSectionBillingInfo.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
		}
		setHeadingText(getString(R.string.payment_method));
		setHeadingButtonText(getString(R.string.done));
		setHeadingButtonOnClick(mTopRightClickListener);

		if (mSectionLocation != null) {
			mSectionLocation.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getLocation());
		}

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
		mSectionBillingInfo.setLineOfBusiness(getLob());
		formContainer.addView(mSectionBillingInfo);

		//TODO: REMOVE OR REARRANGE OR WHATEVER, WE WONT PROBABLY BE HAVING ONE FORM AFTER THE OTHER
		mSectionLocation = (SectionLocation) View.inflate(getActivity(), R.layout.section_edit_address, null);
		formContainer.addView(mSectionLocation);

	}

	@Override
	protected void onFormClosed() {

	}
}
