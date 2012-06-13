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

	public static void onCreateOptionsMenu(Context context, com.actionbarsherlock.view.Menu menu) {
		if (!AndroidUtils.isRelease(context)) {
			com.actionbarsherlock.view.MenuInflater inflater = new com.actionbarsherlock.view.MenuInflater(context);
			inflater.inflate(R.menu.menu_debug, menu);
			updateStatus(context, menu);
		}
	}

	public static void onPrepareOptionsMenu(Context context, Menu menu) {
		updateStatus(context, menu);
	}

	public static void onPrepareOptionsMenu(Context context, com.actionbarsherlock.view.Menu menu) {
		updateStatus(context, menu);
	}

	public static boolean onOptionsItemSelected(Context context, MenuItem item) {
		// Do nothing for now, except consume debug item clicks
		switch (item.getItemId()) {
		case R.id.debug_menu_item:
			return true;
		}
		return false;
	}

	public static boolean onOptionsItemSelected(Context context, com.actionbarsherlock.view.MenuItem item) {
		// Do nothing for now, except consume debug item clicks
		switch (item.getItemId()) {
		case R.id.debug_menu_item:
			return true;
		}
		return false;
	}

	private static void updateStatus(Context context, Menu menu) {
		MenuItem statusMenuItem = menu.findItem(R.id.debug_menu_item);
		if (statusMenuItem != null) {
			statusMenuItem.setTitle(getStatus(context));
		}
	}

	private static void updateStatus(Context context, com.actionbarsherlock.view.Menu menu) {
		com.actionbarsherlock.view.MenuItem statusMenuItem = menu.findItem(R.id.debug_menu_item);
		if (statusMenuItem != null) {
			statusMenuItem.setTitle(getStatus(context));
		}
	}

	private static String getStatus(Context context) {
		switch (ExpediaServices.getEndPoint(context)) {
		case PRODUCTION:
			return "Server: Production";
		case DEV:
			return "Server: Dev";
		case PROXY:
			return "Server: Proxy";
		case INTEGRATION:
			return "Server: Integration";
		case STABLE:
			return "Server: Stable";
		case TRUNK:
			return "Server: Trunk";
		default:
			return "Server: Unknown";
		}
	}
}
