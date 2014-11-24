package com.expedia.bookings.test.ui.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

/**
 * Created by dmadan on 7/28/14.
 */
public class AndroidFileOpener implements FileOpener {

	protected static Context mContext;

	public AndroidFileOpener(Context context) {
		mContext = context;
	}

	@Override
	public InputStream openFile(String filename) throws IOException {
		InputStream inputStream = mContext.getResources().getAssets().open(filename);
		return inputStream;
	}
}
