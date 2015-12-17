package com.expedia.bookings.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.mobiata.mocke3.FileOpener;

public class AndroidFileOpener implements FileOpener {

	protected static Context mContext;

	public AndroidFileOpener(Context context) {
		mContext = context;
	}

	@Override
	public InputStream openFile(String filename) throws IOException {
		return mContext.getResources().getAssets().open(filename);
	}
}
