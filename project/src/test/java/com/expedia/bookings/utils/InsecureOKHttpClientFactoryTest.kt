package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.http.CaptchaRedirectInterceptor
import com.expedia.bookings.server.EndPoint
import com.expedia.bookings.server.EndpointProviderInterface
import com.expedia.bookings.services.PersistentCookiesCookieJar
import com.expedia.bookings.test.robolectric.RobolectricRunner
import okhttp3.Cache
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class InsecureOKHttpClientFactoryTest {
    lateinit var sut: InsecureOKHttpClientFactory
    lateinit var context: Context
    lateinit var builder: OkHttpClient.Builder
    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = InsecureOKHttpClientFactory(context, MockPersistentCookiesCookieJar(), makeCache(), MockEndpointProvider(), Interceptor { it.proceed(it.request()) })
        builder = OkHttpClient.Builder()
    }

    @Test
    fun testCaptchaInterceptorAdded() {
        assertTrue(builder.interceptors().isEmpty())
        sut.addInterceptors(builder)
        val captchaInterceptors = builder.interceptors().filter {
            it is CaptchaRedirectInterceptor
        }
        assertEquals(1, captchaInterceptors.size)
    }

    class MockPersistentCookiesCookieJar : PersistentCookiesCookieJar {
        override fun removeNamedCookies(endpointURL: String?, names: Array<out String>?) {
        }

        override fun setMC1Cookie(guid: String?, posUrl: String?) {
        }

        override fun clear() {
        }

        override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>?) {
        }

        override fun loadForRequest(url: HttpUrl?): MutableList<Cookie> {
            return mutableListOf()
        }
    }

    fun makeCache(): Cache {
        val directory = File(context.cacheDir, "okhttp")
        val size = (10).toLong()
        return Cache(directory, size)
    }

    class MockEndpointProvider : EndpointProviderInterface {
        override val endPoint: EndPoint = EndPoint.DEV
    }
}
