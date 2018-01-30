package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.shared.data.SuggestionDataItem
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.SuggestionAdapterViewModel

abstract class BaseSuggestionAdapter(val viewModel: SuggestionAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var suggestionItems: List<SuggestionDataItem> = emptyList()
    protected var pastSuggestionsShownCount = 0

    private val TYPE_SUGGESTION_V4 = 1
    private val TYPE_SUGGESTION_LABEL = 2

    init {
        viewModel.suggestionItemsSubject.subscribe { newSuggestions ->
            this.suggestionItems = newSuggestions
            pastSuggestionsShownCount = 0
            for (item in suggestionItems) {
                if (item is SuggestionDataItem.SuggestionDropDown && item.suggestion.isHistoryItem) {
                    pastSuggestionsShownCount++
                }
            }
            notifyDataSetChanged()
        }
    }

    abstract fun getSuggestionViewModel(): BaseSuggestionViewModel

    override fun getItemCount(): Int {
        return suggestionItems.size
    }

    override fun getItemViewType(position: Int): Int {
        when (suggestionItems[position]) {
            is SuggestionDataItem.SuggestionDropDown -> return TYPE_SUGGESTION_V4
            is SuggestionDataItem.Label -> return TYPE_SUGGESTION_LABEL
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val vm = getSuggestionViewModel()
        when (viewType) {
            TYPE_SUGGESTION_LABEL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_dropdown_label, parent, false)
                return SuggestionLabelViewHolder(view as TextView, vm)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_dropdown_item, parent, false)
                return SuggestionViewHolder(view as ViewGroup, vm)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is SuggestionViewHolder -> {
                val item = suggestionItems[position] as SuggestionDataItem.SuggestionDropDown
                holder.vm.bind(item.suggestion)
                holder.itemView.setOnClickListener(SuggestionClickListener(item.suggestion, position))
            }
            is SuggestionLabelViewHolder -> {
                val item = suggestionItems[position] as SuggestionDataItem.Label
                holder.vm.bindLabel(item.suggestionLabel)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    open fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int): SuggestionTrackingData? {
        return null
    }

    private class SuggestionLabelViewHolder(val suggestionLabel: TextView, val vm: BaseSuggestionViewModel)
        : RecyclerView.ViewHolder(suggestionLabel) {

        init {
            vm.suggestionLabelTitleObservable.subscribeText(suggestionLabel)
        }
    }

    private inner class SuggestionClickListener(private val suggestion: SuggestionV4,
                                                private val position: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            val searchSuggestion = SearchSuggestion(suggestion)
            searchSuggestion.trackingData = getSuggestionTrackingData(suggestion, position)
            viewModel.suggestionSelectedSubject.onNext(searchSuggestion)
        }
    }
}
