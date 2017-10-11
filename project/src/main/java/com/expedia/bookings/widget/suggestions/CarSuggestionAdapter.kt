package com.expedia.bookings.widget.suggestions

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
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.CarSuggestionViewModel
import com.expedia.vm.SuggestionAdapterViewModel

class CarSuggestionAdapter(val viewmodel: SuggestionAdapterViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    val marginTop = viewmodel.context.resources.getDimensionPixelSize(R.dimen.package_suggestion_margin_top)
    val marginBottom = viewmodel.context.resources.getDimensionPixelSize(R.dimen.package_suggestion_margin_bottom)
    override fun getItemCount(): Int {
        return viewmodel.suggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.car_dropdown_item, parent, false)
        if (!viewmodel.getCustomerSelectingOrigin()) {
            val titleTextview = view.findViewById<TextView>(R.id.title_textview)
            val params = titleTextview.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, marginTop, 0, marginBottom)
        }
        val vm = CarSuggestionViewModel(parent.context)
        vm.suggestionSelected.subscribe(viewmodel.suggestionSelectedSubject)
        return CarSuggestionViewHolder(view as ViewGroup, vm)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as CarSuggestionViewHolder).vm.suggestionObserver.onNext(viewmodel.suggestions[position])
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

class CarSuggestionViewHolder(val root: ViewGroup, val vm : CarSuggestionViewModel) : RecyclerView.ViewHolder(root), View.OnClickListener {
    val displayName: TextView by bindView(R.id.location_title_textView)
    val dropdownImage: ImageView by bindView(R.id.cars_dropdown_imageView)
    val cityName: TextView by bindView(R.id.location_subtitle_textView)

    init {
        itemView.setOnClickListener(this)
        dropdownImage.setColorFilter(dropdownImage.context.resources.getColor(Ui.obtainThemeResID(dropdownImage.context, R.attr.skin_carsSecondaryColor)))

        vm.titleObservable.subscribeText(displayName)
        vm.iconObservable.subscribe { imageSource ->
            dropdownImage.setImageResource(imageSource)
        }

        vm.cityNameObservable.subscribeText(cityName)
        vm.cityNameVisibility.subscribeVisibility(cityName)

    }

    override fun onClick(view: View) {
        val suggestion = vm.suggestionObserver.value
        suggestion.regionNames.displayName = suggestion.regionNames.displayName.replace("\"", "")
        vm.suggestionSelected.onNext(SearchSuggestion(suggestion))
    }
}


