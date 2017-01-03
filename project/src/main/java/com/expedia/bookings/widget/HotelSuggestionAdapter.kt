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
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.HotelSuggestionViewModel
import kotlin.text.replace

class HotelSuggestionAdapter(val viewmodel: HotelSuggestionAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    override fun getItemCount(): Int {
        return viewmodel.suggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_dropdown_item, parent, false)
        val vm = HotelSuggestionViewModel()
        vm.suggestionSelected.subscribe(viewmodel.suggestionSelectedSubject)
        return HotelSuggestionViewHolder(view as ViewGroup, vm)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is HotelSuggestionViewHolder -> holder.vm.suggestionObserver.onNext(viewmodel.suggestions[position])
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
}

class HotelSuggestionViewHolder(val root: ViewGroup, val vm : HotelSuggestionViewModel) : RecyclerView.ViewHolder(root), View.OnClickListener {
    val title: TextView by root.bindView(R.id.title_textview)
    val icon: ImageView by root.bindView(R.id.icon_imageview)
    val hierarchyIcon: ImageView by root.bindView(R.id.hierarchy_imageview)

    init {
        itemView.setOnClickListener(this)
        vm.titleObservable.subscribeText(title)

        vm.isChildObservable.subscribe { isChild ->
            if (isChild) {
                hierarchyIcon.visibility = View.VISIBLE
                icon.visibility = View.GONE

            } else {
                hierarchyIcon.visibility = View.GONE
                icon.visibility = View.VISIBLE
            }
        }

        vm.iconObservable.subscribe { imageSource ->
            icon.setImageResource(imageSource)
        }
    }

    override fun onClick(view: View) {
        val suggestion = vm.suggestionObserver.value
        suggestion.regionNames.displayName = suggestion.regionNames.displayName.replace("\"", "")
        vm.suggestionSelected.onNext(suggestion)
    }
}

