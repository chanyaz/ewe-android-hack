package com.expedia.bookings.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.utils.ClearPrivateDataUtil;

public class ClearPrivateDataDialogPreference extends DialogPreference {
	public interface ClearPrivateDataListener {
		void onClearPrivateData(boolean signedOut);
	}

	private ClearPrivateDataListener mClearPrivateDataListener;

	public ClearPrivateDataDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		view.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.preference_ripple));
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			boolean signedIn = User.isLoggedIn(getContext());
			ClearPrivateDataUtil.clear(getContext());

			if (mClearPrivateDataListener != null) {
				mClearPrivateDataListener.onClearPrivateData(signedIn);
			}
			else {
				Toast.makeText(getContext(), R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
			}
		}
	}

	public void setClearPrivateDataListener(ClearPrivateDataListener clearPrivateDataListener) {
		mClearPrivateDataListener = clearPrivateDataListener;
	}
}
