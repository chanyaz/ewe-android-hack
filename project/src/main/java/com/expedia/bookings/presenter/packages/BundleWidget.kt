package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isDisplayFlightSeatingClassForShoppingEnabled
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.packages.InboundFlightWidget
import com.expedia.bookings.widget.packages.OutboundFlightWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.packages.BundleFlightViewModel
import com.expedia.vm.packages.BundleHotelViewModel
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageSearchType
import io.reactivex.subjects.BehaviorSubject

class BundleWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val stepOneText: TextView by bindView(R.id.step_one_text)
    val stepTwoText: TextView by bindView(R.id.step_two_text)
    val stepThreeText: TextView by bindView(R.id.step_three_text)
    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.package_bundle_hotel_widget)
    val outboundFlightWidget: OutboundFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: InboundFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    val packageAirlineFeeWarningTextView: TextView by bindView(R.id.package_airline_fee_warning_text)
    val opacity: Float = 0.25f
    val scrollSpaceView: View by bindView(R.id.scroll_space_bundle)

    val toggleMenuObservable = BehaviorSubject.create<Boolean>()

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe { param ->
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)

            outboundFlightWidget.updateHotelParams(param)
            inboundFlightWidget.updateHotelParams(param)

            if (!param.isChangePackageSearch()) {
                outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                inboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            }

            viewModel.searchParamsChangeObservable.onNext(Unit)
        }
        vm.hotelResultsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
        }

        vm.autoAdvanceObservable.subscribe { searchType ->
            when (searchType) {
                PackageSearchType.HOTEL -> bundleHotelWidget.openHotels()
                PackageSearchType.OUTBOUND_FLIGHT -> outboundFlightWidget.openFlightsForDeparture()
                PackageSearchType.INBOUND_FLIGHT -> inboundFlightWidget.openFlightsForArrival()
            }
        }

        vm.flightParamsObservable.subscribe { param ->
            if (param.isChangePackageSearch()) {
                bundleHotelWidget.toggleHotelWidget(opacity, false)
                outboundFlightWidget.disableFlightIcon()
                inboundFlightWidget.refreshTravelerInfoOnChangeFlights()
                inboundFlightWidget.disable()
            }
            if (param.isOutboundSearch(isMidAPIEnabled(context))) {
                outboundFlightWidget.showLoading()
                inboundFlightWidget.toggleFlightWidget(opacity, false)
            } else {
                if (param.isChangePackageSearch()) {
                    outboundFlightWidget.toggleFlightWidget(opacity, false)
                }
                inboundFlightWidget.showLoading()
            }
            viewModel.searchParamsChangeObservable.onNext(Unit)
        }
        vm.flightResultsObservable.subscribe { searchType ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                outboundFlightWidget.handleResultsLoaded()
                PackagesPageUsableData.FLIGHT_OUTBOUND.pageUsableData.markAllViewsLoaded()
            } else {
                inboundFlightWidget.handleResultsLoaded()
                PackagesPageUsableData.FLIGHT_INBOUND.pageUsableData.markAllViewsLoaded()
            }
        }
        vm.stepOneTextObservable.subscribeText(stepOneText)
        vm.stepOneContentDescriptionObservable.subscribe { stepOneText.contentDescription = it }
        vm.stepTwoTextObservable.subscribeText(stepTwoText)
        vm.stepThreeTextObservale.subscribeTextAndVisibility(stepThreeText)
        vm.airlineFeePackagesWarningTextObservable.subscribeTextAndVisibility(packageAirlineFeeWarningTextView)

        vm.cancelSearchSubject.subscribe {
            bundleHotelWidget.cancel()
            outboundFlightWidget.cancel()
            inboundFlightWidget.cancel()
        }
    }

    fun revertBundleViewToSelectHotel() {
        bundleHotelWidget.viewModel.hotelIconImageObservable.onNext(R.drawable.packages_hotel_icon)
        inboundFlightWidget.toggleFlightWidget(opacity, false)
        outboundFlightWidget.disable()
        outboundFlightWidget.setTravelerInfoText()
        outboundFlightWidget.travelInfoText.visibility = View.VISIBLE
    }

    fun revertBundleViewToSelectOutbound() {
        outboundFlightWidget.enable()
        inboundFlightWidget.disable()
        inboundFlightWidget.setTravelerInfoText()
        inboundFlightWidget.travelInfoText.visibility = View.VISIBLE
    }

    fun revertBundleViewToSelectInbound() {
        inboundFlightWidget.enable()
    }

    init {
        View.inflate(context, R.layout.bundle_widget, this)
        orientation = VERTICAL

        val isDisplayFlightSeatingClassForShoppingEnabled = isDisplayFlightSeatingClassForShoppingEnabled(context)

        outboundFlightWidget.showFlightCabinClass = isDisplayFlightSeatingClassForShoppingEnabled
        inboundFlightWidget.showFlightCabinClass = isDisplayFlightSeatingClassForShoppingEnabled

        bundleHotelWidget.viewModel = BundleHotelViewModel(context)
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.PACKAGES)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.PACKAGES)

        outboundFlightWidget.viewModel.flightsRowExpanded.subscribe {
            inboundFlightWidget.collapseFlightDetails()
            bundleHotelWidget.collapseSelectedHotel()
        }
        inboundFlightWidget.viewModel.flightsRowExpanded.subscribe {
            outboundFlightWidget.collapseFlightDetails()
            bundleHotelWidget.collapseSelectedHotel()
        }
        bundleHotelWidget.viewModel.hotelRowExpanded.subscribe {
            outboundFlightWidget.collapseFlightDetails()
            inboundFlightWidget.collapseFlightDetails()
        }

        outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight1_icon)
        inboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight2_icon)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun collapseBundleWidgets() {
        bundleHotelWidget.backButtonPressed()
        outboundFlightWidget.backButtonPressed()
        inboundFlightWidget.backButtonPressed()
    }
}
