package com.expedia.bookings.test.ui.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dmadan on 7/28/14.
 */
public interface FileOpener {
	public InputStream openFile(String filename) throws IOException;
}
