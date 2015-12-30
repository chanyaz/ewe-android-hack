package com.expedia.bookings.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.dialog.ClearPrivateDataDialogPreference.ClearPrivateDataListener;
import com.expedia.ui.EBPreferencesFragment;
import com.mobiata.android.Log;
import com.squareup.phrase.Phrase;

public class ExpediaBookingPreferenceActivity extends AppCompatActivity implements ClearPrivateDataListener {
	public static final int RESULT_NO_CHANGES = 1;
	public static final int RESULT_CHANGED_PREFS = 2;

	private static final int DIALOG_CLEAR_DATA = 0;
	private static final int DIALOG_CLEAR_DATA_SIGNED_OUT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);

		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
		}

		setResult(RESULT_NO_CHANGES);

		getFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, new EBPreferencesFragment())
			.addToBackStack(EBPreferencesFragment.class.getName())
			.commit();
	}

	@Override
	public void setTitle(CharSequence title) {
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setTitle(title);
		}
		else {
			super.setTitle(title);
		}
	}

	public void changedPrefs() {
		setResult(RESULT_CHANGED_PREFS);
	}

	@Override
	public void onBackPressed() {
		Log.e("onBackPressed. back stack = " + getFragmentManager().getBackStackEntryCount());
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager.getBackStackEntryCount() > 1) {
			fragmentManager.popBackStack();
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CLEAR_DATA: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_title_cleared_private_data);
			builder.setMessage(R.string.dialog_message_cleared_private_data);
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});
			builder.setPositiveButton(R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});

			return builder.create();
		}
		case DIALOG_CLEAR_DATA_SIGNED_OUT: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_title_signed_out_and_cleared_private_data);
			builder.setMessage(Phrase.from(this, R.string.dialog_message_signed_out_and_cleared_private_data_TEMPLATE)
				.put("brand", BuildConfig.brand).format());
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});
			builder.setPositiveButton(R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});

			return builder.create();
		}
		}

		return super.onCreateDialog(id);
	}

	@Override
	public void onClearPrivateData(boolean signedOut) {
		showDialog(signedOut ? DIALOG_CLEAR_DATA_SIGNED_OUT : DIALOG_CLEAR_DATA);

		setResult(RESULT_CHANGED_PREFS);
	}
}
