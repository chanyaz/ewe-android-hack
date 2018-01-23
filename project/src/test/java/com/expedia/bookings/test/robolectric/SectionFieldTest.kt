package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.section.SectionFieldList
import com.expedia.bookings.section.ValidationIndicatorExclamation
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class SectionFieldTest {
    var context: Context by Delegates.notNull()
    val mFields = SectionFieldList<BillingInfo>()

    val validLastName1 = ValidationIndicatorExclamation<BillingInfo>(
            R.id.edit_last_name)
    val validFirstName = ValidationIndicatorExclamation<BillingInfo>(
            R.id.edit_first_name)
    val validLastName2 = ValidationIndicatorExclamation<BillingInfo>(
            R.id.edit_last_name)
    val validEmail = ValidationIndicatorExclamation<BillingInfo>(
            R.id.edit_email_address)
    val validEmail2 = ValidationIndicatorExclamation<BillingInfo>(
            R.id.edit_email_address)

    @Before
    fun before() {
        context = Robolectric.buildActivity(Activity::class.java).create().get()

        validLastName1.bindField(makeField(R.id.edit_last_name))
        validFirstName.bindField(makeField(R.id.edit_first_name))
        validLastName2.bindField(makeField(R.id.edit_last_name))
        validEmail.bindField(makeField(R.id.edit_email_address))
        validEmail2.bindField(makeField(R.id.edit_email_address))

        mFields.add(validLastName1)
        mFields.add(validFirstName)
        mFields.add(validLastName2)
        mFields.add(validEmail)
        mFields.add(validEmail2)
    }

    @Test
    fun testFindField() {
        assertEquals(validLastName2, mFields.getLastFieldWithId(R.id.edit_last_name))
        assertEquals(validFirstName, mFields.getLastFieldWithId(R.id.edit_first_name))
        assertEquals(validEmail2, mFields.getLastFieldWithId(R.id.edit_email_address))
    }

    private fun makeField(id: Int): ViewGroup {
        val parent = FrameLayout(context, null)
        val textView = TextView(context, null)
        textView.id = id
        parent.addView(textView)
        return parent
    }
}
