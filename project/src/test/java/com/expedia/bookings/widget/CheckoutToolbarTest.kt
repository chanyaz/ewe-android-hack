package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.support.v7.widget.ActionMenuView
import android.view.LayoutInflater
import android.view.accessibility.AccessibilityManager
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.CheckoutToolbarViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class CheckoutToolbarTest {

    @Test
    fun testToolbarMenuItem() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val toolbar = LayoutInflater.from(activity).inflate(R.layout.test_checkout_toolbar, null) as CheckoutToolbar
        toolbar.viewModel = CheckoutToolbarViewModel(activity)
        val actionMenuView = toolbar.getChildAt(0) as ActionMenuView
        assertNull(actionMenuView.getChildAt(0))

        toolbar.viewModel.menuVisibility.onNext(true)
        assertNotNull(actionMenuView.getChildAt(0))

        toolbar.viewModel.formFilledIn.onNext(false)
        assertEquals("Next", actionMenuView.getChildAt(0).contentDescription)

        toolbar.viewModel.formFilledIn.onNext(true)
        assertEquals("Done", actionMenuView.getChildAt(0).contentDescription)
    }

    @Test
    fun testToolbarMenuItemWithAccessibility() {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val toolbar = CheckoutToolbar(spyContext, null)
        toolbar.viewModel = CheckoutToolbarViewModel(spyContext)
        toolbar.context

        val actionMenuView = toolbar.getChildAt(0) as ActionMenuView
        assertNull(actionMenuView.getChildAt(0))

        toolbar.viewModel.menuVisibility.onNext(true)
        toolbar.viewModel.formFilledIn.onNext(false)

        assertNotNull(actionMenuView.getChildAt(0))
        assertEquals("Next button", actionMenuView.getChildAt(0).contentDescription)

        toolbar.viewModel.formFilledIn.onNext(true)
        assertEquals("Done button", actionMenuView.getChildAt(0).contentDescription)
    }
}