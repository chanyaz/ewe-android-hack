package com.expedia.bookings.utils;

import android.content.Context;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * This is a utility for debug build related tasks
 */
public class ExpediaDebugUtil {

	/**
	 * Notifies QA with a toast that a memory crash has been saved to disk.
	 */
	public static void showExpediaDebugToastIfNeeded(Context context) {
		if (!AndroidUtils.isRelease(context)
				&& SettingUtils.get(context, context.getString(R.string.preference_debug_notify_oom_crash), false)) {
			Toast.makeText(context, "Memory crash. Open ExpediaDebug for upload.", 1000).show();
			SettingUtils.save(context, context.getString(R.string.preference_debug_notify_oom_crash), false);
		}
	}

}
