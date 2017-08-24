package com.expedia.bookings.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.expedia.bookings.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mobiata.android.Log;

public class GooglePlayServicesDialog {
	private final Activity mActivity;
	private GooglePlayServicesConnectionSuccessListener mListener;

	public interface GooglePlayServicesConnectionSuccessListener {
		void onGooglePlayServicesConnectionSuccess();
	}

	public GooglePlayServicesDialog(Activity activity) {
		mActivity = activity;
	}

	public GooglePlayServicesDialog(Activity activity, GooglePlayServicesConnectionSuccessListener listener) {
		mActivity = activity;
		mListener = listener;
	}

	private final DialogInterface.OnCancelListener mGooglePlayServicesOnCancelListener = new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			Log.d("Google Play Services: onCancel");
			startChecking();
		}
	};

	private final DialogInterface.OnKeyListener mGooglePlayServicesOnKeyListener = new DialogInterface.OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			Log.d("Google Play Services: onKey");
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
				mActivity.finish();
				return true;
			}
			else {
				return false;
			}
		}
	};

	private final DialogInterface.OnClickListener mGooglePlayServicesOnClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Log.d("Google Play Services: onClick");
			mActivity.finish();
		}
	};

	public void startChecking() {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int result = apiAvailability.isGooglePlayServicesAvailable(mActivity);
		switch (result) {
		case ConnectionResult.SERVICE_DISABLED:
		case ConnectionResult.SERVICE_INVALID:
		case ConnectionResult.SERVICE_MISSING:
		case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
		case ConnectionResult.SERVICE_UPDATING: {
			Log.d("Google Play Services: Raising dialog for user recoverable error " + result);
			Dialog dialog = apiAvailability.getErrorDialog(mActivity, result, 0);
			dialog.setOnCancelListener(mGooglePlayServicesOnCancelListener);
			dialog.setOnKeyListener(mGooglePlayServicesOnKeyListener);
			dialog.show();
			break;
		}
		case ConnectionResult.SUCCESS: {
			// We are fine - proceed
			Log.d("Google Play Services: Everything fine, proceeding");
			if (mListener != null) {
				mListener.onGooglePlayServicesConnectionSuccess();
			}
			break;
		}
		default: {
			// The rest are unrecoverable codes that developer configuration error or what have you
			//throw new RuntimeException("Google Play Services status code indicates unrecoverable error: " + result);
			Log.d("Google Play Services: Raising dialog for unrecoverable error " + result);
			Context context = (Context) mActivity;
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(R.string.google_play_services_unrecoverable_error);
			builder.setNeutralButton(R.string.ok, mGooglePlayServicesOnClickListener);

			Dialog dialog = builder.create();
			dialog.setOnCancelListener(mGooglePlayServicesOnCancelListener);
			dialog.setOnKeyListener(mGooglePlayServicesOnKeyListener);
			dialog.show();
		}
		}
	}
}
