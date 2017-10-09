package com.expedia.bookings.hotel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.presenter.hotel.HotelSearchPresenter
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.Ui
import com.expedia.vm.HotelSearchViewModel
import javax.inject.Inject

class HotelSearchActivity : AppCompatActivity() {
    val presenter: HotelSearchPresenter by lazy {
        findViewById(R.id.widget_hotel_search) as HotelSearchPresenter
    }

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HotelComponentInjector().inject(this)
        Ui.getApplication(this).hotelComponent().inject(this)
        setContentView(R.layout.hotel_search_presenter_stub)
        presenter.visibility = View.VISIBLE
        initializeSearchPresenter()
    }

    private fun initializeSearchPresenter() {
        val searchViewModel = HotelSearchViewModel(this, hotelSearchManager)
        presenter.searchViewModel = searchViewModel

        searchViewModel.genericSearchSubject.subscribe { params -> handleGenericSearch(params) }
        searchViewModel.hotelIdSearchSubject.subscribe { params ->
            HotelTracking.trackPinnedSearch()
            handleHotelIdSearch(params, goToResults = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPinnedSearch))
        }
        searchViewModel.rawTextSearchSubject.subscribe { params -> handleGeoSearch(params) }
    }

    override fun onDestroy() {
        presenter.shopWithPointsWidget.subscription.unsubscribe()
        presenter.shopWithPointsWidget.shopWithPointsViewModel.subscription.unsubscribe()

        super.onDestroy()
    }


    fun handleGenericSearch(params: HotelSearchParams) {
        hotelSearchManager.searchParams = params
        val intent = Intent(this, HotelResultsActivity::class.java)
        startActivity(intent)

        //todo launch results
//        updateSearchParams(params)
//
//        resultsPresenter.resetListOffset()
//        show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
//        resultsPresenter.viewModel.paramsSubject.onNext(params)
    }

    private fun handleHotelIdSearch(params: HotelSearchParams, goToResults: Boolean = false) {
        //todo out of scope
//        updateSearchParams(params)
//
//        if (goToResults) {
//            setDefaultTransition(HotelActivity.Screen.RESULTS)
//            resultsPresenter.resetListOffset()
//            show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
//            resultsPresenter.viewModel.paramsSubject.onNext(params)
//        } else {
//            setDefaultTransition(HotelActivity.Screen.DETAILS)
//            showDetails(params.suggestion.hotelId)
//        }
    }

    private fun handleGeoSearch(params: HotelSearchParams) {
        //todo
    }
}