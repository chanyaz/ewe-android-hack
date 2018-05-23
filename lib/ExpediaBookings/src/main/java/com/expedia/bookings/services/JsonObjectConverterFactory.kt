package com.expedia.bookings.services

import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

class JsonObjectConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> =
            JsonConverter.INSTANCE

    class JsonConverter : Converter<ResponseBody, JSONObject> {

        @Throws(IOException::class)
        override fun convert(responseBody: ResponseBody): JSONObject {
            try {
                return JSONObject(responseBody.string())
            } catch (e: JSONException) {
                throw IOException("Failed to parse JSON", e)
            }
        }

        companion object {
            val INSTANCE = JsonConverter()
        }
    }
}
