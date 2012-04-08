package com.expedia.bookings.utils;

import android.support.v4.app.CompatFragmentActivity;
import android.support.v4.app.Fragment;

/**
 * Adds compatibility library fragment support to Ui.
 */
public class Ui extends com.mobiata.android.util.Ui {

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findFragment(CompatFragmentActivity activity, String tag) {
		return (T) activity.getSupportFragmentManager().findFragmentByTag(tag);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findFragment(CompatFragmentActivity activity, int id) {
		return (T) activity.getSupportFragmentManager().findFragmentById(id);
	}

}
