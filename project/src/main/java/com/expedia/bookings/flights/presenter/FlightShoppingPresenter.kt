package com.expedia.bookings.flights.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.flights.activity.FlightResultsActivity
import com.expedia.bookings.flights.activity.FlightShoppingControllerActivity
import com.expedia.bookings.presenter.IntentPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.flight.FlightSearchAirportDropdownPresenter
import com.expedia.bookings.presenter.flight.FlightSearchPresenter
import com.expedia.bookings.services.DateTimeTypeAdapter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.*
import com.expedia.ui.FlightActivity
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.FlightWebCheckoutViewViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.expedia.vm.flights.TripType
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import javax.inject.Inject


class FlightShoppingPresenter(context: Context, attrs: AttributeSet) : IntentPresenter(context, attrs) {

    lateinit var flightServices: FlightServices
        @Inject set

    lateinit var flightCreateTripViewModel: FlightCreateTripViewModel
        @Inject set

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set
    lateinit var webCheckoutViewModel: FlightWebCheckoutViewViewModel
        @Inject set

    val itinTripServices: ItinTripServices by lazy {
        Ui.getApplication(context).flightComponent().itinTripService()
    }

    lateinit var travelerManager: TravelerManager
    lateinit var createTripBuilder: FlightCreateTripParams.Builder

    val isByotEnabled = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightByotSearch)
    val pageUsableData = PageUsableData()
    val EBAndroidAppFlightSubpubChange = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSubpubChange)
    val isUserEvolableBucketed = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsEvolable)
    val isNativeRateDetailsWebviewCheckoutEnabled = isShowFlightsNativeRateDetailsWebviewCheckoutEnabled(context)

    val searchPresenter: FlightSearchPresenter by lazy {
        if (displayFlightDropDownRoutes()) {
            val viewStub = findViewById<ViewStub>(R.id.search_restricted_airport_dropdown_presenter)
            viewStub.inflate() as FlightSearchAirportDropdownPresenter
        } else {
            val viewStub = findViewById<ViewStub>(R.id.search_presenter)
            viewStub.inflate() as FlightSearchPresenter
        }
    }

    private fun displayFlightDropDownRoutes(): Boolean {
        return PointOfSale.getPointOfSale().displayFlightDropDownRoutes()
    }

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        searchPresenter.searchViewModel = vm
        vm.searchParamsObservable.subscribe { params ->
            announceForAccessibility(context.getString(R.string.accessibility_announcement_searching_flights))
            //flightOfferViewModel.searchParamsObservable.onNext(params)
//            flightOfferViewModel.isOutboundSearch = true
//            errorPresenter.getViewModel().paramsSubject.onNext(params)
            travelerManager.updateDbTravelers(params)
            // Starting a new search clear previous selection
            Db.sharedInstance.clearPackageFlightSelection()
            openFlightsResults(null)

//            outBoundPresenter.clearBackStack()
//            outBoundPresenter.showResults()
//            show(outBoundPresenter, Presenter.FLAG_CLEAR_TOP)
        }
//        if (isFlightGreedySearchEnabled(context)) {
//            vm.greedySearchParamsObservable.subscribe(flightOfferViewModel.greedyFlightSearchObservable)
//        }
    }

    fun openFlightsResults(results: TripType?) {
        val intent = Intent(context, FlightResultsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (!(results == null)) {
            val gson = GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                    .create()
            intent.putExtra("results", gson.toJson(results))
        }
        val activity = context as Activity
        activity.startActivityForResult(intent, Constants.FLIGHT_RESULTS_REQUEST_CODE, null)
        activity.overridePendingTransition(0, 0)
    }

    init {
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        Ui.getApplication(getContext()).flightComponent().inject(this)
        View.inflate(context, R.layout.flight_presenter, this)
        searchViewModel = FlightSearchViewModel(context)
//        searchViewModel.deeplinkDefaultTransitionObservable.subscribe { screen ->
//            setDefaultTransition(screen)
//        }

        searchViewModel.searchTravelerParamsObservable.subscribe { searchParams ->
            searchPresenter.travelerWidgetV2.traveler.getViewModel().travelerParamsObservable
                    .onNext(TravelerParams(searchParams.numAdults, emptyList(), emptyList(), emptyList()))
        }
//        searchViewModel.trackSearchClicked.subscribe {
//            FlightsV2Tracking.trackSearchClick(Db.getFlightSearchParams(), true, flightOfferViewModel.isGreedyCallAborted)
//        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun setDefaultTransition(screen: FlightShoppingControllerActivity.Screen) {
        val defaultTransition = defaultSearchTransition
//                when (screen) {
//            FlightActivity.Screen.RESULTS -> defaultOutboundTransition
//            else -> defaultSearchTransition
//        }
        if (!hasDefaultTransition()) {
            addDefaultTransition(defaultTransition)
        }
        if (screen == FlightShoppingControllerActivity.Screen.SEARCH) {
            show(searchPresenter)
        }
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(getDefaultSearchPresenterClassName()) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            FlightsV2Tracking.trackSearchPageLoad()
        }
    }

    private fun getDefaultSearchPresenterClassName(): String {
        return if (displayFlightDropDownRoutes()) {
            FlightSearchAirportDropdownPresenter::class.java.name
        } else {
            FlightSearchPresenter::class.java.name
        }
    }
}
