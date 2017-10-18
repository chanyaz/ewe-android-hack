package com.expedia.bookings.hotel.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.widget.adapter.HotelDetailedCompareAdapter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.navigation.HotelNavUtils
import javax.inject.Inject
import com.readystatesoftware.chuck.internal.ui.MainActivity
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

        detailedCompareAdapter = HotelDetailedCompareAdapter(this)

        detailedCompareRecycler.layoutManager = TwoSpanGridLayoutManager(this, 1)
        detailedCompareRecycler.adapter = detailedCompareAdapter

        hotelInfoManager.infoSuccessSubject.subscribe { offer ->
            detailedCompareAdapter.addHotel(offer)
        }

        val ids = intent.getStringArrayExtra(HotelExtras.COMPARE_HOTEL_IDS)
        for (id in ids) {
            hotelInfoManager.fetchDatelessInfo(id)
        }

        detailedCompareAdapter.hotelSelectedSubject.subscribe { id ->
            //todo make smarter, add dates, add correct name
            val params = HotelSearchParams()
            params.hotelId = id
            params.query = id
            params.searchType = HotelSearchParams.SearchType.HOTEL
            val now = LocalDate.now()
            params.checkInDate = now
            params.checkOutDate = now.plusDays(1)
            params.numAdults = 2
            params.children = null
            HotelNavUtils.goToHotels(this, params)

        }
    }

    class TwoSpanGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {
        init {
            orientation = LinearLayoutManager.HORIZONTAL
        }
    }
}