package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.WalletUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.HotelErrorPresenter
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.ui.HotelActivity.Screen
import com.expedia.util.endlessObserver
import com.expedia.vm.GeocodeSearchModel
import com.expedia.vm.HotelCheckoutViewModel
import com.expedia.vm.HotelConfirmationViewModel
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelErrorViewModel
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelPresenterViewModel
import com.expedia.vm.HotelResultsViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.HotelSearchViewModel
import com.google.android.gms.maps.MapView
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    lateinit var hotelServices: HotelServices
        @Inject set

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    var hotelDetailViewModel: HotelDetailViewModel by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val resultsMapView: MapView by bindView(R.id.map_view)
    val detailsMapView: MapView by bindView(R.id.details_map_view)
    val searchPresenter: HotelSearchPresenter by bindView(R.id.widget_hotel_params)
    val searchPresenterV2: HotelSearchPresenterV2 by bindView(R.id.widget_hotel_params_v2)
    val errorPresenter: HotelErrorPresenter by bindView(R.id.widget_hotel_errors)
    val resultsStub: ViewStub by bindView(R.id.results_stub)
    val resultsPresenter: HotelResultsPresenter by lazy {
        var presenter = resultsStub.inflate() as HotelResultsPresenter
        var resultsStub = presenter.findViewById(R.id.stub_map) as FrameLayout
        resultsMapView.visibility = View.VISIBLE
        removeView(resultsMapView);
        resultsStub.addView(resultsMapView)
        presenter.mapView = resultsMapView
        presenter.mapView.getMapAsync(presenter)
        presenter.viewmodel = HotelResultsViewModel(getContext(), hotelServices, LineOfBusiness.HOTELSV2)
        presenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)
        presenter.viewmodel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        presenter.viewmodel.errorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }
        presenter.viewmodel.showHotelSearchViewObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }
        presenter.searchOverlaySubject.subscribe(searchResultsOverlayObserver)
        presenter.showDefault()
        presenter
    }
    val detailsStub: ViewStub by bindView(R.id.details_stub)
    val detailPresenter: HotelDetailPresenter by lazy {
        var presenter = detailsStub.inflate() as HotelDetailPresenter
        var detailsStub = presenter.hotelMapView.findViewById(R.id.stub_map) as FrameLayout
        detailsMapView.visibility = View.VISIBLE
        removeView(detailsMapView);
        detailsStub.addView(detailsMapView)
        presenter.hotelMapView.mapView = detailsMapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView);
        presenter.hotelDetailView.viewmodel = hotelDetailViewModel
        presenter.hotelDetailView.viewmodel.depositInfoContainerClickObservable.subscribe { pair: Pair<String, HotelOffersResponse.HotelRoomResponse> ->
            presenter.hotelDepositInfoObserver.onNext(pair)
        }
        presenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        presenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(presenter.hotelRenovationObserver)
        presenter.hotelDetailView.viewmodel.hotelPayLaterInfoObservable.subscribe { pair: Pair<String, List<HotelOffersResponse.HotelRoomResponse>> ->
            presenter.hotelPayLaterInfoObserver.onNext(pair)
        }
        presenter.hotelDetailView.viewmodel.vipAccessInfoObservable.subscribe(presenter.hotelVIPAccessInfoObserver)
        presenter.hotelDetailView.viewmodel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, presenter.hotelDetailView.viewmodel.scrollToRoom, presenter.hotelDetailView.viewmodel.hotelSoldOut)
        presenter.hotelDetailView.viewmodel.changeDates.subscribe(changeDatesObserver)

        viewModel = HotelPresenterViewModel(checkoutPresenter.hotelCheckoutWidget.createTripViewmodel, checkoutPresenter.hotelCheckoutViewModel, presenter.hotelDetailView.viewmodel)
        viewModel.selectedRoomSoldOut.subscribe(presenter.hotelDetailView.viewmodel.selectedRoomSoldOut)
        viewModel.hotelSoldOutWithHotelId.subscribe ((resultsPresenter.mapCarouselRecycler.adapter as HotelMapCarouselAdapter).hotelSoldOut)
        viewModel.hotelSoldOutWithHotelId.subscribe (resultsPresenter.adapter.hotelSoldOut)
        viewModel.hotelSoldOutWithHotelId.subscribe (resultsPresenter.mapViewModel.hotelSoldOutWithIdObserver)

        presenter
    }

    val checkoutStub: ViewStub by bindView(R.id.checkout_stub)
    val checkoutPresenter: HotelCheckoutPresenter by lazy {
        var presenter = checkoutStub.inflate() as HotelCheckoutPresenter
        presenter.hotelCheckoutWidget.createTripViewmodel = HotelCreateTripViewModel(hotelServices, paymentModel)
        presenter.hotelCheckoutViewModel = HotelCheckoutViewModel(hotelServices, paymentModel)
        confirmationPresenter.hotelConfirmationViewModel = HotelConfirmationViewModel(presenter.hotelCheckoutViewModel.checkoutResponseObservable, context)
        presenter.hotelCheckoutViewModel.checkoutParams.subscribe { presenter.cvv.enableBookButton(false) }
        presenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            checkoutDialog.dismiss()
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
            WalletUtils.unbindFullWalletDataFromBillingInfo(Db.getWorkingBillingInfoManager().workingBillingInfo)
        })

        presenter.hotelCheckoutViewModel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        presenter.hotelCheckoutViewModel.errorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            checkoutDialog.dismiss()
            show(errorPresenter)
        }
        presenter.hotelCheckoutViewModel.noResponseObservable.subscribe {
            val retryFun = fun() {
                presenter.hotelCheckoutWidget.slideAllTheWayObservable.onNext(Unit)
            }
            val cancelFun = fun() {
                show(detailPresenter)
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
        presenter.hotelCheckoutViewModel.checkoutParams.subscribe {
            checkoutDialog.show()
        }
        presenter.hotelCheckoutWidget.slideAllTheWayObservable.subscribe {
            checkoutDialog.hide()
        }

        presenter.hotelCheckoutWidget.createTripViewmodel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        presenter.hotelCheckoutWidget.createTripViewmodel.errorObservable.delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }
        presenter.hotelCheckoutWidget.createTripViewmodel.noResponseObservable.subscribe {
            val retryFun = fun() {
                presenter.hotelCheckoutWidget.doCreateTrip()
            }
            val cancelFun = fun() {
                show(detailPresenter)
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }

        presenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(presenter.hotelCheckoutWidget.createTripResponseListener)
        presenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(endlessObserver { createTripResponse ->
            checkoutDialog.dismiss()
            show(presenter, Presenter.FLAG_CLEAR_BACKSTACK)
            presenter.show(presenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_BACKSTACK)
        })
        presenter.hotelCheckoutWidget.setSearchParams(hotelSearchParams)
        presenter
    }

    val confirmationPresenter: HotelConfirmationPresenter by bindView(R.id.hotel_confirmation_presenter)
    val reviewsView: HotelReviewsView by bindView(R.id.hotel_reviews_presenter)
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    val ANIMATION_DURATION = 400
    val geoCodeSearchModel = GeocodeSearchModel(context)
    private val checkoutDialog = ProgressDialog(context)
    var viewModel: HotelPresenterViewModel by Delegates.notNull()
    private val DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW = 100L
    val isUserBucketedSearchScreenTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest)

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)

        hotelDetailViewModel = HotelDetailViewModel(context, hotelServices, endlessObserver<HotelOffersResponse.HotelRoomResponse> {
            checkoutPresenter.hotelCheckoutWidget.couponCardView.viewmodel.hasDiscountObservable.onNext(false)
            checkoutPresenter.hotelCheckoutWidget.setSearchParams(hotelSearchParams)
            checkoutPresenter.showCheckout(it)
            show(checkoutPresenter)
        })

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
        checkoutDialog.setCancelable(false)
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
                if(isUserBucketedSearchScreenTest) {
                    addDefaultTransition(defaultSearchV2Transition)
                    show(searchPresenterV2)
                } else {
                    addDefaultTransition(defaultSearchTransition)
                    show(searchPresenter)
                }
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

        // TODO Have to set create one vm based on test
        searchPresenter.searchViewModel = HotelSearchViewModel(context)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(searchObserver)
        searchPresenterV2.searchViewModel = HotelSearchViewModel(context)


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

        errorPresenter.hotelDetailViewModel = hotelDetailViewModel
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

        geoCodeSearchModel.errorObservable.subscribe(errorPresenter.viewmodel.apiErrorObserver)
        geoCodeSearchModel.errorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }

        loadingOverlay.setBackground(R.color.hotels_primary_color)
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(HotelSearchPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
        }
    }

    private val defaultSearchV2Transition = object : Presenter.DefaultTransition(HotelSearchPresenterV2::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenterV2.visibility = View.VISIBLE
        }
    }

    private val defaultDetailsTransition = object : Presenter.DefaultTransition(HotelDetailPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            loadingOverlay.visibility = View.GONE
            detailPresenter.visibility = View.VISIBLE
            if (forward) {
                detailPresenter.hotelDetailView.refresh()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
                backStack.push(searchPresenter)
            }
        }
    }

    private val defaultResultsTransition = object : Presenter.DefaultTransition(HotelResultsPresenter::class.java.name) {

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
            searchPresenter.visibility = View.VISIBLE
            resultsPresenter.visibility = View.VISIBLE
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
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            searchPresenter.animationFinalize(forward)
            resultsPresenter.animationFinalize(forward)
            if (!forward) HotelV2Tracking().trackHotelV2SearchBox()
        }
    }

    private val resultsToDetail = object : Presenter.Transition(HotelResultsPresenter::class.java.name, HotelDetailPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {
        private var detailsHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
            }
            else {
                detailPresenter.hotelDetailView.refresh()
            }
            val parentHeight = height
            detailsHeight = parentHeight - Ui.getStatusBarHeight(getContext())
            val pos = (if (forward) detailsHeight else 0).toFloat()
            detailPresenter.translationY = pos
            detailPresenter.visibility = View.VISIBLE
            detailPresenter.animationStart()
            resultsPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) (detailsHeight - (f * detailsHeight)) else (f * detailsHeight)
            detailPresenter.translationY = pos
            detailPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            detailPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            detailPresenter.translationY = 0f
            resultsPresenter.animationFinalize(!forward)
            detailPresenter.animationFinalize()
            loadingOverlay.visibility = View.GONE
            if (forward) {
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            } else {
                resultsPresenter.recyclerView.adapter.notifyDataSetChanged()
                HotelV2Tracking().trackHotelsV2Search(hotelSearchParams, resultsPresenter.viewmodel.hotelResultsObservable.value)
            }
        }
    }

    private val checkoutToError = object : Presenter.Transition(HotelCheckoutPresenter::class.java.name, HotelErrorPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            checkoutPresenter.visibility = View.VISIBLE
            errorPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            checkoutPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
        }
    }


    private val searchToError = object : Presenter.Transition(HotelSearchPresenter::class.java.name, HotelErrorPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
            searchPresenter.visibility = View.VISIBLE
            errorPresenter.visibility = View.VISIBLE
            searchPresenter.animationStart(!forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            searchPresenter.animationFinalize(forward)
            errorPresenter.animationFinalize()
            if (!forward) HotelV2Tracking().trackHotelV2SearchBox()
        }
    }

    private val searchToDetails = object : ScaleTransition(this, HotelSearchPresenter::class.java, HotelDetailPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
            searchPresenter.animationStart(!forward)
            searchPresenter.animationFinalize(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (forward) {
                detailPresenter.hotelDetailView.refresh()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            }
            else {
                HotelV2Tracking().trackHotelV2SearchBox()
            }
        }
    }
    private val resultsToError = ScaleTransition(this, HotelResultsPresenter::class.java, HotelErrorPresenter::class.java)

    private val detailsToCheckout = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelCheckoutPresenter::class.java) {
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

    private val detailsToError = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelErrorPresenter::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                trackHotelDetail()
            }
        }
    }

    private val checkoutToConfirmation = ScaleTransition(this, HotelCheckoutPresenter::class.java, HotelConfirmationPresenter::class.java)
    private val detailsToReview = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelReviewsView::class.java) {
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
        if (params.suggestion.hotelId != null) {
            // Hotel name search - go straight to details
            showDetails(params.suggestion.hotelId, true)
        } else if (params.suggestion.type.equals("RAW_TEXT_SEARCH")) {
            // fire off geo search to resolve raw text into lat/long
            geoCodeSearchModel.searchObserver.onNext(params)
        } else {
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

    private val hotelDetailsListener: Observer<HotelDetailsRequestMetadata> = endlessObserver {
        if (it.isOffersRequest) {
            loadingOverlay.animate(false)
        }

        if (it.hotelOffersResponse.hasErrors() && it.hotelOffersResponse.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE && it.isOffersRequest) {
            //Just show the Hotel Details Screen in "Sold Out" state, fields being fetched from "/info/" API
            showDetails(it.hotelId, false)
        } else if (!it.hotelOffersResponse.hasErrors()) {
            //correct hotel name if it is a deeplink HOTEL search
            val intent = (context as Activity).intent
            if (intent.getBooleanExtra(Codes.FROM_DEEPLINK, false)) {
                intent.putExtra(Codes.FROM_DEEPLINK, false)
                if (hotelSearchParams.suggestion.type == "HOTEL") {
                    searchPresenter.searchViewModel.locationTextObservable.onNext(it.hotelOffersResponse.hotelName)
                }
            }
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

    data class HotelDetailsRequestMetadata(val hotelId: String, val hotelOffersResponse: HotelOffersResponse, val isOffersRequest: Boolean)

    private fun showDetails(hotelId: String, fetchOffers: Boolean) {
        if (fetchOffers) {
            loadingOverlay.visibility = View.VISIBLE
            loadingOverlay.animate(true)
        }

        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(hotelSearchParams)
        val subject = PublishSubject.create<HotelOffersResponse>()
        subject.subscribe(object : Observer<HotelOffersResponse> {
            override fun onNext(t: HotelOffersResponse?) {
                hotelDetailsListener.onNext(HotelDetailsRequestMetadata(hotelId, t!!, fetchOffers))
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        showDetails(hotelId, fetchOffers)
                    }
                    val cancelFun = fun() {
                        show(searchPresenter)
                    }
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
