package com.expedia.bookings.test.stepdefs.phone.utils

import com.expedia.bookings.test.TestBootstrap
import com.expedia.bookings.test.stepdefs.phone.model.ApiRequestData
import com.expedia.bookings.utils.RequestInterceptor
import com.expedia.bookings.utils.Ui
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.net.URLDecoder
import java.util.HashMap

class StepDefUtils private constructor() {

    companion object {
        private val apiCallAliases: HashMap<String, String> = {
            val apiCallAliases = HashMap<String, String>()
            apiCallAliases["FlightSearch"] = "/api/flight/search"
            apiCallAliases["FlightCreateTrip"] = "/api/flight/trip/create"
            apiCallAliases["GetPackagesV1"] = "/getpackages/v1"
            apiCallAliases["TypeAheadSFO"] = "/api/v4/typeahead/sfo"
            apiCallAliases["TypeAheadLAS"] = "/api/v4/typeahead/LAS"
            apiCallAliases
        }()

        private fun Request.convertToApiCallParams(): ApiRequestData {
            val queryParams: HashMap<String, List<String>> = HashMap()

            url().queryParameterNames().forEach {
                queryParams.put(it, url().queryParameterValues(it))
            }

            val formData: HashMap<String, String> = HashMap()
            val searchRequestBody = body() as FormBody?

            if (searchRequestBody != null) {
                for (index in 0..searchRequestBody.size() - 1) {
                    formData.put(URLDecoder.decode(searchRequestBody.encodedName(index), "UTF-8"), searchRequestBody.encodedValue(index))
                }
            }

            return ApiRequestData(queryParams, formData)
        }

        @Throws(Throwable::class)
        @JvmStatic
        fun interceptApiCalls(apiCallsAliases: List<String>, onRequest: ((ApiRequestData) -> Unit)?, onResponse: ((Response) -> Unit)?) {
            val okHttpClient = Ui.getApplication(TestBootstrap.mActivity).appComponent().okHttpClient()
            val requestInterceptor = okHttpClient.networkInterceptors().firstOrNull { it is RequestInterceptor } as RequestInterceptor
            val apiCalls: List<String> = apiCallsAliases.map {
                apiCallAliases[it]
            }.filterNotNull()

            requestInterceptor.urlToIntercept = apiCalls
            requestInterceptor.onRequest = { request ->
                onRequest?.invoke(request.convertToApiCallParams())
            }
            requestInterceptor.onResponse = { response ->
                onResponse?.invoke(response)
            }
        }
    }
}