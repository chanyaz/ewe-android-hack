package com.expedia.bookings.widget.suggestions

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.packages.SuggestionViewModel

class SuggestionViewHolder(val root: ViewGroup, val vm: SuggestionViewModel) : RecyclerView.ViewHolder(root), View.OnClickListener {
    val title: TextView by bindView(R.id.title_textview)
    val subtitle: TextView by bindView(R.id.suggestion_subtittle)
    val icon: ImageView by bindView(R.id.icon_imageview)
    val hierarchyIcon: ImageView by bindView(R.id.hierarchy_imageview)

    init {
        itemView.setOnClickListener(this)
        icon.setColorFilter(ContextCompat.getColor(root.context, Ui.obtainThemeResID(root.context, R.attr.primary_color)))
        hierarchyIcon.setColorFilter(ContextCompat.getColor(root.context, Ui.obtainThemeResID(root.context, R.attr.primary_color)))

        vm.titleObservable.subscribeText(title)
        vm.subtitleObservable.subscribeTextAndVisibility(subtitle)

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
        vm.suggestionSelected.onNext(SearchSuggestion(suggestion))
    }

}
