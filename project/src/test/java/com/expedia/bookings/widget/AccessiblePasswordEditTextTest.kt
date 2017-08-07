package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.accessibility.AccessiblePasswordEditText
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class AccessiblePasswordEditTextTest {

    var editText by Delegates.notNull<AccessiblePasswordEditText>()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        editText = LayoutInflater.from(activity).inflate(R.layout.test_accessible_password_edit_text, null) as AccessiblePasswordEditText
        editText.setAccessibilityDelegate(object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                //do nothing
            }
        })
    }

    @Test
    fun testContentDescriptionEmptyText()
    {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals("CVV", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionWithText()
    {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("123")
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals("CVV", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionWithTextAndError()
    {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("321")
        editText.valid = false
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals("CVV, Error, ", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionWithTextAndErrorMessage()
    {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("321")
        editText.valid = false
        editText.errorMessage = "Enter a valid CVV"
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals("CVV, Error, Enter a valid CVV", testNode.text.toString())
    }
}