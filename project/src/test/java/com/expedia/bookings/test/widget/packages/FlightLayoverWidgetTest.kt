package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.widget.packages.FlightLayoverWidget
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import kotlin.properties.Delegates
import kotlin.test.assertEquals

public class FlightLayoverWidgetTest {
    var testWidget: TestFlightLayoverWidget by Delegates.notNull()

    val SEGMENT_DURATION = 5
    val LEG_DURATION = SEGMENT_DURATION * 2
    val MAX_LEG_DURATION = LEG_DURATION * 2
    val LEG_OVER_MAX = MAX_LEG_DURATION + 5
    val X_COORD = 0f

    val DURATION_BAR_PADDING = 5f

    val DEPART_CODE = "ORD"
    val ARRIVE_CODE = "LHR"

    @Before
    fun before() {
        val context = Mockito.mock(Context::class.java)
        val resources = Mockito.mock(Resources::class.java)
        val mockTheme = Mockito.mock(Resources.Theme::class.java)
        val mockAttrs = Mockito.mock(TypedArray::class.java)
        val mockDrawable = Mockito.mock(BitmapDrawable::class.java)

        Mockito.`when`(context.resources).thenReturn(resources)
        Mockito.`when`(context.theme).thenReturn(mockTheme)
        Mockito.`when`(mockTheme.obtainStyledAttributes(Matchers.any(), Matchers.any(), Matchers.anyInt(),Matchers.anyInt()))
                .thenReturn(mockAttrs)
        Mockito.`when`(mockAttrs.getDrawable(Matchers.anyInt())).thenReturn(mockDrawable)
        Mockito.`when`(mockAttrs.getDimension(Matchers.anyInt(), Matchers.anyFloat())).thenReturn(DURATION_BAR_PADDING)
        Mockito.`when`(resources.getColor(Matchers.anyInt())).thenReturn(0)
        testWidget = TestFlightLayoverWidget(context, null)
    }

    @Test
    fun testDurationBarAboveMaxDuration() {
        val expectedWidth = 10f
        testWidget.flightLegDuration = LEG_OVER_MAX.toFloat()
        testWidget.totalWidthForDurationBars = expectedWidth

        val durationBar = testWidget.createDurationBar(0, LEG_OVER_MAX, X_COORD)

        assertEquals(X_COORD, durationBar.leftX)
        assertEquals(expectedWidth, durationBar.rightX)
    }

    @Test
    fun testFullDurationBarHalfOfMax() {
        val expectedWidth = 5f
        testWidget.totalWidthForDurationBars = expectedWidth * 2
        testWidget.maxLegDuration = MAX_LEG_DURATION.toFloat()
        testWidget.flightLegDuration = LEG_DURATION.toFloat()

        val durationBar = testWidget.createDurationBar(0, LEG_DURATION, X_COORD)

        assertEquals(X_COORD, durationBar.leftX)
        assertEquals(expectedWidth, durationBar.rightX)
    }

    @Test
    fun testHalfDurationBarMaxLeg() {
        val expectedWidth = 5f
        testWidget.totalWidthForDurationBars = expectedWidth * 2
        testWidget.maxLegDuration = MAX_LEG_DURATION.toFloat()
        testWidget.flightLegDuration = MAX_LEG_DURATION.toFloat()

        val durationBar = testWidget.createDurationBar(0, LEG_DURATION, X_COORD)
        assertEquals(X_COORD, durationBar.leftX)
        assertEquals(expectedWidth, durationBar.rightX)
    }

    @Test
    fun testHalfDurationHalfMax() {
        val expectedWidth = 5f
        testWidget.totalWidthForDurationBars = expectedWidth * 4
        testWidget.maxLegDuration = MAX_LEG_DURATION.toFloat()
        testWidget.flightLegDuration = LEG_DURATION.toFloat()

        val durationBar = testWidget.createDurationBar(0, SEGMENT_DURATION, X_COORD)
        assertEquals(X_COORD, durationBar.leftX)
        assertEquals(expectedWidth, durationBar.rightX)
    }

    @Test
    fun testAirportWidthCalculation() {
        val expectedAirportWidth = testWidget.CODE_WIDTH * 2
        val expectedDurationPadding = testWidget.durationBarPadding * 2
        val expectedWidth = expectedAirportWidth + expectedDurationPadding + testWidget.LEFT_RIGHT_PADDING * 2 + testWidget.OFF_BY_ONE_BUFFER

        addAirport(DEPART_CODE, ARRIVE_CODE)

        val widthOfAirports = testWidget.calculateAirportsAndPaddingWidth()

        assertEquals(expectedWidth, widthOfAirports)
    }

    private fun addAirport(departCode: String, arriveCode: String) {
        val testFlightSegment = FlightLeg.FlightSegment()
        testFlightSegment.departureAirportCode = departCode
        testFlightSegment.arrivalAirportCode = arriveCode
        testWidget.flightSegmentList.add(testFlightSegment)
    }

    class TestFlightLayoverWidget(context: Context, attrs: AttributeSet?) : FlightLayoverWidget(context, attrs) {
        val CODE_WIDTH = 5
        val LEFT_RIGHT_PADDING = 10

        override fun initPaints() {
            // Do nothing
        }

        override fun calculateTextBounds(airportCode: String) : Rect {
            val mockRect = Mockito.mock(Rect::class.java)
            Mockito.`when`(mockRect.width()).thenReturn(CODE_WIDTH)
            return mockRect;
        }

        override fun getPaddingLeft(): Int {
            return LEFT_RIGHT_PADDING
        }

        override fun getPaddingRight(): Int {
            return LEFT_RIGHT_PADDING
        }
    }
}