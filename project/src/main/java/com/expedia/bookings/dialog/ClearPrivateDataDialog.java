package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.utils.ClearPrivateDataUtil;

public class ClearPrivateDataDialog extends DialogFragment {
	private ClearPrivateDataDialogListener listener;

	public interface ClearPrivateDataDialogListener {
		void onPrivateDataCleared();
		void onDialogCancel();
	}

	public void setListener(ClearPrivateDataDialogListener listener) {
		this.listener = listener;
	}


	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AccountDialogTheme);

		builder.setTitle(R.string.dialog_clear_private_data_title);
		if (User.isLoggedIn(getActivity())) {
			builder.setMessage(R.string.dialog_sign_out_and_clear_private_data_msg);
		}
		else {
			builder.setMessage(R.string.dialog_clear_private_data_msg);
		}
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ClearPrivateDataUtil.clear(getActivity());

				if (listener != null ) {
					listener.onPrivateDataCleared();
				}
				if (getActivity() instanceof ClearPrivateDataDialogListener) {
					((ClearPrivateDataDialogListener) getActivity()).onPrivateDataCleared();
				}
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
				if (listener != null) {
					listener.onDialogCancel();
				}
			}
		});

		return builder.create();
	}

}
