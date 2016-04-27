package com.expedia.bookings.widget.suggestions

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.packages.SuggestionViewModel

abstract class AbstractSuggestionViewHolder(val root: ViewGroup, val vm: SuggestionViewModel) : RecyclerView.ViewHolder(root), View.OnClickListener {
    val title: TextView by root.bindView(R.id.title_textview)
    val icon: ImageView by root.bindView(R.id.icon_imageview)
    val hierarchyIcon: ImageView by root.bindView(R.id.hierarchy_imageview)

    init {
        itemView.setOnClickListener(this)
        icon.setColorFilter(ContextCompat.getColor(root.context, R.color.packages_primary_color))
        hierarchyIcon.setColorFilter(ContextCompat.getColor(root.context, R.color.packages_primary_color))

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
        vm.suggestionSelected.onNext(suggestion)
        trackRecentSearchClick()
    }

    abstract fun trackRecentSearchClick()
}
