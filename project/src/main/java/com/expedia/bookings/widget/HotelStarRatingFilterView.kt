package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.hotel.BaseHotelFilterViewModel

class HotelStarRatingFilterView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    private val filterOne: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_one)
    private val filterTwo: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_two)
    private val filterThree: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_three)
    private val filterFour: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_four)
    private val filterFive: HotelStarRatingFilterItem by bindView(R.id.hotel_filter_rating_five)

    var viewModel: BaseHotelFilterViewModel by notNullAndObservable { vm ->
        filterOne.clickedSubject.subscribe {
            filterOne.toggle()
            vm.oneStarFilterObserver.onNext(Unit)
        }

        filterTwo.clickedSubject.subscribe {
            filterTwo.toggle()
            vm.twoStarFilterObserver.onNext(Unit)
        }

        filterThree.clickedSubject.subscribe {
            filterThree.toggle()
            vm.threeStarFilterObserver.onNext(Unit)
        }

        filterFour.clickedSubject.subscribe {
            filterFour.toggle()
            vm.fourStarFilterObserver.onNext(Unit)
        }

        filterFive.clickedSubject.subscribe {
            filterFive.toggle()
            vm.fiveStarFilterObserver.onNext(Unit)
        }
    }

    init {
        View.inflate(context, R.layout.hotel_star_rating_filter_view, this)
    }

    fun reset() {
        filterOne.deselect()
        filterTwo.deselect()
        filterThree.deselect()
        filterFour.deselect()
        filterFive.deselect()
    }
}