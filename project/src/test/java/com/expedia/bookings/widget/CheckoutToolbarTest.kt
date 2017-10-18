package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.support.v7.widget.ActionMenuView
import android.view.LayoutInflater
import android.view.accessibility.AccessibilityManager
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.vm.CheckoutToolbarViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CheckoutToolbarTest {

    private var activity: Activity by Delegates.notNull()
    private lateinit var toolbar: CheckoutToolbar

    @Test
    fun testToolbarMenuItem() {
        setUpActivity()
        getToolbar()
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
        setUpActivity()
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
        toolbar.viewModel.formFilledIn.onNext(false)

        assertNotNull(actionMenuView.getChildAt(0))
        assertEquals("Next button", actionMenuView.getChildAt(0).contentDescription)

        toolbar.viewModel.formFilledIn.onNext(true)
        assertEquals("Done button", actionMenuView.getChildAt(0).contentDescription)
    }

    @Test
    fun testCustomToolbar() {
        setUpActivity()
        setSecureIconAbacusAndFeatureToggle()
        getToolbar()
        val toolbarCustomTitleTestSubscriber = TestObserver.create<String>()
        toolbar.viewModel.toolbarCustomTitle.subscribe(toolbarCustomTitleTestSubscriber)

        toolbar.viewModel.toolbarTitle.onNext("asdf")
        assertEquals("asdf", toolbarCustomTitleTestSubscriber.values()[0])
    }

    private fun setSecureIconAbacusAndFeatureToggle() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppSecureCheckoutIcon)
        SettingUtils.save(activity.applicationContext, R.string.preference_enable_secure_icon, true)
        assertTrue(FeatureToggleUtil.isUserBucketedAndFeatureEnabled(activity.applicationContext, AbacusUtils.EBAndroidAppSecureCheckoutIcon, R.string.preference_enable_secure_icon))
    }

    private fun setUpActivity() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Rail)
    }

    private fun getToolbar() {
        toolbar = LayoutInflater.from(activity).inflate(R.layout.test_checkout_toolbar, null) as CheckoutToolbar
        toolbar.viewModel = CheckoutToolbarViewModel(activity)
    }
}