package com.expedia.bookings.services

import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

class JsonArrayConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> =
            JsonConverter.INSTANCE

    class JsonConverter : Converter<ResponseBody, JSONArray> {

        @Throws(IOException::class)
        override fun convert(responseBody: ResponseBody): JSONArray {
            try {
                return JSONArray(responseBody.string())
            } catch (e: JSONException) {
                throw IOException("Failed to parse JSON", e)
            }
        }

        companion object {
            val INSTANCE = JsonConverter()
        }
    }
}