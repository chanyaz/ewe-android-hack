package com.expedia.bookings.extensions

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.shared.data.SuggestionDataItem

fun List<SuggestionV4>.toDataItemList(): List<SuggestionDataItem.SuggestionDropDown> {
    return this.map { suggestion -> SuggestionDataItem.SuggestionDropDown(suggestion) }
}

fun List<SearchInfo>.toSearchInfoDataItemList(): List<SuggestionDataItem.SearchInfoDropDown> {
    return this.map { suggestion -> SuggestionDataItem.SearchInfoDropDown(suggestion) }
}
