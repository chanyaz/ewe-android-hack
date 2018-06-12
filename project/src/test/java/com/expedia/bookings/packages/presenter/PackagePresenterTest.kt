package com.expedia.bookings.packages.presenter

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackagePresenterTest {

    val context = RuntimeEnvironment.application
    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    private lateinit var packagePresenter: PackagePresenter
    private lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun testSearchParamsObservable() {
        val searchParams = PackageTestUtil.getMIDPackageSearchParams()
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null, false) as PackagePresenter

        val errorPresenterParamsSubjectObserver = TestObserver<PackageSearchParams>()
        val bundlePresenterHotelParamsObserver = TestObserver<PackageSearchParams>()

        packagePresenter.errorPresenter.viewmodel.paramsSubject.subscribe(errorPresenterParamsSubjectObserver)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.hotelParamsObservable.subscribe(bundlePresenterHotelParamsObserver)

        packagePresenter.searchPresenter.searchViewModel.searchParamsObservable.onNext(searchParams)

        assertEquals(searchParams, errorPresenterParamsSubjectObserver.values()[0])
        assertEquals(searchParams, bundlePresenterHotelParamsObserver.values()[0])
        assertNull(Db.getPackageSelectedHotel())
        assertNull(Db.getPackageFlightBundle())
    }

    @Test
    fun testShowBundleTotalObservableWithShowSavings() {
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null, false) as PackagePresenter

        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        val packagePrice = Money("200", "USD")
        val packageSavings = Money("30", "USD")

        val currentOfferPrice = PackageOfferModel.PackagePrice()
        currentOfferPrice.packageTotalPrice = packagePrice
        currentOfferPrice.tripSavings = packageSavings
        currentOfferPrice.showTripSavings = true

        hotelResponse.setCurrentOfferPrice(currentOfferPrice)
        Db.setPackageResponse(hotelResponse)

        val totalPriceObservable = TestObserver<Money>()
        val packageSavingsObservable = TestObserver<Money>()
        val showSavingsObservable = TestObserver<Boolean>()
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.total.subscribe(totalPriceObservable)
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.savings.subscribe(packageSavingsObservable)
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.shouldShowSavings.subscribe(showSavingsObservable)

        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)

        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.totalPriceWidget.visibility)
        assertTrue(showSavingsObservable.values()[0])
        assertEquals(packagePrice, totalPriceObservable.values()[0])
        assertEquals(packageSavings, packageSavingsObservable.values()[0])
    }

    @Test
    fun testShowBundleTotalObservableWithoutShowSavings() {
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null, false) as PackagePresenter

        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        val packagePrice = Money("200", "USD")
        val packageSavings = Money("3", "USD")

        val currentOfferPrice = PackageOfferModel.PackagePrice()
        currentOfferPrice.packageTotalPrice = packagePrice
        currentOfferPrice.tripSavings = packageSavings
        currentOfferPrice.showTripSavings = false

        hotelResponse.setCurrentOfferPrice(currentOfferPrice)
        Db.setPackageResponse(hotelResponse)

        val showSavingsObservable = TestObserver<Boolean>()
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.shouldShowSavings.subscribe(showSavingsObservable)

        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)

        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.totalPriceWidget.visibility)
        assertFalse(showSavingsObservable.values()[0])
    }

    @Test
    fun testDefaultErrorObservable() {
        val searchParams = PackageTestUtil.getMIDPackageSearchParams()
        Db.setPackageParams(searchParams)

        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null, false) as PackagePresenter
        val outboundShowLoadingStateObserver = TestObserver<Boolean>()
        val inboundShowLoadingStateObserver = TestObserver<Boolean>()

        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.subscribe(outboundShowLoadingStateObserver)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.showLoadingStateObservable.subscribe(inboundShowLoadingStateObserver)

        packagePresenter.searchPresenter.searchViewModel.searchParamsObservable.onNext(searchParams)
        packagePresenter.errorPresenter.viewmodel.defaultErrorObservable.onNext(Unit)

        assertEquals(false, outboundShowLoadingStateObserver.values()[0])
        assertEquals(false, inboundShowLoadingStateObserver.values()[0])
    }
}
