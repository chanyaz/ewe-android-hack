package com.expedia.bookings.testrule

import com.expedia.bookings.interceptors.MockInterceptor
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import rx.Scheduler
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import kotlin.properties.Delegates

open class ServicesRule<T : Any>(val servicesClass: Class<T>, val rootPath: String = "../lib/mocked/templates", val setExpediaDispatcher: Boolean = true) : TestRule {
    var server: MockWebServer by Delegates.notNull()
    var services: T? = null

    public fun setDefaultExpediaDispatcher(){
        server.setDispatcher(diskExpediaDispatcher())
    }

    override fun apply(base: Statement, description: Description): Statement {
        server = MockWebServer()
        if(setExpediaDispatcher) {
            server.setDispatcher(diskExpediaDispatcher())
        }

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

        val client = OkHttpClient().newBuilder()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        client.addInterceptor(logger)
        client.addInterceptor(MockInterceptor())

        return servicesClass.getConstructor(String::class.java, OkHttpClient::class.java, Scheduler::class.java,
                Scheduler::class.java).newInstance("http://localhost:" + server.port, client.build(),
                Schedulers.immediate(), Schedulers.immediate())
    }

    private fun diskExpediaDispatcher(): ExpediaDispatcher {
        val root: String
        try {
            root = File(rootPath).canonicalPath
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        val opener = FileSystemOpener(root)
        return ExpediaDispatcher(opener)
    }
}
