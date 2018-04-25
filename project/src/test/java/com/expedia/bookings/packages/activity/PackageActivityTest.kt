package com.expedia.bookings.packages.activity

import android.app.Activity
import android.content.Intent
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.Constants
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageActivityTest {

    val context = RuntimeEnvironment.application
    lateinit var packageActivity: PackageActivity

    @Before
    fun setUp() {
        packageActivity = Robolectric.buildActivity(PackageActivity::class.java).create().get()
    }

    @Test
    fun testHotelInfositeErrorHandling() {
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams())

        val testSubscriber = TestObserver<Pair<ApiError.Code, ApiCallFailing>>()
        packageActivity.packagePresenter.hotelOffersErrorObservable.subscribe(testSubscriber)

        var expectedErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR
        val expectedApiCallFailing = "PACKAGE_HOTEL_INFOSITE"

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, true)
        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)

        assertErrorDetails(testSubscriber.values()[0], expectedErrorCode, expectedApiCallFailing)

        // UNKNOWN_ERROR
        expectedErrorCode = ApiError.Code.UNKNOWN_ERROR

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, true)
        testSubscriber.awaitValueCount(2, 2, TimeUnit.SECONDS)
        assertErrorDetails(testSubscriber.values()[1], expectedErrorCode, expectedApiCallFailing)
    }

    @Test
    fun testHotelInfositeChangeErrorHandling() {
        val params = PackageTestUtil.getPackageSearchParams()
        params.pageType = Constants.PACKAGE_CHANGE_HOTEL
        Db.setPackageParams(params)

        val testSubscriber = TestObserver<Pair<ApiError.Code, ApiCallFailing>>()
        packageActivity.packagePresenter.hotelOffersErrorObservable.subscribe(testSubscriber)

        var expectedErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR
        val expectedApiCallFailing = "PACKAGE_HOTEL_INFOSITE_CHANGE"

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, true)
        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)

        assertErrorDetails(testSubscriber.values()[0], expectedErrorCode, expectedApiCallFailing)

        // UNKNOWN_ERROR
        expectedErrorCode = ApiError.Code.UNKNOWN_ERROR

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, true)
        testSubscriber.awaitValueCount(2, 2, TimeUnit.SECONDS)
        assertErrorDetails(testSubscriber.values()[1], expectedErrorCode, expectedApiCallFailing)
    }

    @Test
    fun testHotelRoomErrorHandling() {
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams())

        val testSubscriber = TestObserver<Pair<ApiError.Code, ApiCallFailing>>()
        packageActivity.packagePresenter.hotelOffersErrorObservable.subscribe(testSubscriber)

        var expectedErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR
        val expectedApiCallFailing = "PACKAGE_HOTEL_ROOM"

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, false)
        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)

        assertErrorDetails(testSubscriber.values()[0], expectedErrorCode, expectedApiCallFailing)

        // UNKNOWN_ERROR
        expectedErrorCode = ApiError.Code.UNKNOWN_ERROR

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, false)
        testSubscriber.awaitValueCount(2, 2, TimeUnit.SECONDS)
        assertErrorDetails(testSubscriber.values()[1], expectedErrorCode, expectedApiCallFailing)
    }

    @Test
    fun testHotelRoomChangeErrorHandling() {
        val params = PackageTestUtil.getPackageSearchParams()
        params.pageType = Constants.PACKAGE_CHANGE_HOTEL
        Db.setPackageParams(params)

        val testSubscriber = TestObserver<Pair<ApiError.Code, ApiCallFailing>>()
        packageActivity.packagePresenter.hotelOffersErrorObservable.subscribe(testSubscriber)

        var expectedErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR
        val expectedApiCallFailing = "PACKAGE_HOTEL_ROOM_CHANGE"

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, false)
        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)
        assertErrorDetails(testSubscriber.values()[0], expectedErrorCode, expectedApiCallFailing)

        // UNKNOWN_ERROR
        expectedErrorCode = ApiError.Code.UNKNOWN_ERROR

        setupActivityForResult(expectedErrorCode.name, expectedErrorCode.name, false)
        testSubscriber.awaitValueCount(2, 2, TimeUnit.SECONDS)
        assertErrorDetails(testSubscriber.values()[1], expectedErrorCode, expectedApiCallFailing)
    }

    private fun setupActivityForResult(errorCode: String, errorKey: String, isInfositeFailing: Boolean) {
        val intent = Intent(context, PackageHotelActivity::class.java)
        packageActivity.startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)

        val requestIntent = Intent(context, PackageHotelActivity::class.java)
        val resultIntent = Intent(context, packageActivity::class.java)
        resultIntent.putExtra(Constants.PACKAGE_HOTEL_OFFERS_ERROR, errorCode)
        resultIntent.putExtra(Constants.PACKAGE_HOTEL_OFFERS_ERROR_KEY, errorKey)
        resultIntent.putExtra(Constants.PACKAGE_HOTEL_DID_INFOSITE_CALL_FAIL, isInfositeFailing)

        shadowOf(packageActivity).receiveResult(requestIntent, Activity.RESULT_OK, resultIntent)
    }

    private fun assertErrorDetails(errorDetails: Pair<ApiError.Code, ApiCallFailing>, expectedErrorCode: ApiError.Code, expectedApiCall: String) {
        val apiCallFailingDetails = errorDetails.second

        assertEquals(expectedErrorCode, errorDetails.first)
        assertEquals(expectedErrorCode.name, apiCallFailingDetails.errorCode)
        assertEquals(expectedApiCall, apiCallFailingDetails.apiCall)
    }
}
