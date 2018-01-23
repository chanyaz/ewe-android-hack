package com.expedia.vm

import android.content.Context
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.bookings.utils.isBreadcrumbsPackagesEnabled
import com.expedia.util.Optional
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.LocalDate

class FlightToolbarViewModel(private val context: Context) {
    //input
    val refreshToolBar = BehaviorSubject.create<Boolean>()
    val isOutboundSearch = BehaviorSubject.create<Boolean>() // TODO - move this into flightSearchViewModel
    val setTitleOnly = BehaviorSubject.create<String>()
    val regionNames = BehaviorSubject.create<Optional<SuggestionV4.RegionNames>>()
    val country = BehaviorSubject.create<Optional<String>>()
    val airport = BehaviorSubject.create<Optional<String>>()
    val travelers = BehaviorSubject.create<Int>()
    val date = BehaviorSubject.create<LocalDate>()
    val lob = BehaviorSubject.create<LineOfBusiness>()

    //output
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<CharSequence>()
    val menuVisibilitySubject = BehaviorSubject.create<Boolean>()

    init {
        setTitleOnly.subscribe { title ->
            titleSubject.onNext(title)
            subtitleSubject.onNext("")
            menuVisibilitySubject.onNext(false)
        }

        ObservableOld.combineLatest(refreshToolBar, isOutboundSearch, regionNames, country, airport, lob, travelers, date, { isResults, isOutboundSearch, regionNamesOptional, country, airportCode, lob, numTravelers, date ->
            regionNamesOptional.value?.let { regionNames ->
                if (lob == LineOfBusiness.FLIGHTS_V2) {
                    titleSubject.onNext(getFlightTitle(isResults, isOutboundSearch, regionNames.displayName))
                } else if (lob == LineOfBusiness.PACKAGES) {
                    airportCode.value?.let {
                        titleSubject.onNext(getPackageTitle(isOutboundSearch, regionNames, country.value, it))
                    }
                }
            }
            subtitleSubject.onNext(getSubtitle(date, numTravelers))
            menuVisibilitySubject.onNext(isResults)
        }).subscribe()
    }

    private fun getFlightTitle(isResults: Boolean, isOutboundSearch: Boolean, displayName: String): String {
        val resultsTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, HtmlCompat.stripHtml(displayName)))
        val overviewTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.flight_to_template, HtmlCompat.stripHtml(displayName)))
        val resultsOutInboundTitle: String = context.resources.getString(R.string.select_return_flight)
        return if (isResults && !isOutboundSearch) resultsOutInboundTitle else if (isResults) resultsTitle else overviewTitle
    }

    private fun getPackageTitle(isOutboundSearch: Boolean, regionNames: SuggestionV4.RegionNames, country: String?, airportCode: String): String {
        var cityName: String = ""
        var stateOrCountry: String = ""

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
        if (shouldShowBreadcrumbsInToolbarTitle()) {
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

    private fun shouldShowBreadcrumbsInToolbarTitle(): Boolean {
        return (isBreadcrumbsPackagesEnabled(context) && !Db.sharedInstance.packageParams.isChangePackageSearch())
    }

    private fun getSubtitle(date: LocalDate, numTravelers: Int): String {
        val travelers = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers)
        val subtitle = Phrase.from(context, R.string.flight_calendar_instructions_date_with_guests_TEMPLATE)
                .put("startdate", DateFormatUtils.formatLocalDateToShortDayAndDate(date))
                .put("guests", travelers)
                .format()
                .toString()
        return subtitle
    }

    private fun getStateOrCountryValue(country: String?, state: String?): String {
        return state ?: country ?: ""
    }

    private fun getCountry(country: String?): String {
        return country ?: ""
    }
}
