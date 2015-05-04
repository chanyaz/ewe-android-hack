package com.mobiata.mocke3

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

public class FileSystemOpener(private val mRoot: String) : FileOpener {
	throws(javaClass<IOException>())
	override fun openFile(filename: String): InputStream {
		return FileInputStream(mRoot + "/" + filename)
	}
}
