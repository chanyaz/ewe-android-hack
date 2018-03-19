package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.utils.bindView
import io.reactivex.subjects.PublishSubject

class HotelGuestRatingFilterView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    val threeGuestRatingSubject = PublishSubject.create<Unit>()
    val fourGuestRatingSubject = PublishSubject.create<Unit>()
    val fiveGuestRatingSubject = PublishSubject.create<Unit>()

    private val filterThree: HotelGuestRatingFilterItem by bindView(R.id.hotel_filter_guest_rating_three)
    private val filterFour: HotelGuestRatingFilterItem by bindView(R.id.hotel_filter_guest_rating_four)
    private val filterFive: HotelGuestRatingFilterItem by bindView(R.id.hotel_filter_guest_rating_five)

    private var guestRatings = UserFilterChoices.GuestRatings()

    init {
        View.inflate(context, R.layout.hotel_guest_rating_filter_view, this)
        setUpText()

        filterThree.clickedSubject.subscribe { toggleGuestRatingThree() }
        filterFour.clickedSubject.subscribe { toggleGuestRatingFour() }
        filterFive.clickedSubject.subscribe { toggleGuestRatingFive() }
    }

    fun reset() {
        filterThree.deselect()
        filterFour.deselect()
        filterFive.deselect()
        guestRatings = UserFilterChoices.GuestRatings()
    }

    fun update(hotelGuestRating: UserFilterChoices.GuestRatings) {
        if (hotelGuestRating.three) toggleGuestRatingThree()
        if (hotelGuestRating.four) toggleGuestRatingFour()
        if (hotelGuestRating.five) toggleGuestRatingFive()
    }

    private fun toggleGuestRatingThree() {
        if (filterFour.guestRatingSelected) {
            toggleGuestRatingFour()
        }
        if (filterFive.guestRatingSelected) {
            toggleGuestRatingFive()
        }
        filterThree.toggle()
        guestRatings.three = !guestRatings.three
        threeGuestRatingSubject.onNext(Unit)
    }

    private fun toggleGuestRatingFour() {
        if (filterThree.guestRatingSelected) {
            toggleGuestRatingThree()
        }
        if (filterFive.guestRatingSelected) {
            toggleGuestRatingFive()
        }
        filterFour.toggle()
        guestRatings.four = !guestRatings.four
        fourGuestRatingSubject.onNext(Unit)
    }

    private fun toggleGuestRatingFive() {
        if (filterThree.guestRatingSelected) {
            toggleGuestRatingThree()
        }
        if (filterFour.guestRatingSelected) {
            toggleGuestRatingFour()
        }
        filterFive.toggle()
        guestRatings.five = !guestRatings.five
        threeGuestRatingSubject.onNext(Unit)
    }

    private fun setUpText() {
        filterThree.filterGuestRating.text = context.resources.getString(R.string.guest_rating_three_and_above)
        filterFour.filterGuestRating.text = context.resources.getString(R.string.guest_rating_four_and_above)
        filterFive.filterGuestRating.text = context.resources.getString(R.string.guest_rating_five)
    }
}
