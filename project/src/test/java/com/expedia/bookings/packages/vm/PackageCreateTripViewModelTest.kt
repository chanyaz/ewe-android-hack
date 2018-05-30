package com.expedia.bookings.packages.vm

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageCreateTripViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: PackageCreateTripViewModel

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    @Before
    fun setup() {
        sut = PackageCreateTripViewModel(packageServiceRule.services!!, context)
    }

    @Test
    fun createTripRequestFired() {
        val tripResponseSubscriber = TestObserver.create<MultiItemApiCreateTripResponse>()

        sut.multiItemResponseSubject.subscribe(tripResponseSubscriber)

        setParamsInDB("mid_create_trip")

        sut.performMultiItemCreateTripSubject.onNext(Unit)

        tripResponseSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        tripResponseSubscriber.assertValueCount(1)
    }

    @Test
    fun testCreateTripErrorScenario() {
        val tripResponseSubscriber = TestObserver.create<MultiItemApiCreateTripResponse>()
        val tripErrorSubscriber = TestObserver.create<String>()

        sut.multiItemResponseSubject.subscribe(tripResponseSubscriber)
        sut.midCreateTripErrorObservable.subscribe(tripErrorSubscriber)

        setParamsInDB("error")

        sut.performMultiItemCreateTripSubject.onNext(Unit)

        tripResponseSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        tripResponseSubscriber.assertValueCount(0)
        tripErrorSubscriber.assertValueCount(1)
        tripErrorSubscriber.assertValues("MIS_INVALID_REQUEST")
    }

    @Test
    fun testCreateTripThrowsException() {
        val tripErrorSubscriber = TestObserver.create<ApiError>()
        sut.createTripErrorObservable.subscribe(tripErrorSubscriber)

        setParamsInDB("mid_create_trip_http_exception")
        sut.performMultiItemCreateTripSubject.onNext(Unit)
        tripErrorSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.MID_COULD_NOT_FIND_RESULTS), tripErrorSubscriber.values()[0])

        setParamsInDB("mid_create_trip_error")
        sut.performMultiItemCreateTripSubject.onNext(Unit)
        tripErrorSubscriber.assertValueCount(2)
        assertEquals(ApiError(ApiError.Code.UNKNOWN_ERROR), tripErrorSubscriber.values()[1])

        setParamsInDB("mid_create_trip_network_error")
        sut.performMultiItemCreateTripSubject.onNext(Unit)
        tripErrorSubscriber.assertValueCount(2)
    }

    private fun setParamsInDB(filename: String) {
        val params = PackageTestUtil.getPackageSearchParams()
        params.latestSelectedOfferInfo.flightPIID = filename
        params.latestSelectedOfferInfo.hotelId = filename
        params.latestSelectedOfferInfo.inventoryType = filename
        params.latestSelectedOfferInfo.ratePlanCode = filename
        params.latestSelectedOfferInfo.roomTypeCode = filename
        params.latestSelectedOfferInfo.productOfferPrice = PackageOfferModel.PackagePrice()
        params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice = Money(BigDecimal(300.50), "USD")
        params.latestSelectedOfferInfo.hotelCheckInDate = LocalDate.now().plusDays(2).toString()
        params.latestSelectedOfferInfo.hotelCheckOutDate = LocalDate.now().plusDays(4).toString()
        Db.setPackageParams(params)
    }
}
