package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.vm.HotelSuggestionViewModel
import rx.subjects.PublishSubject

class HotelSuggestionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    val suggestionClicked = PublishSubject.create<SearchSuggestion>()

    private var suggestions: List<SuggestionV4> = emptyList()
    private var pastSuggestionsShownCount = 0

    fun setSuggestions(suggestions: List<SuggestionV4>) {
        this.suggestions = suggestions
        pastSuggestionsShownCount = 0
        for (suggestion in suggestions) {
            if (suggestion.isHistoryItem) pastSuggestionsShownCount++
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_dropdown_item, parent, false)
        val vm = HotelSuggestionViewModel()
        return HotelSuggestionViewHolder(view as ViewGroup, vm)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is HotelSuggestionViewHolder -> {
                val suggestionV4 = suggestions[position]
                holder.bind(suggestionV4)
                holder.itemView.setOnClickListener {
                    val hotelSuggestion = SearchSuggestion(suggestionV4)
                    hotelSuggestion.trackingData = getSuggestionTrackingData(suggestionV4, position)
                    suggestionClicked.onNext(hotelSuggestion)
                }
            }
        }
    }

    override fun getFilter(): Filter? {
        return filter
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int) : SuggestionTrackingData {
        val trackingData = SuggestionTrackingData()
        trackingData.selectedSuggestionPosition = position + 1
        trackingData.suggestionsShownCount = suggestions.count()
        trackingData.previousSuggestionsShownCount = pastSuggestionsShownCount

        // api doesn't give us parent information so we need to manually check
        if (!suggestion.isChild && position + 1 < suggestions.count()) {
            trackingData.isParent = suggestions[position + 1].isChild
        }
        trackingData.isChild = suggestion.isChild
        trackingData.updateData(suggestion)

        return trackingData
    }
}
