package com.mobiata.android;

import android.content.Context;
import android.content.pm.PackageManager;

public class DebugUtils {

	public static String getBuildInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("BUILD INFO:\n");
		addBuildInfo(sb, "MODEL", android.os.Build.MODEL);
		addBuildInfo(sb, "RELEASE VERSION", android.os.Build.VERSION.RELEASE);
		addBuildInfo(sb, "ID", android.os.Build.ID);
		addBuildInfo(sb, "FINGERPRINT", android.os.Build.FINGERPRINT);
		return sb.toString().trim();
	}

	private static void addBuildInfo(StringBuilder sb, String label, String value) {
		if (value != null && value.length() > 0) {
			sb.append(label);
			sb.append(": ");
			sb.append(value);
			sb.append("\n");
		}
	}

	/**
	 * Checks if the log enabler app is installed.
	 * @param context the context of the current app
	 * @param appPackage the package of the current app 
	 * @return true if a valid log enabler is installed
	 */
	public static boolean isLogEnablerInstalled(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			return pm.checkSignatures(context.getPackageName(), "com.mobiata.logger") == PackageManager.SIGNATURE_MATCH;
		}
		catch (Exception e) {
			Log.e(Params.LOGGING_TAG, "Could not determine if log enabler is installed.", e);
			return false;
		}
	}

	/**
	 * Easy way to add sleep to a thread when debugging timing issues, without
	 * having to write a boilerplate try-catch loop.
	 * 
	 * If you actually want to sleep the thread (and not just debug), DO NOT
	 * USE THIS METHOD.  You should handle the InterruptedException yourself.
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {
			Log.w("Wow, we were actually interrupted during a sleep...", e);
		}
	}
}
