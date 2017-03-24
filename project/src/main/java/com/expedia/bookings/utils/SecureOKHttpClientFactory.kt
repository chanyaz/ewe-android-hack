package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.google.android.gms.security.ProviderInstaller
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext


abstract class SecureOKHttpClientFactory(private val context: Context, private val cookieManager: PersistentCookiesCookieJar, private val cache: Cache,
                                         private val logLevel: HttpLoggingInterceptor.Level, private val chuckInterceptor: ChuckInterceptor) {

    private var clientBuilder: OkHttpClient.Builder? = null


    fun getOkHttpClient(cookieJar: CookieJar? = null): OkHttpClient {
        if (clientBuilder == null) {
            makeOkHttpClient()
        }

        if (cookieJar != null) {
            clientBuilder!!.cookieJar(cookieJar)
        }

        return clientBuilder!!.build()
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

    private fun makeOkHttpClient() {
        try {
            ProviderInstaller.installIfNeeded(context)
        } catch (e: Exception) {
            // rely on the PlayServices checking code that runs when first activity starts
            // to guide the user through the recovery process
        }

        val client = OkHttpClient().newBuilder()
        setupClient(client, cache, cookieManager)
        addInterceptors(client, logLevel, chuckInterceptor)
        setupSSLSocketFactoryAndConnectionSpec(client, makeSslContext())

        this.clientBuilder = client
    }

    private fun makeSslContext(): SSLContext {
        try {
            return SSLContext.getDefault()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
