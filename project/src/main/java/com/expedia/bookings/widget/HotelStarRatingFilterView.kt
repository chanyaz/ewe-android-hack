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
    val oneStarSubject = PublishSubject.create<Unit>()
    val twoStarSubject = PublishSubject.create<Unit>()
    val threeStarSubject = PublishSubject.create<Unit>()
    val fourStarSubject = PublishSubject.create<Unit>()
    val fiveStarSubject = PublishSubject.create<Unit>()

    private val filterOne: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_one)
    private val filterTwo: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_two)
    private val filterThree: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_three)
    private val filterFour: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_four)
    private val filterFive: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_five)

    private var starRatings = UserFilterChoices.StarRatings()

    init {
        View.inflate(context, R.layout.hotel_star_rating_filter_view, this)

        filterOne.clickedSubject.subscribe { toggleStarOne() }
        filterTwo.clickedSubject.subscribe { toggleStarTwo() }
        filterThree.clickedSubject.subscribe { toggleStarThree() }
        filterFour.clickedSubject.subscribe { toggleStarFour() }
        filterFive.clickedSubject.subscribe { toggleStarFive() }
    }

    fun reset() {
        filterOne.deselect()
        filterTwo.deselect()
        filterThree.deselect()
        filterFour.deselect()
        filterFive.deselect()
        starRatings = UserFilterChoices.StarRatings()
    }

    fun update(hotelStarRating: UserFilterChoices.StarRatings) {
        if (hotelStarRating.one) toggleStarOne()
        if (hotelStarRating.two) toggleStarTwo()
        if (hotelStarRating.three) toggleStarThree()
        if (hotelStarRating.four) toggleStarFour()
        if (hotelStarRating.five) toggleStarFive()
    }

    private fun toggleStarOne() {
        filterOne.toggle()
        starRatings.one = if (!starRatings.one) true else false
        starRatingsSubject.onNext(starRatings)
        oneStarSubject.onNext(Unit)
    }

    private fun toggleStarTwo() {
        filterTwo.toggle()
        starRatings.two = if (!starRatings.two) true else false
        starRatingsSubject.onNext(starRatings)
        twoStarSubject.onNext(Unit)
    }

    private fun toggleStarThree() {
        filterThree.toggle()
        starRatings.three = if (!starRatings.three) true else false
        starRatingsSubject.onNext(starRatings)
        threeStarSubject.onNext(Unit)
    }

    private fun toggleStarFour() {
        filterFour.toggle()
        starRatings.four = if (!starRatings.four) true else false
        starRatingsSubject.onNext(starRatings)
        fourStarSubject.onNext(Unit)
    }

    private fun toggleStarFive() {
        filterFive.toggle()
        starRatings.five = if (!starRatings.five) true else false
        starRatingsSubject.onNext(starRatings)
        fiveStarSubject.onNext(Unit)
    }
}