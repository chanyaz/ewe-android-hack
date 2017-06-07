package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class AccessibleEditTextTest {

    var editText by Delegates.notNull<AccessibleEditText>()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        editText = LayoutInflater.from(activity).inflate(R.layout.test_accessible_edit_text, null) as AccessibleEditText
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
        assertEquals(" Last Name", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionWithText()
    {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("Test text")
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals(" Last Name, Test text", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionWithTextAndError()
    {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("Test text")
        editText.valid = false
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals(" Last Name, Test text, Error, ", testNode.text.toString())
    }

    @Test
    fun testContentDescriptionWithTextAndErrorMessage()
    {
        val testNode = AccessibilityNodeInfo.obtain()
        editText.setText("Test text")
        editText.errorMessage = "Enter valid text"
        editText.valid = false
        editText.onInitializeAccessibilityNodeInfo(testNode)
        assertEquals(" Last Name, Test text, Error, Enter valid text", testNode.text.toString())
    }
}