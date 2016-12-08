package com.expedia.vm.cars

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.bookings.widget.TimeSlider
import com.expedia.util.endlessObserver
import com.expedia.vm.SearchViewModelWithTimeSliderCalendar
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class CarSearchViewModel(context: Context) : SearchViewModelWithTimeSliderCalendar(context) {

    val carParamsBuilder = CarSearchParam.Builder()
    val searchParamsObservable = PublishSubject.create<CarSearchParam>()
    val defaultTimeTooltipColor = ContextCompat.getColor(context, R.color.cars_primary_color)
    val errorTimeTooltipColor = ContextCompat.getColor(context, R.color.cars_tooltip_disabled_color)

    // Inputs
    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
        originValidObservable.onNext(getParamsBuilder().hasOriginLocation())
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

    fun computeCalendarCardViewText(startMillis: Int, endMillis: Int, isContentDescription: Boolean): CharSequence {
        val dateTimeRangeText = computeDateTimeRangeText(startMillis, endMillis, isContentDescription)
        val sb = SpannableBuilder()

        if (startDate() != null && isContentDescription) {
            sb.append(Phrase.from(context, R.string.car_search_date_range_cont_desc_TEMPLATE)
                    .put("date_time_range", dateTimeRangeText)
                    .format().toString())
        } else {
            sb.append(dateTimeRangeText)
        }
        return sb.build()
    }

    fun computeDateTimeRangeText(startMillis: Int, endMillis: Int, isContentDescription: Boolean): String? {
        if (startDate() == null) {
            var stringID = if (isContentDescription) R.string.packages_search_dates_cont_desc else R.string.select_pickup_and_dropoff_dates
            return context.resources.getString(stringID)
        } else {
            return DateFormatUtils.formatStartEndDateTimeRange(context, DateUtils.localDateAndMillisToDateTime(startDate(), startMillis),
                    DateUtils.localDateAndMillisToDateTime(endDate(), endMillis), isContentDescription);
        }
    }

    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_car_search);
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
        dateInstructionObservable.onNext(computeDateInstructionText(dates.first, dates.second))
        calendarTooltipTextObservable.onNext(computeTooltipText(dates.first, dates.second))

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

    override fun computeDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_pickup_date);
        }

        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)
        return sb.build()
    }

    override fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.resources.getString(R.string.select_pickup_and_dropoff_dates)
        } else if (end == null) {
            return context.resources.getString(R.string.select_drop_off_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(start)).put("enddate", DateUtils.localDateToMMMd(end)).format().toString()
        }
    }

    override fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.cars_calendar_start_date_label
                else R.string.calendar_drag_to_modify
        val instructions = context.resources.getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
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
}
