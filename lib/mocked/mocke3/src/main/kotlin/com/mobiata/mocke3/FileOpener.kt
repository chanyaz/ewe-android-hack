package com.mobiata.mocke3

import java.io.IOException
import java.io.InputStream

public interface FileOpener {
	@Throws(IOException::class)
	public fun openFile(filename: String): InputStream
}
