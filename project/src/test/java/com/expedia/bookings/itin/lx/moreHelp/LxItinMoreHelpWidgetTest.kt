package com.expedia.bookings.itin.lx.moreHelp

import android.app.Activity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toolbar
import com.expedia.account.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.IMoreHelpViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LxItinMoreHelpWidgetTest {
    private lateinit var activity: Activity
    private lateinit var widget: LxItinMoreHelpWidget
    private lateinit var vm: IMoreHelpViewModel

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_lx_itin_more_help_widget, null) as LxItinMoreHelpWidget
        widget.viewModel = MockLxMoreHelpViewModel()
        vm = widget.viewModel
    }

    @Test
    fun testHelpTextExists() {
        vm.helpTextSubject.onNext("For special requests or questions about your reservation, contact Fun Place")
        assertEquals("For special requests or questions about your reservation, contact Fun Place",
                widget.helpText.text.toString())
    }

    @Test
    fun testHelpTextDoesNotExist() {
        vm.helpTextSubject.onNext("")
        assertEquals("", widget.helpText.text.toString())
    }

    @Test
    fun testShowConfirmationNumber() {
        vm.confirmationNumberSubject.onNext("12345")
        vm.confirmationTitleVisibilitySubject.onNext(true)
        assertTrue(widget.confirmationTitle.visibility == View.VISIBLE)
        assertEquals("12345", widget.confirmationNumber.text.toString())
    }

    @Test
    fun testHideConfirmationTitle() {
        vm.confirmationTitleVisibilitySubject.onNext(false)
        assertTrue(widget.confirmationTitle.visibility == View.GONE)
    }

    @Test
    fun testPhoneNumberClicked() {
        val phoneNumberClickedTestObserver = TestObserver<Unit>()
        vm.phoneNumberClickSubject.subscribe(phoneNumberClickedTestObserver)
        phoneNumberClickedTestObserver.assertEmpty()

        vm.phoneNumberSubject.onNext("1112223333")
        widget.phoneNumberButton.performClick()

        phoneNumberClickedTestObserver.assertValue(Unit)
    }

    @Test
    fun testPhoneNumberViewGetsFocusedOnTouch() {
        val touchEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        widget.phoneNumberButton.dispatchTouchEvent(touchEvent)
        assertTrue(widget.phoneNumberButton.hasFocus())
    }

    @Test
    fun testSelectionToolbarCallButtonClicked() {
        val phoneNumberClickedTestObserver = TestObserver<Unit>()
        vm.phoneNumberClickSubject.subscribe(phoneNumberClickedTestObserver)
        phoneNumberClickedTestObserver.assertEmpty()

        val toolbar = Toolbar(activity)
        activity.menuInflater.inflate(R.menu.test_menu, toolbar.menu)
        toolbar.menu.add(0, android.R.id.textAssist, 0, "")
        val menuItem = toolbar.menu.findItem(android.R.id.textAssist)
        widget.phoneNumberButton.customSelectionActionModeCallback.onActionItemClicked(null, menuItem)

        phoneNumberClickedTestObserver.assertValue(Unit)
    }

    @Test
    @Config(constants = BuildConfig::class, sdk = intArrayOf(26))
    fun testSelectingConfirmationNumberRemovesPhoneButtonInActionToolbar() {
        val toolbar = Toolbar(activity)
        activity.menuInflater.inflate(R.menu.test_menu, toolbar.menu)
        toolbar.menu.add(0, android.R.id.textAssist, 0, "")
        assertNotNull(toolbar.menu.findItem(android.R.id.textAssist))
        widget.confirmationNumber.customSelectionActionModeCallback.onPrepareActionMode(null, toolbar.menu)

        assertNull(toolbar.menu.findItem(android.R.id.textAssist))
    }

    class MockLxMoreHelpViewModel : IMoreHelpViewModel {
        override val phoneNumberSubject: PublishSubject<String> = PublishSubject.create()
        override val callButtonContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
        override val helpTextSubject: PublishSubject<String> = PublishSubject.create()
        override val confirmationNumberSubject: PublishSubject<String> = PublishSubject.create()
        override val confirmationTitleVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()
        override val confirmationNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
        override val phoneNumberClickSubject: PublishSubject<Unit> = PublishSubject.create()
    }
}
