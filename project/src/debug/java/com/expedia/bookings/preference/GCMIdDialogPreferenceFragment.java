package com.expedia.bookings.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.user.UserSource;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.UniqueIdentifierPersistenceProvider;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UniqueIdentifierHelper;

public class GCMIdDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

	public static GCMIdDialogPreferenceFragment newInstance(String key) {
		final GCMIdDialogPreferenceFragment fragment = new GCMIdDialogPreferenceFragment();
		final Bundle b = new Bundle(1);
		b.putString(ARG_KEY, key);
		fragment.setArguments(b);
		return fragment;
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		Context context = getContext();
		final String gcmId = GCMRegistrationKeeper.getInstance(context).getRegistrationId(context);
		UserStateManager userStateManager = Ui.getApplication(context).appComponent().userStateManager();
		UserSource userDetail = userStateManager.getUserSource();
		StringBuilder sb = new StringBuilder("Token= ");
		sb.append(gcmId);
		sb.append("\n \n");
		if (userStateManager.isUserAuthenticated()) {
			sb.append("TUID= ");
			sb.append(userDetail.getTuid());
			sb.append("\n \n");
			sb.append("ExpUserID= ");
			sb.append(userDetail.getExpUserId());
			sb.append("\n \n");
		}
		sb.append("Device ID = ");
		sb.append(UniqueIdentifierHelper.getID(new UniqueIdentifierPersistenceProvider(context)));
		final String gcmInfo = sb.toString();

		builder.setTitle("GCM Info");

		if (TextUtils.isEmpty(gcmId)) {
			builder.setMessage("GCM ID NOT CURRENTLY AVAILABLE! Please wait a little a while (and ensure this isn't a VSC build).");

		}
		else {
			builder.setMessage(gcmInfo).setNeutralButton("Copy Text", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ClipboardUtils.setText(getContext(), gcmInfo);
					Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
				}

			});
		}
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
	}
}
