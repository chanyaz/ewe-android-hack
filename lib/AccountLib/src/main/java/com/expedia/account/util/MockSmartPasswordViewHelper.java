package com.expedia.account.util;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.expedia.account.AccountView;
import com.expedia.account.AnalyticsListener;

public class MockSmartPasswordViewHelper extends SmartPasswordViewHelper {

	final CharSequence[] items = new CharSequence[] {
		"success@test.com", "failure@failure.com"
	};

	public MockSmartPasswordViewHelper(AnalyticsListener analyticsListener,FragmentActivity currentActivity) {
		super(analyticsListener,currentActivity);
		retrieveCredentialsDialog.dismiss();
		new AlertDialog.Builder(currentActivity)
			.setTitle("Choose a Smart Lock Account")
			.setItems(items, endpointListener)
			.setNegativeButton(android.R.string.cancel, null)
			.setCancelable(true)
			.create()
			.show();

	}

	private DialogInterface.OnClickListener endpointListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0:
				Events.post(new Events.SignInButtonClicked(items[0].toString(), items[0].toString()));
				break;
			case 1:
				Events.post(new Events.SignInButtonClicked(items[1].toString(), items[1].toString()));
				break;
			}
		}
	};

	@Override
	public void onConnected(@Nullable Bundle bundle) {

	}

	@Override
	public void saveCredentials(String email, String password) {

	}

}
