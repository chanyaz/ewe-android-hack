package com.expedia.bookings.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
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
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.CountDownLatch

public class HotelSuggestionAdapter(val viewmodel: HotelSuggestionAdapterViewModel) : BaseAdapter(), Filterable {
    private val DEFAULT_AUTOFILL_ITEM_VIEW = 0
    private val SUGGESTION_ITEM_VIEW = 1
    private val ITEM_VIEW_TYPE_COUNT = 2

    init {
        viewmodel.suggestionsObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            notifyDataSetChanged()
        }
    }

    private val filter = object : Filter() {
        override public fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
            notifyDataSetChanged()
        }

        override fun performFiltering(input: CharSequence?): Filter.FilterResults {
            viewmodel.queryObserver.onNext(input?.toString() ?: "")

            val results = Filter.FilterResults()
            results.count = viewmodel.suggestions.size()
            results.values = viewmodel.suggestions
            return results
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val itemViewType = getItemViewType(position)

        var view: View? = null
        when (itemViewType) {
            DEFAULT_AUTOFILL_ITEM_VIEW -> view = getDefaultAutofillItemView(parent.getContext(), convertView)

            SUGGESTION_ITEM_VIEW -> view = getSuggestionItemView(position, convertView, parent)
        }

        return view
    }

    override fun getCount(): Int {
        return viewmodel.suggestions.size()
    }

    override fun getViewTypeCount(): Int {
        return ITEM_VIEW_TYPE_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) DEFAULT_AUTOFILL_ITEM_VIEW else SUGGESTION_ITEM_VIEW
    }

    private fun getDefaultAutofillItemView(context: Context, convertView: View?): View {
        var view = convertView
        if (view == null) {
            view = View(context)
            view.setLayoutParams(AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))
        }

        return view
    }

    private fun getSuggestionItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_dropdown_item, parent, false)
            view.setTag(HotelSuggestionViewHolder(view as ViewGroup, HotelSuggestionViewModel()))
        }

        val holder = view.getTag() as HotelSuggestionViewHolder
        holder.viewmodel.suggestionObserver.onNext(getItem(position))

        return view
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

