package com.expedia.bookings.utils;

import android.graphics.Rect;
import android.view.MotionEvent;
import com.expedia.bookings.widget.NavigationButton;

/**
 * This class helps with polishing the UX wrt our custom navigation popup dropdown as it pertains to lifecycle events
 */
public class ActionBarNavUtils {

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
