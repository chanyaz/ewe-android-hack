package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.HotelSearchParamsUtil
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject

class HotelSearchViewModel(context: Context) : BaseSearchViewModel(context) {
    override val paramsBuilder = HotelSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    val userBucketedObservable = BehaviorSubject.create<Boolean>()
    val externalSearchParamsObservable = BehaviorSubject.create<Boolean>()
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()

    // Outputs

    var shopWithPointsViewModel: ShopWithPointsViewModel by notNullAndObservable {
        it.swpEffectiveAvailability.subscribe{
            paramsBuilder.shopWithPoints(it)
        }
    }
        @Inject set

    // Inputs
    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(paramsBuilder.areRequiredParamsFilled())
        destinationValidObservable.onNext(paramsBuilder.hasDestinationLocation())
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.destination(suggestion)
        locationTextObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())
        requiredSearchParamsObserver.onNext(Unit)
    }

    val suggestionTextChangedObserver = endlessObserver<Unit> {
        paramsBuilder.destination(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            if (!paramsBuilder.hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else {
                val hotelSearchParams = paramsBuilder.build()
                HotelSearchParamsUtil.saveSearchHistory(context, hotelSearchParams)

                searchParamsObservable.onNext(hotelSearchParams)
            }
        } else {
            if (!paramsBuilder.hasDestinationLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
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
        val (start, end) = dates

        paramsBuilder.startDate(start)
        if (start != null && end == null) {
            paramsBuilder.endDate(start.plusDays(1))
        } else {
            paramsBuilder.endDate(end)
        }

        dateTextObservable.onNext(computeDateText(start, end))
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
        datesObservable.onNext(dates)
    }

    init {
        val intent = (context as Activity).intent
        val isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(
                AbacusUtils.EBAndroidAppHotelRecentSearchTest)
        userBucketedObservable.onNext(isUserBucketedForTest)
        externalSearchParamsObservable.onNext(!intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS) && !isUserBucketedForTest)
        Ui.getApplication(context).hotelComponent().inject(this)
    }
}
