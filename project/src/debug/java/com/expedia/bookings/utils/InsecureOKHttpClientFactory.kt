package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.mobiata.android.Log
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

open class InsecureOKHttpClientFactory(context: Context, cookieManager: PersistentCookiesCookieJar, cache: Cache, logLevel: HttpLoggingInterceptor.Level, chuckInterceptor: ChuckInterceptor) : SecureOKHttpClientFactory(context, cookieManager, cache, logLevel, chuckInterceptor) {

    override fun setupClient(client: OkHttpClient.Builder, cache: Cache, cookieManager: PersistentCookiesCookieJar) {
        super.setupClient(client, cache, cookieManager)

        StethoShim.install(client)
    }

    override fun addInterceptors(client: OkHttpClient.Builder, logLevel: HttpLoggingInterceptor.Level, chuckInterceptor: ChuckInterceptor?) {
        super.addInterceptors(client, logLevel, chuckInterceptor)

        if (!ExpediaBookingApp.isAutomation()) {
            client.addInterceptor(chuckInterceptor)
        }
    }

    override fun setupSSLSocketFactoryAndConnectionSpec(client: OkHttpClient.Builder, sslContext: SSLContext) {

        configureClientToAcceptAnyServer(client)
    }

    private fun configureClientToAcceptAnyServer(client: OkHttpClient.Builder) {

        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = makeTrustAllCertificatesTrustManager()

            // Install the all-trusting trust manager
            val ssContext = SSLContext.getInstance("SSL")
            ssContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val ssSocketFactory = ssContext.socketFactory
            client.sslSocketFactory(ssSocketFactory, trustAllCerts[0] as X509TrustManager)

            val hostnameVerifier = HostnameVerifier { hostname, session -> true }
            client.hostnameVerifier(hostnameVerifier)

            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                                     .allEnabledCipherSuites()
                                     .build()
            client.connectionSpecs(listOf(spec))

        } catch (e: Exception) {
            Log.e("", "Something went wrong and I couldn't setup the okhttp client to support any server", e)
        }
    }

    private fun makeTrustAllCertificatesTrustManager(): Array<TrustManager> {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })
        return trustAllCerts
    }
}
