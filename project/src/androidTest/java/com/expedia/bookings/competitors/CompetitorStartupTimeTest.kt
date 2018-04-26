package com.expedia.bookings.competitors

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.uiautomator.By
import android.support.test.uiautomator.BySelector
import android.support.test.uiautomator.UiObject
import android.support.test.uiautomator.UiObject2
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
import com.expedia.bookings.test.espresso.Common.isOneOfUiObjectsPresent
import com.expedia.bookings.test.espresso.Common.uiAutomation
import com.expedia.bookings.test.espresso.Common.device

@RunWith(AndroidJUnit4::class)
class CompetitorStartupTimeTest {

    var classWatchman: TestRule = object : TestWatcher() {
        override fun succeeded(description: Description?) {
            createStartupTimeResultsFile()
        }
    }
        @Rule get

    private lateinit var context: Context

    private val startupTimeMapping = HashMap<String, ArrayList<Long>>()

    private fun getAppInfoList(): ArrayList<AppInfo> {
        val appInfoList = ArrayList<AppInfo>()

        appInfoList.add(AppInfo("Expedia", "com.expedia.bookings",
                arrayListOf(ReadyTrigger.HOTELS)))
        appInfoList.add(AppInfo("Priceline", "com.priceline.android.negotiator",
                arrayListOf(ReadyTrigger.HOTELS)))
        appInfoList.add(AppInfo("Booking.com Hotels", "com.booking",
                arrayListOf(ReadyTrigger.SEARCH)))
        appInfoList.add(AppInfo("KAYAK", "com.kayak.android",
                arrayListOf(ReadyTrigger.FLIGHTS_UCASE)))
        appInfoList.add(AppInfo("Agoda", "com.agoda.mobile.consumer",
                arrayListOf(ReadyTrigger.FLIGHTS)))
        appInfoList.add(AppInfo("Skyscanner", "net.skyscanner.android.main",
                arrayListOf(ReadyTrigger.HOTELS)))
        appInfoList.add(AppInfo("TripAdvisor", "com.tripadvisor.tripadvisor",
                arrayListOf(ReadyTrigger.HOTELS)))
        appInfoList.add(AppInfo("Airbnb", "com.airbnb.android",
                arrayListOf(ReadyTrigger.AIRBNB)))

        return appInfoList
    }

    private val appInfoList = getAppInfoList()

    private val CrashTimeValue: Long = -1
    private val TimeOutTimeValue: Long = -2
    private val AppNotFoundTimeValue: Long = -3

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        grantReadWritePermission()
    }

    @Test
    @Throws(Throwable::class)
    fun testStartupTimeOfAllApps() {
        val testRepetition = 3
        clearRecentApps()

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
        val timeLogger = TimeLogger(pageName = appInfo.name)
        val appToTest = findAppInHomeScreen(appInfo.name)

        if (appToTest == null) {
            Log.d("StartupTime", appInfo.name + ": app not found")
            startupTimeMapping[appInfo.name]!!.add(AppNotFoundTimeValue)
            return
        }
        forceStopAppProcess(appInfo.packageName)

        timeLogger.startTime = System.currentTimeMillis()
        appToTest.click()
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

        clearRecentApps()
        forceStopAppProcess(appInfo.packageName)
    }

    private fun clearRecentApps() {
        val noRecentItemsSelectorCollection: ArrayList<BySelector> = ArrayList()
        noRecentItemsSelectorCollection.add(By.text("No recent"))
        noRecentItemsSelectorCollection.add(By.text("No recent items"))
        noRecentItemsSelectorCollection.add(By.text("No recent applications"))
        noRecentItemsSelectorCollection.add(By.text("No recently used apps"))

        val recentsViewSelector = By.res("com.android.systemui:id/recents_view")

        device.pressHome()
        device.pressRecentApps()
        device.wait(Until.findObject(recentsViewSelector), 10000)

        if (isOneOfUiObjectsPresent(noRecentItemsSelectorCollection)) {
            device.pressHome()
            device.wait(Until.gone(recentsViewSelector), 10000)
            return
        }

        val height = device.displayHeight
        val width = device.displayWidth

        //Swipe To The Right while
        var swipeCounter = 0
        while (device.findObject(recentsViewSelector) != null && swipeCounter < 50) {
            device.swipe(width / 8, height / 2, width, height / 2, 5)
            swipeCounter++
            Thread.sleep(300)

            if (isOneOfUiObjectsPresent(noRecentItemsSelectorCollection)) {
                device.pressHome()
                device.wait(Until.gone(recentsViewSelector), 10000)
                return
            }
        }
    }

    private fun waitForAppToLoad(appInfo: AppInfo): UiObject2? {
        val readyTriggerList = appInfo.readyTriggerList
        val startTime = System.currentTimeMillis()
        val timeout = 30000
        var bySelector: BySelector
        var uiSelector: UiSelector

        while (System.currentTimeMillis() - startTime < timeout) {
            for (readyTrigger in readyTriggerList) {
                bySelector = By.text(readyTrigger.triggerString)
                uiSelector = UiSelector().text(readyTrigger.triggerString)

                if (device.findObject(bySelector) != null &&
                        device.findObject(uiSelector) != null &&
                        device.findObject(bySelector).isEnabled &&
                        device.findObject(uiSelector).exists() &&
                        device.findObject(uiSelector).isEnabled) {
                    return device.findObject(bySelector)
                }
            }
        }
        return null
    }

    private fun forceStopAppProcess(packageName: String) {
        uiAutomation.executeShellCommand("am force-stop $packageName")
        Thread.sleep(3000)
    }

    private fun grantReadWritePermission() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant \"com.expedia.bookings.debug\" android.permission.READ_EXTERNAL_STORAGE")
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant \"com.expedia.bookings.debug\" android.permission.WRITE_EXTERNAL_STORAGE")
    }

    private fun findAppInHomeScreen(appName: String): UiObject? {
        val maxPageAttempt = 2
        var currentAttempt = 0
        val appToTest = device.findObject(UiSelector().textMatches(appName))
        while (currentAttempt < maxPageAttempt) {
            if (appToTest.exists()) {
                break
            }
            goNextPage()
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
        val stringBuilder = StringBuilder("")
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
                } else if (time == AppNotFoundTimeValue) {
                    stringBuilder.append("appNotFound")
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
        HOTELS("Hotels"),
        HOTELS_UCASE("HOTELS"),
        FLIGHTS("Flights"),
        FLIGHTS_UCASE("FLIGHTS"),
        SEARCH("Search"),
        AIRBNB("Explore Airbnb")
    }

    private data class AppInfo(val name: String, val packageName: String, var readyTriggerList: ArrayList<ReadyTrigger> = ArrayList<ReadyTrigger>())
}
