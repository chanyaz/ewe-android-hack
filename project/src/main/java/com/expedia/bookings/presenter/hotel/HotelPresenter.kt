package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.RoomSelected
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelCheckoutViewModel
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelResultsViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.HotelSearchViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    var hotelServices: HotelServices by Delegates.notNull()
        @Inject set

    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val searchPresenter: HotelSearchPresenter by bindView(R.id.widget_hotel_params)
    val resultsPresenter: HotelResultsPresenter by bindView(R.id.widget_hotel_results)
    val detailPresenter: HotelDetailPresenter by bindView(R.id.widget_hotel_detail)
    val checkoutPresenter: HotelCheckoutPresenter by bindView(R.id.hotel_checkout_presenter)
    val confirmationPresenter: HotelConfirmationPresenter by bindView(R.id.hotel_confirmation_presenter)
    val reviewsPresenter: HotelReviewsPresenter by bindView(R.id.hotel_reviews_presenter)

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()

        addTransition(searchToResults)
        addTransition(resultsToDetail)
        addTransition(detailsToCheckout)
        addTransition(checkoutToConfirmation)
        addTransition(detailsToReview)
        addDefaultTransition(defaultTransition)
        show(searchPresenter)
        searchPresenter.viewmodel = HotelSearchViewModel(getContext())
        searchPresenter.viewmodel.searchParamsObservable.subscribe(searchObserver)

        resultsPresenter.viewmodel = HotelResultsViewModel(getContext(), hotelServices)
        searchPresenter.viewmodel.searchParamsObservable.subscribe(resultsPresenter.viewmodel.paramsSubject)
        resultsPresenter.hotelSubject.subscribe(hotelSelectedObserver)

        RoomSelected.observer = selectedRoomObserver

        checkoutPresenter.viewmodel = HotelCheckoutViewModel(hotelServices)
        checkoutPresenter.viewmodel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            confirmationPresenter.bind(checkoutResponse)
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        })
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<HotelSearchPresenter>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
            detailPresenter.setVisibility(View.GONE)
        }
    }
    private val searchToResults = LeftToRightTransition(this, javaClass<HotelSearchPresenter>(), javaClass<HotelResultsPresenter>())
    private val resultsToDetail = LeftToRightTransition(this, javaClass<HotelResultsPresenter>(), javaClass<HotelDetailPresenter>())
    private val detailsToCheckout = LeftToRightTransition(this, javaClass<HotelDetailPresenter>(), javaClass<HotelCheckoutPresenter>())
    private val checkoutToConfirmation = LeftToRightTransition(this, javaClass<HotelCheckoutPresenter>(), javaClass<HotelConfirmationPresenter>())
    private val detailsToReview = LeftToRightTransition(this, javaClass<HotelDetailPresenter>(),javaClass<HotelReviewsPresenter>())

    val searchObserver: Observer<HotelSearchParams> = endlessObserver { params ->
        hotelSearchParams = params
        checkoutPresenter.hotelCheckoutWidget.setSearchParams(params)
        show(resultsPresenter)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        detailPresenter.hotelDetailView.viewmodel = HotelDetailViewModel(getContext(), hotelServices)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(hotelSearchParams)
        detailPresenter.hotelDetailView.viewmodel.hotelSelectedSubject.onNext(hotel)
        detailPresenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(detailPresenter.hotelRenovationObserver)
        detailPresenter.showDefault()
        show(detailPresenter)
    }

    val reviewsObserver: Observer<Hotel> = endlessObserver { hotel ->
        reviewsPresenter.viewModel = HotelReviewsViewModel(getContext())
        reviewsPresenter.viewModel.hotelObserver.onNext(hotel)
        show(reviewsPresenter)
    }

    val selectedRoomObserver = object : Observer<HotelOffersResponse.HotelRoomResponse> {
        override fun onNext(t: HotelOffersResponse.HotelRoomResponse) {
            checkoutPresenter.showCheckout(t)
            show(checkoutPresenter)
        }

        override fun onCompleted() {
            throw UnsupportedOperationException()
        }

        override fun onError(e: Throwable) {
            throw OnErrorNotImplementedException(e)
        }
    }
}
