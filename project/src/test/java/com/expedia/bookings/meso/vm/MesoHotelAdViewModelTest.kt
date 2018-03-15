package com.expedia.bookings.meso.vm

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.expedia.bookings.meso.model.MesoAdResponse
import com.expedia.bookings.meso.model.MesoHotelAdResponse
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.text.HtmlCompat
import com.expedia.util.Optional
import com.google.android.gms.ads.formats.NativeAd
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Created by cplachta on 1/17/18.
 */

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.EXPEDIA])
class MesoHotelAdViewModelTest {
    lateinit var vm: MesoHotelAdViewModel
    lateinit var context: Context
    lateinit var mockHotelAdData: MesoAdResponse

    @Before
    fun before() {
        context = Robolectric.buildActivity(Activity::class.java).create().get()
        vm = MesoHotelAdViewModel(context)
    }

    @Test
    fun testDataIsValid() {
        pushMockData()
        assertTrue { vm.dataIsValid() }
    }

    @Test
    fun testDataIsNotValid() {
        assertFalse { vm.dataIsValid() }
    }

    @Test
    fun testShouldFormatSubTextIfGreaterThanOne() {
        assertTrue { vm.shouldFormatSubText(2) }
    }

    @Test
    fun testShouldFormatSubTextIfEqualToOne() {
        assertFalse { vm.shouldFormatSubText(1) }
    }

    @Test
    fun testShouldFormatSubTextIfLessThanOne() {
        assertFalse { vm.shouldFormatSubText(0) }
    }

    @Test
    fun testShouldReturnBackgroundImageNotNull() {
        assertNotNull(vm.backgroundImage)
    }

    @Test
    fun testShouldReturnBackgroundImageNotNullWithData() {
        pushMockData()
        assertNotNull(vm.backgroundImage)
    }

    @Test
    fun testGetPercentageOffStringNotNull() {
        assertNotNull(vm.percentageOff)
    }

    @Test
    fun testGetPercentageOffStringNotNullWithData() {
        pushMockData()
        assertNotNull(vm.percentageOff)
    }

    @Test
    fun testGetPercentageOffHasCorrectFormat() {
        pushMockData()
        assertTrue { vm.percentageOff == mockHotelAdData.HotelAdResponse?.percentageOff }
    }

    @Test
    fun testGetPercentageOffNotNull() {
        assertNotNull(vm.percentageOff)
    }

    @Test
    fun testGetPercentageOffNotNullWithData() {
        pushMockData()
        assertNotNull(vm.percentageOff)
    }

    @Test
    fun testGetHotelNameNotNull() {
        assertNotNull(vm.hotelName)
    }

    @Test
    fun testGetHotelNameNotNullWithData() {
        pushMockData()
        assertNotNull(vm.hotelName)
    }

    @Test
    fun testOneLineSubTextHasCorrectFormat() {
        pushMockData()
        assertTrue {
            HtmlCompat.fromHtml(vm.oneLineSubText).toString() ==
                    "${mockHotelAdData.HotelAdResponse?.propertyLocation} · from ${mockHotelAdData.HotelAdResponse?.strikethroughPrice} ${mockHotelAdData.HotelAdResponse?.offerPrice}/night"
        }
    }

    @Test
    fun testOneLineSubTextNotNull() {
        assertNotNull(vm.oneLineSubText)
    }

    @Test
    fun testOneLineSubTextNotNullWithData() {
        pushMockData()
        assertNotNull(vm.oneLineSubText)
    }

    @Test
    fun testTwoLineSubTextHasCorrectFormat() {
        pushMockData()
        assertTrue {
            HtmlCompat.fromHtml(vm.twoLineSubText).toString() ==
                    "${mockHotelAdData.HotelAdResponse?.propertyLocation}\nfrom ${mockHotelAdData.HotelAdResponse?.strikethroughPrice} ${mockHotelAdData.HotelAdResponse?.offerPrice}/night"
        }
    }

    @Test
    fun testTwoLineSubTextNotNull() {
        assertNotNull(vm.twoLineSubText)
    }

    @Test
    fun testTwoLineSubTextNotNullWithData() {
        pushMockData()
        assertNotNull(vm.twoLineSubText)
    }

    @Test
    fun subTextOnlyShowsPropertyLocationWithNullOfferPrice() {
        pushMockData(getMesoAdResponseMockData(offerPrice = null))
        assertTrue {
            HtmlCompat.fromHtml(vm.twoLineSubText).toString() ==
                    "${mockHotelAdData.HotelAdResponse?.propertyLocation}"
        }
    }

    @Test
    fun subTextOnlyShowsPropertyLocationWithBlankOfferPrice() {
        pushMockData(getMesoAdResponseMockData(offerPrice = ""))
        assertTrue {
            HtmlCompat.fromHtml(vm.twoLineSubText).toString() ==
                    "${mockHotelAdData.HotelAdResponse?.propertyLocation}"
        }
    }

    @Test
    fun subTextOnlyShowsPropertyLocationWithNullStrikeThroughPrice() {
        pushMockData(getMesoAdResponseMockData(strikeThroughPrice = null))
        assertTrue {
            HtmlCompat.fromHtml(vm.twoLineSubText).toString() ==
                    "${mockHotelAdData.HotelAdResponse?.propertyLocation}"
        }
    }

    @Test
    fun subTextOnlyShowsPropertyLocationWithBlankStrikeThroughPrice() {
        pushMockData(getMesoAdResponseMockData(strikeThroughPrice = ""))
        assertTrue {
            HtmlCompat.fromHtml(vm.twoLineSubText).toString() ==
                    "${mockHotelAdData.HotelAdResponse?.propertyLocation}"
        }
    }

    @Test
    fun testHotelSearchParamsNotNull() {
        assertNotNull(vm.hotelParamsForSearch)
    }

    @Test
    fun testHotelSearchParamsWithData() {
        pushMockData()
        assertNotNull(vm.hotelParamsForSearch)
    }

    @Test
    fun testHotelSearchParamsHasFutureDateWhenTheDayIsFriday() {
        // The time in milliseconds on Friday 1/12/18
        val timeInMillisecondsOfAFriday = 1515797866000
        DateTimeUtils.setCurrentMillisFixed(timeInMillisecondsOfAFriday)

        pushMockData()
        assertFalse(vm.hotelParamsForSearch.startDate == LocalDate.now())
    }

    @Test
    fun testHotelSearchParamsHasFutureDateWhenTheDayIsBeforeFriday() {
        // The time in milliseconds on Thursday 1/11/18
        val timeInMillisecondsOfAThursday = 1515711466000
        DateTimeUtils.setCurrentMillisFixed(timeInMillisecondsOfAThursday)

        pushMockData()
        assertTrue(vm.hotelParamsForSearch.startDate == (LocalDate.now().plusDays(1)))
    }

    @Test
    fun testDataFromProvider() {
        val providerPublishSubject = vm.getMesoHotelSubject(object : Observer<Optional<MesoHotelAdResponse>> {
            override fun onNext(mesoHotelAdResponse: Optional<MesoHotelAdResponse>) {
                assertEquals(mesoHotelAdResponse.value, mockHotelAdData.HotelAdResponse)
            }

            override fun onError(e: Throwable) {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onComplete() {
            }
        })

        mockHotelAdData = getMesoAdResponseMockData()
        providerPublishSubject.onNext(mockHotelAdData)
    }

    private fun pushMockData(mockData: MesoAdResponse = getMesoAdResponseMockData()) {
        val providerPublishSubject = vm.getMesoHotelSubject(object : Observer<com.expedia.util.Optional<MesoHotelAdResponse>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: Optional<MesoHotelAdResponse>) {
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
            }
        })

        mockHotelAdData = mockData
        providerPublishSubject.onNext(mockHotelAdData)
    }

    private fun getMesoAdResponseMockData(
        headline: String? = "Check out this hotel",
        hotelId: String? = "123456",
        hotelName: String? = "Really Great Fake Hotel",
        offerPrice: String? = "$200",
        percentageOff: String? = "33%",
        propertyLocation: String? = "Ann Arbor, Michigan",
        regionId: String? = "0",
        strikeThroughPrice: String? = "$300"
    ): MesoAdResponse {

        val mesoHotelAdResponse = MesoHotelAdResponse(object : NativeAd.Image() {
            override fun getDrawable(): Drawable? {
                return null
            }

            override fun getUri(): Uri? {
                return Uri.parse("https://images.trvl-media.com/hotels/22000000/21120000/21118500/21118500/985a38ba_z.jpg")
            }

            override fun getScale(): Double {
                return 0.0
            }
        },
                headline,
                hotelId,
                hotelName,
                offerPrice,
                percentageOff,
                propertyLocation,
                regionId,
                strikeThroughPrice)

        return MesoAdResponse(mesoHotelAdResponse)
    }
}
