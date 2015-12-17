package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.Calendar
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

fun unUrlEscape(str: String): String {
	return str.replace("%20", " ")
}

fun parseYearMonthDay(ymd: String?, hour: Int, minute: Int): Calendar {
	val parts = ymd!!.splitBy("-")
	val year = Integer.parseInt(parts[0])
	val month = Integer.parseInt(parts[1]) - 1
	val day = Integer.parseInt(parts[2])
	val cal = Calendar.getInstance()
	cal.set(year, month, day, hour, minute)
	return cal
}

fun parseRequest(request: RecordedRequest): MutableMap<String, String> {
	if ("GET" == request.getMethod() && request.getRequestLine().contains("?")) {
		var requestLine = request.getRequestLine().splitBy("?")[1]
		// Replace "HTTP version" from request line.
		requestLine = requestLine.substring(0, requestLine.lastIndexOf(" "))
		return constructParamsFromVarArray(requestLine)
	}
	else if ("POST" == request.getMethod()) {
		return constructParamsFromVarArray(request.getUtf8Body())
	}
	return LinkedHashMap()
}

fun constructParamsFromVarArray(requestStr: String): MutableMap<String, String> {
	val requestVariablePairs = requestStr.splitBy("&")
	val params = LinkedHashMap<String, String>()
	for (pair in requestVariablePairs) {
        if (pair.isBlank()) {
            continue
        }

		val idx = pair.indexOf("=")
		try {
			val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
			val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
			params.put(key, value)
		}
		catch (e: UnsupportedEncodingException) {
			// ignore - just skip the pair
		}

	}
	return params
}

fun makeEmptyResponse(): MockResponse {
	val resp = MockResponse()
	resp.setResponseCode(200)
	return resp
}

fun makeDelayedResponse(): MockResponse {
	val resp = makeEmptyResponse()
	resp.setBody("Much delay, wow")
	resp.setBodyDelay(7, TimeUnit.SECONDS)
	return resp
}

fun make404(): MockResponse {
	return MockResponse().setResponseCode(404)
}

fun makeResponse(filePath: String, params: Map<String, String>?, fileOpener: FileOpener): MockResponse {
	var path = filePath
	// Handle all FileOpener implementations
	if (path.startsWith("/")) {
		path = path.substring(1)
	}
    if (path.contains('?')) {
        path = path.substring(0, path.indexOf('?'))
    }

    System.out.println("MockWebSever: " + path)
	val resp = MockResponse()
	try {
		var body = getResponse(path, fileOpener)
		if (params != null) {
			val it = params.entrySet().iterator()
			while (it.hasNext()) {
				val entry = it.next()
				val key = "\${" + entry.getKey() + "}"
				if (body.contains(entry.getKey())) {
					body = body.replace(key, entry.getValue())
				}
			}
		}
		resp.setBody(body)
		resp.setHeader("Content-Type", "application/json")
	}
	catch (e: Exception) {
        e.printStackTrace()
		resp.setResponseCode(404)
	}

	return resp
}

// Read the json responses from the FileOpener
@throws(IOException::class)
private fun getResponse(filename: String, fileOpener: FileOpener): String {
	val inputStream = fileOpener.openFile(filename)
	val br = BufferedReader(InputStreamReader(inputStream))
	try {
		val sb = StringBuilder()
		var line: String? = br.readLine()

		while (line != null) {
			sb.append(line)
			line = br.readLine()
		}
		return sb.toString()
	}
	finally {
		br.close()
	}
}

