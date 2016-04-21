package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.SuggestionViewModel

class RailSuggestionAdapter(viewmodel: SuggestionAdapterViewModel) : SuggestionAdapter(viewmodel) {
    override fun makeViewHolder(root: ViewGroup, vm: SuggestionViewModel): RecyclerView.ViewHolder {
        return RailSuggestionViewHolder(root, vm)
    }
}

class RailSuggestionViewHolder(root: ViewGroup, vm: SuggestionViewModel) : AbstractSuggestionViewHolder(root, vm) {
    override fun trackRecentSearchClick() {
        //TODO
    }
}
