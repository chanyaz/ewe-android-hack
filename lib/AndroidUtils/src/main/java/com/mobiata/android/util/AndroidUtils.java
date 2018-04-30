package com.mobiata.android.util;

import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.UserManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.mobiata.android.Log;
import com.mobiata.android.Params;
import com.mobiata.android.R;

public class AndroidUtils {
	public static boolean isTablet(Context context) {
		return context.getResources().getBoolean(R.bool.tablet);
	}

	/**
	 * Returns the code of the app.  Returns 0 if it cannot be determined.
	 *
	 * @param context the context of the app
	 * @return the version code for the app, or 0 if there is no versionCode
	 */
	public static int getAppCode(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		}
		catch (Exception e) {
			// PackageManager is traditionally wonky, need to accept all exceptions here.
			Log.w(Params.LOGGING_TAG, "Couldn't get package info in order to show version code #!", e);
			return 0;
		}
	}

	public static Calendar getAppBuildDate(Context context) {
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(time);
			return cal;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the screen width/height, as a Point (x is width, y is height).
	 */
	@TargetApi(13)
	@SuppressWarnings("deprecation")
	public static Point getScreenSize(Context context) {
		Point size = new Point();
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		size.x = metrics.widthPixels;
		size.y = metrics.heightPixels;
		return size;
	}

	/**
	 * Similar to AndroidUtils.getScreenSize(), but takes the notification and nav bar in to account to
	 * provide a consistent experience across different orientations, API levels, and hardware.
	 * @param context
	 * @return
	 */
	public static Point getDisplaySize(Context context) {
		Point size = new Point();

		WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);

		size.x = metrics.widthPixels;
		size.y = metrics.heightPixels;

		try {
			Point realSize = new Point();
			Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
			size.x = realSize.x;
			size.y = realSize.y;
		}
		catch (Exception ignored) {
		}

		return size;
	}

	/**
	 * Returns the true if package with packageName exists on the system, false otherwise
	 */
	public static boolean isPackageInstalled(Context context, final String packageName) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
			// We would have blown up if the package didn't exist
			return true;
		}
		catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * @param packageName PackageName of the app in the current market
	 * @return market link to the app.
	 */
	public static String getMarketAppLink(Context context, String packageName) {
		String marketPrefix = context.getString(R.string.market_prefix);
		return marketPrefix + packageName;
	}

	/**
	 * Are we currently using a restricted profile?
	 * https://developer.android.com/about/versions/android-4.3.html#RestrictedProfiles
	 */
	public static boolean isRestrictedProfile(Context context) {
		UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
		Bundle restrictions = um.getUserRestrictions();
		return restrictions.getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false);
	}

	public static int getScreenDpi(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.densityDpi;
	}

	public static String getScreenDensityClass(Context context) {

		String densityClass;
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float density = metrics.density;

		if (density >= 4.0) {
			densityClass = "xxxhdpi";
		}
		else if (density >= 3.0) {
			densityClass = "xxhdpi";
		}
		else if (density >= 2.0) {
			densityClass = "xhdpi";
		}
		else if (density >= 1.5) {
			densityClass = "hdpi";
		}
		else if (density >= 1.0) {
			densityClass = "mdpi";
		}
		else {
			densityClass = "ldpi";
		}

		return densityClass;
	}

	public static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return Math.round(dp * displayMetrics.density);
	}
}
