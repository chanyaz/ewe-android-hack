package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.server.EndPoint
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.mobiata.android.Log
import com.mobiata.android.util.SettingUtils
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

open class InsecureOKHttpClientFactory(context: Context, cookieManager: PersistentCookiesCookieJar, cache: Cache, endpointProvider: EndpointProvider) : SecureOKHttpClientFactory(context, cookieManager, cache, endpointProvider) {

    override fun addInterceptors(client: OkHttpClient.Builder) {
        super.addInterceptors(client)

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        client.addInterceptor(logger)

        if (!ExpediaBookingApp.isAutomation()) {
            val chuckInterceptor = ChuckInterceptor(context)
            chuckInterceptor.showNotification(SettingUtils.get(context, context.getString(R.string.preference_enable_chuck_notification), false))
            client.addInterceptor(chuckInterceptor)

            setupStetho()
            client.networkInterceptors().add(StethoInterceptor())
        }

        if (ExpediaBookingApp.isInstrumentation()) {
            client.addNetworkInterceptor(UITestRequestInterceptor())
        }
    }

    override fun setupSSLSocketFactoryAndConnectionSpec(client: OkHttpClient.Builder, sslContext: SSLContext) {
        if (isModernHttpsSecurityEnabled()) {
            super.setupSSLSocketFactoryAndConnectionSpec(client, sslContext)
        }
        else {
            configureClientToAcceptAnyServer(client)
        }
    }

    override fun makeSslContext(): SSLContext {
        if (isModernHttpsSecurityEnabled()) {
            return super.makeSslContext()
        }
        else {
            val easyTrustManager = arrayOf(getInsecureX509TrustManager())
            val socketContext = SSLContext.getInstance("TLS")
            socketContext.init(null, easyTrustManager, java.security.SecureRandom())
            return socketContext
        }
    }

    private fun setupStetho() {
        Stetho.initialize(Stetho.newInitializerBuilder(context)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
                .build())
    }

    private fun getInsecureX509TrustManager(): X509TrustManager {
        return object: X509TrustManager {

            override fun getAcceptedIssuers(): Array<out X509Certificate> {
                return emptyArray()
            }

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }
        }
    }

    private fun isModernHttpsSecurityEnabled(): Boolean {
        val isMockMode = endpointProvider.endPoint == EndPoint.MOCK_MODE
        val isCustomServer = endpointProvider.endPoint == EndPoint.CUSTOM_SERVER
        if (ExpediaBookingApp.isAutomation() || isMockMode || isCustomServer) {
            return false
        }

        return !SettingUtils.get(context, context.getString(R.string.preference_disable_modern_https_security), false)
    }

    private fun configureClientToAcceptAnyServer(client: OkHttpClient.Builder) {

        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = makeInsecureTrustAllCertificatesTrustManager()

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val ssSocketFactory = sslContext.socketFactory
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

    private fun makeInsecureTrustAllCertificatesTrustManager(): Array<TrustManager> {
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
