package com.expedia.bookings.widget

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import butterknife.ButterKnife
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.mobiata.android.time.widget.CalendarPicker
import rx.Observer
import rx.Subscription
import java.util.ArrayList
import kotlin.properties.Delegates

public class HotelSuggestionAdapter(val suggestionServices: SuggestionV4Services) : BaseAdapter(), Filterable {

    private var suggestions = ArrayList<SuggestionV4>()
    private val filter = SuggestFilter()
    private var suggestSubscription: Subscription? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_dropdown_item, parent, false)
            view.setTag(SuggestionViewHolder(view as ViewGroup))
        }

        val holder = view.getTag() as SuggestionViewHolder
        holder.bind(getItem(position))

        return view
    }

    override fun getCount(): Int {
        return suggestions.size()
    }

    override fun getFilter(): Filter? {
        return filter
    }

    override fun getItem(position: Int): SuggestionV4 {
        return suggestions.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    public class SuggestionViewHolder(val root: ViewGroup) {

        val displayName: TextView by root.bindView(R.id.display_name_textView)

        val dropdownImage: ImageView by root.bindView(R.id.cars_dropdown_imageView)

        val groupImage: ImageView by root.bindView(R.id.hotel_group_imageView)

        public fun bind(suggestion: SuggestionV4) {

            displayName.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)))

            if (suggestion.hierarchyInfo.isChild) {
                //is child show add left margin and change the image view with arrow
                groupImage.setVisibility(View.VISIBLE)
                dropdownImage.setVisibility(View.GONE)
                groupImage.setColorFilter(dropdownImage.getContext().getResources().getColor(R.color.hotels_primary_color))
                return
            } else {
                groupImage.setVisibility(View.GONE)
                dropdownImage.setVisibility(View.VISIBLE)
                dropdownImage.setColorFilter(dropdownImage.getContext().getResources().getColor(R.color.hotels_primary_color))
            }

            if (suggestion.iconType === SuggestionV4.IconType.HISTORY_ICON) {
                dropdownImage.setImageResource(R.drawable.recents)
            } else if (suggestion.iconType === SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                dropdownImage.setImageResource(R.drawable.ic_suggest_current_location)
            } else if (suggestion.type == "HOTEL") {
                dropdownImage.setImageResource(R.drawable.hotel_suggest)
            } else if (suggestion.type == "AIRPORT") {
                dropdownImage.setImageResource(R.drawable.airport_suggest)
            } else {
                dropdownImage.setImageResource(R.drawable.search_type_icon)
            }

        }

    }

    public inner class SuggestFilter: Filter() {
        override public fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
            notifyDataSetChanged()
        }

        override fun performFiltering(input: CharSequence?): Filter.FilterResults {
            val query = input?.toString() ?: ""

            val results = Filter.FilterResults()
            if (query.isNotBlank() && query.length() >= 3) {
                cleanup()
                suggestSubscription = suggest(suggestionServices, query)
            } else {
                suggestions.clear()
            }

            results.count = suggestions.size()
            results.values = suggestions
            return results
        }
    }

    private var suggestionsObserverV4 = object : Observer<List<SuggestionV4>> {
        override fun onCompleted() {
            filter.publishResults("", null)
            cleanup()
        }

        override fun onError(e: Throwable) {
        }

        override fun onNext(suggests: List<SuggestionV4>) {
            suggestions.clear()
            suggestions.addAll(suggests)
        }
    }

    public fun cleanup() {
            suggestSubscription?.unsubscribe()
            suggestSubscription = null
    }

    protected fun suggest(suggestionServices: SuggestionV4Services, query: CharSequence): Subscription {
        return suggestionServices.getHotelSuggestionsV4(query.toString(), suggestionsObserverV4)
    }
}
