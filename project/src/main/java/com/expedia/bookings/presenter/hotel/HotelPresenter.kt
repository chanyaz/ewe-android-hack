package com.expedia.bookings.presenter.hotel

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
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
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelErrorPresenter
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.*
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
    val reviewsView: HotelReviewsView by bindView(R.id.hotel_reviews_presenter)
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    val ANIMATION_DURATION = 400
    val geoCodeSearchModel = GeocodeSearchModel(context)
    private var checkoutDialog = ProgressDialog(context)

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)

        geoCodeSearchModel.geoResults.subscribe { geoResults ->
            fun triggerNewSearch(selectedResultIndex: Int) {
                val newHotelSearchParams = hotelSearchParams.copy()
                val geoLocation = geoResults.get(selectedResultIndex)
                newHotelSearchParams.suggestion.coordinates.lat = geoLocation.latitude
                newHotelSearchParams.suggestion.coordinates.lng = geoLocation.longitude
                newHotelSearchParams.suggestion.type = ""
                // trigger search with selected geoLocation
                searchObserver.onNext(newHotelSearchParams)
            }

            if (geoResults.count() == 1) {
                triggerNewSearch(0) // search with the only lat/long we got back
            }
            else if (geoResults.count() > 1) {
                val freeformLocations = StrUtils.formatAddresses(geoResults)
                val builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.ChooseLocation)
                val dialogItemClickListener = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        triggerNewSearch(which)
                    }
                }
                builder.setItems(freeformLocations, dialogItemClickListener)
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
        checkoutDialog.setMessage(resources.getString(R.string.booking_loading))
        checkoutDialog.isIndeterminate = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

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
        searchPresenter.searchViewModel = HotelSearchViewModel(getContext())
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(searchObserver)

        errorPresenter.viewmodel = HotelErrorViewModel(context)
        errorPresenter.viewmodel.searchErrorObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }
        errorPresenter.viewmodel.defaultErrorObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }
        errorPresenter.viewmodel.checkoutCardErrorObservable.subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.setExpanded(true, true)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutAlreadyBookedObservable.subscribe {
            NavUtils.goToItin(context)
        }

        errorPresenter.viewmodel.checkoutPaymentFailedObservable.subscribe(errorPresenter.viewmodel.checkoutCardErrorObservable)

        errorPresenter.viewmodel.sessionTimeOutObservable.subscribe { show(searchPresenter) }

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

        errorPresenter.viewmodel.productKeyExpiryObservable.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        resultsPresenter.viewmodel = HotelResultsViewModel(getContext(), hotelServices)
        resultsPresenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)

        detailPresenter.hotelDetailView.viewmodel = HotelDetailViewModel(context, hotelServices, selectedRoomObserver)
        detailPresenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(detailPresenter.hotelRenovationObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelPayLaterInfoObservable.subscribe(detailPresenter.hotelPayLaterInfoObserver)
        detailPresenter.hotelDetailView.viewmodel.vipAccessInfoObservable.subscribe(detailPresenter.hotelVIPAccessInfoObserver)
        resultsPresenter.viewmodel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        resultsPresenter.viewmodel.errorObservable.delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }

        resultsPresenter.searchOverlaySubject.subscribe(searchResultsOverlayObserver)

        checkoutPresenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            checkoutDialog.dismiss()
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        checkoutPresenter.hotelCheckoutViewModel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        checkoutPresenter.hotelCheckoutViewModel.errorObservable.delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            checkoutDialog.dismiss()
            show(errorPresenter)
        }
        checkoutPresenter.hotelCheckoutViewModel.checkoutParams.subscribe {
            checkoutDialog.show()
        }

        checkoutPresenter.hotelCheckoutWidget.viewmodel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        checkoutPresenter.hotelCheckoutWidget.viewmodel.errorObservable.delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }

        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(checkoutPresenter.hotelCheckoutWidget.createTripResponseListener)
        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(endlessObserver { createTripResponse ->
            checkoutDialog.dismiss()
            show(checkoutPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        loadingOverlay.setBackground(R.color.hotels_primary_color)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(HotelSearchPresenter::class.java.getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
            detailPresenter.setVisibility(View.INVISIBLE)
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
            resultsPresenter.animationFinalize(forward)
            if (!forward) HotelV2Tracking().trackHotelV2SearchBox()
        }
    }

    private val resultsToDetail = object : Presenter.Transition(HotelResultsPresenter::class.java.getName(), HotelDetailPresenter::class.java.getName(), DecelerateInterpolator(), ANIMATION_DURATION) {
        private var detailsHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
            }
            else {
                detailPresenter.hotelDetailView.resetGallery()
            }
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
            detailPresenter.setVisibility(if (forward) View.VISIBLE else View.GONE)
            resultsPresenter.setVisibility(if (forward) View.GONE else View.VISIBLE)
            detailPresenter.setTranslationY(0f)
            resultsPresenter.animationFinalize(!forward)
            detailPresenter.animationFinalize()
            loadingOverlay.setVisibility(View.GONE)
            if (forward) {
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            } else {
                resultsPresenter.recyclerView.adapter.notifyDataSetChanged()
                HotelV2Tracking().trackHotelsV2Search(hotelSearchParams, resultsPresenter.viewmodel.hotelResultsObservable.value)
            }
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
            if (!forward) HotelV2Tracking().trackHotelV2SearchBox()
        }
    }

    private val searchToDetails = object : ScaleTransition(this, HotelSearchPresenter::class.java, HotelDetailPresenter::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (forward) {
                detailPresenter.hotelDetailView.resetGallery()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            }
            loadingOverlay.visibility = View.GONE
            if (!forward) HotelV2Tracking().trackHotelV2SearchBox()
        }
    }
    private val resultsToError = ScaleTransition(this, HotelResultsPresenter::class.java, HotelErrorPresenter::class.java)

    private val detailsToCheckout = object: ScaleTransition(this, HotelDetailPresenter::class.java, HotelCheckoutPresenter::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                trackHotelDetail()
            }
        }

    }

    private val checkoutToConfirmation = ScaleTransition(this, HotelCheckoutPresenter::class.java, HotelConfirmationPresenter::class.java)
    private val detailsToReview = object: ScaleTransition(this, HotelDetailPresenter::class.java, HotelReviewsView::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (forward) {
                reviewsView.transitionFinished()
            } else {
                trackHotelDetail()
            }
        }
    }

    val searchObserver: Observer<HotelSearchParams> = endlessObserver { params ->
        hotelSearchParams = params
        errorPresenter.viewmodel.paramsSubject.onNext(params)
        checkoutPresenter.hotelCheckoutWidget.setSearchParams(params)
        if (params.suggestion.hotelId != null) {
            // Hotel name search - go straight to details
            showDetails(params.suggestion.hotelId)
        }
        else if (params.suggestion.type.equals("RAW_TEXT_SEARCH")) {
            // fire off geo search to resolve raw text into lat/long
            geoCodeSearchModel.searchObserver.onNext(params)
        }
        else {
            // Hotel region search
            show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
            resultsPresenter.viewmodel.paramsSubject.onNext(params)
            resultsPresenter.filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch)
            resultsPresenter.filterView.viewmodel.clearObservable.onNext(Unit)
        }
    }

    val reviewsObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
        reviewsView.viewModel = HotelReviewsViewModel(getContext())
        reviewsView.viewModel.hotelObserver.onNext(hotel)
        show(reviewsView)
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
        return true
    }

    val searchResultsOverlayObserver: Observer<Unit> = endlessObserver { params ->
        show(searchPresenter)
    }

    private fun trackHotelDetail() {
        val hotelOffersResponse = detailPresenter.hotelDetailView.viewmodel.hotelOffersResponse
        val hasEtpOffer = detailPresenter.hotelDetailView.viewmodel.hasEtpOffer(hotelOffersResponse)
        HotelV2Tracking().trackPageLoadHotelV2Infosite(hotelOffersResponse, hotelSearchParams, hasEtpOffer, hotelSearchParams.suggestion.isCurrentLocationSearch)
    }

}
