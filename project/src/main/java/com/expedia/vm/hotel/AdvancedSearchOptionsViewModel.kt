package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class AdvancedSearchOptionsViewModel(val context: Context) {
    //inputs
    val doneObservable = PublishSubject.create<Unit>()
    val clearObservable = PublishSubject.create<Unit>()

    //out
    val doneClickedSubject = PublishSubject.create<Unit>()
    val resetViewsSubject = PublishSubject.create<UserFilterChoices>()
    val searchOptionsSubject = PublishSubject.create<UserFilterChoices>()
    val searchOptionsSummarySubject = PublishSubject.create<String>()
    val showClearButtonSubject = BehaviorSubject.create<Boolean>()

    private val searchOptions = UserFilterChoices()

    init {
        doneObservable.subscribe {
            doneClickedSubject.onNext(Unit)
            searchOptionsSubject.onNext(searchOptions)
            searchOptionsSummarySubject.onNext(getSummaryString())
        }

        clearObservable.subscribe {
            resetUserFilters()
            filterCountChanged()
            resetViewsSubject.onNext(searchOptions)
            HotelTracking.trackLinkHotelSuperSearchClearFilter()
        }
    }

    fun selectSortOption(selectedSort: DisplaySort) {
        searchOptions.userSort = selectedSort
        filterCountChanged()
        HotelTracking.trackHotelSuperSearchSortBy(selectedSort.toString())
    }

    fun selectHotelName(hotelName: String) {
        searchOptions.name = hotelName
        filterCountChanged()
    }

    fun updateStarRating(starRating: UserFilterChoices.StarRatings) {
        searchOptions.hotelStarRating = starRating
        filterCountChanged()
        HotelTracking.trackLinkHotelSuperSearchStarRating(starRating.toString())
    }

    fun isVipAccess(vipChecked: Boolean) {
        searchOptions.isVipOnlyAccess = vipChecked
        filterCountChanged()
        HotelTracking.trackLinkHotelSuperSearchVip(vipChecked)
    }

    fun getSummaryString(): String {
        val sb = StringBuffer()

        if (searchOptions.name.isNotEmpty()) addField(sb, searchOptions.name)

        addStarRating(sb)

        if (searchOptions.isVipOnlyAccess) {
            addField(sb, context.getString(R.string.vip_only))
        }

        if (searchOptions.userSort != ProductFlavorFeatureConfiguration.getInstance().defaultSort) {
            val sortBy = Phrase.from(context, R.string.hotel_sort_by_search_option_TEMPLATE)
                .put("sortby", context.getString(searchOptions.userSort.resId))
                .format()
                .toString()
            addField(sb, sortBy)
        }

        if (sb.length == 0) {
            sb.append(context.getString(R.string.advanced_options))
        }

        return sb.toString()
    }

    private fun addStarRating(sb: StringBuffer) {
        val hotelStarRating = searchOptions.hotelStarRating
        if (hotelStarRating.getStarRatingParamsAsList().size > 0) {
            if (hotelStarRating.one) {
                addField(sb, context.getString(R.string.one_star))
            }
            if (hotelStarRating.two) {
                addField(sb, context.getString(R.string.two_star))
            }
            if (hotelStarRating.three) {
                addField(sb, context.getString(R.string.three_star))
            }
            if (hotelStarRating.four) {
                addField(sb, context.getString(R.string.four_star))
            }
            if (hotelStarRating.five) {
                addField(sb, context.getString(R.string.five_star))
            }
        }
    }

    private fun filterCountChanged() {
        showClearButtonSubject.onNext(searchOptions.filterCount() > 0
                || searchOptions.userSort != DisplaySort.RECOMMENDED)
    }

    private fun resetUserFilters() {
        searchOptions.userSort = DisplaySort.RECOMMENDED
        searchOptions.isVipOnlyAccess = false
        searchOptions.hotelStarRating = UserFilterChoices.StarRatings()
        searchOptions.name = ""
    }

    private fun addField(sb: StringBuffer, field: String) {
        if (sb.isNotEmpty()) {
            sb.append(" ").append(context.getString(R.string.bullet_point)).append(" ")
        }
        sb.append(field)
    }
}