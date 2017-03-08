package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.presenter.hotel.HotelErrorPresenter
import com.expedia.vm.HotelErrorViewModel
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class HotelErrorPresenterTest {
    @Test fun testSoldOutErrorScreen() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)

        val hotelErrorPresenter = HotelErrorPresenter(activity, null)
        hotelErrorPresenter.viewmodel = HotelErrorViewModel(activity)
        hotelErrorPresenter.getViewModel().apiErrorObserver.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
        hotelErrorPresenter.errorButton.performClick()

        Assert.assertEquals(View.VISIBLE, hotelErrorPresenter.errorButton.visibility)
        Assert.assertEquals(View.VISIBLE, hotelErrorPresenter.errorImage.visibility)
        Assert.assertEquals(View.VISIBLE, hotelErrorPresenter.errorText.visibility)

        Assert.assertEquals(activity.getString(R.string.error_room_sold_out), hotelErrorPresenter.errorText.text)
        Assert.assertEquals(activity.getString(R.string.select_another_room), hotelErrorPresenter.errorButton.text)
        val errorImageShadow = org.robolectric.Shadows.shadowOf(hotelErrorPresenter.errorImage)
        Assert.assertEquals(R.drawable.error_default, errorImageShadow.imageResourceId)
    }
}
