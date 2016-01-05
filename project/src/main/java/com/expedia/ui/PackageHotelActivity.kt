package com.expedia.ui

import android.os.Bundle
import android.os.PersistableBundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.hotel.BaseResultsPresenter
import com.expedia.bookings.presenter.packages.PackageHotelResultsPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Ui
import com.expedia.vm.PackageHotelResultsViewModel
import com.google.android.gms.maps.MapView
import javax.inject.Inject

public class PackageHotelActivity : AbstractAppCompatActivity() {
    val resultsPresenter: PackageHotelResultsPresenter by lazy {
        findViewById(R.id.hotel_results_presenter) as PackageHotelResultsPresenter
    }

    val resultsMapView: MapView by lazy {
        resultsPresenter.findViewById(R.id.map_view) as MapView
    }

    lateinit var packageServices: PackageServices
        @Inject set
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).packageComponent().inject(this)
        setContentView(R.layout.package_hotel_results)
        Ui.showTransparentStatusBar(this)
        resultsMapView.onCreate(savedInstanceState)
        resultsPresenter.viewmodel = PackageHotelResultsViewModel(this)
        resultsPresenter.viewmodel.paramsSubject.onNext(Db.getPackageParams())
        resultsPresenter.viewmodel.resultsSubject.onNext(Db.getPackageResponse())
    }

    override fun onPause() {
        resultsMapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        resultsMapView.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        resultsMapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        resultsMapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        resultsMapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onBackPressed() {
        if (!resultsPresenter.back()) {
            super.onBackPressed()
        }
    }

}