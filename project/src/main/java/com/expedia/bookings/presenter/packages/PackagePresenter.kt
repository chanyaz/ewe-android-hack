package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.PackageCreateTripViewModel
import javax.inject.Inject

public class PackagePresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    val searchPresenter: PackageSearchPresenter by bindView(R.id.widget_package_search_presenter)
    val bundlePresenter: BundleOverviewPresenter by bindView(R.id.widget_bundle_overview)
    val confirmationPresenter: PackageConfirmationPresenter by bindView(R.id.widget_package_confirmation)

    init {
        Ui.getApplication(getContext()).packageComponent().inject(this)
        View.inflate(context, R.layout.package_presenter, this)
        bundlePresenter.viewModel = BundleOverviewViewModel(context, packageServices)
        bundlePresenter.checkoutPresenter.viewModel = BaseCheckoutViewModel(context, packageServices)
        bundlePresenter.checkoutPresenter.viewModel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        bundlePresenter.checkoutPresenter.viewModel.checkoutResponse.subscribe {
            show(confirmationPresenter)
            confirmationPresenter.itinNumber.text = it.newTrip?.itineraryNumber
        }
        bundlePresenter.createTripViewModel = PackageCreateTripViewModel(packageServices)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe {
            show(bundlePresenter)
            bundlePresenter.show(BundleOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        }
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(bundlePresenter.viewModel.hotelParamsObservable)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultSearchTransition)
        addTransition(searchToBundle)
        addTransition(bundleToConfirmation)
        show(searchPresenter)
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(PackageSearchPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
        }
    }

    private val searchToBundle = LeftToRightTransition(this, PackageSearchPresenter::class.java, BundleOverviewPresenter::class.java)

    private val bundleToConfirmation = ScaleTransition(this, BundleOverviewPresenter::class.java, PackageConfirmationPresenter::class.java)
}
