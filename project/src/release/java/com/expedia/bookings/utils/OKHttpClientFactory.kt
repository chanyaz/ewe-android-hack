package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.google.android.gms.security.ProviderInstaller
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

class OKHttpClientFactory: AbstractOKHttpClientFactory() {

    override protected fun setupSSLSocketFactoryAndConnectionSpec(client: okhttp3.OkHttpClient.Builder, sslContext: SSLContext) {
        val socketFactory = TLSSocketFactory(sslContext)
        client.sslSocketFactory(socketFactory)
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build()
        client.connectionSpecs(listOf(spec))
    }
}
