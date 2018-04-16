package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.vm.hotel.HotelDetailViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelDetailViewTest {

    private var activity: Activity by Delegates.notNull()
    private var testVM: HotelDetailViewModel by Delegates.notNull()
    private var contentView: HotelDetailView by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        contentView = android.view.LayoutInflater.from(activity).inflate(R.layout.test_hotel_detail_view, null) as HotelDetailView
        testVM = HotelDetailViewModel(activity,
                HotelInfoManager(Mockito.mock(HotelServices::class.java)),
                Mockito.mock(HotelSearchManager::class.java))
        contentView.viewmodel = testVM
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDatelessBottomButton() {
        testVM.isDatelessObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        assertEquals("Select Dates", contentView.bottomButtonWidget.buttonBottom.text)
        assertEquals((contentView.bottomButtonWidget.buttonBottom.background as ColorDrawable).color,
                ContextCompat.getColor(activity, R.color.app_primary))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSoldOutBottomButton() {
        testVM.isDatelessObservable.onNext(false)
        testVM.hotelSoldOut.onNext(true)
        assertEquals("Change Dates", contentView.bottomButtonWidget.buttonBottom.text)
        assertEquals((contentView.bottomButtonWidget.buttonBottom.background as ColorDrawable).color,
                ContextCompat.getColor(activity, R.color.app_primary))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSelectRoom() {
        testVM.isDatelessObservable.onNext(false)
        testVM.hotelSoldOut.onNext(false)
        assertEquals("Select a Room", contentView.bottomButtonWidget.buttonBottom.text)
        assertEquals((contentView.bottomButtonWidget.buttonBottom.background as ColorDrawable).color,
                ContextCompat.getColor(activity, R.color.app_primary))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSelectRoomJustSoldOutFalse() {
        // a successful hotel response will return only trigger the hotelSoldOut=false observable
        testVM.hotelSoldOut.onNext(false)
        assertEquals("Select a Room", contentView.bottomButtonWidget.buttonBottom.text)
        assertEquals((contentView.bottomButtonWidget.buttonBottom.background as ColorDrawable).color,
                ContextCompat.getColor(activity, R.color.app_primary))
    }
}
