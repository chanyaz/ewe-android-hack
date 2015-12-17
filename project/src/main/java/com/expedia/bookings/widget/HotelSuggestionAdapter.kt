package com.expedia.bookings.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribe
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.HotelSuggestionViewModel
import java.util.concurrent.CountDownLatch

public class HotelSuggestionAdapter(val viewmodel: HotelSuggestionAdapterViewModel) : BaseAdapter(), Filterable {

    private val filter = object : Filter() {
        override public fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
            notifyDataSetChanged()
        }

        override fun performFiltering(input: CharSequence?): Filter.FilterResults {
            val latch = CountDownLatch(1)
            viewmodel.suggestionsObservable.subscribe {
                latch.countDown()
            }
            viewmodel.queryObserver.onNext(input?.toString() ?: "")
            latch.await()

            val results = Filter.FilterResults()
            results.count = viewmodel.suggestions.size()
            results.values = viewmodel.suggestions
            return results
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_dropdown_item, parent, false)
            view.setTag(HotelSuggestionViewHolder(view as ViewGroup, HotelSuggestionViewModel()))
        }

        val holder = view.getTag() as HotelSuggestionViewHolder
        holder.viewmodel.suggestionObserver.onNext(getItem(position))

        return view
    }

    override fun getCount(): Int {
        return viewmodel.suggestions.size()
    }

    override fun getFilter(): Filter? {
        return filter
    }

    override fun getItem(position: Int): SuggestionV4 {
        return viewmodel.suggestions.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

public class HotelSuggestionViewHolder(val root: ViewGroup, val viewmodel: HotelSuggestionViewModel) {
    val title: TextView by root.bindView(R.id.title_textview)
    val icon: ImageView by root.bindView(R.id.icon_imageview)
    val hierarchyIcon: ImageView by root.bindView(R.id.hierarchy_imageview)

    init {
        icon.setColorFilter(root.getContext().getResources().getColor(R.color.hotels_primary_color))
        hierarchyIcon.setColorFilter(root.getContext().getResources().getColor(R.color.hotels_primary_color))

        viewmodel.titleObservable.subscribe(title)

        viewmodel.isChildObservable.subscribe { isChild ->
            if (isChild) {
                hierarchyIcon.setVisibility(View.VISIBLE)
                icon.setVisibility(View.GONE)

            } else {
                hierarchyIcon.setVisibility(View.GONE)
                icon.setVisibility(View.VISIBLE)
            }
        }

        viewmodel.iconObservable.subscribe { imageSource ->
            icon.setImageResource(imageSource)
        }
    }
}

