package com.expedia.bookings.itin.widget

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.ItinContentGenerator
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ShareItinDialogTest {

    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    @Test
    fun testShareDialogShown() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        val sut = ShareItinDialog(activity)
        val flightBuilder = ItinCardDataFlightBuilder()
        val itinCardData = flightBuilder.build()
        val mItinContentGenerator = ItinContentGenerator.createGenerator(activity, itinCardData)

        sut.showNativeShareDialog(mItinContentGenerator.shareTextShort, mItinContentGenerator.type.toString())
        val shadowActivity = Shadows.shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult().intent

        val startTime = flightBuilder.startTime
        val formattedStartTime = DateUtils.formatDateTime(activity, startTime.millis, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NUMERIC_DATE)
        val expectedShareText = "I'm flying to Las Vegas on $formattedStartTime! https://www.expedia.com/m/trips/shared/Skjwnx0TSXPI_gAzTrkDcsCAIbyUbsTP02ftiXxiG7-a7LmXuYlvqWYakZdZb3A4nbHr4NK8dCRsxeYg6A-8zJUuaMKD5zEFQY5BJSzha7SgqPL1GQkwxnX0qAcoP8rJoGAJSAG6a-4EMd6zDOzeIh4_4QAm4Xodp_NEU6DMtA3AoA"

        assertEquals(expectedShareText, intent.getStringExtra(Intent.EXTRA_TEXT))
    }
}
