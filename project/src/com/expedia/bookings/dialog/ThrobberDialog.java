package com.expedia.bookings.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;

public class ThrobberDialog extends DialogFragment {
	private CharSequence mMessage;

	private CancelListener mCancelListener;

	public interface CancelListener {
		public void onCancel();
	}

	private static String ARG_MESSAGE = "ARG_MESSAGE";

	public static ThrobberDialog newInstance(CharSequence message) {
		ThrobberDialog dialog = new ThrobberDialog();
		dialog.setCancelable(true);
		Bundle args = new Bundle();
		args.putCharSequence(ARG_MESSAGE, message);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog pd = new ProgressDialog(getActivity());
		Bundle args = getArguments();
		if (args.containsKey(ARG_MESSAGE)) {
			pd.setMessage(args.getCharSequence(ARG_MESSAGE));
		}
		pd.setCanceledOnTouchOutside(false);
		pd.setIndeterminateDrawable(getResources().getDrawable(R.drawable.abs__progress_medium_holo));
		return pd;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (mCancelListener != null) {
			mCancelListener.onCancel();
		}
		else if (getActivity() != null) {
			getActivity().finish();
		}
	}

	public void setCancelListener(CancelListener listener) {
		mCancelListener = listener;
	}

	public void setText(CharSequence text) {
		ProgressDialog pd = (ProgressDialog) this.getDialog();
		if (pd != null) {
			pd.setMessage(text);
		}
		getArguments().putCharSequence(ARG_MESSAGE, text);
	}
}
