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
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribe
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.HotelSuggestionViewModel
import com.mobiata.android.time.widget.CalendarPicker
import java.util.ArrayList

public class HotelSuggestionAdapter(private val vm: HotelSuggestionAdapterViewModel) : BaseAdapter(), Filterable {

    private val suggestions = ArrayList<SuggestionV4>()
    private val filter = SuggestFilter()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_dropdown_item, parent, false)
            view.setTag(SuggestionViewHolder(view as ViewGroup))
        }

        val holder = view.getTag() as SuggestionViewHolder
        holder.bind(HotelSuggestionViewModel(getItem(position)))

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

        val dropdownImage: ImageView by root.bindView(R.id.hotels_dropdown_imageView)

        val groupImage: ImageView by root.bindView(R.id.hotel_group_imageView)

        public fun bind(viewModel: HotelSuggestionViewModel) {
            viewModel.displayNameObservable.subscribe(displayName)

            viewModel.groupNameObservable.subscribe { isChild ->
                if (isChild) {
                    //is child show add left margin and change the image view with arrow
                    groupImage.setVisibility(View.VISIBLE)
                    dropdownImage.setVisibility(View.GONE)
                    groupImage.setColorFilter(dropdownImage.getContext().getResources().getColor(R.color.hotels_primary_color))
                } else {
                    groupImage.setVisibility(View.GONE)
                    dropdownImage.setVisibility(View.VISIBLE)
                    dropdownImage.setColorFilter(dropdownImage.getContext().getResources().getColor(R.color.hotels_primary_color))
                }

            }
            viewModel.dropdownImageObservable.subscribe { imageSource ->
                dropdownImage.setImageResource(imageSource)
            }

        }

    }

    public inner class SuggestFilter : Filter() {
        override public fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
            notifyDataSetChanged()
        }

        override fun performFiltering(input: CharSequence?): Filter.FilterResults {
            val results = Filter.FilterResults()
            vm.hotelSearchTextObserver.onNext(input)
            results.count = suggestions.size()
            results.values = suggestions
            return results
        }
    }

    init {
        vm.updateSuggestionsV4Observable.subscribe { suggests ->
            suggestions.clear()
            if (suggests != null) {
                suggestions.addAll(suggests)
            }
            filter.publishResults("", null)
        }
    }


}
