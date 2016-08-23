package com.expedia.vm

import android.content.Context
import com.expedia.util.endlessObserver
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


abstract class SearchViewModelWithTimeSliderCalendar(context: Context) : BaseSearchViewModel(context) {
    val departTimeSubject = BehaviorSubject.create<Int>(0)
    val returnTimeSubject = BehaviorSubject.create<Int>(0)
    val departTimeSliderTooltipColor = BehaviorSubject.create<Int>()
    val returnTimeSliderTooltipColor = BehaviorSubject.create<Int>()
    val isRoundTripSearchObservable = BehaviorSubject.create<Boolean>(false)

    val setUpTimeSliderSubject = BehaviorSubject.create<Pair<LocalDate?, LocalDate?>>()

    val buildDateTimeObserver = endlessObserver<Pair<Int, Int>> { data ->
        onTimesChanged(data)
    }

    fun getStartDateTimeAsMillis(): Int {
        return departTimeSubject.value
    }

    fun getEndDateTimeAsMillis(): Int{
        return returnTimeSubject.value
    }


    init {
        setUpTimeSliderSubject.subscribe { dates ->
            val (start, end) = dates
            if (start != null) {
                val now = DateTime.now();
                if (start.equals(LocalDate.now()) && now.getHourOfDay() >= 8
                        && getStartDateTimeAsMillis() < now.plusHours(1).millisOfDay) {
                    departTimeSubject.onNext(now.plusHours(1).millisOfDay);
                }
                if (end != null && end.equals(LocalDate.now()) && now.getHourOfDay() >= 16
                        && getEndDateTimeAsMillis() < now.plusHours(3).millisOfDay) {
                    returnTimeSubject.onNext(now.plusHours(3).millisOfDay);
                }
                validateTimes()
            }
            else{
                departTimeSubject.onNext(DateTime().withHourOfDay(9).withMinuteOfHour(0).millisOfDay);
                returnTimeSubject.onNext(DateTime().withHourOfDay(18).withMinuteOfHour(0).millisOfDay);
            }
        }
    }
    fun isStartDateEqualToToday(): Boolean {
        return if (startDate() != null) startDate()!!.isEqual(LocalDate.now()) else false
    }

    fun isStartEqualToEnd(): Boolean {
        return if (startDate() != null && endDate() != null) startDate()!!.isEqual(endDate()) else false
    }

    // Reset times if the start is equal to today and the selected time is before the current time
    // or if the end time is earlier or equal to the start time and its the same day.
    abstract fun getCalendarSliderTooltipStartTimeLabel(): String
    abstract fun getCalendarSliderTooltipEndTimeLabel(): String


    abstract fun onTimesChanged(times: Pair<Int, Int>)
    abstract fun validateTimes()
    abstract fun getAllowedMinProgress(now: DateTime): Int
    abstract fun isStartTimeBeforeAllowedTime(now: DateTime): Boolean
    abstract fun isEndTimeBeforeStartTime(): Boolean
}
