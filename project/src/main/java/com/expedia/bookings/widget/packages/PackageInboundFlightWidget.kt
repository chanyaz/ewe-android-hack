package com.expedia.bookings.widget.packages

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.ui.PackageFlightActivity
import com.squareup.phrase.Phrase

class PackageInboundFlightWidget(context: Context, attrs: AttributeSet?) : PackageBundleFlightWidget(context, attrs) {

    override fun showLoading() {
        viewModel.showLoadingStateObservable.onNext(true)
        viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
        toggleFlightWidget(1f, true)
    }

    override fun handleResultsLoaded() {
        viewModel.showLoadingStateObservable.onNext(false)
        viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
        viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests)).format().toString())
    }

    override fun enable() {
        toggleFlightWidget(1f, true)

        viewModel.flightDetailsIconObservable.onNext(false)
        viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
        viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests))
                .format()
                .toString())
    }

    override fun disable() {
        toggleFlightWidget(opacity, false)

        viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        viewModel.flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
        viewModel.flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        viewModel.flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        viewModel.flightSelectIconObservable.onNext(false)
    }

    override fun rowClicked() {
        openFlightsForArrival()
    }

    fun openFlightsForArrival() {
        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE, null)
    }

    fun updateHotelParams(params: PackageSearchParams) {
        viewModel.suggestion.onNext(params.origin)
        viewModel.date.onNext(params.checkOut)
        viewModel.guests.onNext(params.guests)
        toggleFlightWidget(opacity, false)
    }
}