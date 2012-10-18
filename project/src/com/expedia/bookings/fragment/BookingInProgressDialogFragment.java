package com.expedia.bookings.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

import com.expedia.bookings.R;

public class BookingInProgressDialogFragment extends DialogFragment {

	public static final String TAG = BookingInProgressDialogFragment.class.getName();

	public static BookingInProgressDialogFragment newInstance() {
		BookingInProgressDialogFragment fragment = new BookingInProgressDialogFragment();
		fragment.setCancelable(false);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(getString(R.string.booking_loading));
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// Prevent all attempts to cancel by consuming all key events
				return true;
			}
		});
		return pd;
	}
}