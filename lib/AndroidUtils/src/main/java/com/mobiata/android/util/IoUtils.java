package com.mobiata.android.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.Context;

import com.mobiata.android.Log;

public class IoUtils {

	private static final String TMP_FILE = "mobiata-tmpfile";

	/**
	 * Writes a string to a gzipped file. This is the preferred method for saving data
	 * because it writes to temp file first, then moves it onto the file, thus
	 * minimizing the risk of corrupted data.  Use readStringFromFile() to retrieve
	 * the data again.
	 *
	 * @param filename the file destination
	 * @param contents the contents of the file
	 * @param context the app context
	 * @throws IOException
	 * @see #readStringFromFile(String, Context)
	 */
	public static synchronized void writeStringToFile(String filename, String contents, Context context)
			throws IOException {
		FileOutputStream fOut = context.openFileOutput(TMP_FILE, Context.MODE_PRIVATE);
		GZIPOutputStream gOut = new GZIPOutputStream(fOut);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gOut));
		writer.write(contents);
		writer.close();

		File tmpFile = context.getFileStreamPath(TMP_FILE);
		File destFile = context.getFileStreamPath(filename);
		tmpFile.renameTo(destFile);
	}

	/**
	 * Reads a string from a gzipped file. Use writeStringToFile() to write
	 * the string file that this method reads.
	 * @param filename the file to read from
	 * @param context the app context
	 * @return the contents of the file
	 * @throws IOException
	 * @see writeStringToFile()
	 */
	public static synchronized String readStringFromFile(String filename, Context context) throws IOException {
		FileInputStream fIn = context.openFileInput(filename);
		GZIPInputStream gIn = new GZIPInputStream(fIn);
		return convertStreamToString(gIn);
	}

	/**
	 * Because I am getting irritated having to figure this out over and over again...
	 *
	 * Warning: only use this for short streams.  If it's longer you may want to consider
	 * a streaming solution for parsing.
	 *
	 * From http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
	 *
	 * @param is the input stream
	 * @return the stream as a String
	 */
	public static String convertStreamToString(final InputStream is) {
		return convertStreamToString(is, 8192, "UTF-8");
	}

	public static String convertStreamToString(final InputStream is, final int bufferSize, final String encoding)
	{
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try {
			final Reader in = new BufferedReader(new InputStreamReader(is, encoding));
			try {
				for (int len; (len = in.read(buffer, 0, bufferSize)) != -1;) {
					out.append(buffer, 0, len);
				}
			}
			finally {
				in.close();
			}
		}
		catch (IOException ex) {
			Log.w("Could not convert InputStream to String", ex);
			return null;
		}
		return out.toString();
	}
}
