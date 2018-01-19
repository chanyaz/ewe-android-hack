package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionDataItem
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.BaseSuggestionViewModel

abstract class BaseSuggestionAdapter(val viewModel: SuggestionAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SUGGESTIONV4 = 1
    private val TYPE_SUGGESTION_LABEL = 2
    private val TYPE_CURRENT_LOCATION = 3

    protected var suggestionItems: List<SuggestionDataItem> = emptyList()
    protected var pastSuggestionsShownCount = 0

    init {
        viewModel.suggestionItemsSubject.subscribe { newSuggestions ->
            this.suggestionItems = newSuggestions
            pastSuggestionsShownCount = 0
            for (item in suggestionItems) {
                if (item is SuggestionDataItem.V4 && item.suggestion.isHistoryItem) pastSuggestionsShownCount++
            }
            notifyDataSetChanged()
        }
    }

    abstract fun getSuggestionViewModel(): BaseSuggestionViewModel
    abstract fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int): SuggestionTrackingData?

    override fun getItemCount(): Int {
        return suggestionItems.size
    }

    override fun getItemViewType(position: Int): Int {
        when (suggestionItems[position]) {
            is SuggestionDataItem.V4 -> return TYPE_SUGGESTIONV4
            is SuggestionDataItem.Label -> return TYPE_SUGGESTION_LABEL
            is SuggestionDataItem.CurrentLocation -> return TYPE_CURRENT_LOCATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val vm = getSuggestionViewModel()
        when (viewType) {
            TYPE_SUGGESTION_LABEL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_dropdown_label, parent, false)
                return SuggestionLabelViewHolder(view as TextView, vm)
            }
            TYPE_CURRENT_LOCATION -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_dropdown_current_location, parent, false)
                return CurrentLocationViewHolder(view)
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
                val item = suggestionItems[position] as SuggestionDataItem.V4
                holder.vm.bind(item.suggestion)
                holder.displayDivider(shouldDisplayDivider(position))
                holder.itemView.setOnClickListener(SuggestionClickListener(item.suggestion, position))
            }
            is CurrentLocationViewHolder -> {
                val item = suggestionItems[position] as SuggestionDataItem.CurrentLocation
                holder.itemView.setOnClickListener(SuggestionClickListener(item.suggestion, position))
            }
            is SuggestionLabelViewHolder -> {
                val item = suggestionItems[position] as SuggestionDataItem.Label
                holder.vm.bindLabel(item.suggestionlabel)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun shouldDisplayDivider(position: Int): Boolean {
        return position + 1 < suggestionItems.size && getItemViewType(position + 1) != TYPE_SUGGESTION_LABEL
    }

    class SuggestionLabelViewHolder(val suggestionLabel: TextView, val vm: BaseSuggestionViewModel)
        : RecyclerView.ViewHolder(suggestionLabel) {

        init {
            vm.suggestionLabelTitleObservable.subscribeText(suggestionLabel)
        }
    }

    class CurrentLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    private inner class SuggestionClickListener(private val suggestion: SuggestionV4,
                                          private val position: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            val searchSuggestion = SearchSuggestion(suggestion)
            searchSuggestion.trackingData = getSuggestionTrackingData(suggestion, position)
            viewModel.suggestionSelectedSubject.onNext(searchSuggestion)
        }
    }
}
