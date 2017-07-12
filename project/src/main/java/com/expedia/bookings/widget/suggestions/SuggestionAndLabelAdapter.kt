package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionType
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.AirportSuggestionViewModel
import com.expedia.vm.packages.SuggestionViewModel

class SuggestionAndLabelAdapter(viewmodel: AirportSuggestionViewModel) : SuggestionAdapter(viewmodel) {

    val TYPE_SUGGESTIONV4 = 1
    val TYPE_SUGGESTION_LABEL = 2

    override fun getItemCount(): Int {
        return viewmodel.suggestionsAndLabel.size
    }

    override fun getItemViewType(position: Int): Int {
        when (viewmodel.suggestionsAndLabel.get(position)) {
            is SuggestionType.SUGGESTIONV4 -> return TYPE_SUGGESTIONV4
            is SuggestionType.SUGGESTIONLABEL -> return TYPE_SUGGESTION_LABEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {

        val vm = SuggestionViewModel(viewmodel.getCustomerSelectingOrigin())
        when (viewType) {
            TYPE_SUGGESTION_LABEL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_suggestion_label, parent, false)
                return SuggestionLabelViewHolder(view as TextView, vm)
            }
            else -> {
                return super.onCreateViewHolder(parent, viewType)
            }
        }

    }

    init {
        viewmodel.suggestionsAndLabelObservable.subscribe {
            viewmodel.suggestionsAndLabel = it
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        when (holder) {
            is SuggestionViewHolder -> holder.vm.suggestionObserver.onNext((viewmodel.suggestionsAndLabel[position] as SuggestionType.SUGGESTIONV4).suggestion)
            is SuggestionLabelViewHolder -> holder.vm.suggestionLabelObserver.onNext((viewmodel.suggestionsAndLabel[position] as SuggestionType.SUGGESTIONLABEL).suggestionlabel)
        }
    }

    class SuggestionLabelViewHolder(val suggestionLabel: TextView, val vm: SuggestionViewModel) : RecyclerView.ViewHolder(suggestionLabel) {

        init {
            vm.suggestionLabelTitleObservable.subscribeText(suggestionLabel)
        }
    }
}
