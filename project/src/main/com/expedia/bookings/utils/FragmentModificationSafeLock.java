package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * You cannot modify the Fragment stack after onSaveInstanceState()
 * and before Fragment.onResume() (or Activity.onPostResume()).  This
 * utility helps queue actions to run when safe.
 * 
 * It is up to the user to mark when it's safe or not.  The reason for this
 * is because I believe it becomes safe at different times for Fragments
 * to modify the stack vs. Activities.  Reference:
 * 
 * Fragment.onSaveInstanceState() - set unsafe
 * Fragment.onResume() - set safe
 * 
 * Activity.onSaveInstanceState() - set unsafe
 * Activity.onPostResume() - set safe
 *
 */
public class FragmentModificationSafeLock {

	private List<Runnable> mRunWhenSafe;

	private boolean mSafe = false;

	public void setSafe(boolean isSafe) {
		if (mSafe != isSafe) {
			mSafe = isSafe;

			// If we're safe now, run anything we were deferring to later
			if (mSafe && mRunWhenSafe != null) {
				for (Runnable runnable : mRunWhenSafe) {
					runnable.run();
				}

				mRunWhenSafe.clear();
			}
		}
	}

	public boolean isSafe() {
		return mSafe;
	}

	public void runWhenSafe(Runnable runnable) {
		if (mSafe) {
			runnable.run();
		}
		else {
			if (mRunWhenSafe == null) {
				mRunWhenSafe = new ArrayList<Runnable>();
			}

			mRunWhenSafe.add(runnable);
		}
	}
}
