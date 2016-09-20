package com.expedia.bookings.utils

import android.content.Context
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.Toolbar
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
        toolbar.navigationIcon = ArrowXDrawableUtil.getNavigationIconDrawable(spyContext, ArrowXDrawableUtil.ArrowDrawableType.CLOSE);

        assertNull(toolbar.findFocus())
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
        assertTrue(toolbar.findFocus().javaClass.equals(ImageButton::class.java))
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
        assertTrue(textView.findFocus().javaClass.equals(TextView::class.java))
    }

    @Test
    fun testToolbarNavIconGetsDelayedFocus() {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val toolbar = Toolbar(spyContext)
        toolbar.navigationIcon = ArrowXDrawableUtil.getNavigationIconDrawable(spyContext, ArrowXDrawableUtil.ArrowDrawableType.CLOSE);

        assertNull(toolbar.findFocus())
        AccessibilityUtil.delayFocusToToolbarNavigationIcon(toolbar, 0)
        assertTrue(toolbar.findFocus().javaClass.equals(ImageButton::class.java))
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
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val mockAccessibilityManager = Mockito.mock(AccessibilityManager::class.java)

        Mockito.`when`(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager)
        Mockito.`when`(mockAccessibilityManager.isEnabled).thenReturn(true)
        Mockito.`when`(mockAccessibilityManager.isTouchExplorationEnabled).thenReturn(true)

        val toolbar = Toolbar(spyContext)
        toolbar.inflateMenu(R.menu.checkout_menu)

        val actionMenuView = toolbar.getChildAt(0) as ActionMenuView
        assertEquals("Done", actionMenuView.getChildAt(0).contentDescription)
        val contentDescription = "Done button"
        AccessibilityUtil.setMenuItemContentDescription(toolbar, contentDescription)
        assertEquals(contentDescription, actionMenuView.getChildAt(0).contentDescription)
    }
}
