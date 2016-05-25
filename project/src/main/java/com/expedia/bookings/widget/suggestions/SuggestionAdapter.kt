package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.expedia.bookings.R
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.SuggestionViewModel

abstract class SuggestionAdapter(val viewmodel: SuggestionAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    override fun getItemCount(): Int {
        return viewmodel.suggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_dropdown_item, parent, false)
        val vm = SuggestionViewModel(viewmodel.getCustomerSelectingOrigin())
        vm.suggestionSelected.subscribe(viewmodel.suggestionSelectedSubject)
        return makeViewHolder(view as ViewGroup, vm)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is AbstractSuggestionViewHolder -> holder.vm.suggestionObserver.onNext(viewmodel.suggestions[position])
        }
    }

    init {
        viewmodel.suggestionsObservable.subscribe {
            viewmodel.suggestions = it
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter? {
        return filter
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    abstract fun makeViewHolder(root: ViewGroup, vm: SuggestionViewModel): RecyclerView.ViewHolder;
}
