package com.expedia.bookings.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mobiata.android.Log;

public class LoggingInputStream extends FilterInputStream {
	private static final String TAG = "EBStream";
	private StringBuilder mData;

	public LoggingInputStream(InputStream is) {
		super(is);
		mData = new StringBuilder();
	}

	@Override
	public int read() throws IOException {
		int readByte = super.read();
		recordByte(readByte);
		return readByte;
	}

	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException {
		int readBytes = super.read(buffer, offset, count);
		recordBytes(buffer, offset, readBytes);
		return readBytes;
	}

	@Override
	public void close() throws IOException {
		super.close();
		logIt();
	}

	private void recordBytes(byte[] buffer, int offset, int readBytes) {
		if (!isFull()) {
			String str = new String(buffer, offset, offset + readBytes);
			mData.append(str);
		}

		if (isFull()) {
			logIt();
		}
	}

	private void recordByte(int readByte) {
		if (!isFull()) {
			mData.append((char) readByte);
		}

		if (isFull()) {
			logIt();
		}
	}

	private boolean isFull() {
		return mData == null || mData.length() > 1000;
	}

	private void logIt() {
		if (mData != null) {
			Log.v(TAG, mData.toString());
			mData = null;
		}
	}
}

