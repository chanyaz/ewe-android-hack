package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.presenter.BaseErrorPresenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelDetailsToolbar
import com.expedia.util.notNullAndObservable
import com.expedia.vm.AbstractErrorViewModel
import com.expedia.vm.HotelErrorViewModel
import com.expedia.vm.hotel.HotelDetailViewModel

class HotelErrorPresenter(context: Context, attr: AttributeSet?) : BaseErrorPresenter(context, attr) {

    val hotelDetailsToolbar: HotelDetailsToolbar by bindView(R.id.hotel_details_toolbar)

    var hotelDetailViewModel: HotelDetailViewModel by notNullAndObservable { vm ->
        vm.hotelOffersSubject.subscribe{
            hotelDetailsToolbar.setHotelDetailViewModel(HotelDetailViewModel.convertToToolbarViewModel(vm))
        }
    }

    init {
        hotelDetailsToolbar.toolbar.setNavigationOnClickListener {
            viewmodel.defaultErrorObservable.onNext(Unit)
        }
        standardToolbar.setNavigationOnClickListener {
            handleCheckoutErrors()
        }
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

    override fun back(): Boolean {
        handleCheckoutErrors()
        return true
    }

    private fun handleCheckoutErrors() {
        val checkoutError = getViewModel().error

        when (checkoutError.errorCode) {
            ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS -> {
                 viewmodel.checkoutCardErrorObservable.onNext(Unit)
            }
            ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS -> {
                 viewmodel.checkoutTravelerErrorObservable.onNext(Unit)
            }
            ApiError.Code.PAYMENT_FAILED -> {
                viewmodel.checkoutCardErrorObservable.onNext(Unit)
            }
            ApiError.Code.INVALID_CARD_NUMBER -> {
                viewmodel.checkoutCardErrorObservable.onNext(Unit)
            }
            ApiError.Code.CARD_LIMIT_EXCEEDED -> {
                 viewmodel.checkoutCardErrorObservable.onNext(Unit)
            }
            ApiError.Code.INVALID_CARD_EXPIRATION_DATE -> {
                viewmodel.checkoutCardErrorObservable.onNext(Unit)
            }
            else -> {
                 viewmodel.defaultErrorObservable.onNext(Unit)
            }
        }
    }
}
