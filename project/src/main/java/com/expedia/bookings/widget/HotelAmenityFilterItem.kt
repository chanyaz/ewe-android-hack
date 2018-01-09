package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject

class HotelAmenityFilterItem(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val clickedSubject = PublishSubject.create<Unit>()

    val filterAmenity: ImageButton by bindView(R.id.hotel_amenity_image)
    val amenityLables: TextView by bindView(R.id.amenity_labels)

    private var amenitySelected = false

    init {
        View.inflate(context, R.layout.hotel_amenity_filter_item, this)
        filterAmenity.setOnClickListener {
            clickedSubject.onNext(Unit)
        }
    }

    fun toggle() {
        if (amenitySelected) {
            deselect()
        } else {
            select()
        }
    }

    fun deselect() {
        selectAmenity(false)
    }

    fun select() {
        selectAmenity(true)
    }

    private fun selectAmenity(selected: Boolean = true) {
        val colorId = if (selected) android.R.color.white else Ui.obtainThemeResID(context, R.attr.primary_color)
        val backgroundId = if (selected) Ui.obtainThemeResID(context, R.attr.primary_color) else android.R.color.white

        filterAmenity.setColorFilter(ContextCompat.getColor(context, colorId))
        filterAmenity.setBackgroundColor(ContextCompat.getColor(context, backgroundId))
        //updateContentDescription(selected)
        amenitySelected = selected
    }

    //TODO
//    private fun updateContentDescription(isSelected: Boolean = false) {
//        val template = if (isSelected) R.string.star_rating_selected_cont_desc_TEMPLATE else R.string.star_rating_not_selected_cont_desc_TEMPLATE
//        filterAmenity.contentDescription = Phrase.from(context, template)
//                .put("star_rating", valueContentDescription)
//                .format().toString()
//    }
}
