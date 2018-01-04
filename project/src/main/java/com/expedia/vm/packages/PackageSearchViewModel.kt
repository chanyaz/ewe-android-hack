package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.PackageCalendarDirections
import com.expedia.util.PackageCalendarRules
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import org.joda.time.LocalDate
import rx.subjects.PublishSubject
import javax.inject.Inject

class PackageSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    private val rules = PackageCalendarRules(context)
    private val calendarInstructions = PackageCalendarDirections(context)

    // Inputs
    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }
    // Outputs
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()

    val packageParamsBuilder = PackageSearchParams.Builder(rules.getMaxSearchDurationDays(), rules.getMaxDateRange())
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
        return calendarInstructions.getDateInstructionText(start, end)
    }

    override fun getEmptyDateText(forContentDescription: Boolean): String {
        val selectDatesText = context.getString(R.string.select_dates)
        if (forContentDescription) {
            return getDateAccessibilityText(selectDatesText, "")
        }
        return selectDatesText
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        return calendarInstructions.getNoEndDateText(start, forContentDescription)
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        return calendarInstructions.getCompleteDateText(start, end, forContentDescription)
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        return calendarInstructions.getToolTipInstructions(end)
    }
}
