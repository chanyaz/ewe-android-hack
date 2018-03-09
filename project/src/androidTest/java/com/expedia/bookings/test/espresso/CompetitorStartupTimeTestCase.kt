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
import android.support.test.uiautomator.UiSelector
import android.support.test.uiautomator.Until
import android.util.Log
import com.expedia.bookings.tracking.TimeLogger
import org.junit.rules.TestWatcher
import org.junit.rules.TestRule
import org.junit.Rule
import org.junit.runner.Description
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class CompetitorStartupTimeTestCase {

    var classWatchman: TestRule = object : TestWatcher() {
        override fun succeeded(description: Description?) {
            createStartupTimeResultsFile()
        }
    }
        @Rule get

    private lateinit var device: UiDevice
    private lateinit var context: Context

    private val startupTimeMapping = HashMap<String, ArrayList<Long>>()

    private val appInfoList = arrayListOf(
            AppInfo("Priceline", "com.priceline.android.negotiator", ReadyTrigger.PERMISSION),
            AppInfo("Booking", "com.booking", ReadyTrigger.SIGNIN),
            AppInfo("KAYAK", "com.kayak.android", ReadyTrigger.SIGNIN),
            AppInfo("Agoda", "com.agoda.mobile.consumer", ReadyTrigger.PERMISSION),
            AppInfo("Airbnb", "com.airbnb.android", ReadyTrigger.CONTINUEWITH)
    )

    private val CrashTimeValue: Long = -1
    private val TimeOutTimeValue: Long = -2

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        grantReadWritePermission()
    }

    @Throws(Throwable::class)
    @Test
    fun testStartupTimeOfAllApps() {
        val testRepetition = 3

        for (appInfo in appInfoList) {
            for (i in 0 until testRepetition) {
                try {
                    testStartupTime(appInfo)
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

    private fun testStartupTime(appInfo: AppInfo) {
        clearRecentApps()

        val timeLogger = TimeLogger(pageName = appInfo.name)
        var appToTest = findAppInHomeScreen(appInfo.name)

        if (!appToTest.exists()) {
            Log.d("StartupTime", appInfo.name + ": app not found")
            return
        }

        clearAppData(appInfo.packageName)
        // Need a small wait, sometime app fail to launch right after clearing data
        Thread.sleep(1000)
        appToTest.click()

        timeLogger.startTime = System.currentTimeMillis()
        val view = device.wait(Until.findObject(By.textContains(appInfo.readyTrigger.triggerString)), 15000)
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

    private fun clearAppData(packageName: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm clear $packageName")
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
        CONTINUEWITH("Continue with")
    }

    private data class AppInfo(val name: String, val packageName: String, val readyTrigger: ReadyTrigger)
}
