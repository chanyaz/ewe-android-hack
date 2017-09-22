package com.expedia.vm.packages

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.subjects.PublishSubject
import javax.inject.Inject

class PackageSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    // Inputs
    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }
    // Outputs
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()

    val packageParamsBuilder = PackageSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())
    val previousSearchParamsObservable = PublishSubject.create<PackageSearchParams>()

    val performSearchObserver = endlessObserver<PackageSearchParams> { params ->
        travelerValidator.updateForNewSearch(params)
        searchParamsObservable.onNext(params)
        SearchParamsHistoryUtil.savePackageParams(context, params)
    }

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        previousSearchParamsObservable.subscribe { params ->
            setupViewModelFromPastSearch(params)
        }
    }

    private fun setupViewModelFromPastSearch(pastSearchParams: PackageSearchParams) {
        val currentDate = LocalDate.now()
        val invalidDates = pastSearchParams.startDate.isBefore(currentDate) || pastSearchParams.endDate?.isBefore(currentDate) ?: false
        if (!invalidDates) {
            datesUpdated(pastSearchParams.startDate, pastSearchParams.endDate)
        }
        originLocationObserver.onNext(pastSearchParams.origin)
        destinationLocationObserver.onNext(pastSearchParams.destination)
    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (getParamsBuilder().isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far, getMaxSearchDurationDays()))
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

    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_package_stay)
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.max_calendar_selectable_date_range)
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        formattedDestinationObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        requiredSearchParamsObserver.onNext(Unit)
    }

    override fun getParamsBuilder(): PackageSearchParams.Builder {
        return packageParamsBuilder
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return false
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
        val selectReturnDate = Phrase.from(context, R.string.select_return_date_TEMPLATE)
                .put("startdate", DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(start))
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

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }
}
