package com.expedia.bookings.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mobiata.android.Log;

public class FragmentAvailabilityUtils {

	public static final int INVISIBLE_FRAG = -1;
	public static final int DIALOG_FRAG = -2;

	private static final String ARG_IS_FRAGMENT_ADDED_EBAD = "ARG_IS_FRAGMENT_ADDED_EBAD";

	public interface IFragmentAvailabilityProvider {
		Fragment getExistingLocalInstanceFromTag(String tag);

		Fragment getNewFragmentInstanceFromTag(String tag);

		void doFragmentSetup(String tag, Fragment frag);
	}

	public static <T extends Fragment> T getFrag(FragmentManager manager, String tag) {
		return (T) manager.findFragmentByTag(tag);
	}

	public static <T extends Fragment> T setFragmentAvailability(boolean available, String tag, FragmentManager manager,
		FragmentTransaction transaction, IFragmentAvailabilityProvider provider, int container,
		boolean alwaysRunSetup) {
		T frag = (T) provider.getExistingLocalInstanceFromTag(tag);
		if (available) {
			if (frag == null || !frag.isAdded()) {
				if (frag == null) {
					frag = (T) manager.findFragmentByTag(tag);
				}
				if (frag == null) {
					frag = (T) provider.getNewFragmentInstanceFromTag(tag);
				}

				if (!frag.isAdded()) {
					if (internalCheckIsFragmentAdded(frag)) {
						Log.v("FragAvailability", tag + " add ignored");
						return frag;
					}
					else {
						addTrackingArg(frag);
					}

					Log.v("FragAvailability", tag + " added");
					if (container == DIALOG_FRAG) {
						transaction.add(frag, tag);
					}
					else if (container == INVISIBLE_FRAG) {
						transaction.add(frag, tag);
					}
					else {
						transaction.add(container, frag, tag);
					}
				}
				provider.doFragmentSetup(tag, frag);
			}
			else if (alwaysRunSetup) {
				provider.doFragmentSetup(tag, frag);
			}
		}
		else {
			if (frag != null) {
				Log.v("FragAvailability", tag + " removed");
				transaction.remove(frag);
			}
			frag = null;
		}
		return frag;
	}

	private static boolean internalCheckIsFragmentAdded(Fragment frag) {
		Bundle args = frag.getArguments();
		if (args != null) {
			if (args.containsKey(ARG_IS_FRAGMENT_ADDED_EBAD)) {
				return true;
			}
		}

		return false;
	}

	private static void addTrackingArg(Fragment frag) {
		Bundle args = frag.getArguments();
		if (args == null) {
			args = new Bundle();
		}

		args.putBoolean(ARG_IS_FRAGMENT_ADDED_EBAD, true);
		frag.setArguments(args);
	}
}
