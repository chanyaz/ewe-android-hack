package com.expedia.bookings.widget.packages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.CrashlyticsLoggingUtil.logWhenNotAutomation
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.packages.activity.PackageFlightActivity
import com.squareup.phrase.Phrase

class OutboundFlightWidget(context: Context, attrs: AttributeSet?) : BaseBundleFlightWidget(context, attrs) {
    override fun isInboundFlight(): Boolean {
        return false
    }

    override fun showLoading() {
        toggleFlightWidget(1f, false)

        viewModel.showLoadingStateObservable.onNext(true)
        viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to))
    }

    override fun handleResultsLoaded() {
        viewModel.showLoadingStateObservable.onNext(false)
        viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatCityName(viewModel.searchParams.value.destination)))
        setTravelerInfoText()
    }

    fun setTravelerInfoText() {
        viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(viewModel.searchParams.value.startDate))
                .put("travelers", StrUtils.formatTravelerString(context, viewModel.searchParams.value.guests)).format().toString())
    }

    override fun enable() {
        toggleFlightWidget(1f, true)
        viewModel.flightDetailsIconObservable.onNext(false)
        viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatCityName(viewModel.searchParams.value.destination)))
        viewModel.travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(viewModel.searchParams.value.startDate))
                .put("travelers", StrUtils.formatTravelerString(context, viewModel.searchParams.value.guests))
                .format()
                .toString())
    }

    override fun disable() {
        toggleFlightWidget(opacity, false)
        viewModel.flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
        viewModel.flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatCityName(viewModel.searchParams.value.destination)))
        viewModel.flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        viewModel.flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        viewModel.flightSelectIconObservable.onNext(false)
    }

    override fun rowClicked() {
        openFlightsForDeparture()
    }

    fun openFlightsForDeparture() {
        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val activity = context as Activity
        activity.startActivityForResult(intent, Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE, null)
        activity.overridePendingTransition(0, 0)
    }

    fun updateHotelParams(params: PackageSearchParams) {
        params.destination?.let {
            viewModel.suggestion.onNext(it)
        }
        viewModel.date.onNext(params.startDate)
        viewModel.guests.onNext(params.guests)
        logWhenNotAutomation("onNext() called on searchParams of BundleFlightViewModel in OutboundFlightWidget.")
        viewModel.searchParams.onNext(params)
        toggleFlightWidget(opacity, false)
    }

    fun disableFlightIcon() {
        flightIcon.setImageResource(R.drawable.packages_flight1_icon)
        flightIcon.setColorFilter(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
    }
}
