package com.expedia.bookings.utils;

import android.app.ActionBar;
import android.app.Activity;

import com.expedia.bookings.R;
import com.mobiata.android.util.AndroidUtils;

/**
 * Temporary class used to deal with ActionBar for the ConfirmationFragmentActivity.
 * TODO: get rid of this extra class once we use ActionBarSherlock.
 * @author doug
 *
 */
public class ConfirmationFragmentActivityActionBarHelper {
	Activity mActivity;

	public ConfirmationFragmentActivityActionBarHelper(Activity activity) {
		this.mActivity = activity;
	}

	public void configure() {
		ActionBar actionBar = mActivity.getActionBar();

		// Only display the actionbar on tablets, for now
		if (actionBar != null) {
			// Configure the ActionBar
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(false);
			if (AndroidUtils.getSdkVersion() >= 14) {
				actionBar.setHomeButtonEnabled(false);
			}
			actionBar.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.bg_action_bar));
		}
	}
}