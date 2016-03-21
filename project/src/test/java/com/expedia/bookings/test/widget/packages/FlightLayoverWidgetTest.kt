package com.expedia.bookings.test

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.packages.FlightLayoverWidget
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class FlightLayoverWidgetTest {
    var testWidget: TestFlightLayoverWidget by Delegates.notNull()

    val SEGMENT_DURATION = 5
    val LEG_DURATION = SEGMENT_DURATION * 2
    val MAX_LEG_DURATION = LEG_DURATION * 2
    val LEG_OVER_MAX = MAX_LEG_DURATION + 5
    val X_COORD = 0f

    val DURATION_BAR_PADDING = 5f

    val DEPART_CODE = "ORD"
    val LAYOVER_CODE = "JFK"
    val ARRIVE_CODE = "LHR"

    @Before
    fun before() {
        testWidget = TestFlightLayoverWidget(RuntimeEnvironment.application, null)
    }

    @Test
    fun testDurationBarAboveMaxDuration() {
        val expectedWidth = 10f
        testWidget.legDuration = LEG_OVER_MAX.toFloat()
        testWidget.totalWidthForDurationBars = expectedWidth

        val durationBarRect = testWidget.createDurationBar(0, LEG_OVER_MAX, X_COORD, 0f, 0f)

        assertEquals(X_COORD, durationBarRect.left)
        assertEquals(expectedWidth, durationBarRect.right)
    }

    @Test
    fun testFullDurationBarHalfOfMax() {
        val expectedWidth = 5f
        testWidget.totalWidthForDurationBars = expectedWidth * 2
        testWidget.maxLegDuration = MAX_LEG_DURATION.toFloat()
        testWidget.legDuration = LEG_DURATION.toFloat()

        val durationBarRect = testWidget.createDurationBar(0, LEG_DURATION, X_COORD, 0f, 0f)

        assertEquals(X_COORD, durationBarRect.left)
        assertEquals(expectedWidth, durationBarRect.right)
    }

    @Test
    fun testHalfDurationBarMaxLeg() {
        val expectedWidth = 5f
        testWidget.totalWidthForDurationBars = expectedWidth * 2
        testWidget.maxLegDuration = MAX_LEG_DURATION.toFloat()
        testWidget.legDuration = MAX_LEG_DURATION.toFloat()

        val durationBarRect = testWidget.createDurationBar(0, LEG_DURATION, X_COORD, 0f, 0f)
        assertEquals(X_COORD, durationBarRect.left)
        assertEquals(expectedWidth, durationBarRect.right)
    }

    @Test
    fun testHalfDurationHalfMax() {
        val expectedWidth = 5f
        testWidget.totalWidthForDurationBars = expectedWidth * 4
        testWidget.maxLegDuration = MAX_LEG_DURATION.toFloat()
        testWidget.legDuration = LEG_DURATION.toFloat()

        val durationBarRect = testWidget.createDurationBar(0, SEGMENT_DURATION, X_COORD, 0f, 0f)
        assertEquals(X_COORD, durationBarRect.left)
        assertEquals(expectedWidth, durationBarRect.right)
    }

    @Test
    fun testAirportWidthCalculation() {
        val expectedAirportWidth = testWidget.CODE_WIDTH * 2
        val expectedDurationPadding = testWidget.durationBarPadding * 2
        val expectedWidth = expectedAirportWidth + expectedDurationPadding + testWidget.LEFT_RIGHT_PADDING * 2 + testWidget.OFF_BY_ONE_BUFFER

        addAirport(DEPART_CODE, ARRIVE_CODE, 0)

        val widthOfAirports = testWidget.calculateLocationsAndPaddingWidth()

        assertEquals(expectedWidth, widthOfAirports)
    }

    @Test
    fun testAirportsPopulated() {
        addAirport(DEPART_CODE, ARRIVE_CODE, 0)
        testWidget.createDrawObjects()
        assertFalse(testWidget.drawObjects.isEmpty())

        assertEquals(DEPART_CODE, testWidget.drawObjects[0].locationCode)
        assertEquals(ARRIVE_CODE, testWidget.drawObjects[1].locationCode)
    }

    @Test
    fun testAirportCodesWithLayover() {
        addAirport(DEPART_CODE, LAYOVER_CODE, 5)
        addAirport(LAYOVER_CODE, ARRIVE_CODE, 0)
        testWidget.createDrawObjects()
        assertFalse(testWidget.drawObjects.isEmpty())

        assertEquals(DEPART_CODE, testWidget.drawObjects[0].locationCode)
        assertEquals(LAYOVER_CODE, testWidget.drawObjects[1].locationCode)
        assertEquals(ARRIVE_CODE, testWidget.drawObjects[2].locationCode)
    }

    private fun addAirport(departCode: String, arriveCode: String, layoverDurationHours: Int) {
        val testFlightSegment = FlightLeg.FlightSegment()
        testFlightSegment.departureAirportCode = departCode
        testFlightSegment.arrivalAirportCode = arriveCode
        testFlightSegment.layoverDurationHours = layoverDurationHours
        testWidget.flightSegmentList.add(testFlightSegment)
    }

    class TestFlightLayoverWidget(context: Context, attrs: AttributeSet?) : FlightLayoverWidget(context, attrs) {
        val CODE_WIDTH = 5
        val LEFT_RIGHT_PADDING = 10

        override fun getPaddingLeft(): Int {
            return LEFT_RIGHT_PADDING
        }

        override fun getPaddingRight(): Int {
            return LEFT_RIGHT_PADDING
        }

        override fun calculateTextBounds(airportCode: String) : Rect {
            val mockRect = Mockito.mock(Rect::class.java)
            Mockito.`when`(mockRect.width()).thenReturn(CODE_WIDTH)
            return mockRect;
        }
    }
}