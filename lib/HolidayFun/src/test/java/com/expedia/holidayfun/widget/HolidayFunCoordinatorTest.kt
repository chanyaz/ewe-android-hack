package com.expedia.holidayfun.widget

import android.content.Context
import android.view.View
import com.expedia.holidayfun.BuildConfig
import com.jetradarmobile.snowfall.SnowfallView
import junit.framework.Assert.assertTrue
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class HolidayFunCoordinatorTest {

    private val context: Context by lazy {
        RuntimeEnvironment.application
    }

    private lateinit var holidayFunWidget: AnimatedHolidayFunWidget
    private lateinit var snowfall: SnowfallView
    private lateinit var sut: HolidayFunCoordinator

    @Before
    fun setup() {
        holidayFunWidget = AnimatedHolidayFunWidget(context, Robolectric.buildAttributeSet().build())
        snowfall = SnowfallView(context, Robolectric.buildAttributeSet().build())
        sut = HolidayFunCoordinator(holidayFunWidget, snowfall)
    }

    @Test
    fun startingHolidayFunIsTracked() {
        var analyticsFired = false
        sut.setAnalyticsListener { analyticsFired = true }
        holidayFunWidget.callOnClick()

        assertTrue(analyticsFired)
    }

    @Test
    fun settingVisibilityGoneMakesEverythingGone() {
        holidayFunWidget.visibility = View.VISIBLE
        snowfall.visibility = View.VISIBLE

        sut.visibility = View.GONE

        assertEquals(View.GONE, holidayFunWidget.visibility)
        assertEquals(View.GONE, snowfall.visibility)
    }

    @Test
    fun settingVisibilityVisibleMakesOnlyHolidayFunWidgetVisible() {
        holidayFunWidget.visibility = View.GONE
        snowfall.visibility = View.GONE

        sut.visibility = View.VISIBLE

        assertEquals(View.VISIBLE, holidayFunWidget.visibility)
        assertEquals(View.GONE, snowfall.visibility)
    }
}