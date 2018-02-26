package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.widget.packages.MaterialBillingDetailsPaymentWidget
import com.expedia.vm.PaymentViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CreditCardExpiryEditTextTest {
    private lateinit var materialBillingDetailsPaymentWidget: MaterialBillingDetailsPaymentWidget
    private lateinit var activity: Activity

    @Before
    fun before() {
        Db.sharedInstance.clear()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        materialBillingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as MaterialBillingDetailsPaymentWidget
        materialBillingDetailsPaymentWidget.viewmodel = PaymentViewModel(activity)
    }

    @Test
    fun testExpiryDateTextWhenUserEntersThree() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.setText("3")

        assertEquals("03/", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEntersOne() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.setText("1")

        assertEquals("1", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEntersZero() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.setText("0")

        assertEquals("0", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEnters05() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.setText("05")

        assertEquals("05/", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEnters054() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("0")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("5")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("4")

        assertEquals("05/4", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testDeletionWhenTextViewWithValue054() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("0")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("5")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("4")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.text.delete(3, 4)

        assertEquals("05/", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testContinuousDeletionWhenTextViewWithValue054() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("0")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("5")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("4")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.text.delete(3, 4)
        materialBillingDetailsPaymentWidget.creditCardExpiryText.text.delete(2, 3)

        assertEquals("0", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testDeletionWhenTextViewWithValue0542() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("0")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("5")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("4")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("2")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.text.delete(4, 5)

        assertEquals("05/4", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testTextViewWithValue05421() {
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("0")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("5")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("4")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("2")
        materialBillingDetailsPaymentWidget.creditCardExpiryText.append("1")

        assertEquals("05/42", materialBillingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }
}
