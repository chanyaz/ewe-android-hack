package com.expedia.bookings.widget

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.LXSuggestionAdapterViewModel
import com.expedia.vm.LXSuggestionViewModel
import kotlin.text.replace

class LXSuggestionAdapter(val viewmodel: LXSuggestionAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    override fun getItemCount(): Int {
        return viewmodel.suggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lx_dropdown_item, parent, false)
        val vm = LXSuggestionViewModel(parent.context)
        vm.suggestionSelected.subscribe(viewmodel.suggestionSelectedSubject)
        return LXSuggestionViewHolder(view as ViewGroup, vm)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as LXSuggestionViewHolder).vm.suggestionObserver.onNext(viewmodel.suggestions[position])
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
}

class LXSuggestionViewHolder(val root: ViewGroup, val vm : LXSuggestionViewModel) : RecyclerView.ViewHolder(root), View.OnClickListener {
    val displayName: TextView by root.bindView(R.id.title_textview)
    val dropdownImage: ImageView by root.bindView(R.id.lx_dropdown_imageView)
    val cityName: TextView by root.bindView(R.id.city_name_textView)

    init {
        itemView.setOnClickListener(this)
        dropdownImage.setColorFilter(ContextCompat.getColor(root.context, R.color.app_primary))

        vm.titleObservable.subscribeText(displayName)
        vm.iconObservable.subscribe { imageSource ->
            dropdownImage.setImageResource(imageSource)
        }

        vm.cityNameObservable.subscribeText(cityName)
        vm.cityNameVisibility.subscribeVisibility(cityName)
        dropdownImage.setColorFilter(ContextCompat.getColor(dropdownImage.context,
                Ui.obtainThemeResID(dropdownImage.context, R.attr.skin_lxPrimaryColor)))

    }

    override fun onClick(view: View) {
        val suggestion = vm.suggestionObserver.value
        suggestion.regionNames.displayName = suggestion.regionNames.displayName.replace("\"", "")
        vm.suggestionSelected.onNext(SearchSuggestion(suggestion))
    }
}

