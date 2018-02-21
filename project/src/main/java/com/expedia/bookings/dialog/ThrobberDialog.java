package com.expedia.bookings.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.DeprecatedProgressDialog;

public class ThrobberDialog extends DialogFragment {

	public static final String TAG = ThrobberDialog.class.getName();

	private CancelListener mCancelListener;

	public interface CancelListener {
		void onCancel();
	}

	private static final String ARG_MESSAGE = "ARG_MESSAGE";

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
		DeprecatedProgressDialog pd = new DeprecatedProgressDialog(getActivity());
		Bundle args = getArguments();
		if (args.containsKey(ARG_MESSAGE)) {
			pd.setMessage(args.getCharSequence(ARG_MESSAGE));
		}
		pd.setCanceledOnTouchOutside(false);
		pd.setIndeterminateDrawable(getResources().getDrawable(R.drawable.abs__progress_medium_holo));
		return pd;
	}

	@Override
	public void onCancel(DialogInterface iDialog) {
		super.onCancel(iDialog);

		if (mCancelListener != null) {
			mCancelListener.onCancel();
		}
		else if (getActivity() != null) {
			getActivity().finish();
		}
	}


	/*
	THIS FIXES A BUG IN THE COMPAT LIB THAT DESTROYS THE DIALOG ON ROTATION
	http://stackoverflow.com/questions/14657490/how-to-properly-retain-a-dialogfragment-through-rotation
	 */
	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

	public void setCancelListener(CancelListener listener) {
		mCancelListener = listener;
	}

	public void setText(CharSequence text) {
		DeprecatedProgressDialog pd = (DeprecatedProgressDialog) this.getDialog();
		if (pd != null) {
			pd.setMessage(text);
		}
		getArguments().putCharSequence(ARG_MESSAGE, text);
	}
}
