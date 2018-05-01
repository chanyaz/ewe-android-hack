package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.services.PersistentCookiesCookieJar
import okhttp3.Cache
import okhttp3.Interceptor

class OKHttpClientFactory(context: Context, cookieManager: PersistentCookiesCookieJar, cache: Cache, endpointProvider: EndpointProvider, hmacInterceptor: Interceptor) : SecureOKHttpClientFactory(context, cookieManager, cache, endpointProvider, hmacInterceptor)
