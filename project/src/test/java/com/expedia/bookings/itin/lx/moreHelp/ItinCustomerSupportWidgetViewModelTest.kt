package com.expedia.bookings.itin.lx.moreHelp

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinCustomerSupportViewModel
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasItinType
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItinCustomerSupportWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var vm: ItinCustomerSupportViewModel<MockCustomerSupportWidgetViewModelScope>

    private lateinit var customerSupportHeaderTextTestObserver: TestObserver<String>
    private lateinit var phoneNumberTestObserver: TestObserver<String>
    private lateinit var customerSupportTextTestObserver: TestObserver<String>
    private lateinit var customerSupportButtonClickedTestObserver: TestObserver<Unit>
    private lateinit var itineraryNumberTestObserver: TestObserver<String>
    private lateinit var itineraryHeaderVisibilityTestObserver: TestObserver<Boolean>
    private lateinit var phoneNumberContentDescriptionTestObserver: TestObserver<String>
    private lateinit var customerSupportTextContentDescriptionTestObserver: TestObserver<String>
    private lateinit var itineraryNumberContentDescriptionTestObserver: TestObserver<String>

    @Before
    fun setup() {
        customerSupportHeaderTextTestObserver = TestObserver()
        phoneNumberTestObserver = TestObserver()
        customerSupportTextTestObserver = TestObserver()
        customerSupportButtonClickedTestObserver = TestObserver()
        itineraryNumberTestObserver = TestObserver()
        itineraryHeaderVisibilityTestObserver = TestObserver()
        phoneNumberContentDescriptionTestObserver = TestObserver()
        customerSupportTextContentDescriptionTestObserver = TestObserver()
        itineraryNumberContentDescriptionTestObserver = TestObserver()
        setupViewModel()
    }

    @After
    fun tearDown() {
        customerSupportHeaderTextTestObserver.dispose()
        phoneNumberTestObserver.dispose()
        customerSupportTextTestObserver.dispose()
        customerSupportButtonClickedTestObserver.dispose()
        itineraryNumberTestObserver.dispose()
        itineraryHeaderVisibilityTestObserver.dispose()
        phoneNumberContentDescriptionTestObserver.dispose()
        customerSupportTextContentDescriptionTestObserver.dispose()
        itineraryNumberContentDescriptionTestObserver.dispose()
    }

    @Test
    fun testDisplayFullCustomerSupportDetails() {
        vm.itinObserver.onChanged(ItinMocker.lxDetailsAlsoHappy)

        val customerSupportHeaderText = R.string.itin_customer_support_header_text_TEMPLATE
                .toString().plus(mapOf("brand" to BuildConfig.brand))
        customerSupportHeaderTextTestObserver.assertValue(customerSupportHeaderText)

        phoneNumberTestObserver.assertValue("+1-877-787-3117")

        val customerSupportText = R.string.itin_hotel_customer_support_site_header_TEMPLATE
                .toString().plus(mapOf("brand" to BuildConfig.brand))
        customerSupportTextTestObserver.assertValue(customerSupportText)

        customerSupportButtonClickedTestObserver.assertEmpty()

        itineraryNumberTestObserver.assertValue("7337689803181")

        itineraryHeaderVisibilityTestObserver.assertValue(true)

        val phoneNumberContDesc = R.string.itin_call_support_button_content_description_TEMPLATE
                .toString().plus(mapOf("brand" to BuildConfig.brand, "phonenumber" to "+1-877-787-3117"))
        phoneNumberContentDescriptionTestObserver.assertValue(phoneNumberContDesc)

        val customerSupportContDesc = R.string.itin_customer_support_site_button_content_description_TEMPLATE
                .toString().plus(mapOf("brand" to BuildConfig.brand))
        customerSupportTextContentDescriptionTestObserver.assertValue(customerSupportContDesc)

        val itinNumberContDesc = R.string.itin_customer_support_itin_number_content_description_TEMPLATE
                .toString().plus(mapOf("number" to "7337689803181".replace(".".toRegex(), "$0 ")))
        itineraryNumberContentDescriptionTestObserver.assertValue(itinNumberContDesc)
    }

    @Test
    fun testNoCustomerSupportInformation() {
        vm.itinObserver.onChanged(ItinMocker.lxDetailsHappy)

        phoneNumberTestObserver.assertEmpty()
        customerSupportTextTestObserver.assertEmpty()
        phoneNumberContentDescriptionTestObserver.assertEmpty()
        customerSupportTextContentDescriptionTestObserver.assertEmpty()
    }

    @Test
    fun testCustomerServiceLinkClickedAndTracked() {
        vm.itinObserver.onChanged(ItinMocker.lxDetailsAlsoHappy)

        assertFalse(vm.scope.tripsTracking.trackItinLxCustomerServiceLinkClicked)
        vm.customerSupportButtonClickedSubject.onNext(Unit)
        val webViewLauncher = vm.scope.webViewLauncher

        assertEquals("https://www.expedia.com/service/", webViewLauncher.lastSeenURL)
        assertEquals(R.string.itin_customer_service_webview_heading, webViewLauncher.lastSeenTitle)
        assertEquals("72d43105-3cd2-4f83-ae10-55f390397ac0", webViewLauncher.lastSeenTripId)
        assertTrue(vm.scope.tripsTracking.trackItinLxCustomerServiceLinkClicked)
    }

    private fun setupViewModel() {
        vm = ItinCustomerSupportViewModel(MockCustomerSupportWidgetViewModelScope())

        vm.customerSupportHeaderTextSubject.subscribe(customerSupportHeaderTextTestObserver)
        vm.phoneNumberSubject.subscribe(phoneNumberTestObserver)
        vm.customerSupportTextSubject.subscribe(customerSupportTextTestObserver)
        vm.customerSupportButtonClickedSubject.subscribe(customerSupportButtonClickedTestObserver)
        vm.itineraryNumberSubject.subscribe(itineraryNumberTestObserver)
        vm.itineraryHeaderVisibilitySubject.subscribe(itineraryHeaderVisibilityTestObserver)
        vm.phoneNumberContentDescriptionSubject.subscribe(phoneNumberContentDescriptionTestObserver)
        vm.customerSupportTextContentDescriptionSubject.subscribe(customerSupportTextContentDescriptionTestObserver)
        vm.itineraryNumberContentDescriptionSubject.subscribe(itineraryNumberContentDescriptionTestObserver)
    }

    private class MockCustomerSupportWidgetViewModelScope : HasStringProvider, HasLxRepo, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher, HasItinType {
        override val strings: StringSource = MockStringProvider()
        override val webViewLauncher = MockWebViewLauncher()
        override val tripsTracking = MockTripsTracking()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
        override val type = TripProducts.ACTIVITY.name
    }
}
