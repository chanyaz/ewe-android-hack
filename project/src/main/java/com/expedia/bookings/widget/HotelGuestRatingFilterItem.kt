package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.squareup.phrase.Phrase
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import io.reactivex.subjects.PublishSubject

class HotelGuestRatingFilterItem(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val clickedSubject = PublishSubject.create<Unit>()
    val filterGuestRating: Button by bindView(R.id.hotel_guest_rating_button)

    var guestRatingSelected = false
        private set

    init {
        View.inflate(context, R.layout.hotel_guest_rating_filter_item, this)
        filterGuestRating.setOnClickListener {
            clickedSubject.onNext(Unit)
        }
    }

    fun toggle() {
        if (guestRatingSelected) {
            deselect()
        } else {
            select()
        }
    }

    fun deselect() {
        selectGuestRating(false)
    }

    fun select() {
        selectGuestRating(true)
    }

    private fun selectGuestRating(selected: Boolean = true) {
        val colorId = if (selected) android.R.color.white else R.color.hotel_filter_neighborhood_text
        val backgroundId = if (selected) Ui.obtainThemeResID(context, R.attr.primary_color) else android.R.color.white

        filterGuestRating.setTextColor(ContextCompat.getColor(context, colorId))
        filterGuestRating.setBackgroundColor(ContextCompat.getColor(context, backgroundId))
        updateContentDescription(selected)
        guestRatingSelected = selected
    }

    private fun updateContentDescription(isSelected: Boolean = false) {
        val template = if (isSelected) R.string.star_rating_selected_cont_desc_TEMPLATE else R.string.star_rating_not_selected_cont_desc_TEMPLATE
        filterGuestRating.contentDescription = Phrase.from(context, template)
                .put("star_rating", filterGuestRating.text)
                .format().toString()
    }
}
