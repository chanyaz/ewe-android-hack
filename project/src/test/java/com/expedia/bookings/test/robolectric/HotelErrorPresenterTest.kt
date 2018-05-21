package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil.Companion.getDummyHotelSearchParams
import com.expedia.bookings.utils.Ui
import com.expedia.testutils.Assert.assertViewIsNotVisible
import com.expedia.testutils.Assert.assertViewIsVisible
import com.expedia.vm.HotelErrorViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricRunner::class)
class HotelErrorPresenterTest {

    private lateinit var activity: Activity
    private lateinit var hotelPresenter: HotelPresenter

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
        hotelPresenter.errorPresenter.viewmodel = HotelErrorViewModel(activity)
        hotelPresenter.hotelSearchParams = getDummyHotelSearchParams(activity)
    }

    @Test fun testSoldOutErrorScreen() {
        hotelPresenter.errorPresenter.viewmodel.apiErrorObserver.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
        hotelPresenter.errorPresenter.errorButton.performClick()

        Assert.assertEquals(View.VISIBLE, hotelPresenter.errorPresenter.errorButton.visibility)
        Assert.assertEquals(View.VISIBLE, hotelPresenter.errorPresenter.errorImage.visibility)
        Assert.assertEquals(View.VISIBLE, hotelPresenter.errorPresenter.errorText.visibility)

        Assert.assertEquals(activity.getString(R.string.error_room_sold_out), hotelPresenter.errorPresenter.errorText.text)
        Assert.assertEquals(activity.getString(R.string.select_another_room), hotelPresenter.errorPresenter.errorButton.text)
        val errorImageDrawable = shadowOf(hotelPresenter.errorPresenter.errorImage.drawable)
        Assert.assertEquals(R.drawable.error_default, errorImageDrawable.createdFromResId)
    }

    @Test
    fun testPaymentFailedSlideToPurchaseNotVisible() {
        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.slideWidget.visibility = View.VISIBLE
        assertViewIsVisible(hotelPresenter.checkoutPresenter.hotelCheckoutWidget.slideWidget)
        hotelPresenter.errorPresenter.viewmodel.checkoutPaymentFailedObservable.onNext(Unit)
        assertViewIsNotVisible(hotelPresenter.checkoutPresenter.hotelCheckoutWidget.slideWidget)
    }

    @Test
    fun testCardErrorSlideToPurchaseNotVisible() {
        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.slideWidget.visibility = View.VISIBLE
        assertViewIsVisible(hotelPresenter.checkoutPresenter.hotelCheckoutWidget.slideWidget)
        hotelPresenter.errorPresenter.viewmodel.checkoutCardErrorObservable.onNext(Unit)
        assertViewIsNotVisible(hotelPresenter.checkoutPresenter.hotelCheckoutWidget.slideWidget)
    }
}
