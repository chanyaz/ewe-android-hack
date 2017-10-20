package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.DividerItemDecoration.HORIZONTAL
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.widget.adapter.HotelCompareAdapter
import com.expedia.bookings.utils.Ui
import javax.inject.Inject

class HotelCompareActivity : AppCompatActivity() {

    private val toolbar by lazy {
        findViewById(R.id.hotel_detailed_compare_toolbar) as Toolbar
    }

    private val detailedCompareRecycler: RecyclerView by lazy {
        findViewById(R.id.detailed_compare_recycler) as RecyclerView
    }

    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    private lateinit var detailedCompareAdapter: HotelCompareAdapter

    //todo replace with real data https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/7296
    private val hotelIdList = listOf("875099", "12105", "3982675",
            "14937343", "1503825", "3383521")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_compare_activity)

        HotelComponentInjector().inject(this)
        Ui.getApplication(this).hotelComponent().inject(this)

        toolbar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        initCompareRecycler()

        hotelInfoManager.infoSuccessSubject.subscribe { offer ->
            detailedCompareAdapter.addHotel(offer)
        }

        fetchHotelData()
    }

    private fun initCompareRecycler() {
        detailedCompareAdapter = HotelCompareAdapter(this)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        detailedCompareRecycler.layoutManager = layoutManager
        detailedCompareRecycler.adapter = detailedCompareAdapter
        detailedCompareRecycler.addItemDecoration(DividerItemDecoration(this, HORIZONTAL))

        detailedCompareAdapter.hotelSelectedSubject.subscribe { id ->
            //todo https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/7301
        }
    }

    private fun fetchHotelData() {
        //todo need to use offer call instead. Requires dates https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/7296
        for (id in hotelIdList) {
            hotelInfoManager.fetchDatelessInfo(id)
        }
    }
}