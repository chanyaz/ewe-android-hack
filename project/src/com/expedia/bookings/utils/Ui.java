package com.expedia.bookings.utils;

import android.support.v4.app.CompatFragmentActivity;
import android.support.v4.app.Fragment;

/**
 * Adds compatibility library fragment support to Ui.
 */
public class Ui extends com.mobiata.android.util.Ui {

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(CompatFragmentActivity activity, String tag) {
		return (T) activity.getSupportFragmentManager().findFragmentByTag(tag);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(CompatFragmentActivity activity, int id) {
		return (T) activity.getSupportFragmentManager().findFragmentById(id);
	}

	/**
	 * Convenience method that either:
	 * 
	 * 1. Finds and returns the Fragment if already exists in FragmentManager
	 * 2. Creates and adds the Fragment to android.R.id.content if doesn't exit
	 * 
	 * Either way, it returns the Fragment, ready for use.
	 * 
	 * Should only be used if there is a single Fragment that is in android.R.id.content.
	 */
	public static <T extends Fragment> T findOrAddSupportFragment(CompatFragmentActivity activity,
			Class<T> fragmentClass, String tag) {
		T fragment = findSupportFragment(activity, tag);
		if (fragment == null) {
			try {
				fragment = fragmentClass.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			activity.getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, tag).commit();
		}
		return fragment;
	}
}
