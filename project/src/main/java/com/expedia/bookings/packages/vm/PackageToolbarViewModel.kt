package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.vm.BaseToolbarViewModel
import com.squareup.phrase.Phrase

class PackageToolbarViewModel(private val context: Context) : BaseToolbarViewModel(context) {

    init {
        ObservableOld.combineLatest(refreshToolBar, isOutboundSearch, regionNames, country, airport, travelers, date,
                { isResults, isOutboundSearch, regionNamesOptional, countryOptional, airportOptional, numTravelers, date ->
                    regionNamesOptional.value?.let { regionNames ->
                        airportOptional.value?.let { airportCode ->
                            titleSubject.onNext(getPackageTitle(isOutboundSearch, regionNames, countryOptional.value, airportCode))
                        }
                    }
                    subtitleSubject.onNext(getSubtitle(date, numTravelers))
                    menuVisibilitySubject.onNext(isResults)
                }).subscribe()
    }

    private fun getPackageTitle(isOutboundSearch: Boolean, regionNames: SuggestionV4.RegionNames, country: String?, airportCode: String): String {
        var cityName = ""
        var stateOrCountry = ""

        val shortName = StrUtils.formatPackageCityName(regionNames.shortName) ?: ""
        val lastSearchName = StrUtils.formatPackageCityName(regionNames.lastSearchName) ?: ""
        val displayName = StrUtils.formatPackageCityName(HtmlCompat.stripHtml(regionNames.displayName)) ?: ""
        val fullName = StrUtils.formatPackageCityName(regionNames.fullName) ?: ""

        if (Strings.isNotEmpty(shortName)) {
            cityName = shortName
            stateOrCountry = getStateOrCountryValue(country, StrUtils.formatStateName(regionNames.shortName))
        } else if (Strings.isNotEmpty(lastSearchName)) {
            cityName = lastSearchName
            stateOrCountry = getCountry(country)
        } else if (Strings.isNotEmpty(displayName)) {
            cityName = displayName
            stateOrCountry = getCountry(country)
        } else if (Strings.isNotEmpty(fullName)) {
            cityName = fullName
            stateOrCountry = getCountry(country)
        }

        val resultsOutboundTitle: String = getResultsTitle(cityName, stateOrCountry, airportCode, true)
        val resultsInboundTitle: String = getResultsTitle(cityName, stateOrCountry, airportCode, false)

        return if (!isOutboundSearch) resultsInboundTitle else resultsOutboundTitle
    }

    private fun getResultsTitle(cityName: String, stateOrCountry: String, airportCode: String, isOutbound: Boolean): String {
        if (!Db.sharedInstance.packageParams.isChangePackageSearch()) {
            if (isOutbound) {
                return Phrase.from(context, R.string.flight_to_outbound_breadcrumbs_TEMPLATE)
                        .put("destination", cityName)
                        .format()
                        .toString()
            } else {
                return Phrase.from(context, R.string.flight_to_inbound_breadcrumbs_TEMPLATE)
                        .put("origin", cityName)
                        .format()
                        .toString()
            }
        } else {
            if (isOutbound) {
                return Phrase.from(context, R.string.package_flight_outbound_toolbar_title_TEMPLATE)
                        .put("cityname", cityName)
                        .put("stateorcountry", stateOrCountry)
                        .put("airportcode", airportCode)
                        .format()
                        .toString()
            } else {
                return Phrase.from(context, R.string.package_flight_inbound_toolbar_title_TEMPLATE)
                        .put("cityname", cityName)
                        .put("stateorcountry", stateOrCountry)
                        .put("airportcode", airportCode)
                        .format()
                        .toString()
            }
        }
    }

    private fun getStateOrCountryValue(country: String?, state: String?): String {
        return state ?: country ?: ""
    }

    private fun getCountry(country: String?): String {
        return country ?: ""
    }
}
