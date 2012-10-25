package com.expedia.bookings.utils;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.widget.NavigationButton;
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
	public static MenuItem setupActionLayoutButton(final SherlockFragmentActivity activity, Menu menu, int resId) {
		final MenuItem item = menu.findItem(resId);

		View actionView = item.getActionView();
		if (actionView instanceof TextView) {
			ViewUtils.setAllCaps((TextView) actionView);
		}

		actionView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onOptionsItemSelected(item);
			}
		});

		return item;
	}

	public static boolean removePopupDropdownIfNecessaryOnBackPressed(NavigationButton navButton) {
		boolean dropdownRemoved = false;
		if (isPopupDropdownShowing(navButton)) {
			togglePopupDropdown(navButton);
			dropdownRemoved = true;
		}

		return dropdownRemoved;
	}

	public static boolean removePopupDropdownIfNecessaryOnTouch(MotionEvent ev, NavigationButton navButton) {
		boolean dropdownRemoved = false;
		if (navButton != null && navButton.getImageDropdown().getPopupWindow().isShowing()) {
			Rect bounds = new Rect();
			navButton.getImageDropdown().getPopupWindow().getContentView().getHitRect(bounds);
			if (!bounds.contains((int) ev.getX(), (int) ev.getY())) {
				togglePopupDropdown(navButton);
				dropdownRemoved = true;
			}
		}
		return dropdownRemoved;
	}

	private static boolean isPopupDropdownShowing(final NavigationButton navButton) {
		return navButton == null ? false : navButton.getImageDropdown().getPopupWindow().isShowing();
	}

	private static void togglePopupDropdown(NavigationButton navButton) {
		navButton.getImageDropdown().toggleDisplayDropdown();
	}
}
