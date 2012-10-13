package com.expedia.bookings.fragment;

import com.expedia.bookings.R;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class OverviewPriceChangeFailureDialogFragment extends DialogFragment {

	public interface IPriceChangeFailureDialogListener {
		public void priceChangeFailureCancel();

		public void priceChangeFailureRetry();
	}

	private IPriceChangeFailureDialogListener mListener;

	public static OverviewPriceChangeFailureDialogFragment newInstance(IPriceChangeFailureDialogListener listener) {
		OverviewPriceChangeFailureDialogFragment fragment = new OverviewPriceChangeFailureDialogFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		fragment.setListener(listener);
		fragment.setCancelable(false);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.dialog_price_check_failure_title);
		builder.setMessage(R.string.dialog_price_check_failure_message);
		builder.setPositiveButton(R.string.try_again, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				OverviewPriceChangeFailureDialogFragment.this.dismiss();
				getFragmentManager().beginTransaction().remove(OverviewPriceChangeFailureDialogFragment.this).commit();
				mListener.priceChangeFailureRetry();
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.priceChangeFailureCancel();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mListener.priceChangeFailureCancel();
			}
		});

		return builder.create();
	}

	public void setListener(IPriceChangeFailureDialogListener listener) {
		mListener = listener;
	}
}
