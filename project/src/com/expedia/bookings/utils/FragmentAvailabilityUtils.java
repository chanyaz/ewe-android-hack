package com.expedia.bookings.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentAvailabilityUtils {

	public interface IFragmentAvailabilityProvider {
		public Fragment getExisitingLocalInstanceFromTag(String tag);

		public Fragment getNewFragmentInstanceFromTag(String tag);

		public void doFragmentSetup(String tag, Fragment frag);
	}

	public static Fragment setFragmentAvailability(boolean available, String tag, FragmentManager manager,
			FragmentTransaction transaction, IFragmentAvailabilityProvider provider, int container, boolean alwaysRunSetup) {
		Fragment frag = provider.getExisitingLocalInstanceFromTag(tag);
		if (available) {
			if (frag == null || !frag.isAdded()) {
				if (frag == null) {
					frag = manager.findFragmentByTag(tag);
				}
				if (frag == null) {
					frag = provider.getNewFragmentInstanceFromTag(tag);
				}
				if (!frag.isAdded()) {
					transaction.add(container, frag, tag);
				}
				provider.doFragmentSetup(tag, frag);
			}
			else if (alwaysRunSetup) {
				provider.doFragmentSetup(tag, frag);
			}
		}
		else {
			if (frag != null) {
				transaction.remove(frag);
			}
			frag = null;
		}
		return frag;
	}

}
