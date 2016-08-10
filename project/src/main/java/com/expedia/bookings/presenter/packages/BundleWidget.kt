package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.packages.InboundFlightWidget
import com.expedia.bookings.widget.packages.OutboundFlightWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.packages.BundleFlightViewModel
import com.expedia.vm.packages.BundleHotelViewModel
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageSearchType
import rx.subjects.BehaviorSubject

class BundleWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val stepOneText: TextView by bindView(R.id.step_one_text)
    val stepTwoText: TextView by bindView(R.id.step_two_text)
    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.package_bundle_hotel_widget)
    val outboundFlightWidget: OutboundFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: InboundFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    val opacity: Float = 0.25f

    val toggleMenuObservable = BehaviorSubject.create<Boolean>()

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe { param ->
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)

            outboundFlightWidget.updateHotelParams(param)
            inboundFlightWidget.updateHotelParams(param)

            if (!param.isChangePackageSearch()) {
                outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                inboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            } else {
                toggleMenuObservable.onNext(false)
            }
            viewModel.resetStepText()
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
                inboundFlightWidget.disable()
                toggleMenuObservable.onNext(false)
            }
            if (param.isOutboundSearch()) {
                outboundFlightWidget.showLoading()
                inboundFlightWidget.toggleFlightWidget(opacity, false)
            } else {
                if (param.isChangePackageSearch()) {
                    outboundFlightWidget.toggleFlightWidget(opacity, false)
                }
                inboundFlightWidget.showLoading()
            }
            viewModel.resetStepText()
        }
        vm.flightResultsObservable.subscribe { searchType ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                outboundFlightWidget.handleResultsLoaded()
            } else {
                inboundFlightWidget.handleResultsLoaded()
            }
        }
        vm.stepOneTextObservable.subscribeText(stepOneText)
        vm.stepTwoTextObservable.subscribeText(stepTwoText)

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
    }

    fun revertBundleViewToSelectOutbound() {
        outboundFlightWidget.enable()
        inboundFlightWidget.disable()
    }

    fun revertBundleViewToSelectInbound() {
        inboundFlightWidget.enable()
    }

    fun revertBundleViewAfterChangedOutbound() {
        revertBundleViewToSelectInbound()
        revertBundleViewToSelectOutbound()
        bundleHotelWidget.toggleHotelWidget(1f, true)
        outboundFlightWidget.toggleFlightWidget(1f, true)
        inboundFlightWidget.toggleFlightWidget(1f, true)
    }

    init {
        View.inflate(context, R.layout.bundle_widget, this)
        orientation = VERTICAL

        bundleHotelWidget.viewModel = BundleHotelViewModel(context)
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.PACKAGES)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.PACKAGES)

        outboundFlightWidget.viewModel.flightsRowExpanded.subscribe {
            inboundFlightWidget.collapseFlightDetails()
            bundleHotelWidget.collapseSelectedHotel()
        }
        inboundFlightWidget.viewModel.flightsRowExpanded.subscribe() {
            outboundFlightWidget.collapseFlightDetails()
            bundleHotelWidget.collapseSelectedHotel()
        }
        bundleHotelWidget.viewModel.hotelRowExpanded.subscribe() {
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