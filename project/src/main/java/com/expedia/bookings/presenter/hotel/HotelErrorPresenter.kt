package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseErrorPresenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelDetailsToolbar
import com.expedia.util.notNullAndObservable
import com.expedia.vm.AbstractErrorViewModel
import com.expedia.vm.HotelErrorViewModel
import com.expedia.vm.HotelInfoToolbarViewModel
import com.expedia.bookings.hotel.vm.HotelDetailViewModel

class HotelErrorPresenter(context: Context, attr: AttributeSet?) : BaseErrorPresenter(context, attr) {

    val hotelDetailsToolbar: HotelDetailsToolbar by bindView(R.id.hotel_details_toolbar)

    val hotelInfoToolbarViewModel = HotelInfoToolbarViewModel(context)

    var hotelDetailViewModel: HotelDetailViewModel by notNullAndObservable { vm ->
        vm.hotelOffersSubject.subscribe { hotelOffersResponse ->
            hotelInfoToolbarViewModel.bind(hotelOffersResponse)
        }
    }

    init {
        hotelDetailsToolbar.toolbar.setNavigationOnClickListener {
            viewmodel.defaultErrorObservable.onNext(Unit)
        }
        standardToolbar.setNavigationOnClickListener {
            getViewModel().handleCheckoutErrors()
        }

        hotelDetailsToolbar.setHotelDetailViewModel(hotelInfoToolbarViewModel)
        hotelDetailsToolbar.hideGradient()
    }

    override fun setupViewModel(vm: AbstractErrorViewModel) {
        super.setupViewModel(vm)
        vm as HotelErrorViewModel
        vm.hotelSoldOutErrorObservable.subscribe { isSoldOut ->
            // show appropriate toolbar
            standardToolbarContainer.visibility = if (isSoldOut) View.GONE else View.VISIBLE
            hotelDetailsToolbar.visibility = if (isSoldOut) View.VISIBLE else View.GONE
        }
    }

    override fun getViewModel(): HotelErrorViewModel {
        return viewmodel as HotelErrorViewModel
    }
}
