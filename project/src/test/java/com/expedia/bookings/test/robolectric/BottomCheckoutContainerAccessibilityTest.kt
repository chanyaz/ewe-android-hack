package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import com.expedia.bookings.R
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.presenter.BottomCheckoutContainer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
class BottomCheckoutContainerAccessibilityTest {
    lateinit private var bottomContainer: BottomCheckoutContainer

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)

        val spyContext = Mockito.spy(activity)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)
        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)
        bottomContainer = BottomCheckoutContainer(spyContext, null)
    }

    @Test
    fun testToggleBottomContainerViewsAccessibility() {
        bottomContainer.toggleCheckoutButtonOrSlider(false, TwoScreenOverviewState.BUNDLE)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.VISIBLE)
        assertTrue(bottomContainer.accessiblePurchaseButton.visibility == View.GONE)

        bottomContainer.toggleCheckoutButtonOrSlider(true, TwoScreenOverviewState.CHECKOUT)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.GONE)
        assertTrue(bottomContainer.accessiblePurchaseButton.visibility == View.VISIBLE)
    }
}