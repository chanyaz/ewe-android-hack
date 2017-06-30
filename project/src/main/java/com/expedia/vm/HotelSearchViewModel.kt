package com.expedia.vm

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.R.id.search
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.provider.HotelSearchProvider
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.PublishSubject
import java.util.ArrayList
import javax.inject.Inject

class HotelSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    var greedySearchOn = false
    var hotelSearchParams: HotelSearchParams ?= null
    val hotelParamsBuilder = HotelSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange(), true)
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()
    var notSuperSearch = true

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
            if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far))
            } else {
                val hotelSearchParamsChanged = getParamsBuilder().build()
                if(isBucketedGreedySearch() && paramsCompare(hotelSearchParamsChanged) && notSuperSearch) {
                    greedySearchOn = false
                    hotelSearchParams!!.sameParameters = true
                    searchParamsObservable.onNext(hotelSearchParams)
                } else {
                    greedySearchOn = false
                    hotelSearchParamsChanged.sameParameters = false
                    searchParamsObservable.onNext(hotelSearchParamsChanged)
                    notSuperSearch=true
                }
            }
        } else {
            if (!getParamsBuilder().hasDestinationLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
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

        if (start!=null && end != null && Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelGreedySearch)) {
            hotelSearchParams = getParamsBuilder().build()
            greedySearchOn = true
            searchParamsObservable.onNext(hotelSearchParams)
        }
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

    private fun isBucketedGreedySearch(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelGreedySearch)
    }

    private fun paramsCompare(hotelSearchParamsChanged: HotelSearchParams): Boolean {
        return hotelSearchParamsChanged.origin!=null && hotelSearchParams?.origin!=null && hotelSearchParamsChanged.origin == hotelSearchParams?.origin &&
                hotelSearchParamsChanged.adults == hotelSearchParams?.adults && hotelSearchParamsChanged.children == hotelSearchParams?.children &&
                hotelSearchParamsChanged.shopWithPoints == hotelSearchParams?.shopWithPoints
    }
}