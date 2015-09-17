package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import butterknife.InjectView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.lx.LXDetailsPresenter
import com.expedia.bookings.presenter.lx.LXResultsPresenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.bookings.widget.RoomSelected
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelCheckoutViewModel
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelResultsViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelConfirmationViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject
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
    val loadingOverlay : LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    val ANIMATION_DURATION = 400

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

        checkoutPresenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(checkoutPresenter.hotelCheckoutWidget.createTripResponseListener)
        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(endlessObserver { createTripResponse ->
            show(checkoutPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        loadingOverlay.setBackground(R.color.hotels_primary_color)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<HotelSearchPresenter>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
            detailPresenter.setVisibility(View.GONE)
            loadingOverlay.setVisibility(View.GONE)
        }
    }

    private val searchToResults = object : Presenter.Transition(javaClass<HotelSearchPresenter>(), javaClass<HotelResultsPresenter>()) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.VISIBLE)
            searchPresenter.animationStart(!forward)
            resultsPresenter.animationStart()
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            resultsPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            searchPresenter.setVisibility(if (forward) View.GONE else View.VISIBLE)
            resultsPresenter.setVisibility(if (forward) View.VISIBLE else View.GONE)
            searchPresenter.animationFinalize()
            resultsPresenter.animationFinalize()
        }
    }

    private val resultsToDetail = object : Presenter.Transition(javaClass<HotelResultsPresenter>().getName(), javaClass<HotelDetailPresenter>().getName(), DecelerateInterpolator(), ANIMATION_DURATION) {
        private var detailsHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            val parentHeight = getHeight()
            detailsHeight = parentHeight - Ui.getStatusBarHeight(getContext())
            val pos = (if (forward) parentHeight + detailsHeight else detailsHeight).toFloat()
            detailPresenter.setTranslationY(pos)
            detailPresenter.setVisibility(View.VISIBLE)
            detailPresenter.animationStart(!forward)
            resultsPresenter.setVisibility(View.VISIBLE)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) detailsHeight + (-f * detailsHeight) else (f * detailsHeight)
            detailPresenter.setTranslationY(pos)
            detailPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            detailPresenter.setTranslationY((if (forward) 0 else detailsHeight).toFloat())
        }

        override fun finalizeTransition(forward: Boolean) {
            detailPresenter.setTranslationY((if (forward) 0 else detailsHeight).toFloat())
            detailPresenter.setVisibility(if (forward) View.VISIBLE else View.GONE)
            resultsPresenter.setVisibility(if (forward) View.GONE else View.VISIBLE)
            detailPresenter.animationFinalize(!forward)
            loadingOverlay.setVisibility(View.GONE)
        }
    }

    private val detailsToCheckout = ScaleTransition(this, javaClass<HotelDetailPresenter>(), javaClass<HotelCheckoutPresenter>())
    private val checkoutToConfirmation = ScaleTransition(this, javaClass<HotelCheckoutPresenter>(), javaClass<HotelConfirmationPresenter>())
    private val detailsToReview = ScaleTransition(this, javaClass<HotelDetailPresenter>(),javaClass<HotelReviewsPresenter>())

    val searchObserver: Observer<HotelSearchParams> = endlessObserver { params ->
        hotelSearchParams = params
        checkoutPresenter.hotelCheckoutWidget.setSearchParams(params)
        show(resultsPresenter)
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

    val downloadListener: Observer<HotelOffersResponse> = endlessObserver { response ->
        loadingOverlay.animate(false)
        detailPresenter.hotelDetailView.viewmodel.hotelOffersResponse = response
        detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(response)
        detailPresenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(detailPresenter.hotelRenovationObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelPayLaterInfoObservable.subscribe(detailPresenter.hotelPayLaterInfoObserver)
        detailPresenter.showDefault()
        show(detailPresenter)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        loadingOverlay.setVisibility(View.VISIBLE)
        loadingOverlay.animate(true)
        if (hotel.isSponsoredListing) AdImpressionTracking.trackAdClickOrImpression(getContext(), hotel.clickTrackingUrl, null)
        detailPresenter.hotelDetailView.viewmodel = HotelDetailViewModel(getContext(), hotelServices)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(hotelSearchParams)
        detailPresenter.hotelDetailView.viewmodel.hotelSelectedSubject.onNext(hotel)

        val subject = PublishSubject.create<HotelOffersResponse>()
        subject.subscribe { downloadListener.onNext(it) }
        hotelServices.details(hotelSearchParams, hotel.hotelId, subject)
    }

}
