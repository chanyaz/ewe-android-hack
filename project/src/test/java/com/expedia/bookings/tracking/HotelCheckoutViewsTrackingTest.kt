package com.expedia.bookings.tracking

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class HotelCheckoutViewsTrackingTest {
    private var checkout: HotelCheckoutMainViewPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
    }

    @Test
    fun testTrackMaterialHotelEditTravelerEntry() {
        setupMaterialForms()
        checkout.travelerSummaryCardView.performClick()
        checkout.travelersPresenter.closeSubject.onNext(Unit)
        OmnitureTestUtils.assertStateTracked("App.Hotels.Checkout.Traveler.Edit.Info",
                OmnitureMatchers.withProps(mapOf(34 to "24870.0.1")),
                mockAnalyticsProvider)
    }

    @Test
    fun testTrackControlHotelTravelerEntryWidget() {
        checkout.mainContactInfoCardView.performClick()
        OmnitureTestUtils.assertStateTracked("App.Hotels.Checkout.Traveler.Edit.Info",
                OmnitureMatchers.withProps(mapOf(34 to "24870.0.0")),
                mockAnalyticsProvider)
    }

    private fun setupMaterialForms() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
    }
}
