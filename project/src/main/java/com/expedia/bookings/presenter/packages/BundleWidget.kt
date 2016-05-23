package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageBundleFlightWidget
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.packages.BundleFlightViewModel
import com.expedia.vm.packages.BundleHotelViewModel
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageSearchType
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class BundleWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val stepOneText: TextView by bindView(R.id.step_one_text)
    val stepTwoText: TextView by bindView(R.id.step_two_text)
    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.package_bundle_hotel_widget)
    val outboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    val opacity: Float = 0.25f

    val toggleMenuObservable = BehaviorSubject.create<Boolean>()

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe { param ->
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)

            outboundFlightWidget.viewModel.suggestion.onNext(Db.getPackageParams().destination)
            outboundFlightWidget.viewModel.date.onNext(Db.getPackageParams().checkIn)
            outboundFlightWidget.viewModel.guests.onNext(Db.getPackageParams().guests)

            inboundFlightWidget.viewModel.suggestion.onNext(Db.getPackageParams().origin)
            inboundFlightWidget.viewModel.date.onNext(Db.getPackageParams().checkOut)
            inboundFlightWidget.viewModel.guests.onNext(Db.getPackageParams().guests)

            if (!param.isChangePackageSearch()) {
                outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                inboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            } else {
                toggleMenuObservable.onNext(false)
            }
            outboundFlightWidget.toggleFlightWidget(opacity, false)
            inboundFlightWidget.toggleFlightWidget(opacity, false)
        }
        vm.hotelResultsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
        }
        vm.flightParamsObservable.subscribe { param ->
            if (param.isChangePackageSearch()) {
                bundleHotelWidget.toggleHotelWidget(opacity, false)
                outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight1_icon)
                outboundFlightWidget.flightIcon.setColorFilter(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
                inboundFlightWidget.viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
                inboundFlightWidget.flightDetailsIcon.visibility = View.GONE
                toggleMenuObservable.onNext(false)
            }
            if (param.isOutboundSearch()) {
                outboundFlightWidget.toggleFlightWidget(1f, true)
                inboundFlightWidget.toggleFlightWidget(opacity, false)
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
                outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
                inboundFlightWidget.viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                        .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests))
                        .format()
                        .toString())
            } else {
                if (param.isChangePackageSearch()) {
                    outboundFlightWidget.toggleFlightWidget(opacity, false)
                }
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
                inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
                inboundFlightWidget.toggleFlightWidget(1f, true)
            }
        }
        vm.flightResultsObservable.subscribe { searchType ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
                outboundFlightWidget.viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkIn))
                        .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests)).format().toString())
            } else {
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
                inboundFlightWidget.viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                        .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests)).format().toString())
            }
        }
        vm.stepOneTextObservable.subscribeText(stepOneText)
        vm.stepTwoTextObservable.subscribeText(stepTwoText)

    }

    fun revertBundleViewToSelectHotel() {
        bundleHotelWidget.viewModel.hotelIconImageObservable.onNext(R.drawable.packages_hotel_icon)
        outboundFlightWidget.toggleFlightWidget(opacity, false)
        inboundFlightWidget.toggleFlightWidget(opacity, false)
        outboundFlightWidget.viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
        outboundFlightWidget.viewModel.flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        outboundFlightWidget.viewModel.flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        outboundFlightWidget.viewModel.flightSelectIconObservable.onNext(false)
    }

    fun revertBundleViewToSelectOutbound() {
        outboundFlightWidget.toggleFlightWidget(1f, true)
        inboundFlightWidget.toggleFlightWidget(opacity, false)

        outboundFlightWidget.viewModel.flightDetailsIconObservable.onNext(false)
        outboundFlightWidget.viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
        outboundFlightWidget.viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests))
                .format()
                .toString())
        inboundFlightWidget.viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
        inboundFlightWidget.viewModel.flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        inboundFlightWidget.viewModel.flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        inboundFlightWidget.viewModel.flightSelectIconObservable.onNext(false)
    }

    fun revertBundleViewToSelectInbound() {
        inboundFlightWidget.toggleFlightWidget(1f, true)
        inboundFlightWidget.viewModel.flightDetailsIconObservable.onNext(false)
        inboundFlightWidget.viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
        inboundFlightWidget.viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests))
                .format()
                .toString())
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
        outboundFlightWidget.isOutbound = true
        inboundFlightWidget.isOutbound = false
        outboundFlightWidget.viewModel = BundleFlightViewModel(context)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context)
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