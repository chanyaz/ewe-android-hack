package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.section.CreditCardSection
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CVVEntryWidgetTest {
    var cvvEntryWidget by Delegates.notNull<CVVEntryWidget>()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        cvvEntryWidget = LayoutInflater.from(activity).inflate(R.layout.test_cvv_entry_widget, null) as CVVEntryWidget
        val signatureName = "abc DEF"
        cvvEntryWidget.mCreditCardSection = Ui.findView<CreditCardSection>(cvvEntryWidget, R.id.credit_card_section)
        cvvEntryWidget.mCreditCardSection.bind(signatureName, PaymentType.CARD_AMERICAN_EXPRESS, "345104799171123")
    }

    @Test
    fun testToolbarContentDescription() {
        assertEquals("Back", cvvEntryWidget.toolbar.navigationContentDescription)
    }
}
