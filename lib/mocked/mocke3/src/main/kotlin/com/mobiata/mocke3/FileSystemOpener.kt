package com.mobiata.mocke3

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

public class FileSystemOpener(private val root: String) : FileOpener {
	@Throws(IOException::class)
	override fun openFile(filename: String): InputStream {
		return FileInputStream(root + "/" + filename)
	}
}
