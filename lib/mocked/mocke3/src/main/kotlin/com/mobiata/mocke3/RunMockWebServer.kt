package com.mobiata.mocke3

import java.io.File
import java.net.InetAddress

import com.squareup.okhttp.mockwebserver.MockWebServer

public object RunMockWebServer {
	@Throws(Throwable::class)
	@JvmStatic public fun main(args: Array<String>) {
		val server = MockWebServer()

		val root = File("../templates").canonicalPath
		println("Template path: " + root)
		val opener = FileSystemOpener(root)

		val dispatcher = ExpediaDispatcher(opener)
		server.setDispatcher(dispatcher)
		server.start(InetAddress.getByName("0.0.0.0"), 7000)
	}
}
