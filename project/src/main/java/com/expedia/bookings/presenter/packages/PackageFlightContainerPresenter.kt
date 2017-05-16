package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.SlidingBundleWidget
import com.expedia.vm.packages.PackageFlightContainerViewModel
import com.expedia.vm.packages.PackageSearchType
import javax.inject.Inject

class PackageFlightContainerPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    lateinit var packageServices: PackageServices
        @Inject set

    var flightsPresenter: PackageFlightPresenter? = null

    val viewModel: PackageFlightContainerViewModel by lazy {
        PackageFlightContainerViewModel(context, packageServices)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        Ui.getApplication(context).packageComponent().inject(this)
        val intent = (context as AppCompatActivity).intent

        if (!intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT) &&
                !intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT) &&
                isRemoveBundleOverviewFeatureEnabled()) {
            initFlightLoadingPresenter()
        } else {
            initFlightResultsPresenter()
        }
    }

    private fun isRemoveBundleOverviewFeatureEnabled(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppPackagesRemoveBundleOverview)
    }

    private fun initFlightLoadingPresenter() {
        val viewStub = findViewById(R.id.package_flight_loading_presenter_stub) as ViewStub
        viewStub.inflate()

        viewModel.flightSearchResponseObservable.subscribe {
            initFlightResultsPresenter()
        }

        val type = if (Db.getPackageParams().isOutboundSearch()) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT
        viewModel.performFlightSearch.onNext(type)
    }

    private fun initFlightResultsPresenter() {
        val viewStub = findViewById(R.id.package_flight_presenter_stub) as ViewStub
        flightsPresenter = viewStub.inflate() as PackageFlightPresenter

        val flightLoadingPresenter = findViewById(R.id.package_flight_loading_presenter)
        removeView(flightLoadingPresenter)
    }

    override fun back(): Boolean {
        viewModel.back()
        return flightsPresenter?.back() ?: false
    }
}