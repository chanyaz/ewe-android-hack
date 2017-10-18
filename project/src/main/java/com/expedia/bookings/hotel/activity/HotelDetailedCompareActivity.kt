package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.DividerItemDecoration.HORIZONTAL
import android.support.v7.widget.DividerItemDecoration.VERTICAL
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.widget.adapter.HotelDetailedCompareAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.navigation.HotelNavUtils
import javax.inject.Inject
import org.joda.time.LocalDate


class HotelDetailedCompareActivity : AppCompatActivity() {
    private val toolbar by lazy {
        findViewById(R.id.hotel_detailed_compare_toolbar) as Toolbar
    }

    private val detailedCompareRecycler: RecyclerView by lazy {
        findViewById(R.id.detailed_compare_recycler) as RecyclerView
    }

    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    private lateinit var detailedCompareAdapter: HotelDetailedCompareAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_detailed_compare_activity)

        HotelComponentInjector().inject(this)
        Ui.getApplication(this).hotelComponent().inject(this)

        toolbar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        initDetailedList()

        hotelInfoManager.infoSuccessSubject.subscribe { offer ->
            detailedCompareAdapter.addHotel(offer)
        }

        hotelInfoManager.offerSuccessSubject.subscribe { offer ->
            detailedCompareAdapter.addHotel(offer)
        }

        fetchHotelData()
    }

    private fun initDetailedList() {
        detailedCompareAdapter = HotelDetailedCompareAdapter(this)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        detailedCompareRecycler.layoutManager = layoutManager
        detailedCompareRecycler.adapter = detailedCompareAdapter

        detailedCompareRecycler.addItemDecoration(DividerItemDecoration(this, HORIZONTAL))

        detailedCompareAdapter.hotelSelectedSubject.subscribe { id ->
            val checkIn = intent.getStringExtra(HotelExtras.HOTEL_SEARCH_CHECK_IN)
            val checkOut = intent.getStringExtra(HotelExtras.HOTEL_SEARCH_CHECK_OUT)

            //todo make smarter, add correct name, add guests
            val params = HotelSearchParams()
            params.hotelId = id
            params.query = id
            params.searchType = HotelSearchParams.SearchType.HOTEL

            if (checkIn != null && checkOut != null) {
                params.checkInDate = LocalDate.parse(checkIn)
                params.checkOutDate = LocalDate.parse(checkOut)
            } else {
                val now = LocalDate.now()
                params.checkInDate = now
                params.checkOutDate = now.plusDays(1)
            }

            params.numAdults = 2
            params.children = null
            HotelNavUtils.goToHotels(this, params)
        }

        detailedCompareAdapter.removeHotelSubject.subscribe { id ->
            HotelFavoriteCache.removeHotelId(this, id)
        }
    }

    private fun fetchHotelData() {
        val checkIn = intent.getStringExtra(HotelExtras.HOTEL_SEARCH_CHECK_IN)
        val checkOut = intent.getStringExtra(HotelExtras.HOTEL_SEARCH_CHECK_OUT)
        val ids = intent.getStringArrayExtra(HotelExtras.COMPARE_HOTEL_IDS)

        if (checkIn != null && checkOut != null) {
            for (id in ids) {
                hotelInfoManager.fetchOffers(checkIn, checkOut, id)
            }
        } else {
            for (id in ids) {
                hotelInfoManager.fetchDatelessInfo(id)
            }
        }
    }
}