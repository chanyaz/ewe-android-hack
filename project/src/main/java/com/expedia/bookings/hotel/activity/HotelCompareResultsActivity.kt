package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.widget.adapter.HotelCompareAdapter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import javax.inject.Inject

class HotelCompareResultsActivity : AppCompatActivity() {
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.hotel_compare_recycler_view) as RecyclerView
    }

    private lateinit var compareAdapter: HotelCompareAdapter

    private val hotelIdList = listOf("20567", "795934", "808299", "1679779", "875099", "3383521")

    private val hotelHashMap = HashMap<String, HotelOffersResponse>()

    lateinit var hotelServices: HotelServices
        @Inject set
    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        HotelComponentInjector().inject(this)
        Ui.getApplication(this).hotelComponent().inject(this)

        setContentView(R.layout.hotel_compare_activity)

        compareAdapter = HotelCompareAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = compareAdapter
    }

    override fun onResume() {
        super.onResume()
//        if (hotelInfoManager != null) {
//            hotelInfoManager.infoSuccessSubject.subscribe { response ->
//                hotelHashMap.put(response.hotelId, response)
//                compareAdapter.updateHotels(hotelHashMap.values.toList())
//            }
//
//            fetchIdsInfo()
//        }
    }

    private fun fetchIdsInfo() {
        for (id in hotelIdList) {
            hotelInfoManager.fetchDatelessInfo(id)
        }
    }
}