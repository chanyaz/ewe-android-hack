package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor

class OKHttpClientFactory(context: Context, cookieManager: PersistentCookiesCookieJar, cache: Cache, logLevel: HttpLoggingInterceptor.Level, chuckInterceptor: ChuckInterceptor, endpointProvider: EndpointProvider) : InsecureOKHttpClientFactory(context, cookieManager, cache, logLevel, chuckInterceptor, endpointProvider)
