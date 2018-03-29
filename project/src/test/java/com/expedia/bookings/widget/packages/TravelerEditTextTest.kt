package com.expedia.bookings.widget.packages

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.autofill.AutofillValue
import com.expedia.account.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.vm.traveler.LastNameViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(constants = BuildConfig::class, sdk = [26])
class TravelerEditTextTest {

    var editText by Delegates.notNull<TravelerEditText>()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        editText = LayoutInflater.from(activity).inflate(R.layout.test_traveler_edit_text, null) as TravelerEditText
        editText.viewModel = LastNameViewModel()
        editText.setAccessibilityDelegate(object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                //do nothing
            }
        })
    }

    @Test
    fun testContentDescriptionTravelerEditText() {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals(" Last Name, , ", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionTravelerEditTextWithText() {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("Test text")
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals(" Last Name, Test text, ", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionTravelerEditTextWithTextAndError() {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("Test text")
        editText.valid = false
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals(" Last Name, Test text, Error, , ", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionTravelerEditTextWithTextAndErrorMessage() {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("Test text")
        editText.errorContDesc = "Enter valid text"
        editText.valid = false
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals(" Last Name, Test text, Error, Enter valid text, ", testNode.text.toString())
    }

    @Test
    fun testEditTextAutoFill() {
        editText.autofill(AutofillValue.forText("blah"))
        assertEquals("blah", editText.text.toString())
    }

    @Test
    fun testAutoFillWorksWhenFieldIsEmptyAndSelected() {
        editText.setText("")
        editText.requestFocus()
        assertTrue(editText.autofillType == View.AUTOFILL_TYPE_TEXT)
    }

    @Test
    fun testFieldIsValidWhenAutoFilled() {
        val errorSubjectTestObserver = TestObserver.create<Boolean>()
        editText.viewModel.errorSubject.subscribe(errorSubjectTestObserver)
        editText.autofill(AutofillValue.forText("blah"))
        assertTrue(editText.viewModel.isValid())
    }
}
