package com.mobiata.mocke3;

import java.io.IOException;
import java.io.InputStream;

public interface FileOpener {
	public InputStream openFile(String filename) throws IOException;
}
