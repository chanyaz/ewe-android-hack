package com.mobiata.mocke3

import java.io.File
import java.net.InetAddress

import com.squareup.okhttp.mockwebserver.MockWebServer
import kotlin.platform.platformStatic

public object RunMockWebServer {
	@throws(Throwable::class)
	platformStatic public fun main(args: Array<String>) {
		val server = MockWebServer()

		val root = File("../templates").getCanonicalPath()
		println("Template path: " + root)
		val opener = FileSystemOpener(root)

		val dispatcher = ExpediaDispatcher(opener)
		server.setDispatcher(dispatcher)
		server.start(InetAddress.getByName("0.0.0.0"), 7000)
	}
}
