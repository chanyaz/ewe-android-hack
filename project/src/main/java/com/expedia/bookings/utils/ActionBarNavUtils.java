package com.expedia.bookings.utils;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobiata.android.util.ViewUtils;

/**
 * This class helps with polishing the UX wrt our custom navigation popup dropdown as it pertains to lifecycle events
 */
public class ActionBarNavUtils {

	/**
	 * Automatically configures a menu item with an actionLayout button.  Here's what it does:
	 *
	 * 1. Sets it to all caps if it's a TextView
	 *
	 * 2. Sets up a listener to call onOptionsItemSelected() when the button is clicked.
	 *
	 */
	public static MenuItem setupActionLayoutButton(final FragmentActivity activity, Menu menu, int resId) {
		final MenuItem item = menu.findItem(resId);

		View actionView = item.getActionView();
		if (actionView instanceof TextView) {
			ViewUtils.setAllCaps((TextView) actionView);
		}
		else if (actionView instanceof ViewGroup) {
			ViewUtils.setAllCaps((ViewGroup) actionView);
		}

		actionView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onOptionsItemSelected(item);
			}
		});

		return item;
	}
}
