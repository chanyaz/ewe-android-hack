package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.services.PersistentCookiesCookieJar
import okhttp3.Cache

class OKHttpClientFactory(context: Context, cookieManager: PersistentCookiesCookieJar, cache: Cache, endpointProvider: EndpointProvider) : SecureOKHttpClientFactory(context, cookieManager, cache, endpointProvider)
