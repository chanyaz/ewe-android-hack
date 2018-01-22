package com.expedia.bookings.utils

import android.app.Activity
import android.content.Context
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.Toolbar
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AccessibilityUtilTest {

    @Test
    fun testIsTalkbackEnabled() {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)

        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(false)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(false)

        var accessibility = AccessibilityUtil.isTalkBackEnabled(spyContext)
        assertFalse(accessibility)

        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(false)

        accessibility = AccessibilityUtil.isTalkBackEnabled(spyContext)
        assertFalse(accessibility)

        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        accessibility = AccessibilityUtil.isTalkBackEnabled(spyContext)
        assertTrue(accessibility)
    }

    @Test
    fun testToolbarNavIconGetsFocus() {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val toolbar = Toolbar(spyContext)
        toolbar.navigationIcon = ArrowXDrawableUtil.getNavigationIconDrawable(spyContext, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)

        assertNull(toolbar.findFocus())
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
        assertTrue(toolbar.findFocus() is ImageButton)
    }

    @Test
    fun testViewGetsDelayedFocus() {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val textView = TextView(spyContext)

        assertNull(textView.findFocus())
        AccessibilityUtil.delayedFocusToView(textView, 0)
        assertTrue(textView.findFocus() is TextView)
    }

    @Test
    fun testToolbarNavIconGetsDelayedFocus() {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val toolbar = Toolbar(spyContext)
        toolbar.navigationIcon = ArrowXDrawableUtil.getNavigationIconDrawable(spyContext, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)

        assertNull(toolbar.findFocus())
        AccessibilityUtil.delayFocusToToolbarNavigationIcon(toolbar, 0)
        assertTrue(toolbar.findFocus() is ImageButton)
    }

    @Test
    fun testSetFocusForView() {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val parent = LinearLayout(spyContext)
        parent.orientation = LinearLayout.VERTICAL

        val textView1 = TextView(spyContext)
        val textView2 = TextView(spyContext)
        parent.addView(textView1)
        parent.addView(textView2)

        assertNull(textView1.findFocus())
        textView1.setFocusForView()
        assertNull(textView1.findFocus())
    }

    @Test
    fun testToolbarMenuItemContentDescription() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Rail)
        val spyContext = Mockito.spy(activity)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val toolbar = Toolbar(spyContext)
        toolbar.inflateMenu(R.menu.checkout_menu)

        val actionMenuView = toolbar.getChildAt(0) as ActionMenuView
        val contentDescription = "Done button"
        AccessibilityUtil.setMenuItemContentDescription(toolbar, contentDescription)
        assertEquals(contentDescription, actionMenuView.getChildAt(0).contentDescription)
    }

    @Test
    fun testToolbarTabContentDescription() {

        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().postCreate(null).resume().get()
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val contentDescription = "Shop travel tab"
        setContentDescriptionToolbarTabs(activity, activity.toolbar.tabLayout)
        assertEquals(contentDescription, activity.toolbar.tabLayout.getTabAt(0)?.contentDescription)

        activity.finish()
    }
}
