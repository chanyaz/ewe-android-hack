package com.expedia.bookings.test.espresso

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiObject
import android.support.test.uiautomator.UiObject2
import android.support.test.uiautomator.UiSelector
import android.util.Log
import com.expedia.bookings.tracking.TimeLogger
import org.junit.rules.TestWatcher
import org.junit.rules.TestRule
import org.junit.Rule
import org.junit.runner.Description
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class CompetitorStartupTimeTest {
    annotation class ExcludeFromFork

    var classWatchman: TestRule = object : TestWatcher() {
        override fun succeeded(description: Description?) {
            createStartupTimeResultsFile()
        }
    }
        @Rule get

    private lateinit var device: UiDevice
    private lateinit var context: Context

    private val startupTimeMapping = HashMap<String, ArrayList<Long>>()

    private fun getAppInfoList(): ArrayList<AppInfo> {
        val appInfoList = ArrayList<AppInfo>()

        appInfoList.add(AppInfo("Expedia", "com.expedia.bookings",
                arrayListOf(ReadyTrigger.EXPEDIA)))
        appInfoList.add(AppInfo("Priceline", "com.priceline.android.negotiator",
                arrayListOf(ReadyTrigger.PERMISSION)))
        appInfoList.add(AppInfo("Booking", "com.booking",
                arrayListOf(ReadyTrigger.SIGNIN)))
        appInfoList.add(AppInfo("KAYAK", "com.kayak.android",
                arrayListOf(ReadyTrigger.SIGNIN)))
        appInfoList.add(AppInfo("Agoda", "com.agoda.mobile.consumer",
                arrayListOf(ReadyTrigger.PERMISSION)))
        appInfoList.add(AppInfo("Skyscanner", "net.skyscanner.android.main",
                arrayListOf(ReadyTrigger.SKYSCANNER)))
        appInfoList.add(AppInfo("TripAdvisor", "com.tripadvisor.tripadvisor",
                arrayListOf(ReadyTrigger.CONTINUE, ReadyTrigger.TRIPADVISOR)))
        appInfoList.add(AppInfo("Airbnb", "com.airbnb.android",
                arrayListOf(ReadyTrigger.CONTINUEWITH)))

        return appInfoList
    }

    private val appInfoList = getAppInfoList()

    private val CrashTimeValue: Long = -1
    private val TimeOutTimeValue: Long = -2

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        grantReadWritePermission()
    }

    @Throws(Throwable::class)
    @Test @ExcludeFromFork
    fun testStartupTimeOfAllApps() {
        val testRepetition = 3

        for (appInfo in appInfoList) {
            for (i in 0 until testRepetition) {
                try {
                    testColdStartupTime(appInfo)
                } catch (e: Exception) {
                    Log.d("StartupTime", appInfo.name + ": failed to run: " + e.localizedMessage)
                    if (startupTimeMapping[appInfo.name] == null) {
                        startupTimeMapping[appInfo.name] = ArrayList()
                    }
                    startupTimeMapping[appInfo.name]!!.add(CrashTimeValue)
                }
            }
        }
    }

    private fun testColdStartupTime(appInfo: AppInfo) {
        clearRecentApps()

        val timeLogger = TimeLogger(pageName = appInfo.name)
        val appToTest = findAppInHomeScreen(appInfo.name)

        if (!appToTest.exists()) {
            Log.d("StartupTime", appInfo.name + ": app not found")
            return
        }

        forceStopAppProcess(appInfo.packageName)
        Thread.sleep(1000)
        clearAppData(appInfo.packageName)
        // Need a small wait, sometime app fail to launch right after clearing data
        Thread.sleep(1000)
        appToTest.click()

        timeLogger.startTime = System.currentTimeMillis()
        val view = waitForAppToLoad(appInfo)
        timeLogger.endTime = System.currentTimeMillis()
        if (startupTimeMapping[appInfo.name] == null) {
            startupTimeMapping[appInfo.name] = ArrayList()
        }
        if (view == null) {
            startupTimeMapping[appInfo.name]!!.add(TimeOutTimeValue)
        } else {
            startupTimeMapping[appInfo.name]!!.add(timeLogger.calculateTotalTime())
        }
    }

    private fun clearRecentApps() {
        device.pressHome()
        device.pressRecentApps()

        val noRecent = device.findObject(UiSelector().textContains("No recent"))
        if (noRecent.exists()) {
            device.pressHome()
            return
        }

        val closeAllButton = device.findObject(UiSelector().textContains("CLOSE ALL"))
        if (closeAllButton.exists()) {
            closeAllButton.click()
            device.pressHome()
            return
        }

        val height = device.displayHeight
        val width = device.displayWidth

        val maxScrollAttempt = 2
        var currentAttempt = 0
        var clearAllButton = device.findObject(UiSelector().textContains("CLEAR ALL"))
        while (!clearAllButton.exists() && currentAttempt < maxScrollAttempt) {
            device.swipe(width / 2, height / 4, width / 2, height, 25)
            clearAllButton = device.findObject(UiSelector().textContains("CLEAR ALL"))
            currentAttempt++
        }

        if (clearAllButton.exists()) {
            clearAllButton.click()
        }

        device.pressHome()
    }

    private fun waitForAppToLoad(appInfo: AppInfo): UiObject2? {
        val readyTriggerList = appInfo.readyTriggerList
        val startTime = System.currentTimeMillis()
        val timeout = 30000

        while (System.currentTimeMillis() - startTime < timeout) {
            for (readyTrigger in readyTriggerList) {
                val view = device.findObject(By.textContains(readyTrigger.triggerString))
                if (view != null) {
                    return view
                }
            }
        }
        return null
    }

    private fun clearAppData(packageName: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm clear $packageName")
    }

    private fun forceStopAppProcess(packageName: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("am force-stop $packageName")
    }

    private fun grantReadWritePermission() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant \"com.expedia.bookings.debug\" android.permission.READ_EXTERNAL_STORAGE")
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant \"com.expedia.bookings.debug\" android.permission.WRITE_EXTERNAL_STORAGE")
    }

    private fun findAppInHomeScreen(appName: String): UiObject {
        val maxPageAttempt = 2
        var currentAttempt = 0
        var appToTest = device.findObject(UiSelector().textMatches(appName))
        while (!appToTest.exists() && currentAttempt < maxPageAttempt) {
            goNextPage()
            appToTest = device.findObject(UiSelector().textStartsWith(appName))
            currentAttempt++
        }
        return appToTest
    }

    private fun goNextPage() {
        val height = device.displayHeight
        val width = device.displayWidth
        device.swipe(width - 1, height / 2, width / 2, height / 2, 25)
    }

    @Throws(IOException::class)
    private fun createStartupTimeResultsFile() {
        val folderName = "/startupTime/"
        val filerName = "competitorStartupTimeResults.txt"

        val startupTimePath = context.filesDir.path + folderName
        val startupTimeDirectory = File(startupTimePath)
        if (!startupTimeDirectory.exists()) {
            startupTimeDirectory.mkdirs()
        }
        val file = File(startupTimePath + filerName)
        var stringBuilder = StringBuilder("")
        for ((appName, timeList) in startupTimeMapping) {
            stringBuilder.append(appName)
            stringBuilder.append(System.getProperty("line.separator"))
            stringBuilder.append("- Time(ms): ")
            var totalTime: Long = 0
            for (time in timeList) {
                if (time == CrashTimeValue) {
                    stringBuilder.append("crash")
                    stringBuilder.append(", ")
                } else if (time == TimeOutTimeValue) {
                    stringBuilder.append("timeout")
                    stringBuilder.append(", ")
                } else {
                    stringBuilder.append(time)
                    stringBuilder.append(", ")
                    totalTime += time
                }
            }
            stringBuilder.append(System.getProperty("line.separator"))
            val averageTime = 1.0 * totalTime / timeList.size
            stringBuilder.append("- Average: ")
            stringBuilder.append(averageTime)
            stringBuilder.append(System.getProperty("line.separator"))
        }
        file.writeText(stringBuilder.toString())
    }

    private enum class ReadyTrigger(val triggerString: String) {
        PERMISSION("ALLOW"),
        SIGNIN("Sign in"),
        CONTINUEWITH("Continue with"),
        CONTINUE("CONTINUE"),
        EXPEDIA("Book on the go"),
        SKYSCANNER("The world’s travel search engine"),
        TRIPADVISOR("Meet your ultimate travel companion"),
        SPLASHSCREENNEXT("Next")
    }

    private data class AppInfo(val name: String, val packageName: String, var readyTriggerList: ArrayList<ReadyTrigger> = ArrayList<ReadyTrigger>())
}
