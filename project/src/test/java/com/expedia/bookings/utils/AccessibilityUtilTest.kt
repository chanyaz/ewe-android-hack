package com.expedia.bookings.utils

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Spy
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowAccessibilityManager
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
}