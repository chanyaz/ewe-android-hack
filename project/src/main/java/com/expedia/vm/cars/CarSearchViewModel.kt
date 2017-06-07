package com.expedia.vm.cars

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.bookings.widget.TimeSlider
import com.expedia.util.endlessObserver
import com.expedia.vm.SearchViewModelWithTimeSliderCalendar
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rx.subjects.PublishSubject

class CarSearchViewModel(context: Context) : SearchViewModelWithTimeSliderCalendar(context) {
    val carParamsBuilder = CarSearchParam.Builder()
    val searchParamsObservable = PublishSubject.create<CarSearchParam>()
    val defaultTimeTooltipColor = ContextCompat.getColor(context, R.color.app_primary)
    val errorTimeTooltipColor = ContextCompat.getColor(context, R.color.cars_tooltip_disabled_color)

    // Inputs
    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
    }

    override val originLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().origin(suggestion)
        formattedOriginObservable.onNext(SuggestionStrUtils.formatCityName(suggestion.regionNames.fullName))
        requiredSearchParamsObserver.onNext(Unit)
    }

    init{
        isRoundTripSearchObservable.onNext(true)

        departTimeSubject.subscribe {
            val isValid = !isStartTimeBeforeNow()
            departTimeSliderTooltipColor.onNext(if (isValid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }

        returnTimeSubject.subscribe {
            val isValid = !isEndTimeBeforeStartTime()
            returnTimeSliderTooltipColor.onNext(if (isValid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }

        setUpTimeSliderSubject.onNext(Pair(null,null))

    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.error_date_too_far))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far))
            } else {
                val carSearchParams = getParamsBuilder().build()
                searchParamsObservable.onNext(carSearchParams)
            }
        } else {
            if (!getParamsBuilder().hasOriginLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_car_search)
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.max_calendar_selectable_date_range)
    }

    override fun getParamsBuilder(): CarSearchParam.Builder {
        return carParamsBuilder
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return false
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        super.onDatesChanged(dates)
        val (startDate, endDate) = dates
        dateInstructionObservable.onNext(getDateInstructionText(startDate, endDate))
        calendarTooltipTextObservable.onNext(getToolTipText(startDate, endDate))
        calendarTooltipContDescObservable.onNext(getToolTipContentDescription(startDate, endDate))

        setUpTimeSliderSubject.onNext(dates)
    }

    // Reset times if the start is equal to today and the selected time is before the current time
    // or if the end time is earlier or equal to the start time and its the same day.
    override fun onTimesChanged(times: Pair<Int, Int>){
        val (startMillis, endMillis) = times

        getParamsBuilder().startDateTimeAsMillis(startMillis)
        getParamsBuilder().endDateTimeAsMillis(endMillis)

        dateTextObservable.onNext(computeCalendarCardViewText(startMillis, endMillis, false))
        dateAccessibilityObservable.onNext(computeCalendarCardViewText(startMillis, endMillis, true))
    }

    override fun validateTimes() {
        val now = DateTime.now();

        if (isStartTimeBeforeNow()) {
            departTimeSubject.onNext(now.plusHours(1).millisOfDay);
        }
        if (isEndTimeBeforeStartTime()) {
            returnTimeSubject.onNext(getStartDateTimeAsMillis() + DateTime().withHourOfDay(2).withMinuteOfHour(0).millisOfDay);
        }
    }

    // Helpers
    override fun getAllowedMinProgress(now: DateTime): Int {
        return TimeSlider.convertMillisToProgress(now.millisOfDay) + R.integer.calendar_min_search_time_car
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_pickup_date);
        } else if (end == null) {
            return context.resources.getString(R.string.select_drop_off_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        }
        return getStartDashEndDateString(start!!, end)
    }

    override fun getStartTimeContDesc(time: String): String {
        return Phrase.from(context, R.string.pick_up_slider_cont_desc_TEMPLATE).put("time", time).format().toString()
    }

    override fun getEndTimeContDesc(time: String): String {
        return Phrase.from(context, R.string.drop_off_slider_cont_desc_TEMPLATE).put("time", time).format().toString()
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        if (end == null) {
            return context.getString(R.string.cars_calendar_start_date_label)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return true
    }

    override fun getCalendarSliderTooltipStartTimeLabel(): String{
        return context.resources.getString(R.string.pick_up_time_label)
    }

    override fun getCalendarSliderTooltipEndTimeLabel(): String{
        return context.resources.getString(R.string.drop_off_time_label)
    }

    override fun getEmptyDateText(forContentDescription: Boolean): String {
        if (forContentDescription) {
            return context.getString(R.string.select_travel_dates_cont_desc)
        }
        return context.getString(R.string.select_pickup_and_dropoff_dates)
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        return "" //no op, car doesn't update until time is selected.
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        return "" //no op, car doesn't update until time is selected.
    }

    private fun computeCalendarCardViewText(startMillis: Int, endMillis: Int, isContentDescription: Boolean): CharSequence {
        if (startDate() == null) {
            return getEmptyDateText(isContentDescription)
        }

        val startDateTimeString = DateUtils.localDateAndMillisToDateTime(startDate(), startMillis)
        val endDateTimeString = DateUtils.localDateAndMillisToDateTime(endDate(), endMillis)
        val dateTimeRangeString = DateFormatUtils.formatStartEndDateTimeRange(context, startDateTimeString,
                endDateTimeString, isContentDescription)
        if (isContentDescription) {
            return getDateAccessibilityText(context.getString(R.string.select_pickup_and_dropoff_dates), dateTimeRangeString)
        }
        return dateTimeRangeString
    }
}
