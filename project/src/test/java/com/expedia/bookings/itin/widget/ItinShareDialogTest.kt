package com.expedia.bookings.itin.widget

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.LabeledIntent
import android.content.pm.ResolveInfo
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import com.expedia.account.BuildConfig
import com.expedia.bookings.itin.common.ItinShareDialog
import com.expedia.bookings.itin.utils.ShareItinTextCreator
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.ItinContentGenerator
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinShareDialogTest {

    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    @Test
    fun testShareDialogShown() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        val sut = ItinShareDialog(activity)
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

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    @Config(constants = BuildConfig::class, sdk = [26])
    fun testShareItinDialogEmailAndSMSApp() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        val shadowPackageManager = Shadows.shadowOf(activity.packageManager)

        val appList = ArrayList<ResolveInfo>()
        val resolveInfo = ResolveInfo()
        val activityInfo = ActivityInfo()
        activityInfo.packageName = "android.email"
        activityInfo.name = "Email"
        resolveInfo.activityInfo = activityInfo
        appList.add(resolveInfo)

        shadowPackageManager.addResolveInfoForIntent(Intent(Intent.ACTION_SEND), appList)
        val sut = ItinShareDialog(activity)

        sut.showItinShareDialog(MockShareItinTextCreator(), "TRIP")

        val shadowActivity = Shadows.shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult().intent

        @Suppress("UNCHECKED_CAST")
        val emailIntents = intent.extras[Intent.EXTRA_INITIAL_INTENTS] as Array<LabeledIntent>

        val sendIntent = intent.extras[Intent.EXTRA_INTENT] as Intent

        assertTrue(emailIntents.size == 1)
        assertEquals("Email Subject", emailIntents.first().getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Email Body", emailIntents.first().getStringExtra(Intent.EXTRA_TEXT))
        assertEquals(null, sendIntent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("SMS Body", sendIntent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    @Config(constants = BuildConfig::class, sdk = [26])
    @Suppress("UNCHECKED_CAST")
    fun testShareItinDialogNoEmailApps() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        val sut = ItinShareDialog(activity)
        sut.showItinShareDialog(MockShareItinTextCreator(), "TRIP")

        val appList = ArrayList<ResolveInfo>()
        val resolveInfo = ResolveInfo()
        val activityInfo = ActivityInfo()
        activityInfo.packageName = "android.not.email"
        activityInfo.name = "Not Email"
        resolveInfo.activityInfo = activityInfo
        appList.add(resolveInfo)

        val shadowActivity = Shadows.shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult().intent

        @Suppress("UNCHECKED_CAST")
        val emailIntents = intent.extras[Intent.EXTRA_INITIAL_INTENTS] as Array<LabeledIntent>

        val sendIntent = intent.extras[Intent.EXTRA_INTENT] as Intent

        assertTrue(emailIntents.isEmpty())
        assertEquals(null, sendIntent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("SMS Body", sendIntent.getStringExtra(Intent.EXTRA_TEXT))
    }

    class MockShareItinTextCreator : ShareItinTextCreator {
        override fun getEmailSubject(): String {
            return "Email Subject"
        }

        override fun getEmailBody(): String {
            return "Email Body"
        }

        override fun getSmsBody(): String {
            return "SMS Body"
        }
    }
}
