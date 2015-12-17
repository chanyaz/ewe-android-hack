package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class TextViewDialog extends DialogFragment {

	private static final String INSTANCE_MESSAGE_RES_ID = "INSTANCE_MESSAGE_RES_ID";
	private static final String INSTANCE_MESSAGE_TEXT = "INSTANCE_MESSAGE_TEXT";

	public interface OnDismissListener {
		void onDismissed();
	}

	private int mMessageId = 0;
	private CharSequence mMessage;
	private OnDismissListener mDismissListener;
	private boolean mCancelOnTouchOutside = true;

	public void setMessage(int stringId) {
		mMessageId = stringId;
		mMessage = null;
	}

	public void setMessage(CharSequence message) {
		mMessageId = 0;
		mMessage = message;
	}

	public void setOnDismissListener(OnDismissListener listener) {
		mDismissListener = listener;
	}

	public void setCanceledOnTouchOutside(boolean cancel) {
		mCancelOnTouchOutside = cancel;
	}

	public boolean isShowing() {
		if (getDialog() == null) {
			return false;
		}
		else {
			return isAdded() && getDialog().isShowing();
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		if (savedInstanceState != null) {
			mMessageId = savedInstanceState.getInt(INSTANCE_MESSAGE_RES_ID);
			mMessage = savedInstanceState.getCharSequence(INSTANCE_MESSAGE_TEXT);
		}
		if (mMessageId != 0) {
			mMessage = getString(mMessageId);
		}
		builder.setMessage(mMessage);

		builder.setNeutralButton(com.mobiata.android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				dismiss();
			}
		});

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(mCancelOnTouchOutside);

		return dialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(INSTANCE_MESSAGE_RES_ID, mMessageId);
		outState.putCharSequence(INSTANCE_MESSAGE_TEXT, mMessage);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mDismissListener != null) {
			mDismissListener.onDismissed();
		}
	}
}
