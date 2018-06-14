package com.expedia.bookings.test.robolectric

import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.lx.AvailabilityInfo
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.data.lx.Offer
import com.expedia.bookings.data.lx.Ticket
import com.expedia.bookings.data.lx.ActivityAvailabilities

import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.Constants

@RunWith(RobolectricRunner::class)
class LXStateTest {

    @Test
    fun testSearchParamsAvailable() {
        val expectedStart = LocalDate.now()
        val expectedEnd = expectedStart.plusDays(14)
        val expectedLocation = "Test"
        val expectedModQualification = true

        val params = LxSearchParams.Builder().location(expectedLocation)
                .modQualified(true).startDate(expectedStart).endDate(expectedEnd).build() as LxSearchParams

        val lxState = LXState()

        Events.post(Events.LXNewSearchParamsAvailable(params))
        val stateSearchParams = lxState.searchParams
        Assert.assertEquals(expectedLocation, stateSearchParams.location)
        Assert.assertEquals(expectedStart, stateSearchParams.activityStartDate)
        Assert.assertEquals(expectedEnd, stateSearchParams.activityEndDate)
        Assert.assertEquals(expectedModQualification, stateSearchParams.modQualified)
    }

    @Test
    fun testCreateTripParamsForMip() {
        val lxState = getLXState(Constants.LX_AIR_HOTEL_MIP)
        val createTripParams = lxState.createTripParams(RuntimeEnvironment.application)
        Assert.assertEquals(Constants.PROMOTION_ID, createTripParams.items[0].promotionId)
    }

    @Test
    fun testCreateTripParamsForMod() {
        val lxState = getLXState(Constants.MOD_PROMO_TYPE)
        val createTripParams = lxState.createTripParams(RuntimeEnvironment.application)
        Assert.assertEquals(Constants.PROMOTION_ID, createTripParams.items[0].promotionId)
    }

    @Test
    fun testCreateTripParamsForNoPromo() {
        val lxState = getLXState(null)
        val createTripParams = lxState.createTripParams(RuntimeEnvironment.application)
        Assert.assertEquals("", createTripParams.items[0].promotionId)
    }

    private fun getLXState(promoDiscountType: String?): LXState {
        val lxState = LXState()
        lxState.activity = getLXActivity()
        lxState.promoDiscountType = promoDiscountType
        lxState.onOfferBooked(Events.LXOfferBooked(getActivityOffer(), arrayListOf<Ticket>()))
        return lxState
    }

    private fun getLXActivity(): LXActivity {
        val activity = LXActivity()
        activity.discountType = Constants.LX_AIR_MIP
        activity.mipDiscountPercentage = 10
        activity.id = "12345"
        activity.regionId = "54321"
        return activity
    }

    private fun getActivityOffer(): Offer {
        val offer = Offer()
        offer.id = "offerId"
        offer.title = "One Day Tour"
        offer.description = "Offer Description"
        offer.availabilityInfoOfSelectedDate = getAvailabilityInfo()
        return offer
    }

    private fun getAvailabilityInfo(): AvailabilityInfo {
        val availabilityInfo = AvailabilityInfo()
        availabilityInfo.availabilities = getActivityAvailabilities()
        return availabilityInfo
    }

    private fun getActivityAvailabilities(): ActivityAvailabilities {
        val activityAvailabilities = ActivityAvailabilities()
        activityAvailabilities.valueDate = "2025-12-10 12:30:00"
        return activityAvailabilities
    }
}
