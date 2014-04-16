package com.expedia.bookings.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentAvailabilityUtils {

	public static final int INVISIBLE_FRAG = -1;

	public interface IFragmentAvailabilityProvider {
		public Fragment getExisitingLocalInstanceFromTag(String tag);

		public Fragment getNewFragmentInstanceFromTag(String tag);

		public void doFragmentSetup(String tag, Fragment frag);
	}

	public static <T extends Fragment> T getFrag(FragmentManager manager, String tag){
		return (T) manager.findFragmentByTag(tag);
	}

	public static <T extends Fragment> T setFragmentAvailability(boolean available, String tag, FragmentManager manager,
			FragmentTransaction transaction, IFragmentAvailabilityProvider provider, int container, boolean alwaysRunSetup) {
		T frag = (T) provider.getExisitingLocalInstanceFromTag(tag);
		if (available) {
			if (frag == null || !frag.isAdded()) {
				if (frag == null) {
					frag = (T) manager.findFragmentByTag(tag);
				}
				if (frag == null) {
					frag = (T) provider.getNewFragmentInstanceFromTag(tag);
				}
				if (!frag.isAdded()) {
					if (container != INVISIBLE_FRAG) {
						transaction.add(container, frag, tag);
					}
					else {
						transaction.add(frag, tag);
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
				transaction.remove(frag);
			}
			frag = null;
		}
		return frag;
	}

}
