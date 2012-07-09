package com.expedia.bookings.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.server.ExpediaServices;

public class ClearPrivateDataDialogPreference extends DialogPreference {
	public interface ClearPrivateDataListener {
		public void onClearPrivateDate(boolean signedOut);
	}

	private ClearPrivateDataListener mClearPrivateDataListener;

	public ClearPrivateDataDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			Context context = getContext();
			BillingInfo info = new BillingInfo();
			info.delete(context);

			ExpediaServices expedia = new ExpediaServices(context);
			boolean signedIn = expedia.isLoggedIn();
			if (signedIn) {
				expedia.signOut();
			}

			if (mClearPrivateDataListener != null) {
				mClearPrivateDataListener.onClearPrivateDate(signedIn);
			}
			else {
				Toast.makeText(context, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
			}
		}
	}

	public void setClearPrivateDataListener(ClearPrivateDataListener clearPrivateDataListener) {
		mClearPrivateDataListener = clearPrivateDataListener;
	}
}