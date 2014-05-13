package com.expedia.bookings.utils;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.util.AndroidUtils;

/**
 * This is a debug menu that you can attach to any Activity in Expedia Bookings.
 *
 * Just add hooks at the appropriate points.
 *
 * TODO: Reflection here, so as not to duplicate code.  (Being lazy for the moment, though.)
 *
 */
public class DebugMenu {

	public static void onCreateOptionsMenu(Context context, Menu menu) {
		if (!AndroidUtils.isRelease(context)) {
			MenuInflater inflater = new MenuInflater(context);
			inflater.inflate(R.menu.menu_debug, menu);
			updateStatus(context, menu);
		}
	}

	public static void onPrepareOptionsMenu(Context context, Menu menu) {
		updateStatus(context, menu);
	}

	public static boolean onOptionsItemSelected(Context context, MenuItem item) {
		// Do nothing for now, except consume debug item clicks
		switch (item.getItemId()) {
		case R.id.debug_menu_build_server:
		case R.id.debug_menu_build_number:
			return true;
		}
		return false;
	}

	private static void updateStatus(Context context, Menu menu) {
		MenuItem serverMenuItem = menu.findItem(R.id.debug_menu_build_server);
		MenuItem buildMenuItem = menu.findItem(R.id.debug_menu_build_number);
		if (serverMenuItem != null) {
			serverMenuItem.setTitle(getBuildServerString(context));
		}
		if (buildMenuItem != null) {
			buildMenuItem.setTitle(getBuildNumberString(context));
		}
	}

	private static String getBuildServerString(Context context) {
		String endpoint = ExpediaServices.getEndPoint(context).toString();
		return context.getString(R.string.connected_server, endpoint);
	}

	private static String getBuildNumberString(Context context) {
		String buildNumber = AndroidUtils.getAlphaBuildNumber(context);
		return context.getString(R.string.build_number, buildNumber);
	}

}
