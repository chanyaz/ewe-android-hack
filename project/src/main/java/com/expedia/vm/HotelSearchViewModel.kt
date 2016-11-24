package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import org.joda.time.LocalDate
import rx.subjects.PublishSubject
import javax.inject.Inject

class HotelSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    val hotelParamsBuilder = HotelSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange(), isFilterUnavailableEnabled())
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()

    // Outputs
    var shopWithPointsViewModel: ShopWithPointsViewModel by notNullAndObservable {
        it.swpEffectiveAvailability.subscribe {
            getParamsBuilder().shopWithPoints(it)
        }
    }
    @Inject set

    // Inputs
    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
        destinationValidObservable.onNext(getParamsBuilder().hasDestinationLocation())
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        locationTextObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        requiredSearchParamsObserver.onNext(Unit)
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }

    val suggestionTextChangedObserver = endlessObserver<Unit> {
        getParamsBuilder().destination(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far))
            } else {
                val hotelSearchParams = getParamsBuilder().build()
                searchParamsObservable.onNext(hotelSearchParams)
            }
        } else {
            if (!getParamsBuilder().hasDestinationLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override fun getParamsBuilder(): HotelSearchParams.Builder {
        return hotelParamsBuilder
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return false // check-in and out dates required
    }

    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_hotel_stay)
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.calendar_max_selectable_date_range)
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val start = dates.first
        var end = dates.second
        getParamsBuilder().startDate(start)
        if (start != null && end == null) {
            getParamsBuilder().endDate(start.plusDays(1))
        } else if (start != null && start.equals(end)) {
            getParamsBuilder().endDate(start.plusDays(1))
            end = start.plusDays(1)
        } else {
            getParamsBuilder().endDate(end)
        }
        dateTextObservable.onNext(computeDateText(start, end))
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
        datesObservable.onNext(dates)
    }

    private fun isFilterUnavailableEnabled(): Boolean {
        return !Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSearchScreenSoldOutTest)
    }

    init {
        Ui.getApplication(context).hotelComponent().inject(this)
    }
}
