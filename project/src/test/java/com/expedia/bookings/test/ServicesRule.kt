package com.expedia.bookings.test

import com.expedia.bookings.interceptors.MockInterceptor
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import rx.Scheduler
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import kotlin.properties.Delegates

class ServicesRule<T : Any>(val servicesClass: Class<T>) : TestRule {
    var server: MockWebServer by Delegates.notNull()
    var services: T? = null

    override fun apply(base: Statement, description: Description): Statement {
        server = MockWebServer()
        server.setDispatcher(diskExpediaDispatcher())

        val createService = object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                services = generateServices()
                base.evaluate()
                services = null
            }
        }

        return server.apply(createService, description)
    }

    @Throws(IllegalAccessException::class, InstantiationException::class, NoSuchMethodException::class, InvocationTargetException::class)
    private fun generateServices(): T {

        return servicesClass.getConstructor(String::class.java, OkHttpClient::class.java, RequestInterceptor::class.java, Scheduler::class.java,
                Scheduler::class.java, RestAdapter.LogLevel::class.java).newInstance("http://localhost:" + server.port, OkHttpClient(), MockInterceptor(),
                Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL)
    }

    private fun diskExpediaDispatcher(): ExpediaDispatcher {
        val root: String
        try {
            root = File("../lib/mocked/templates").canonicalPath
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        val opener = FileSystemOpener(root)
        return ExpediaDispatcher(opener)
    }
}
