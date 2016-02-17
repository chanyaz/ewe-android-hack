package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.PackageBundleFlightWidget
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.PackageBundlePriceWidget
import com.expedia.bookings.widget.PriceChangeWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BundleFlightViewModel
import com.expedia.vm.BundleHotelViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.PackageSearchType
import com.expedia.vm.PriceChangeViewModel
import com.squareup.phrase.Phrase
import java.math.BigDecimal

class BundleWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val bundleContainer: ScrollView by bindView(R.id.bundle_container)
    val stepOneText: TextView by bindView(R.id.step_one_text)
    val stepTwoText: TextView by bindView(R.id.step_two_text)
    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.package_bundle_hotel_widget)
    val outboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    val priceChangeWidget: PriceChangeWidget by bindView(R.id.price_change)
    val bundleTotalPriceWidget: PackageBundlePriceWidget by bindView(R.id.bundle_total)
    val checkoutButton: Button by bindView(R.id.checkout_button)

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe { param ->
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)
            outboundFlightWidget.viewModel.hotelLoadingStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            inboundFlightWidget.viewModel.hotelLoadingStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
        }
        vm.hotelResultsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
        }
        vm.flightParamsObservable.subscribe { param ->
            if (param.isOutboundSearch()) {
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
                outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
            } else {
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
                inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
            }
        }
        vm.flightResultsObservable.subscribe { searchType ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
                outboundFlightWidget.viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkIn))
                        .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests())).format().toString())
            } else {
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
                inboundFlightWidget.viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                        .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests())).format().toString())
            }
        }
        vm.showBundleTotalObservable.subscribe { visible ->
            var packagePrice = Db.getPackageResponse().packageResult.currentSelectedOffer.price

            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                            packagePrice.tripSavings.currencyCode).formattedMoney)
                    .format().toString()
            bundleTotalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode).formattedMoney, packageSavings))
        }
        vm.stepOneTextObservable.subscribeText(stepOneText)
        vm.stepTwoTextObservable.subscribeText(stepTwoText)
        vm.createTripObservable.subscribe(bundleTotalPriceWidget.viewModel.createTripObservable)
    }

    init {
        View.inflate(context, R.layout.bundle_widget, this)
        bundleHotelWidget.viewModel = BundleHotelViewModel(context)
        priceChangeWidget.viewmodel = PriceChangeViewModel(context)
        bundleTotalPriceWidget.viewModel = BundlePriceViewModel(context)

        outboundFlightWidget.isOutbound = true
        inboundFlightWidget.isOutbound = false
        outboundFlightWidget.viewModel = BundleFlightViewModel(context)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context)
        outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight1_icon)
        inboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight2_icon)

        bundleTotalPriceWidget.visibility = View.VISIBLE
        var countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
        var currencyCode = CurrencyUtils.currencyForLocale(countryCode)
        bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal("0.00"), currencyCode).formattedMoney,
                Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                        .put("savings", Money(BigDecimal("0.00"), currencyCode).formattedMoney)
                        .format().toString()))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }
}