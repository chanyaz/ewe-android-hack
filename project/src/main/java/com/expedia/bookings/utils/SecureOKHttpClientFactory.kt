package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.server.EndpointProviderInterface
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.google.android.gms.security.ProviderInstaller
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

abstract class SecureOKHttpClientFactory(protected val context: Context, private val cookieManager: PersistentCookiesCookieJar,
                                         private val cache: Cache, protected val endpointProvider: EndpointProviderInterface) {

    fun getOkHttpClient(cookieJar: CookieJar? = null): OkHttpClient {
        val clientBuilder = makeOkHttpClientBuilder()

        if (cookieJar != null) {
            clientBuilder.cookieJar(cookieJar)
        }

        return clientBuilder.build()
    }

    protected open fun setupSSLSocketFactoryAndConnectionSpec(client: OkHttpClient.Builder, sslContext: SSLContext) {
        val socketFactory = TLSSocketFactory(sslContext)
        client.sslSocketFactory(socketFactory, getSecureX509TrustManager())
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build()
        client.connectionSpecs(listOf(spec))
    }

    open fun addInterceptors(client: OkHttpClient.Builder) {
        //No interceptors for secure OKHttpClient
    }

    protected open fun makeSslContext(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, null, null)
        return sslContext
    }

    private fun setupClient(client: OkHttpClient.Builder, cache: Cache, cookieManager: PersistentCookiesCookieJar) {
        client.cache(cache)
        client.followRedirects(true)
        client.cookieJar(cookieManager)
        client.connectTimeout(10, TimeUnit.SECONDS)
        client.readTimeout(60L, TimeUnit.SECONDS)
    }

    private fun makeOkHttpClientBuilder(): OkHttpClient.Builder {
        try {
            ProviderInstaller.installIfNeeded(context)
        } catch (e: Exception) {
            // rely on the PlayServices checking code that runs when first activity starts
            // to guide the user through the recovery process
        }

        val clientBuilder = OkHttpClient().newBuilder()
        setupClient(clientBuilder, cache, cookieManager)
        addInterceptors(clientBuilder)
        setupSSLSocketFactoryAndConnectionSpec(clientBuilder, makeSslContext())

        return clientBuilder
    }

    private fun getSecureX509TrustManager(): X509TrustManager {
        val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keystore: KeyStore? = null
        factory.init(keystore)
        return factory.trustManagers[0] as X509TrustManager
    }
}
