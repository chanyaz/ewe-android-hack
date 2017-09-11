package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.cars.CarCategory
import com.expedia.bookings.data.cars.CarInfo
import com.expedia.bookings.data.cars.CarType
import com.expedia.bookings.data.cars.CategorizedCarOffers
import com.expedia.bookings.data.cars.RateTerm
import com.expedia.bookings.data.cars.SearchCarFare
import com.expedia.bookings.data.cars.SearchCarOffer
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CarCategoriesListAdapterTests {

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun testCarResultsAccessibility() {
        val viewHolder = createMockViewHolder()
        viewHolder.bindCategorizedOffers(mockCategorizedCarOffer())
        assertEquals("Bags 3", viewHolder.bagCount.contentDescription)
        assertEquals("Passengers 2", viewHolder.passengerCount.contentDescription)
        assertEquals("Doors 1-4", viewHolder.doorCount.contentDescription)
    }

    @Test
    fun testCarResultsWithoutDoorsPassengersAndBagsAccessibility() {
        val viewHolder = createMockViewHolder()
        viewHolder.bindCategorizedOffers(mockCategorizedCarOffer(false))
        assertEquals(null, viewHolder.bagCount.contentDescription)
        assertEquals(null, viewHolder.passengerCount.contentDescription)
        assertEquals(null, viewHolder.doorCount.contentDescription)
    }

    fun createMockViewHolder(): CarCategoriesListAdapter.ViewHolder {
        val mockItemView = Mockito.mock(View::class.java)
        val mockCategoryTextView = TextView(getContext())
        val mockBestPriceTextView = TextView(getContext())
        val mockTotalTextView = TextView(getContext())
        val mockBackgroundImageView = ImageView(getContext())
        val mockPassengerCountTextView = TextView(getContext())
        val mockBagCountTextView = TextView(getContext())
        val mockDoorCountTextView = TextView(getContext())
        val mockCardView = CardView(getContext())
        val mockGradient = View(getContext())
        val mockListCardAnnounceButtonContDesc= View(getContext())

        Mockito.`when`(mockItemView.findViewById<View>(R.id.category_text)).thenReturn(mockCategoryTextView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.category_price_text)).thenReturn(mockBestPriceTextView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.total_price_text)).thenReturn(mockTotalTextView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.background_image_view)).thenReturn(mockBackgroundImageView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.passenger_count)).thenReturn(mockPassengerCountTextView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.bag_count)).thenReturn(mockBagCountTextView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.door_count)).thenReturn(mockDoorCountTextView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.card_view)).thenReturn(mockCardView)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.gradient_mask)).thenReturn(mockGradient)
        Mockito.`when`(mockItemView.findViewById<View>(R.id.list_card_announce_button_cont_desc)).thenReturn(mockListCardAnnounceButtonContDesc)

        Mockito.`when`<Context>(mockItemView.context).thenReturn(getContext())

        return CarCategoriesListAdapter.ViewHolder(mockItemView)
    }

    fun mockCategorizedCarOffer(completeCarInfo: Boolean = true): CategorizedCarOffers {
        val categorizedCarOffer = CategorizedCarOffers("Compact", CarCategory.COMPACT)

        val searchCarFare = SearchCarFare()
        searchCarFare.total = Money(10, "USD")
        searchCarFare.rate = Money(10, "USD")
        searchCarFare.rateTerm = RateTerm.DAILY

        val carInfo = CarInfo()
        carInfo.carCategoryDisplayLabel = "Compact"
        carInfo.type = CarType.FOUR_DOOR_CAR
        if (completeCarInfo) {
            carInfo.adultCapacity = 2
            carInfo.childCapacity = 2
            carInfo.largeLuggageCapacity = 3
            carInfo.smallLuggageCapacity = 2
            carInfo.maxDoors = 4
            carInfo.minDoors = 1
        }

        val searchOffer = SearchCarOffer()
        searchOffer.fare = searchCarFare
        searchOffer.vehicleInfo = carInfo
        categorizedCarOffer.add(searchOffer)

        return categorizedCarOffer
    }
}