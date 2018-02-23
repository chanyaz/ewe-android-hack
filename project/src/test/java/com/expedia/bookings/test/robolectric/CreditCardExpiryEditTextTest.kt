package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.bookings.widget.packages.MaterialBillingDetailsPaymentWidget
import com.expedia.vm.PaymentViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CreditCardExpiryEditTextTest {
    private lateinit var billingDetailsPaymentWidget: MaterialBillingDetailsPaymentWidget
    private lateinit var activity: Activity

    @Before
    fun before() {
        Db.sharedInstance.clear()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as MaterialBillingDetailsPaymentWidget
        billingDetailsPaymentWidget.viewmodel = PaymentViewModel(activity)
    }

    @Test
    fun testExpiryDateTextWhenUserEntersThree() {
        billingDetailsPaymentWidget.creditCardExpiryText.setText("3")

        assertEquals("03/", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEntersOne() {
        billingDetailsPaymentWidget.creditCardExpiryText.setText("1")

        assertEquals("1", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEntersZero() {
        billingDetailsPaymentWidget.creditCardExpiryText.setText("0")

        assertEquals("0", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEnters05() {
        billingDetailsPaymentWidget.creditCardExpiryText.setText("05")

        assertEquals("05/", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testExpiryDateTextWhenUserEnters054() {
        billingDetailsPaymentWidget.creditCardExpiryText.append("0")
        billingDetailsPaymentWidget.creditCardExpiryText.append("5")
        billingDetailsPaymentWidget.creditCardExpiryText.append("4")

        assertEquals("05/4", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testDeletionWhenTextViewWithValue054() {
        billingDetailsPaymentWidget.creditCardExpiryText.append("0")
        billingDetailsPaymentWidget.creditCardExpiryText.append("5")
        billingDetailsPaymentWidget.creditCardExpiryText.append("4")
        billingDetailsPaymentWidget.creditCardExpiryText.text.delete(3, 4)

        assertEquals("05/", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testContinuousDeletionWhenTextViewWithValue054() {
        billingDetailsPaymentWidget.creditCardExpiryText.append("0")
        billingDetailsPaymentWidget.creditCardExpiryText.append("5")
        billingDetailsPaymentWidget.creditCardExpiryText.append("4")
        billingDetailsPaymentWidget.creditCardExpiryText.text.delete(3, 4)
        billingDetailsPaymentWidget.creditCardExpiryText.text.delete(2, 3)

        assertEquals("0", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testDeletionWhenTextViewWithValue0542() {
        billingDetailsPaymentWidget.creditCardExpiryText.append("0")
        billingDetailsPaymentWidget.creditCardExpiryText.append("5")
        billingDetailsPaymentWidget.creditCardExpiryText.append("4")
        billingDetailsPaymentWidget.creditCardExpiryText.append("2")
        billingDetailsPaymentWidget.creditCardExpiryText.text.delete(4, 5)

        assertEquals("05/4", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }

    @Test
    fun testTextViewWithValue05421() {
        billingDetailsPaymentWidget.creditCardExpiryText.append("0")
        billingDetailsPaymentWidget.creditCardExpiryText.append("5")
        billingDetailsPaymentWidget.creditCardExpiryText.append("4")
        billingDetailsPaymentWidget.creditCardExpiryText.append("2")
        billingDetailsPaymentWidget.creditCardExpiryText.append("1")

        assertEquals("05/42", billingDetailsPaymentWidget.creditCardExpiryText.text.toString())
    }
}
