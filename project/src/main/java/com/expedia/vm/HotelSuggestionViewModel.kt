package com.expedia.vm

import android.content.Context
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.utils.StrUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class HotelSuggestionViewModel(private val suggestion: SuggestionV4) {

    val displayNameObservable = BehaviorSubject.create<String>(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)).toString())

    val groupNameObservable = BehaviorSubject.create<Boolean>(suggestion.hierarchyInfo.isChild)

    val dropdownImageObservable = BehaviorSubject.create<Int>(
            if (suggestion.iconType === SuggestionV4.IconType.HISTORY_ICON) {
                R.drawable.recents
            } else if (suggestion.iconType === SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                R.drawable.ic_suggest_current_location
            } else if (suggestion.type == "HOTEL") {
                R.drawable.hotel_suggest
            } else if (suggestion.type == "AIRPORT") {
                R.drawable.airport_suggest
            } else {
                R.drawable.search_type_icon
            }
    )


}
