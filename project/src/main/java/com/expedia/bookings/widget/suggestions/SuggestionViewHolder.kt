package com.expedia.bookings.widget.suggestions

import android.support.annotation.VisibleForTesting
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeFont
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

open class SuggestionViewHolder(val root: ViewGroup, val vm: BaseSuggestionViewModel) : RecyclerView.ViewHolder(root) {
    @VisibleForTesting val icon: ImageView by bindView(R.id.icon_imageview)
    @VisibleForTesting val hierarchyIcon: ImageView by bindView(R.id.hierarchy_imageview)

    private val title: TextView by bindView(R.id.title_textview)
    private val subtitle: TextView by bindView(R.id.suggestion_subtitle)
    private val divider: View by bindView(R.id.suggestion_dropdown_divider)

    init {
        vm.titleObservable.subscribeText(title)
        vm.subtitleObservable.subscribeTextAndVisibility(subtitle)
        vm.titleFontObservable.subscribeFont(title)

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

        vm.iconContentDescriptionObservable.subscribe { type ->
            icon.contentDescription = type
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
