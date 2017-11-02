package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.ActionMenuView
import android.view.LayoutInflater
import android.view.accessibility.AccessibilityManager
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.isSecureIconEnabled
import com.expedia.vm.CheckoutToolbarViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CheckoutToolbarTest {

    private var activity: Activity by Delegates.notNull()
    private lateinit var toolbar: CheckoutToolbar

    @Before
    fun setup() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppSecureCheckoutIcon)
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Rail)
    }

    @Test
    fun testToolbarMenuItem() {
        getToolbar()
        val actionMenuView = toolbar.getChildAt(0) as ActionMenuView
        assertNull(actionMenuView.getChildAt(0))

        toolbar.viewModel.menuVisibility.onNext(true)
        val actionView = actionMenuView.getChildAt(0) as ActionMenuItemView
        assertNotNull(actionView)

        toolbar.viewModel.showDone.onNext(false)
        assertEquals("Next", actionView.text)

        toolbar.viewModel.showDone.onNext(true)
        assertEquals("Done", actionView.text)
    }

    @Test
    fun testToolbarMenuItemWithAccessibility() {
        val spyContext = Mockito.spy(activity)
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
        assertNull(actionMenuView.getChildAt(0).contentDescription)

        toolbar.viewModel.showDone.onNext(false)

        assertNotNull(actionMenuView.getChildAt(0))
        assertEquals("Next button", actionMenuView.getChildAt(0).contentDescription)

        toolbar.viewModel.showDone.onNext(true)
        assertEquals("Done button", actionMenuView.getChildAt(0).contentDescription)
    }

    @Test
    fun testCustomToolbarTitle() {
        setSecureIconABTest()
        getToolbar()
        val toolbarCustomTitleTestSubscriber = TestObserver.create<String>()
        toolbar.viewModel.toolbarCustomTitle.subscribe(toolbarCustomTitleTestSubscriber)

        toolbar.viewModel.toolbarTitle.onNext("test title")
        toolbarCustomTitleTestSubscriber.assertValue("test title")
    }

    @Test
    fun testHideToolbarTitle() {
        setSecureIconABTest()
        getToolbar()
        val hideToolbarTitleTestSubscriber = TestObserver.create<Unit>()
        toolbar.viewModel.hideToolbarTitle.subscribe(hideToolbarTitleTestSubscriber)

        toolbar.viewModel.hideToolbarTitle.onNext(Unit)
        assertEquals("", toolbar.title)
    }

    @Test
    fun testSecureIconABTestIsOn() {
        setSecureIconABTest()
        assertTrue(isSecureIconEnabled(activity))
    }

    @Test
    fun testSecureIconABTestIsOff() {
        assertFalse(isSecureIconEnabled(activity))
    }

    private fun setSecureIconABTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppSecureCheckoutIcon)
    }

    private fun getToolbar() {
        toolbar = LayoutInflater.from(activity).inflate(R.layout.test_checkout_toolbar, null) as CheckoutToolbar
        toolbar.viewModel = CheckoutToolbarViewModel(activity)
    }
}