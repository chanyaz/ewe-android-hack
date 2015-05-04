package com.mobiata.mocke3

import java.io.File

import com.squareup.okhttp.mockwebserver.MockWebServer
import kotlin.platform.platformStatic

public object RunMockWebServer {
	throws(javaClass<Throwable>())
	platformStatic public fun main(args: Array<String>) {
		val mockWebServer = MockWebServer()

		val root = File("../templates").getCanonicalPath()
		println("Template path: " + root)
		val opener = FileSystemOpener(root)

		val dispatcher = ExpediaDispatcher(opener)
		mockWebServer.setDispatcher(dispatcher)
		mockWebServer.start(7000)
	}
}