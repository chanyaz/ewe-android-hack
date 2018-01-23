package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.launch.widget.LaunchHeaderViewHolder
import com.expedia.bookings.launch.widget.LaunchListWidget
import com.expedia.bookings.launch.widget.PhoneLaunchWidget
import com.expedia.bookings.otto.Events
import com.expedia.bookings.widget.CollectionViewHolder
import com.expedia.bookings.widget.HotelViewHolder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.properties.Delegates
import org.junit.Assert.assertEquals

@RunWith(RobolectricRunner::class)
class LaunchScreenTest {
    var activity: Activity by Delegates.notNull()
    var phoneLaunchWidget: PhoneLaunchWidget by Delegates.notNull()
    private val collectionLocation = CollectionLocation()
    private val hotel = Hotel()
    private val hotelNoRating = Hotel()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)
        phoneLaunchWidget = LayoutInflater.from(activity).inflate(R.layout.widget_phone_launch, null) as PhoneLaunchWidget
    }

    @Test
    fun testInternetConnection() {
        val launchError = phoneLaunchWidget.findViewById<View>(R.id.launch_error)
        val lobCard = phoneLaunchWidget.findViewById<View>(R.id.lob_grid_recycler)
        phoneLaunchWidget.hasInternetConnection.onNext(false)
        Assert.assertEquals(launchError.visibility, View.VISIBLE)
        Assert.assertEquals(lobCard.visibility, View.VISIBLE)

        phoneLaunchWidget.hasInternetConnection.onNext(true)
        Assert.assertEquals(launchError.visibility, View.GONE)
        Assert.assertEquals(lobCard.visibility, View.VISIBLE)
    }

    @Test
    fun testListDisplaysCollection() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val v = LayoutInflater.from(activity).inflate(R.layout.launch_screen_test, null)
        val launchListWidget = v.findViewById<View>(R.id.launch_list_widget) as LaunchListWidget
        launchListWidget.setHeaderPaddingTop(10f)
        launchListWidget.showListLoadingAnimation()

        val collection = Collection()
        collection.title = "Title"
        collection.locations = ArrayList()
        collection.locations.add(collectionLocation)
        collection.locations.add(collectionLocation)
        collection.locations.add(collectionLocation)
        val event = Events.CollectionDownloadComplete(collection)
        launchListWidget.onCollectionDownloadComplete(event)
        launchListWidget.measure(0, 0)
        launchListWidget.layout(0, 0, 100, 10000)

        assertEquals(LaunchHeaderViewHolder::class.java, launchListWidget.findViewHolderForAdapterPosition(2).javaClass)
        assertEquals(CollectionViewHolder::class.java, launchListWidget.findViewHolderForAdapterPosition(3).javaClass)
    }

    @Test
    fun testListDisplaysHotels() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val v = LayoutInflater.from(activity).inflate(R.layout.launch_screen_test, null)
        val launchListWidget = v.findViewById<View>(R.id.launch_list_widget) as LaunchListWidget
        launchListWidget.setHeaderPaddingTop(10f)
        launchListWidget.showListLoadingAnimation()

        val hotels = ArrayList<Hotel>()
        setHotelRate()
        hotels.add(hotel)
        hotels.add(hotel)
        hotels.add(hotel)
        hotels.add(hotel)
        val event = Events.LaunchHotelSearchResponse(hotels)
        launchListWidget.onNearbyHotelsSearchResults(event)
        launchListWidget.measure(0, 0)
        launchListWidget.layout(0, 0, 100, 10000)

        assertEquals(LaunchHeaderViewHolder::class.java, launchListWidget.findViewHolderForAdapterPosition(2).javaClass)
        assertEquals(HotelViewHolder::class.java, launchListWidget.findViewHolderForAdapterPosition(3).javaClass)
    }

    @Test
    fun testZeroRating() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val v = LayoutInflater.from(activity).inflate(R.layout.launch_screen_test, null)
        val launchListWidget = v.findViewById<View>(R.id.launch_list_widget) as LaunchListWidget
        launchListWidget.setHeaderPaddingTop(10f)
        launchListWidget.showListLoadingAnimation()

        val hotels = ArrayList<Hotel>()
        setHotelRate()
        hotels.add(hotel)
        hotels.add(hotelNoRating)
        hotels.add(hotelNoRating)
        hotels.add(hotelNoRating)
        hotels.add(hotelNoRating)
        hotels.add(hotelNoRating)
        val event = Events.LaunchHotelSearchResponse(hotels)
        launchListWidget.onNearbyHotelsSearchResults(event)
        launchListWidget.measure(0, 0)
        launchListWidget.layout(0, 0, 100, 10000)

        val h1 = launchListWidget.findViewHolderForAdapterPosition(3) as HotelViewHolder
        val h2 = launchListWidget.findViewHolderForAdapterPosition(4) as HotelViewHolder
        val h3 = launchListWidget.findViewHolderForAdapterPosition(5) as HotelViewHolder
        val h4 = launchListWidget.findViewHolderForAdapterPosition(6) as HotelViewHolder
        val h5 = launchListWidget.findViewHolderForAdapterPosition(7) as HotelViewHolder
        val h6 = launchListWidget.findViewHolderForAdapterPosition(8) as HotelViewHolder

        assertEquals("5.0", h1.rating.text)
        assertEquals(View.VISIBLE.toLong(), h1.ratingText.visibility.toLong())

        assertEquals(View.INVISIBLE.toLong(), h2.ratingInfo.visibility.toLong())
        assertEquals(View.GONE.toLong(), h2.ratingText.visibility.toLong())

        assertEquals(View.INVISIBLE.toLong(), h3.ratingInfo.visibility.toLong())
        assertEquals(View.GONE.toLong(), h3.ratingText.visibility.toLong())

        assertEquals(View.INVISIBLE.toLong(), h4.ratingInfo.visibility.toLong())
        assertEquals(View.GONE.toLong(), h4.ratingText.visibility.toLong())

        assertEquals(View.INVISIBLE.toLong(), h5.ratingInfo.visibility.toLong())
        assertEquals(View.GONE.toLong(), h5.ratingText.visibility.toLong())

        assertEquals("Not Rated", h6.noRatingText.text)
        assertEquals(View.VISIBLE.toLong(), h6.noRatingText.visibility.toLong())
        assertEquals(View.GONE.toLong(), h6.ratingInfo.visibility.toLong())
    }

    private fun setHotelRate() {
        val rate = HotelRate()
        rate.averageRate = 1f
        rate.surchargeTotal = 1f
        rate.surchargeTotalForEntireStay = 1f
        rate.averageBaseRate = 1f
        rate.nightlyRateTotal = 1f
        rate.discountPercent = 1f
        rate.total = 1f
        rate.currencyCode = "USD"
        rate.currencySymbol = "USD"
        rate.discountMessage = ""
        rate.priceToShowUsers = 1f
        rate.strikethroughPriceToShowUsers = 1f
        rate.totalMandatoryFees = 1f
        rate.totalPriceWithMandatoryFees = 1f
        rate.userPriceType = ""
        rate.checkoutPriceType = ""
        rate.roomTypeCode = ""
        rate.ratePlanCode = ""

        hotel.localizedName = "Hotel"
        hotel.lowRateInfo = rate
        hotel.largeThumbnailUrl = ""
        hotel.hotelGuestRating = 5f

        hotelNoRating.localizedName = "Hotel No Rating"
        hotelNoRating.lowRateInfo = rate
        hotelNoRating.largeThumbnailUrl = ""
        hotelNoRating.hotelGuestRating = 0f

        collectionLocation.id = "1"
        collectionLocation.title = "San Francisco"
        collectionLocation.subtitle = "California"
        collectionLocation.imageCode = "image"
        collectionLocation.description = "Place"
    }
}
