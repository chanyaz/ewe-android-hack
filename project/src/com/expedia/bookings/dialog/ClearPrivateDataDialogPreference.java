package com.expedia.bookings.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BackgroundImageCache;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.WorkingBillingInfoManager;
import com.expedia.bookings.model.WorkingTravelerManager;

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

			boolean signedIn = User.isLoggedIn(context);
			if (signedIn) {
				User.signOut(context);
			}

			Db.deleteCachedFlightData(context);
			Db.deleteTravelers(context);

			WorkingBillingInfoManager biManager = new WorkingBillingInfoManager();
			biManager.deleteWorkingBillingInfoFile(context);

			WorkingTravelerManager travManager = new WorkingTravelerManager();
			travManager.deleteWorkingTravelerFile(context);

			try {
				//If the data has already been populated in memory, we should clear that....
				if (Db.getWorkingBillingInfoManager() != null) {
					Db.getWorkingBillingInfoManager().clearWorkingBillingInfo(context);
				}

				if (Db.getWorkingTravelerManager() != null) {
					Db.getWorkingTravelerManager().clearWorkingTraveler(context);
				}

				Db.getBillingInfo().delete(context);
				Db.getTravelers().clear();

				BackgroundImageCache cache = Db.getBackgroundImageCache(context);
				if (cache != null) {
					cache.clearDiskCache(context);
					cache.clearMemCache();
				}

			}
			catch (Exception ex) {
				//Don't care
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