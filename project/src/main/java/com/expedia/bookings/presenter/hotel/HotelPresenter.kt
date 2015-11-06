package com.expedia.bookings.presenter.hotel

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelErrorPresenter
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.ui.HotelActivity.Screen
import com.expedia.util.endlessObserver
import com.expedia.vm.GeocodeSearchModel
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelErrorViewModel
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelPresenterViewModel
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
    val reviewsView: HotelReviewsView by bindView(R.id.hotel_reviews_presenter)
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    val ANIMATION_DURATION = 400
    val geoCodeSearchModel = GeocodeSearchModel(context)
    private val checkoutDialog = ProgressDialog(context)
    var viewModel : HotelPresenterViewModel by Delegates.notNull()
    private val DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW = 100L

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)

        geoCodeSearchModel.geoResults.subscribe { geoResults ->
            fun triggerNewSearch(selectedResultIndex: Int) {
                val newHotelSearchParams = hotelSearchParams.copy()
                val geoLocation = geoResults.get(selectedResultIndex)
                newHotelSearchParams.suggestion.coordinates.lat = geoLocation.latitude
                newHotelSearchParams.suggestion.coordinates.lng = geoLocation.longitude
                newHotelSearchParams.suggestion.type = "GOOGLE_SUGGESTION_SEARCH"
                // trigger search with selected geoLocation
                searchObserver.onNext(newHotelSearchParams)
            }

            if (geoResults.count() > 0) {
                val freeformLocations = StrUtils.formatAddresses(geoResults)
                val builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.ChooseLocation)
                val dialogItemClickListener = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        triggerNewSearch(which)
                        HotelV2Tracking().trackGeoSuggestionClick()
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

    val defaultTransitionObserver: Observer<Screen> = endlessObserver {
        when (it) {
            Screen.DETAILS -> {
                addDefaultTransition(defaultDetailsTransition)
            }
            Screen.RESULTS -> {
                addDefaultTransition(defaultResultsTransition)
            }
            else -> {
                addDefaultTransition(defaultSearchTransition)
                show(searchPresenter)
            }
        }
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
        addTransition(detailsToError)
        searchPresenter.searchViewModel = HotelSearchViewModel(getContext())
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(searchObserver)

        //NOTE
        //Reason for delay(XYZ, TimeUnit.ABC) to various errorObservables below:
        //
        //When "back" is triggered from Hotel Error Presenter, it is handled by HotelActivity, which asks the child currently on the stack to handle it.
        //That child happens to be Hotel Error Presenter. Handling "back" ends up into one of the errorObservable subscriptions which invariably do `show (xyzPresenter, Presenter.FLAG_CLEAR_TOP)`
        //This `show (xyzPresenter, Presenter.FLAG_CLEAR_TOP)` modifies the backstack.
        //When the Presenter.back() resumes (with a return value of true from HotelErrorPresenter, meaning it has handled the "back"), it simply pushes the popped child.
        //A few milliseconds later (depending on delay(XYZ, TimeUnit.ABC)), `show (xyzPresenter, Presenter.FLAG_CLEAR_TOP)` triggers fulfilling our intent to show the right child with appropriate transition.

        //Note that HotelErrorPresenter.back() returns true, to ensure that Presenter.back() knows that it has handled the back, otherwise it would try to
        //`show` the previous state on the stack by animating from the current (popped) state to the previous state, which is not required at all as our intent is to
        //`show (xyzPresenter, Presenter.FLAG_CLEAR_TOP)` which is being done in various errorObservable subscriptions

        errorPresenter.viewmodel = HotelErrorViewModel(context)
        errorPresenter.viewmodel.searchErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }
        errorPresenter.viewmodel.defaultErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutCardErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.setExpanded(true, true)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutAlreadyBookedObservable.subscribe {
            NavUtils.goToItin(context)
        }

        errorPresenter.viewmodel.soldOutObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(detailPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutPaymentFailedObservable.subscribe(errorPresenter.viewmodel.checkoutCardErrorObservable)

        errorPresenter.viewmodel.sessionTimeOutObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutTravellerErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.setExpanded(true, true)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutUnknownErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.productKeyExpiryObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        resultsPresenter.viewmodel = HotelResultsViewModel(getContext(), hotelServices)
        resultsPresenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)

        detailPresenter.hotelDetailView.viewmodel = HotelDetailViewModel(context, hotelServices, selectedRoomObserver)
        detailPresenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(detailPresenter.hotelRenovationObserver)
        detailPresenter.hotelDetailView.viewmodel.hotelPayLaterInfoObservable.subscribe(detailPresenter.hotelPayLaterInfoObserver)
        detailPresenter.hotelDetailView.viewmodel.vipAccessInfoObservable.subscribe(detailPresenter.hotelVIPAccessInfoObserver)
        detailPresenter.hotelDetailView.viewmodel.mapClickedSubject.subscribe(detailPresenter.hotelDetailsEmbeddedMapClickObserver)
        detailPresenter.hotelMapView.viewmodel = HotelMapViewModel(context, detailPresenter.hotelDetailView.viewmodel.scrollToRoom, detailPresenter.hotelDetailView.viewmodel.hotelSoldOut)
        detailPresenter.hotelDetailView.viewmodel.changeDates.subscribe(changeDatesObserver)

        resultsPresenter.viewmodel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        resultsPresenter.viewmodel.errorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }
        resultsPresenter.viewmodel.showHotelSearchViewObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }

        resultsPresenter.searchOverlaySubject.subscribe(searchResultsOverlayObserver)

        viewModel = HotelPresenterViewModel(checkoutPresenter.hotelCheckoutWidget.createTripViewmodel, checkoutPresenter.hotelCheckoutViewModel, detailPresenter.hotelDetailView.viewmodel)
        viewModel.selectedRoomSoldOut.subscribe(detailPresenter.hotelDetailView.viewmodel.selectedRoomSoldOut)
        viewModel.hotelSoldOutWithHotelId.subscribe (resultsPresenter.adapter.hotelSoldOut)

        checkoutPresenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            checkoutDialog.dismiss()
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        checkoutPresenter.hotelCheckoutViewModel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        checkoutPresenter.hotelCheckoutViewModel.errorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            checkoutDialog.dismiss()
            show(errorPresenter)
        }
        checkoutPresenter.hotelCheckoutViewModel.noResponseObservable.subscribe {
            val retryFun = fun() { checkoutPresenter.hotelCheckoutWidget.slideAllTheWayObservable.onNext(Unit) }
            val cancelFun = fun() { show(detailPresenter) }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
        checkoutPresenter.hotelCheckoutViewModel.checkoutParams.subscribe {
            checkoutDialog.show()
        }
        checkoutPresenter.hotelCheckoutWidget.slideAllTheWayObservable.subscribe {
            checkoutDialog.hide()
        }

        checkoutPresenter.hotelCheckoutWidget.createTripViewmodel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        checkoutPresenter.hotelCheckoutWidget.createTripViewmodel.errorObservable.delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }
        checkoutPresenter.hotelCheckoutWidget.createTripViewmodel.noResponseObservable.subscribe {
            val retryFun = fun() { checkoutPresenter.hotelCheckoutWidget.doCreateTrip() }
            val cancelFun = fun() { show(detailPresenter) }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }

        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(checkoutPresenter.hotelCheckoutWidget.createTripResponseListener)
        checkoutPresenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(endlessObserver { createTripResponse ->
            checkoutDialog.dismiss()
            show(checkoutPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        loadingOverlay.setBackground(R.color.hotels_primary_color)
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(HotelSearchPresenter::class.java.getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
            detailPresenter.setVisibility(View.INVISIBLE)
            loadingOverlay.setVisibility(View.GONE)
        }
    }

    private val defaultDetailsTransition = object : Presenter.DefaultTransition(HotelDetailPresenter::class.java.getName()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            loadingOverlay.visibility = View.GONE
            detailPresenter.visibility = View.VISIBLE
            if (forward) {
                detailPresenter.hotelDetailView.resetGallery()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
                backStack.push(searchPresenter)
            }
        }
    }

    private val defaultResultsTransition = object : Presenter.DefaultTransition(HotelResultsPresenter::class.java.getName()) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
            resultsPresenter.visibility = View.VISIBLE
            resultsPresenter.animationStart()
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            resultsPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.animationFinalize(forward)
            backStack.push(searchPresenter)
        }
    }

    private val searchToResults = object : Presenter.Transition(HotelSearchPresenter::class.java, HotelResultsPresenter::class.java) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
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
            loadingOverlay.setVisibility(View.GONE)
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
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
            searchPresenter.animationStart(!forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (forward) {
                detailPresenter.hotelDetailView.resetGallery()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            }
            if (!forward) HotelV2Tracking().trackHotelV2SearchBox()
        }
    }
    private val resultsToError = ScaleTransition(this, HotelResultsPresenter::class.java, HotelErrorPresenter::class.java)

    private val detailsToCheckout = object: ScaleTransition(this, HotelDetailPresenter::class.java, HotelCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            checkoutDialog.hide()
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                trackHotelDetail()
            }
        }

    }

    private val detailsToError = object: ScaleTransition(this, HotelDetailPresenter::class.java, HotelErrorPresenter::class.java){
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
            showDetails(params.suggestion.hotelId, true)
        }
        else if (params.suggestion.type.equals("RAW_TEXT_SEARCH")) {
            // fire off geo search to resolve raw text into lat/long
            geoCodeSearchModel.searchObserver.onNext(params)
        }
        else {
            // Hotel region search
            show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
            resultsPresenter.viewmodel.paramsSubject.onNext(params)
        }
    }

    val changeDatesObserver: Observer<Unit> = endlessObserver {
        show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
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

    val hotelDetailsListener: Observer<HotelDetailsRequestMetadata> = endlessObserver {
        if (it.isOffersRequest) {
            loadingOverlay.animate(false)
        }

        if (it.hotelOffersResponse.hasErrors() && it.hotelOffersResponse.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE && it.isOffersRequest) {
            //Just show the Hotel Details Screen in "Sold Out" state, fields being fetched from "/info/" API
            showDetails(it.hotelId, false)
        } else if (!it.hotelOffersResponse.hasErrors()) {
            detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(it.hotelOffersResponse)
            detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(it.hotelOffersResponse)
            show(detailPresenter)
            detailPresenter.showDefault()
        }
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        //If hotel is known to be "Sold Out", simply show the Hotel Details Screen in "Sold Out" state, otherwise fetch Offers and show those as well
        showDetails(hotel.hotelId, if (hotel.isSoldOut) false else true)
    }

    private data class HotelDetailsRequestMetadata(val hotelId: String, val hotelOffersResponse: HotelOffersResponse, val isOffersRequest: Boolean)

    private fun showDetails(hotelId: String, fetchOffers: Boolean) {
        if (fetchOffers) {
            loadingOverlay.visibility = View.VISIBLE
            loadingOverlay.animate(true)
        }

        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(hotelSearchParams)
        val subject = PublishSubject.create<HotelOffersResponse>()
        subject.subscribe(object: Observer<HotelOffersResponse> {
            override fun onNext(t: HotelOffersResponse?) {
                hotelDetailsListener.onNext(HotelDetailsRequestMetadata(hotelId, t!!, fetchOffers))
            }

            override fun onCompleted() {}

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() { showDetails(hotelId, fetchOffers) }
                    val cancelFun = fun() { show(searchPresenter) }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }

        })
        if (fetchOffers) {
            hotelServices.offers(hotelSearchParams, hotelId, subject)
        } else {
            hotelServices.info(hotelSearchParams, hotelId, subject)
        }
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
        val hotelSoldOut = detailPresenter.hotelDetailView.viewmodel.hotelSoldOut.value
        HotelV2Tracking().trackPageLoadHotelV2Infosite(hotelOffersResponse, hotelSearchParams, hasEtpOffer, hotelSearchParams.suggestion.isCurrentLocationSearch, hotelSoldOut, viewModel.didLastCreateTripOrCheckoutResultInRoomSoldOut.value)
    }
}
