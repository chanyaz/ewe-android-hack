package com.expedia.bookings.marketing.carnival

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.marketing.carnival.view.FullPageDealShopButtonWidget
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Constants
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowLooper
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FullPageDealNotificationActivityTest {

    @Test
    fun fullscreenDealPageShowsContentFromCarnivalMessage() {
        val messageAttributes = createCarnivalMessageAttributes()
        val carnivalMessage = CarnivalMessage("someUrl", "some title,", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra("carnival_message", carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()

        assertEquals("some title", activity.findViewById<TextView>(R.id.deal_title).text)
        assertEquals("some title", activity.findViewById<Toolbar>(R.id.toolbar).title)
        assertEquals("some instructions", activity.findViewById<TextView>(R.id.deal_instructions).text)
        assertEquals("some awesome promo code", activity.findViewById<TextView>(R.id.promo_code).text)
        assertEquals("details title", activity.findViewById<TextView>(R.id.details_title).text)
        assertEquals("details description", activity.findViewById<TextView>(R.id.offer_details_description).text)
        assertEquals("terms title", activity.findViewById<TextView>(R.id.terms_title).text)
        assertEquals("some description about terms", activity.findViewById<TextView>(R.id.terms_description).text)
        assertEquals("shop button text", activity.findViewById<TextView>(R.id.shop_button_text).text)
    }

    @Test
    fun viewsAreHidden_givenCarnivalMessageAttributesAreEmpty() {
        val messageAttributes = createEmptyCarnivalMessageAttributes()
        val carnivalMessage = CarnivalMessage("someUrl", "some title", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra("carnival_message", carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()

        assertEquals(View.GONE, activity.findViewById<TextView>(R.id.deal_title).visibility)
        assertEquals(View.GONE, activity.findViewById<TextView>(R.id.deal_instructions).visibility)
        assertEquals(View.GONE, activity.findViewById<TextView>(R.id.details_title).visibility)
        assertEquals(View.GONE, activity.findViewById<TextView>(R.id.offer_details_description).visibility)
        assertEquals(View.GONE, activity.findViewById<TextView>(R.id.terms_title).visibility)
        assertEquals(View.GONE, activity.findViewById<TextView>(R.id.terms_description).visibility)
        assertEquals(View.GONE, activity.findViewById<View>(R.id.details_horizontal_line).visibility)
        assertEquals(View.GONE, activity.findViewById<View>(R.id.terms_horizontal_line).visibility)
        assertEquals(View.GONE, activity.findViewById<CardView>(R.id.copy_code_button).visibility)
    }

    @Test
    fun deeplinkIsAttachedToIntentOnShopButtonClick() {
        val messageAttributes = createCarnivalMessageAttributes()
        val carnivalMessage = CarnivalMessage("someUrl", "some title,", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra(Constants.CARNIVAL_MESSAGE_DATA, carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()

        activity.findViewById<FullPageDealShopButtonWidget>(R.id.full_page_deal_shop_button).performClick()
        val shadowActivity = Shadows.shadowOf(activity)
        val startedIntent = shadowActivity.nextStartedActivity

        assertEquals(startedIntent.dataString, "expda://hotelSearch")
    }

    @Test
    fun shopButtonIsShownAfterOneSecond() {
        val messageAttributes = createCarnivalMessageAttributes()
        val carnivalMessage = CarnivalMessage("someUrl", "some title,", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra(Constants.CARNIVAL_MESSAGE_DATA, carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertEquals(View.VISIBLE, activity.findViewById<FullPageDealShopButtonWidget>(R.id.full_page_deal_shop_button).visibility)
    }

    @Test
    fun shopButtonTextIsSetToOk_givenDeeplinkNotSentOnCarnivalMessage() {
        val messageAttributes = createEmptyCarnivalMessageAttributes()
        val carnivalMessage = CarnivalMessage("someUrl", "some title,", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra(Constants.CARNIVAL_MESSAGE_DATA, carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertEquals("OK", activity.findViewById<TextView>(R.id.shop_button_text).text)
    }

    @Test
    fun shopButtonClickDismissesActivity_givenDeeplinkNotSentOnCarnivalMessage() {
        val messageAttributes = createEmptyCarnivalMessageAttributes()
        val carnivalMessage = CarnivalMessage("someUrl", "some title,", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra(Constants.CARNIVAL_MESSAGE_DATA, carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        activity.findViewById<FullPageDealShopButtonWidget>(R.id.full_page_deal_shop_button).performClick()

        assertEquals(true, activity.isFinishing)
    }

    @Test
    fun shopButtonTexIsSetToDefault_givenNoButtonTextOnCarnivalMessageButDeeplink() {
        val messageAttributes = createEmptyCarnivalMessageAttributes()
        messageAttributes[Constants.CARNIVAL_DEEPLINK] = "expda://hotelSearch"

        val carnivalMessage = CarnivalMessage("someUrl", "some title,", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra(Constants.CARNIVAL_MESSAGE_DATA, carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertEquals("See deal", activity.findViewById<TextView>(R.id.shop_button_text).text)
    }

    @Test
    fun clickingCopyButtonCopiesText() {
        val messageAttributes = createCarnivalMessageAttributes()
        val carnivalMessage = CarnivalMessage("someUrl", "some title,", messageAttributes, "some text")
        val intent = Intent()
        intent.putExtra("carnival_message", carnivalMessage)

        val activity = Robolectric.buildActivity(FullPageDealNotificationActivity::class.java, intent).create().get()
        activity.findViewById<CardView>(R.id.copy_code_button).performClick()

        val copiedText = ClipboardUtils.getText(activity)
        assertEquals("some awesome promo code", copiedText)
    }

    private fun createCarnivalMessageAttributes() = hashMapOf(Constants.CARNIVAL_TITLE to "some title",
            Constants.CARNIVAL_DEAL_INSTRUCTIONS to "some instructions",
            Constants.CARNIVAL_PROMO_CODE_TEXT to "some awesome promo code",
            Constants.CARNIVAL_DETAILS_TITLE to "details title",
            Constants.CARNIVAL_DETAILS_DESCRIPTION to "details description",
            Constants.CARNIVAL_TERMS_TITLE to "terms title",
            Constants.CARNIVAL_TERMS_DESCRIPTION to "some description about terms",
            Constants.CARNIVAL_SHOP_BUTTON_TITLE to "shop button text",
            Constants.CARNIVAL_DEEPLINK to "expda://hotelSearch")

    private fun createEmptyCarnivalMessageAttributes() = HashMap<String, String>()
}
