package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

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
		final int selectedCountryPosId = getArguments() != null ? getArguments().getInt("selectedCountryPosId") : 0;
		final boolean isChangingCountry = selectedCountryPosId != 0;

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AccountDialogTheme);
		Boolean isUserLoggedIn = userIsAuthenticated(getActivity());

		@StringRes int dialogTitle, dialogPositiveButton;
		String dialogMessage;

		if (isUserLoggedIn) {
			if (isChangingCountry) {
				dialogTitle = R.string.dialog_clear_private_data_title_logged_in_user;
				dialogMessage = Phrase.from(getContext(),
					R.string.dialog_sign_out_and_clear_private_data_msg_TEMPLATE)
					.put("brand", BuildConfig.brand).format().toString();
				dialogPositiveButton = R.string.continue_button;
			}
			else {
				dialogTitle = R.string.dialog_clear_private_data_title;
				dialogMessage = getContext().getResources()
					.getString(R.string.dialog_sign_out_and_clear_private_data_msg);
				dialogPositiveButton = R.string.ok;
			}
		}
		else {
			dialogTitle = R.string.dialog_clear_private_data_title;
			dialogMessage = getContext().getResources().getString(R.string.dialog_clear_private_data_msg);
			dialogPositiveButton = R.string.ok;

		}

		builder.setTitle(dialogTitle);
		builder.setMessage(dialogMessage);
		builder.setPositiveButton(dialogPositiveButton, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				ClearPrivateDataUtil.clear(getActivity());

				if (listener != null) {
					listener.onPrivateDataCleared();
				}
				if (getActivity() instanceof ClearPrivateDataDialogListener) {
					((ClearPrivateDataDialogListener) getActivity()).onPrivateDataCleared();
				}

				if (isChangingCountry) {
					((AboutUtils.CountrySelectDialogListener) getActivity())
						.onNewCountrySelected(selectedCountryPosId);
					AbacusHelperUtils.generateAbacusGuid(getContext());
				}
			}
		});

		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

	private boolean userIsAuthenticated(Context context) {
		return Ui.getApplication(context).appComponent().userStateManager().isUserAuthenticated();
	}
}
