package com.mobiata.mocke3

import java.io.IOException
import java.io.InputStream

public trait FileOpener {
	throws(javaClass<IOException>())
	public fun openFile(filename: String): InputStream
}
