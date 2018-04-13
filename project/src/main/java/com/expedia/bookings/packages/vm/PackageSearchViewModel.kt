package com.expedia.bookings.packages.vm

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.utils.WebViewIntentBuilderUtil
import com.expedia.ui.LOBWebViewActivity
import com.expedia.util.PackageCalendarRules
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import okhttp3.HttpUrl
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class PackageSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    private val rules = PackageCalendarRules(context)

    // Inputs
    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }
    // Outputs
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()
    var isFHPackageSearch = true

    val packageParamsBuilder = PackageSearchParams.Builder(rules.getMaxSearchDurationDays(), rules.getMaxDateRange())
    val previousSearchParamsObservable = PublishSubject.create<PackageSearchParams>()
    val flightCabinClassObserver = endlessObserver<FlightServiceClassType.CabinCode> { cabinCode ->
        getParamsBuilder().flightCabinClass(FlightServiceClassType.getMIDCabinClassRequestName(cabinCode))
    }

    val performSearchObserver = endlessObserver<PackageSearchParams> { params ->
        travelerValidator.updateForNewSearch(params)
        if (!isFHPackageSearch) {
            launchFHCPackageWebView(params)
        } else {
            searchParamsObservable.onNext(params)
        }
        SearchParamsHistoryUtil.savePackageParams(context, params)
    }

    private fun launchFHCPackageWebView(params: PackageSearchParams) {
        var builder = LOBWebViewActivity.IntentBuilder(context)
        builder = WebViewIntentBuilderUtil.setDefaultWebViewIntentProperties(builder)
        builder.setUrl(getFHCPackageWebViewUrlByParams(params))
        builder.setTitle(context.getString(R.string.nav_packages))
        builder.setTrackingName("PackageWebView")
        NavUtils.startActivity(context, builder.intent, null)
        NavUtils.finishIfFlagged(context, 0)
    }

    private fun getFHCPackageWebViewUrlByParams(searchParams: PackageSearchParams): String {
        // Date format is applicable to US only
        val format = DateTimeFormat.forPattern("MM/dd/yyyy")
        val originId = searchParams.originId
        val ftla = searchParams.origin?.hierarchyInfo?.airport?.airportCode
        val destinationId = searchParams.destinationId
        val ttla = searchParams.destination?.hierarchyInfo?.airport?.airportCode
        val fromDate = searchParams.startDate.toString(format)
        val toDate = searchParams.endDate?.toString(format)
        val numberOfRooms = searchParams.numberOfRooms
        val adultsPerRoom = searchParams.adults
        val childrenPerRoom = searchParams.children.size
        val urlBuilder = HttpUrl.Builder()
                .scheme("https")
                .host("www." + PointOfSale.getPointOfSale().url)
                .addPathSegments("flexibleshopping")
                .addQueryParameter("packageType", "fhc")
                .addQueryParameter("originId", originId)
                .addQueryParameter("ftla", ftla)
                .addQueryParameter("destinationId", destinationId)
                .addQueryParameter("ttla", ttla)
                .addQueryParameter("fromDate", fromDate)
                .addQueryParameter("toDate", toDate)
                .addQueryParameter("numberOfRooms", numberOfRooms)
                .addQueryParameter("adultsPerRoom[1]", adultsPerRoom.toString())
                .addQueryParameter("childrenPerRoom[1]", childrenPerRoom.toString())
        for (index in searchParams.children.indices) {
            urlBuilder.addQueryParameter("childAges[1][$index]", searchParams.children[index].toString())
        }
        if (searchParams.infantsInSeats != null) {
            val infantsInSeats = if (searchParams.infantsInSeats!!) "1" else "0"
            urlBuilder.addQueryParameter("infantsInSeats", infantsInSeats)
        }
        urlBuilder.addQueryParameter("mcicid", "App.Package.WebView")
        return urlBuilder.build().url().toString()
    }

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        previousSearchParamsObservable.subscribe { params ->
            setupViewModelFromPastSearch(params)
            travelerValidator.updateForNewSearch(params)
        }
    }

    private fun setupViewModelFromPastSearch(pastSearchParams: PackageSearchParams) {
        val currentDate = LocalDate.now()
        val invalidDates = pastSearchParams.startDate.isBefore(currentDate) || pastSearchParams.endDate?.isBefore(currentDate) ?: false
        if (!invalidDates) {
            datesUpdated(pastSearchParams.startDate, pastSearchParams.endDate)
        }
        pastSearchParams.origin?.let {
            originLocationObserver.onNext(it)
        }
        pastSearchParams.destination?.let {
            destinationLocationObserver.onNext(it)
        }
    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (getParamsBuilder().isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, rules.getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far, rules.getMaxSearchDurationDays()))
            } else {
                performSearchObserver.onNext(getParamsBuilder().build())
            }
        } else {
            if (!getParamsBuilder().hasOriginAndDestination()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        formattedDestinationObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        requiredSearchParamsObserver.onNext(Unit)
    }

    override fun getParamsBuilder(): PackageSearchParams.Builder {
        return packageParamsBuilder
    }

    override fun getCalendarRules(): CalendarRules {
        return rules
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        var (start, end) = dates
        dateTextObservable.onNext(getCalendarCardDateText(start, end, false))
        dateAccessibilityObservable.onNext(getCalendarCardDateText(start, end, true))
        dateInstructionObservable.onNext(getDateInstructionText(start, end))
        calendarTooltipTextObservable.onNext(getToolTipText(start, end))
        calendarTooltipContDescObservable.onNext(getToolTipContentDescription(start, end))
        //dates cant be the same, shift end by 1 day
        if (start != null && (end == null || end.isEqual(start))) {
            end = start.plusDays(1)
        }
        super.onDatesChanged(Pair(start, end))
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_departure_date)
        } else if (end == null) {
            return getNoEndDateText(start, false)
        }
        return getCompleteDateText(start!!, end, false)
    }

    override fun getEmptyDateText(forContentDescription: Boolean): String {
        val selectDatesText = context.getString(R.string.select_dates)
        if (forContentDescription) {
            return getDateAccessibilityText(selectDatesText, "")
        }
        return selectDatesText
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        if (start == null) {
            return ""
        }
        val selectReturnDate = Phrase.from(context, R.string.select_return_date_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToEEEMMMd(start))
                .format().toString()
        if (forContentDescription) {
            return getDateAccessibilityText(selectReturnDate, "")
        }
        return selectReturnDate
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        val dateNightBuilder = SpannableBuilder()
        val nightCount = JodaUtils.daysBetween(start, end)

        val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
        val dateRangeText = if (forContentDescription) {
            getStartToEndDateWithDayString(start, end)
        } else {
            getStartDashEndDateWithDayString(start, end)
        }

        dateNightBuilder.append(dateRangeText)
        dateNightBuilder.append(" ")
        dateNightBuilder.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))

        if (forContentDescription) {
            return getDateAccessibilityText(context.getString(R.string.trip_dates_cont_desc), dateNightBuilder.build().toString())
        }

        return dateNightBuilder.build().toString()
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        if (end == null) {
            return context.getString(R.string.calendar_instructions_date_range_flight_select_return_date)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }
}
