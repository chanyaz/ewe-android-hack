package com.mobiata.mocke3;

import java.io.File;

import com.squareup.okhttp.mockwebserver.MockWebServer;

public class RunMockWebServer {
	public static void main(String[] args) throws Throwable {
		MockWebServer mockWebServer = new MockWebServer();

		String root = new File("../templates").getCanonicalPath();
		System.out.println("Template path: " + root);
		FileOpener opener = new FileSystemOpener(root);

		ExpediaDispatcher dispatcher = new ExpediaDispatcher(opener);
		mockWebServer.setDispatcher(dispatcher);
		mockWebServer.play(7000);
	}
}
