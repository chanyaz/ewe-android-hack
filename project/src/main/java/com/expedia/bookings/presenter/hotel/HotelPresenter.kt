package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelErrorPresenter
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.bookings.widget.RoomSelected
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelErrorViewModel
import com.expedia.vm.HotelResultsViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.HotelSearchViewModel
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    var hotelServices: HotelServices by Delegates.notNull()
        @Inject set

    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val searchPresenter: HotelSearchPresenter by bindView(R.id.widget_hotel_params)
    val errorPresenter: HotelErrorPresenter by bindView(R.id.widget_hotel_errors)
    val resultsPresenter: HotelResultsPresenter by bindView(R.id.widget_hotel_results)
    val detailPresenter: HotelDetailPresenter by bindView(R.id.widget_hotel_detail)
    val checkoutPresenter: HotelCheckoutPresenter by bindView(R.id.hotel_checkout_presenter)
    val confirmationPresenter: HotelConfirmationPresenter by bindView(R.id.hotel_confirmation_presenter)
    val reviewsPresenter: HotelReviewsPresenter by bindView(R.id.hotel_reviews_presenter)
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    val ANIMATION_DURATION = 400

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()

        addTransition(searchToResults)
        addTransition(searchToDetails)
        addTransition(resultsToDetail)
        addTransition(detailsToCheckout)
        addTransition(checkoutToConfirmation)
        addTransition(detailsToReview)
        addTransition(resultsToError)
        addTransition(searchToError)
        addTransition(checkoutToError)
        addDefaultTransition(defaultTransition)
        show(searchPresenter)
        searchPresenter.viewmodel = HotelSearchViewModel(getContext())
        searchPresenter.viewmodel.searchParamsObservable.subscribe(searchObserver)

        errorPresenter.viewmodel = HotelErrorViewModel(context)
        errorPresenter.viewmodel.searchErrorObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_BACKSTACK) }
        errorPresenter.viewmodel.defaultErrorObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_BACKSTACK) }
        errorPresenter.viewmodel.checkoutCardErrorObservable.subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.setExpanded(true, true)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutTravellerErrorObservable.subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.setExpanded(true, true)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutUnknownErrorObservable.subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        resultsPresenter.viewmodel = HotelResultsViewModel(getContext(), hotelServices)
        resultsPresenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)

        detailPresenter.hotelDetailView.viewmodel = HotelDetailViewModel(context, hotelServices)
        detailPresenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(detailPresenter.hotelRenovationObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelPayLaterInfoObservable.subscribe(detailPresenter.hotelPayLaterInfoObserver)

        resultsPresenter.viewmodel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        resultsPresenter.viewmodel.errorObservable.delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }

        resultsPresenter.searchOverlaySubject.subscribe(searchResultsOverlayObserver)

        RoomSelected.observer = selectedRoomObserver

        checkoutPresenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        checkoutPresenter.hotelCheckoutViewModel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        checkoutPresenter.hotelCheckoutViewModel.errorObservable.delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }


        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(checkoutPresenter.hotelCheckoutWidget.createTripResponseListener)
        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(endlessObserver { createTripResponse ->
            show(checkoutPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        loadingOverlay.setBackground(R.color.hotels_primary_color)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(HotelSearchPresenter::class.java.getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
            detailPresenter.setVisibility(View.GONE)
            loadingOverlay.setVisibility(View.GONE)
        }
    }

    private val searchToResults = object : Presenter.Transition(HotelSearchPresenter::class.java, HotelResultsPresenter::class.java) {

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

    private val resultsToDetail = object : Presenter.Transition(HotelResultsPresenter::class.java.getName(), HotelDetailPresenter::class.java.getName(), DecelerateInterpolator(), ANIMATION_DURATION) {
        private var detailsHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            val parentHeight = getHeight()
            detailsHeight = parentHeight - Ui.getStatusBarHeight(getContext())
            val pos = (if (forward) detailsHeight else 0).toFloat()
            detailPresenter.setTranslationY(pos)
            detailPresenter.setVisibility(View.VISIBLE)
            detailPresenter.animationStart()
            resultsPresenter.setVisibility(View.VISIBLE)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) (detailsHeight - (f * detailsHeight)) else (f * detailsHeight)
            detailPresenter.setTranslationY(pos)
            detailPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            detailPresenter.setTranslationY((if (forward) 0 else detailsHeight).toFloat())
            detailPresenter.setVisibility(if (forward) View.VISIBLE else View.GONE)
            resultsPresenter.setVisibility(if (forward) View.GONE else View.VISIBLE)
            detailPresenter.animationFinalize()
            loadingOverlay.setVisibility(View.GONE)
            detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
        }
    }

    private val checkoutToError = object : Presenter.Transition(HotelCheckoutPresenter::class.java.getName(), HotelErrorPresenter::class.java.getName(), DecelerateInterpolator(), ANIMATION_DURATION) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            checkoutPresenter.setVisibility(View.VISIBLE)
            errorPresenter.setVisibility(View.VISIBLE)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            checkoutPresenter.setVisibility(if (forward) View.GONE else View.VISIBLE)
            errorPresenter.setVisibility(if (forward) View.VISIBLE else View.GONE)
            errorPresenter.animationFinalize()
        }
    }


    private val searchToError = object : Presenter.Transition(HotelSearchPresenter::class.java.getName(), HotelErrorPresenter::class.java.getName(), DecelerateInterpolator(), ANIMATION_DURATION) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            searchPresenter.setVisibility(View.VISIBLE)
            errorPresenter.setVisibility(View.VISIBLE)
            searchPresenter.animationStart(!forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            searchPresenter.setVisibility(if (forward) View.GONE else View.VISIBLE)
            errorPresenter.setVisibility(if (forward) View.VISIBLE else View.GONE)
            searchPresenter.animationFinalize()
            errorPresenter.animationFinalize()
        }
    }

    private val searchToDetails = ScaleTransition(this, HotelSearchPresenter::class.java, HotelDetailPresenter::class.java)
    private val resultsToError = ScaleTransition(this, HotelResultsPresenter::class.java, HotelErrorPresenter::class.java)
    private val detailsToCheckout = ScaleTransition(this, HotelDetailPresenter::class.java, HotelCheckoutPresenter::class.java)
    private val checkoutToConfirmation = ScaleTransition(this, HotelCheckoutPresenter::class.java, HotelConfirmationPresenter::class.java)
    private val detailsToReview = ScaleTransition(this, HotelDetailPresenter::class.java, HotelReviewsPresenter::class.java)

    val searchObserver: Observer<HotelSearchParams> = endlessObserver { params ->
        hotelSearchParams = params
        errorPresenter.viewmodel.paramsSubject.onNext(params)
        checkoutPresenter.hotelCheckoutWidget.setSearchParams(params)
        if (params.suggestion.hotelId != null) {
            // Hotel name search - go straight to details
            showDetails(params.suggestion.hotelId)
        } else {
            // Hotel region search
            show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
            resultsPresenter.viewmodel.paramsSubject.onNext(params)
        }
    }

    val reviewsObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
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
        loadingOverlay.visibility = View.GONE
        detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(response)
        show(detailPresenter)
        detailPresenter.showDefault()
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        showDetails(hotel.hotelId)
    }

    private fun showDetails(hotelId: String) {
        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.animate(true)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(hotelSearchParams)

        val subject = PublishSubject.create<HotelOffersResponse>()
        subject.subscribe { downloadListener.onNext(it) }
        hotelServices.details(hotelSearchParams, hotelId, subject)
    }

    override fun back(): Boolean {
        if (loadingOverlay.visibility != View.VISIBLE) {
            return super.back()
        }
        return false
    }

    val searchResultsOverlayObserver: Observer<Unit> = endlessObserver { params ->
        show(searchPresenter)
    }

}
