package com.expedia.bookings.itin.lx.moreHelp

import android.app.Activity
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ICustomerSupportViewModel
import com.expedia.bookings.itin.common.ItinCustomerSupportWidget
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LxItinCustomerSupportWidgetTest {
    private lateinit var activity: Activity
    private lateinit var widget: ItinCustomerSupportWidget
    private lateinit var vm: ICustomerSupportViewModel

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_lx_itin_customer_support_widget, null) as ItinCustomerSupportWidget
        widget.viewModel = MockViewModel()
        vm = widget.viewModel
    }

    @Test
    fun testPhoneNumberVisibility() {
        assertFalse(widget.callSupportActionButton.visibility == View.VISIBLE)
        widget.viewModel.phoneNumberSubject.onNext("4039299393")
        assertTrue(widget.callSupportActionButton.visibility == View.VISIBLE)
    }

    @Test
    fun testCustomerSupportLinkVisiblity() {
        assertFalse(widget.customerSupportSiteButton.visibility == View.VISIBLE)
        widget.viewModel.customerSupportTextSubject.onNext("Expedia customer support")
        assertTrue(widget.customerSupportSiteButton.visibility == View.VISIBLE)
    }

    @Test
    fun testPhoneNumberClickedGoesToCall() {
        val shadowPackageManager = Shadows.shadowOf(RuntimeEnvironment.application.packageManager)
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, true)

        val makePhoneCallTestObserver = TestObserver<Unit>()
        vm.phoneNumberClickedSubject.subscribe(makePhoneCallTestObserver)
        makePhoneCallTestObserver.assertEmpty()

        vm.phoneNumberSubject.onNext("1112223333")
        widget.callSupportActionButton.performClick()

        makePhoneCallTestObserver.assertValue(Unit)
    }

    @Test
    fun testPhoneCallTrackedWhenClickedOnSelectableToolbar() {
        val phoneCallTrackedTestObserver = TestObserver<Unit>()
        vm.phoneNumberClickedSubject.subscribe(phoneCallTrackedTestObserver)
        phoneCallTrackedTestObserver.assertEmpty()

        val toolbar = Toolbar(activity)
        activity.menuInflater.inflate(R.menu.test_menu, toolbar.menu)
        toolbar.menu.add(0, android.R.id.textAssist, 0, "")
        val menuItem = toolbar.menu.findItem(android.R.id.textAssist)
        widget.callSupportActionButton.customSelectionActionModeCallback.onActionItemClicked(null, menuItem)

        phoneCallTrackedTestObserver.assertValue(Unit)
    }

    class MockViewModel : ICustomerSupportViewModel {
        override val customerSupportHeaderTextSubject: PublishSubject<String> = PublishSubject.create()
        override val phoneNumberSubject: PublishSubject<String> = PublishSubject.create()
        override val phoneNumberClickedSubject: PublishSubject<Unit> = PublishSubject.create()
        override val customerSupportTextSubject: PublishSubject<String> = PublishSubject.create()
        override val customerSupportButtonClickedSubject: PublishSubject<Unit> = PublishSubject.create()
        override val itineraryNumberSubject: PublishSubject<String> = PublishSubject.create()
        override val itineraryHeaderVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()
        override val phoneNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
        override val customerSupportTextContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
        override val itineraryNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    }
}
