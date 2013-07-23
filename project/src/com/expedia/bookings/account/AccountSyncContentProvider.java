package com.expedia.bookings.account;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class AccountSyncContentProvider extends ContentProvider {

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// Default method implementation - currently not used.
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		return "vnd.android.cursor.dir/vnd.expedia.items";
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// Default method implementation - currently not used.
		return null;
	}

	@Override
	public boolean onCreate() {
		// Default method implementation - currently not used.
		return false;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
		// Default method implementation - currently not used.
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// Default method implementation - currently not used.
		return 0;
	}

}
