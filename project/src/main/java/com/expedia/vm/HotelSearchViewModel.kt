package com.expedia.vm

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import org.joda.time.LocalDate
import rx.subjects.PublishSubject
import javax.inject.Inject

class HotelSearchViewModel(context: Context, private val hotelSearchManager: HotelSearchManager) : BaseSearchViewModel(context) {

    // outputs
    val hotelIdSearchSubject = PublishSubject.create<HotelSearchParams>()
    val rawTextSearchSubject = PublishSubject.create<HotelSearchParams>()
    val genericSearchSubject = PublishSubject.create<HotelSearchParams>()

    var shopWithPointsViewModel: ShopWithPointsViewModel by notNullAndObservable {
        it.swpEffectiveAvailability.subscribe {
            getParamsBuilder().shopWithPoints(it)
        }
    }
        @Inject set

    private val hotelParamsBuilder = HotelSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange(), true)
    private var prefetchParams: HotelSearchParams? = null

    // Inputs
    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        val requiredParamsFilled = getParamsBuilder().areRequiredParamsFilled()
        if (shouldPrefetchSearch()) {
            prefetchSearch(getParamsBuilder().build())
        }
        searchButtonObservable.onNext(requiredParamsFilled)
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        locationTextObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        requiredSearchParamsObserver.onNext(Unit)
    }

    val advancedOptionsObserver = endlessObserver<UserFilterChoices> { searchOptions ->
        updateAdvancedSearchOptions(searchOptions)
    }

    init {
        Ui.getApplication(context).hotelComponent().inject(this)
    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            validateAndSearch()
        } else {
            handleIncompleteParams()
        }
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
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
        return context.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only)
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        var (start, end) = dates

        dateTextObservable.onNext(getCalendarCardDateText(start, end, false))
        dateAccessibilityObservable.onNext(getCalendarCardDateText(start, end, true))
        dateInstructionObservable.onNext(getDateInstructionText(start, end))
        calendarTooltipTextObservable.onNext(getToolTipText(start, end))
        calendarTooltipContDescObservable.onNext(getToolTipContentDescription(start, end))

        if (start != null && (end == null || start.equals(end))) {
            end = start.plusDays(1)
        }
        super.onDatesChanged(Pair(start, end))
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        if (end == null) {
            return context.getString(R.string.hotel_calendar_tooltip_bottom)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_checkin_date)
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
        val selectCheckoutText = context.getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        if (forContentDescription) {
            return getDateAccessibilityText(selectCheckoutText, "")
        }
        return selectCheckoutText
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        val dateNightText = getDateNightText(start, end, forContentDescription)
        if (forContentDescription) {
            return getDateAccessibilityText(context.getString(R.string.select_dates), dateNightText.toString())
        }
        return dateNightText.toString()
    }

    private fun validateAndSearch() {
        if (!getParamsBuilder().hasValidDateDuration()) {
            errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
        } else if (!getParamsBuilder().isWithinDateRange()) {
            errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far))
        } else {
            handleSearch(getParamsBuilder().build())
        }
    }

    private fun handleSearch(params: HotelSearchParams) {
        if (params.suggestion.hotelId != null) {
            hotelSearchManager.reset()
            hotelIdSearchSubject.onNext(params)
        } else if (params.suggestion.isRawTextSearch) {
            hotelSearchManager.reset()
            rawTextSearchSubject.onNext(params)
        } else {
            if (!params.equalForPrefetch(prefetchParams)) {
                hotelSearchManager.doSearch(params)
            }
            genericSearchSubject.onNext(params)
        }
        prefetchParams = null
    }

    private fun handleIncompleteParams() {
        if (!getParamsBuilder().hasDestinationLocation()) {
            errorNoDestinationObservable.onNext(Unit)
        } else if (!getParamsBuilder().hasStartAndEndDates()) {
            errorNoDatesObservable.onNext(Unit)
        }
    }

    private fun shouldPrefetchSearch() : Boolean {
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelGreedySearch)
                && builderHasValidParams()) {
            val params = hotelParamsBuilder.build()
            val suggestion = params.suggestion

            return !suggestion.isPinnedHotelSearch && !suggestion.isRawTextSearch
                    && params.filterOptions?.isEmpty() ?: true
        }
        return false
    }

    private fun builderHasValidParams() : Boolean {
        return  hotelParamsBuilder.areRequiredParamsFilled()
                && hotelParamsBuilder.hasValidDateDuration()
                && hotelParamsBuilder.isWithinDateRange()
    }

    private fun prefetchSearch(params: HotelSearchParams) {
        prefetchParams = params
        hotelSearchManager.doSearch(params)
    }

    private fun updateAdvancedSearchOptions(searchOptions: UserFilterChoices) {
        val searchBuilder = getParamsBuilder()
        searchBuilder.hotelName(searchOptions.name)
        searchBuilder.starRatings(searchOptions.hotelStarRating.getStarRatingParamsAsList())
        searchBuilder.vipOnly(searchOptions.isVipOnlyAccess)
        if (searchOptions.userSort != ProductFlavorFeatureConfiguration.getInstance().defaultSort) {
            searchBuilder.userSort(searchOptions.userSort.toServerSort())
        } else {
            searchBuilder.clearUserSort()
        }
    }

    private fun getDateNightText(start: LocalDate, end: LocalDate, isContentDescription: Boolean): CharSequence {
        val dateNightBuilder = SpannableBuilder()
        val nightCount = JodaUtils.daysBetween(start, end)

        val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)

        val dateRangeText = if (isContentDescription) {
            getStartToEndDateString(start, end)
        } else {
            getStartDashEndDateString(start, end)
        }

        dateNightBuilder.append(dateRangeText)
        dateNightBuilder.append(" ")
        dateNightBuilder.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))

        return dateNightBuilder.build()
    }
}
