package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.google.android.gms.security.ProviderInstaller
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext


abstract class SecureOKHttpClientFactory {

    fun getOkHttpClient(context: Context, cookieManager: PersistentCookiesCookieJar, cache: Cache,
                                   logLevel: HttpLoggingInterceptor.Level, sslContext: SSLContext,
                                   chuckInterceptor: ChuckInterceptor): OkHttpClient {
        try {
            ProviderInstaller.installIfNeeded(context)
        } catch (e: Exception) {
            // rely on the PlayServices checking code that runs when first activity starts
            // to guide the user through the recovery process
        }

        val client = OkHttpClient().newBuilder()

        setupClient(client, cache, cookieManager)
        addInterceptors(client, logLevel, chuckInterceptor)
        setupSSLSocketFactoryAndConnectionSpec(client, sslContext)

        return client.build()
    }

    abstract protected fun setupSSLSocketFactoryAndConnectionSpec(client: OkHttpClient.Builder, sslContext: SSLContext)

    protected open fun setupClient(client: OkHttpClient.Builder, cache: Cache, cookieManager: PersistentCookiesCookieJar) {
        client.cache(cache)
        client.followRedirects(true)
        client.cookieJar(cookieManager)
        client.connectTimeout(10, TimeUnit.SECONDS)
        client.readTimeout(60L, TimeUnit.SECONDS)
    }

    protected open fun addInterceptors(client: OkHttpClient.Builder, logLevel: HttpLoggingInterceptor.Level, chuckInterceptor: ChuckInterceptor?) {
        val logger = HttpLoggingInterceptor()
        logger.level = logLevel
        client.addInterceptor(logger)
    }
}
