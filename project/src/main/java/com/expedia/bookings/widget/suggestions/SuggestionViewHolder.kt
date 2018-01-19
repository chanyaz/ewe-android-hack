package com.expedia.bookings.widget.suggestions

import android.support.annotation.VisibleForTesting
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.packages.BaseSuggestionViewModel

class SuggestionViewHolder(val root: ViewGroup, val vm: BaseSuggestionViewModel) : RecyclerView.ViewHolder(root) {
    private val title: TextView by bindView(R.id.title_textview)
    private val subtitle: TextView by bindView(R.id.suggestion_subtittle)
    @VisibleForTesting val icon: ImageView by bindView(R.id.icon_imageview)
    @VisibleForTesting val hierarchyIcon: ImageView by bindView(R.id.hierarchy_imageview)
    private val divider: View by bindView(R.id.suggestion_dropdown_divider)

    init {
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

    fun displayDivider(display: Boolean) {
        if (display) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.INVISIBLE
        }
    }
}
