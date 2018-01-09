package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject

class HotelAmenityFilterView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val AmenitySubject = PublishSubject.create<UserFilterChoices.Amenities>()
    val oneSubject = PublishSubject.create<Unit>()
    val twoSubject = PublishSubject.create<Unit>()
    val threeSubject = PublishSubject.create<Unit>()
    val fourSubject = PublishSubject.create<Unit>()
    val fiveSubject = PublishSubject.create<Unit>()
    val sixSubject = PublishSubject.create<Unit>()
    val sevenSubject = PublishSubject.create<Unit>()
    val eightSubject = PublishSubject.create<Unit>()

    private val amenityFilterOne: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_one)
    private val amenityFilterTwo: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_two)
    private val amenityFilterThree: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_three)
    private val amenityFilterFour: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_four)
    private val amenityFilterFive: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_five)
    private val amenityFilterSix: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_six)
    private val amenityFilterSeven: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_seven)
    private val amenityFilterEight: HotelAmenityFilterItem by bindView(R.id.hotel_filter_amenity_eight)

    private var amenities = UserFilterChoices.Amenities()

    init {
        View.inflate(context, R.layout.hotel_amenity_filter_view, this)

        amenityFilterOne.clickedSubject.subscribe { toggleOne() }
        amenityFilterTwo.clickedSubject.subscribe { toggleTwo() }
        amenityFilterThree.clickedSubject.subscribe { toggleThree() }
        amenityFilterFour.clickedSubject.subscribe { toggleFour() }
        amenityFilterFive.clickedSubject.subscribe { toggleFive() }
        amenityFilterSix.clickedSubject.subscribe { toggleSix() }
        amenityFilterSeven.clickedSubject.subscribe { toggleSeven() }
        amenityFilterEight.clickedSubject.subscribe { toggleEight() }

        amenityFilterOne.filterAmenity.setImageResource(R.drawable.itin_hotel_room_free_breakfast)
        amenityFilterTwo.filterAmenity.setImageResource(R.drawable.ic_amenity_pool)
        amenityFilterThree.filterAmenity.setImageResource(R.drawable.ic_amenity_parking)
        amenityFilterFour.filterAmenity.setImageResource(R.drawable.ic_amenity_pets)
        amenityFilterFive.filterAmenity.setImageResource(R.drawable.ic_amenity_internet)
        amenityFilterSix.filterAmenity.setImageResource(R.drawable.ic_amenity_airport_shuttle)
        amenityFilterSeven.filterAmenity.setImageResource(R.drawable.ic_amenity_ac_unit)
        amenityFilterEight.filterAmenity.setImageResource(R.drawable.ic_amenity_all_inclusive)

        amenityFilterOne.amenityLables.setText(R.string.FilterFreeBreakfast)
        amenityFilterTwo.amenityLables.setText(R.string.AmenityPool)
        amenityFilterThree.amenityLables.setText(R.string.AmenityFreeParking)
        amenityFilterFour.amenityLables.setText(R.string.AmenityPetsAllowed)
        amenityFilterFive.amenityLables.setText(R.string.AmenityInternet)
        amenityFilterSix.amenityLables.setText(R.string.FilterFreeAirportShuttle)
        amenityFilterSeven.amenityLables.setText(R.string.AmenityAC)
        amenityFilterEight.amenityLables.setText(R.string.AmenityAllInclusive)
    }

    fun reset() {
        amenityFilterOne.deselect()
        amenityFilterTwo.deselect()
        amenityFilterThree.deselect()
        amenityFilterFour.deselect()
        amenityFilterFive.deselect()
        amenityFilterSix.deselect()
        amenityFilterSeven.deselect()
        amenityFilterEight.deselect()
        amenities = UserFilterChoices.Amenities()
    }

    fun update(amenities: UserFilterChoices.Amenities) {
        if (amenities.one) toggleOne()
        if (amenities.two) toggleTwo()
        if (amenities.three) toggleThree()
        if (amenities.four) toggleFour()
        if (amenities.five) toggleFive()
        if (amenities.six) toggleFive()
        if (amenities.seven) toggleFive()
        if (amenities.eight) toggleFive()
    }

    private fun toggleOne() {
        amenityFilterOne.toggle()
        amenities.one = !amenities.one
        AmenitySubject.onNext(amenities)
        oneSubject.onNext(Unit)
    }

    private fun toggleTwo() {
        amenityFilterTwo.toggle()
        amenities.two = !amenities.two
        AmenitySubject.onNext(amenities)
        twoSubject.onNext(Unit)
    }

    private fun toggleThree() {
        amenityFilterThree.toggle()
        amenities.three = !amenities.three
        AmenitySubject.onNext(amenities)
        threeSubject.onNext(Unit)
    }

    private fun toggleFour() {
        amenityFilterFour.toggle()
        amenities.four = !amenities.four
        AmenitySubject.onNext(amenities)
        fourSubject.onNext(Unit)
    }

    private fun toggleFive() {
        amenityFilterFive.toggle()
        amenities.five = !amenities.five
        AmenitySubject.onNext(amenities)
        fiveSubject.onNext(Unit)
    }

    private fun toggleSix() {
        amenityFilterSix.toggle()
        amenities.five = !amenities.five
        AmenitySubject.onNext(amenities)
        sixSubject.onNext(Unit)
    }

    private fun toggleSeven() {
        amenityFilterSeven.toggle()
        amenities.five = !amenities.five
        AmenitySubject.onNext(amenities)
        sevenSubject.onNext(Unit)
    }

    private fun toggleEight() {
        amenityFilterEight.toggle()
        amenities.five = !amenities.five
        AmenitySubject.onNext(amenities)
        eightSubject.onNext(Unit)
    }
}
