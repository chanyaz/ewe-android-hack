package com.expedia.bookings.services

import com.expedia.bookings.data.RoomUpgradeOffersResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class RoomUpgradeOffersServiceTest {

    val server = MockWebServer()
        @Rule get

    lateinit var service: RoomUpgradeOffersService

    @Before
    fun setup() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        val endpoint = "https://www.expedia.com"
        service = RoomUpgradeOffersService(endpoint,
                OkHttpClient.Builder().addInterceptor(logger).build(), interceptor, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun fetchOffers() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val portNumber = server.port
        val url = "http://localhost:$portNumber/api/trips/c65fb5fb-489a-4fa8-a007-715b946d3b04/8066893350319/74f89606-241f-4d08-9294-8c17942333dd/1/sGUZBxGESgB2eGM7GeXkhqJuzdi8Ucq1jl7NI9NzcW1mSSoGJ4njkXYWPCT2e__Ilwdc4lgBRnwlanmEgukEJWqNybe4NPSppEUZf9quVqD_kCjh_2HSZY_-K1HvZU-tUQ3h/upgradeOffers"

        val testObserver = TestObserver<RoomUpgradeOffersResponse>()
        service.fetchOffers(url, testObserver)

        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val roomUpgradeOfferResponse = testObserver.values().first()
        val firstRoomOffer = roomUpgradeOfferResponse.upgradeOffers.roomOffers.first()

        assertEquals(3, roomUpgradeOfferResponse.upgradeOffers.roomOffers.size)
        assertEquals("3dee74e9-601c-4979-81f6-daf3ff9763c0_V3", firstRoomOffer.productKey)
    }
}
