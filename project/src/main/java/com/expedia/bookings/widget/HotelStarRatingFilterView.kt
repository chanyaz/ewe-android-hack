package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject

class HotelStarRatingFilterView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    val starRatingsSubject = PublishSubject.create<UserFilterChoices.StarRatings>()
    val oneStarSubject by lazy { filterOne.clickedSubject }
    val twoStarSubject by lazy { filterTwo.clickedSubject }
    val threeStarSubject by lazy { filterThree.clickedSubject }
    val fourStarSubject by lazy { filterFour.clickedSubject }
    val fiveStarSubject by lazy { filterFive.clickedSubject }

    private val filterOne: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_one)
    private val filterTwo: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_two)
    private val filterThree: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_three)
    private val filterFour: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_four)
    private val filterFive: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_five)

    private var starRatings = UserFilterChoices.StarRatings()

    init {
        View.inflate(context, R.layout.hotel_star_rating_filter_view, this)

        filterOne.clickedSubject.subscribe {
            filterOne.toggle()
            starRatings.one = if (!starRatings.one) true else false
            starRatingsSubject.onNext(starRatings)
        }

        filterTwo.clickedSubject.subscribe {
            filterTwo.toggle()
            starRatings.two = if (!starRatings.two) true else false
            starRatingsSubject.onNext(starRatings)
        }

        filterThree.clickedSubject.subscribe {
            filterThree.toggle()
            starRatings.three = if (!starRatings.three) true else false
            starRatingsSubject.onNext(starRatings)
        }

        filterFour.clickedSubject.subscribe {
            filterFour.toggle()
            starRatings.four = if (!starRatings.four) true else false
            starRatingsSubject.onNext(starRatings)
        }

        filterFive.clickedSubject.subscribe {
            filterFive.toggle()
            starRatings.five = if (!starRatings.five) true else false
            starRatingsSubject.onNext(starRatings)
        }
    }

    fun reset() {
        filterOne.deselect()
        filterTwo.deselect()
        filterThree.deselect()
        filterFour.deselect()
        filterFive.deselect()
        starRatings = UserFilterChoices.StarRatings()
    }
}