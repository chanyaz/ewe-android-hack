package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.SuggestionViewModel

class PackageSuggestionAdapter(viewmodel: SuggestionAdapterViewModel) : SuggestionAdapter(viewmodel) {
    override fun makeViewHolder(root: ViewGroup, vm: SuggestionViewModel): RecyclerView.ViewHolder {
        return PackageSuggestionViewHolder(root, vm)
    }
}

class PackageSuggestionViewHolder(root: ViewGroup, vm: SuggestionViewModel) : AbstractSuggestionViewHolder(root, vm) {
    override fun trackRecentSearchClick() {
        // no tracking for packages yet
    }
}
