package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class HotelStarRatingFilterItem(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val clickedSubject = PublishSubject.create<Unit>()

    private val filterStar: ImageButton by bindView(R.id.hotel_star_rating_image)

    private var valueContentDescription: String? = null
    private var starSelected = false

    init {
        View.inflate(context, R.layout.hotel_star_rating_filter_item, this)
        if (attrs != null) {
            val attrSet = context.obtainStyledAttributes(attrs, R.styleable.HotelStarRatingFilterItem, 0, 0)
            try {
                if (!isInEditMode && PointOfSale.getPointOfSale().shouldShowCircleForRatings()) {
                    val circleDrawable = attrSet.getDrawable(R.styleable.HotelStarRatingFilterItem_circle_drawable_src)
                    filterStar.setImageDrawable(circleDrawable)
                    valueContentDescription = attrSet.getString(R.styleable.HotelStarRatingFilterItem_circle_cont_desc)
                } else {
                    val starDrawable = attrSet.getDrawable(R.styleable.HotelStarRatingFilterItem_star_drawable_src)
                    filterStar.setImageDrawable(starDrawable)
                    valueContentDescription = attrSet.getString(R.styleable.HotelStarRatingFilterItem_star_cont_desc)
                }
            } finally {
                attrSet.recycle()
            }
        }

        filterStar.setOnClickListener {
            clickedSubject.onNext(Unit)
        }
    }

    fun toggle() {
        if (starSelected) {
            deselect()
        } else {
            select()
        }
    }

    fun deselect() {
        selectStarRating(false)
    }

    fun select() {
        selectStarRating(true)
    }

    private fun selectStarRating(selected: Boolean = true) {
        val colorId = if (selected) android.R.color.white else Ui.obtainThemeResID(context, R.attr.primary_color)
        val backgroundId = if (selected) Ui.obtainThemeResID(context, R.attr.primary_color) else android.R.color.white

        filterStar.setColorFilter(ContextCompat.getColor(context, colorId))
        setBackgroundColor(ContextCompat.getColor(context, backgroundId))
        updateContentDescription(selected)
        starSelected = selected
    }

    private fun updateContentDescription(isSelected: Boolean = false) {
        val template = if (isSelected) R.string.star_rating_selected_cont_desc_TEMPLATE else R.string.star_rating_not_selected_cont_desc_TEMPLATE
        filterStar.contentDescription = Phrase.from(context, template)
                .put("star_rating", valueContentDescription)
                .format().toString()
    }
}
