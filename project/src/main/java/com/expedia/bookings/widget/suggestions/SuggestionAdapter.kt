package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.expedia.bookings.R
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.SuggestionViewModel

open class SuggestionAdapter(val viewmodel: SuggestionAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    val marginTop = viewmodel.context.resources.getDimensionPixelSize(R.dimen.package_suggestion_margin_top)
    val marginBottom = viewmodel.context.resources.getDimensionPixelSize(R.dimen.package_suggestion_margin_bottom)

    override fun getItemCount(): Int {
        return viewmodel.suggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.package_dropdown_item, parent, false)
        if (!viewmodel.getCustomerSelectingOrigin()) {
            val titleTextview = view.findViewById(R.id.title_textview)
            val params = titleTextview.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, marginTop, 0, marginBottom)
        }
        val vm = SuggestionViewModel(viewmodel.getCustomerSelectingOrigin())
        vm.suggestionSelected.subscribe(viewmodel.suggestionSelectedSubject)
        return makeViewHolder(view as ViewGroup, vm)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is SuggestionViewHolder -> holder.vm.suggestionObserver.onNext(viewmodel.suggestions[position])
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

    fun makeViewHolder(root: ViewGroup, vm: SuggestionViewModel): RecyclerView.ViewHolder {
        return SuggestionViewHolder(root, vm)
    }
}
