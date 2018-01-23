package com.expedia.testutils

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

class SimpleTestDispatcher(val path: String) : Dispatcher() {

    override fun dispatch(request: RecordedRequest?): MockResponse? {
        val resp = MockResponse()
        try {
            val body = getResponse(path)
            resp.setBody(body)
            resp.setHeader("Content-Type", "application/json")
            resp.setResponseCode(200)
        } catch (e: Exception) {
            e.printStackTrace()
            resp.setResponseCode(404)
        }

        return resp
    }

    @Throws(IOException::class)
    private fun getResponse(filename: String): String {
        val inputStream = FileInputStream(filename)
        val br = BufferedReader(InputStreamReader(inputStream))

        try {
            val sb = StringBuilder()
            var line: String? = br.readLine()

            while (line != null) {
                sb.append(line)
                line = br.readLine()
            }
            return sb.toString()
        } finally {
            br.close()
        }
    }
}
