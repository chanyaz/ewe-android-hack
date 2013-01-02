package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.FlightPaymentOptionsFragment.FlightPaymentYoYoListener;

public class FlightPaymentSaveDialogFragment extends DialogFragment {

	FlightPaymentYoYoListener mListener;

	public static FlightPaymentSaveDialogFragment newInstance() {
		FlightPaymentSaveDialogFragment frag = new FlightPaymentSaveDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		//frag.setCancelable(false);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.save_billing_info)
				.setMessage(R.string.save_billing_info_message)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setSaveCardToExpediaAccount(true);
						mListener.moveForward();
					}
				})
				.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setSaveCardToExpediaAccount(false);
						mListener.moveForward();
					}
				}).create();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightPaymentYoYoListener)) {
			throw new RuntimeException(
					"FlightPaymentSaveDialogFragment activity must implement FlightPaymentYoYoListener!");
		}

		mListener = (FlightPaymentYoYoListener) activity;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (mListener != null) {
			mListener.moveBackwards();
		}
	}
}
