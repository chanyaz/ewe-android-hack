package com.expedia.bookings.itin.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinActionButtonsTest {
    lateinit var activity: Activity
    lateinit var sut: ItinActionButtons

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = LayoutInflater.from(activity).inflate(R.layout.test_itin_action_buttons, null) as ItinActionButtons
    }

    @Test
    fun testLeftButtonVisibility() {
        sut.viewModel.leftButtonVisibilityObservable.onNext(true)
        assertEquals(View.VISIBLE, sut.leftButton.visibility)

        sut.viewModel.leftButtonVisibilityObservable.onNext(false)
        assertEquals(View.GONE, sut.leftButton.visibility)
    }

    @Test
    fun testRightButtonVisibility() {
        sut.viewModel.rightButtonVisibilityObservable.onNext(true)
        assertEquals(View.VISIBLE, sut.rightButton.visibility)

        sut.viewModel.rightButtonVisibilityObservable.onNext(false)
        assertEquals(View.GONE, sut.rightButton.visibility)
    }

    @Test
    fun testLeftButtonDrawable() {
        sut.viewModel.leftButtonDrawableObservable.onNext(R.drawable.itin_flight_terminal_map_icon)
        val shadowDrawable = shadowOf(sut.leftButtonText.compoundDrawables[0].current)
        assertEquals(R.drawable.itin_flight_terminal_map_icon, shadowDrawable.createdFromResId)
    }

    @Test
    fun testRightButtonDrawable() {
        sut.viewModel.rightButtonDrawableObservable.onNext(R.drawable.ic_directions_icon_cta_button)
        val shadowDrawable = shadowOf(sut.rightButtonText.compoundDrawables[0].current)
        assertEquals(R.drawable.ic_directions_icon_cta_button, shadowDrawable.createdFromResId)
    }

    @Test
    fun testDividerVisibility() {
        sut.viewModel.dividerVisibilityObservable.onNext(true)
        assertEquals(View.VISIBLE, sut.divider.visibility)

        sut.viewModel.dividerVisibilityObservable.onNext(false)
        assertEquals(View.GONE, sut.divider.visibility)
    }

    @Test
    fun testLeftButtonText() {
        sut.viewModel.leftButtonTextObservable.onNext("Terminal Maps")
        assertEquals("Terminal Maps", sut.leftButtonText.text.toString())
    }

    @Test
    fun testRightButtonText() {
        sut.viewModel.rightButtonTextObservable.onNext("Directions")
        assertEquals("Directions", sut.rightButtonText.text.toString())
    }
}