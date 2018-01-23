package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.MaskedCreditCardEditText
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class) class MaskedCreditCardEditTextTest {
    private var editText: MaskedCreditCardEditText by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private val testCardNumber = "4111 1111 1111 1111"
    private val expectedCardNumber = "XXXX XXXX XXXX 1111"

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        activity.setTheme(R.style.V2_Theme_Packages)
        editText = MaskedCreditCardEditText(activity, null)
    }

    @Test fun testMaskedNumber() {
        editText.setText(testCardNumber)
        editText.showMaskedNumber(testCardNumber)
        assertEquals(editText.text.toString(), expectedCardNumber)
    }
}
