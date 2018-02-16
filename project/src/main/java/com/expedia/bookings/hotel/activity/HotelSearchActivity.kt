package com.expedia.bookings.hotel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.presenter.hotel.HotelSearchPresenter
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.HotelSearchViewModel
import javax.inject.Inject

class HotelSearchActivity : AppCompatActivity() {
    val presenter by bindView<HotelSearchPresenter>(R.id.hotel_search_presenter)

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HotelComponentInjector().inject(this)
        Ui.getApplication(this).hotelComponent().inject(this)

        setContentView(R.layout.hotel_search_activity)
        Ui.showTransparentStatusBar(this)

        initializeSearchPresenter()
    }

    override fun onDestroy() {
        presenter.shopWithPointsWidget.subscription.dispose()
        presenter.shopWithPointsWidget.shopWithPointsViewModel.subscription.dispose()
        hotelSearchManager.dispose()

        super.onDestroy()
    }

    private fun initializeSearchPresenter() {
        val searchViewModel = HotelSearchViewModel(this, hotelSearchManager)
        presenter.searchViewModel = searchViewModel

        searchViewModel.genericSearchSubject.subscribe { params -> handleGenericSearch(params) }
        searchViewModel.hotelIdSearchSubject.subscribe { params ->
            HotelTracking.trackPinnedSearch()
            handleHotelIdSearch(params, goToResults = AbacusFeatureConfigManager.isBucketedForTest(this, AbacusUtils.EBAndroidAppHotelPinnedSearch))
        }
        searchViewModel.rawTextSearchSubject.subscribe { params -> handleGeoSearch(params) }
    }

    private fun handleGenericSearch(params: HotelSearchParams) {
        val intent = Intent(this, HotelResultsActivity::class.java)
        startActivity(intent)
    }

    private fun handleHotelIdSearch(params: HotelSearchParams, goToResults: Boolean = false) {
        //todo
    }

    private fun handleGeoSearch(params: HotelSearchParams) {
        //todo
    }
}
