package com.expedia.bookings.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;

public class BookingInProgressDialogFragment extends DialogFragment {

	public static final String TAG = BookingInProgressDialogFragment.class.getName();

	public static BookingInProgressDialogFragment newInstance() {
		BookingInProgressDialogFragment fragment = new BookingInProgressDialogFragment();
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(getString(R.string.booking_loading));
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(false);
		return pd;
	}
}