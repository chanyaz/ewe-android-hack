// TODO commented out code due to BottomCheckoutContainer refactoring. Look at BaseOverviewPresenter lines 30 and 35.
//package com.expedia.bookings.test.robolectric
//
//import android.app.Activity
//import android.view.LayoutInflater
//import android.view.View
//import com.expedia.bookings.BuildConfig
//import com.expedia.bookings.R
//import com.expedia.bookings.lx.presenter.LXOverviewPresenter
//import com.expedia.bookings.lx.presenter.LxCheckoutPresenterV2
//import com.expedia.bookings.utils.ArrowXDrawableUtil
//import com.expedia.bookings.utils.Ui
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.Robolectric
//import org.robolectric.annotation.Config
//import com.expedia.bookings.services.TestObserver
//import kotlin.properties.Delegates
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//
//@RunWith(RobolectricRunner::class)
//
//@Config
//(constants = BuildConfig::class)
//class LXUniversalCheckoutTest {
//
//    private var overview: LXOverviewPresenter by Delegates.notNull()
//    private var checkout: LxCheckoutPresenterV2 by Delegates.notNull()
//    lateinit private var activity: Activity
//
//    @Before
//    fun setup() {
//        activity = Robolectric.buildActivity(Activity::class.java).create().get()
//        activity.setTheme(R.style.V2_Theme_LX)
//        Ui.getApplication(activity).defaultTravelerComponent()
//        Ui.getApplication(activity).defaultLXComponents()
//        overview = LayoutInflater.from(activity).inflate(R.layout.lx_overview_test_layout, null) as LXOverviewPresenter
//        checkout = overview.getCheckoutPresenter()
//        overview.showCheckout()
//    }
//
//    @Test
//    fun testUniversalLx() {
//        assertEquals(View.VISIBLE, overview.visibility)
//        assertEquals(View.VISIBLE, overview.lxSummaryWidget.visibility)
//        assertEquals(View.VISIBLE, overview.summaryContainer.visibility)
//
//        assertFalse(overview.bottomLayout.isShown)
//
//        assertEquals(View.VISIBLE, checkout.visibility)
//        assertEquals(View.VISIBLE, checkout.mainContent.visibility)
//        assertEquals(View.VISIBLE, checkout.travelerSummaryCardView.visibility)
//        assertEquals(View.VISIBLE, checkout.paymentWidget.cardInfoContainer.visibility)
//        assertEquals(View.VISIBLE, checkout.loginWidget.visibility)
//    }
//
//    @Test
//    fun testSummaryVisibility() {
//        val testSubscriber = TestObserver<Boolean>()
//        checkout.getCheckoutViewModel().hideOverviewSummaryObservable.subscribe(testSubscriber)
//        assertEquals(View.VISIBLE, overview.summaryContainer.visibility)
//
//        checkout.getCheckoutViewModel().hideOverviewSummaryObservable.onNext(true)
//        assertEquals(true, testSubscriber.values().last())
//        assertEquals(View.GONE, overview.summaryContainer.visibility)
//
//        checkout.getCheckoutViewModel().hideOverviewSummaryObservable.onNext(false)
//        assertEquals(false, testSubscriber.values().last())
//        assertEquals(View.VISIBLE, overview.summaryContainer.visibility)
//    }
//
//    @Test
//    fun testLxCheckoutToolbarForTraveler() {
//        assertCheckoutToolbar(title = "Checkout", menuIsVisible = false)
//        checkout.travelersPresenter.showSelectOrEntryState()
//
//        assertCheckoutToolbar("Edit Traveler 1 (Adult)", true)
//    }
//
//    @Test
//    fun testLxCheckoutToolbarForPayment() {
//        assertCheckoutToolbar("Checkout", false)
//        checkout.paymentWidget.cardInfoContainer.performClick()
//        checkout.paymentWidget.showPaymentForm(false)
//
//        assertCheckoutToolbar("New Debit/Credit Card", true)
//    }
//
//    private fun assertCheckoutToolbar(title: String, menuIsVisible: Boolean) {
//        assertEquals(title, overview.toolbar.title)
//        assertEquals(ArrowXDrawableUtil.getNavigationIconDrawable(activity, ArrowXDrawableUtil.ArrowDrawableType.BACK), overview.toolbar.toolbarNavIcon)
//        assertEquals(menuIsVisible, overview.toolbar.menuItem.isVisible)
//        assertEquals("Done", overview.toolbar.menuItem.title)
//    }
//}
