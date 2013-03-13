package com.expedia.bookings.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.util.Log;

public class EventTrackingUtils {

	private final static String TAG = "Event Tracking";
	PrintWriter mFileWriter;
	private String mFileName;

	public EventTrackingUtils() {
		Date fileTimeInfo = new Date();
		String fileTimeStamp = fileTimeInfo.toString();
		mFileName = "/sdcard/" + fileTimeStamp + ".txt";
		// Create the empty file with default permissions, etc.
		File file = new File(mFileName);

		try {
			mFileWriter = new PrintWriter
					(file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return mFileName;
	}

	public void addLineToFile(String line, boolean includeTimestamp) {

		String timestamp = "";
		if (includeTimestamp) {
			Date time = new Date();
			timestamp = ": " + time.toString();
		}
		mFileWriter.println(line + timestamp);
		mFileWriter.flush();
	}

	public void flushFileWriter() {
		mFileWriter.flush();
	}

	public void closeFileWriter() {
		if(mFileWriter.checkError()) {
			Log.v(TAG, "There was an error in the file stream");
		}
		mFileWriter.close();
	}
}
