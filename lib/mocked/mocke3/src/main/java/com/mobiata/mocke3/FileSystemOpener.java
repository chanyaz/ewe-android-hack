package com.mobiata.mocke3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSystemOpener implements FileOpener {
	private String mRoot;

	public FileSystemOpener(String root) {
		mRoot = root;
	}

	@Override
	public InputStream openFile(String filename) throws IOException {
		InputStream inputStream = new FileInputStream(mRoot + "/" + filename);
		return inputStream;
	}
}
