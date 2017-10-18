package com.expedia.bookings.hotel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.fragment.ChangeDatesDialogFragment
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.widget.adapter.HotelCompareAdapter
import com.expedia.bookings.hotel.widget.adapter.HotelCompareThumbnailAdapter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.vm.hotel.HotelDetailViewModel
import javax.inject.Inject

class HotelCompareResultsActivity : AppCompatActivity(), HotelCompareAdapter.CompareCheckListener {
    private val toolbar by lazy {
        findViewById(R.id.hotel_compare_results_toolbar) as Toolbar
    }

    private val compareResultsRecycler by lazy {
        findViewById(R.id.hotel_compare_recycler_view) as RecyclerView
    }

    private val thumbnailRecycler by lazy {
        findViewById(R.id.compare_thumbnail_recycler_view) as RecyclerView
    }
    private val thumbnailCompareButton by lazy {
        findViewById(R.id.compare_button) as TextView
    }

    private lateinit var compareAdapter: HotelCompareAdapter
    private lateinit var thumbnailAdapter: HotelCompareThumbnailAdapter

    private var hotelIdList = ArrayList<String>()

    private val mockIdList = listOf("happy1", "happy2", "happy3",
            "happy4", "happy5", "happy6")

    private val hotelHashMap = HashMap<String, HotelOffersResponse>()

    private val compareMap = HashMap<String, HotelOffersResponse>()

    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HotelComponentInjector().inject(this)
        Ui.getApplication(this).hotelComponent().inject(this)

        setContentView(R.layout.hotel_compare_activity)

        compareAdapter = HotelCompareAdapter(this, this)
        thumbnailAdapter = HotelCompareThumbnailAdapter(this)

        compareResultsRecycler.layoutManager = LinearLayoutManager(this)
        compareResultsRecycler.adapter = compareAdapter

        val horizontalLayoutManager = LinearLayoutManager(this)
        horizontalLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        thumbnailRecycler.layoutManager = horizontalLayoutManager
        thumbnailRecycler.adapter = thumbnailAdapter

        thumbnailCompareButton.setOnClickListener {
            if (compareMap.size >= 2) {
                val intent = Intent(this, HotelDetailedCompareActivity::class.java)
                intent.putExtra(HotelExtras.COMPARE_HOTEL_IDS, compareMap.keys.toTypedArray())
                startActivity(intent)
            }
        }

        compareAdapter.fetchPricesSubject.subscribe {
            showChangeDatesDialog()
        }

        toolbar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        hotelIdList = HotelFavoriteCache.getFavorites(this) ?: ArrayList<String>()
    }

    override fun onResume() {
        super.onResume()
        if (hotelInfoManager != null && hotelHashMap.isEmpty()) {
            hotelInfoManager.infoSuccessSubject.subscribe { response ->
                hotelHashMap.put(response.hotelId, response)
                compareAdapter.updateHotels(hotelHashMap.values.toList())
            }

            hotelInfoManager.offerSuccessSubject.subscribe { response ->
                hotelHashMap.put(response.hotelId, response)
                compareAdapter.updateHotels(hotelHashMap.values.toList())

            }

            fetchIdsInfo()
        }
    }

    override fun compareCheckChanged(hotelId: String, checked: Boolean) {
        if (checked) {
            compareMap.put(hotelId, hotelHashMap.get(hotelId)!!)
            thumbnailAdapter.updateThumbnails(compareMap.values.toList())
            thumbnailRecycler.visibility = View.VISIBLE
            thumbnailCompareButton.visibility = View.VISIBLE
        } else {
            compareMap.remove(hotelId)
            thumbnailAdapter.updateThumbnails(compareMap.values.toList())
            if (compareMap.isEmpty()) {
                thumbnailRecycler.visibility = View.GONE
                thumbnailCompareButton.visibility = View.GONE
            }
        }
    }

    private fun fetchIdsInfo() {
        for (id in hotelIdList) {
            hotelInfoManager.fetchDatelessInfo(id)
        }
    }

    private fun showChangeDatesDialog() {
        val dialogFragment = ChangeDatesDialogFragment()
        dialogFragment.datesChangedSubject.subscribe { dates ->
            for (id in hotelIdList) {
                hotelInfoManager.fetchOffers(dates.first, dates.second, id)
            }
        }
        val fragmentManager = (this as FragmentActivity).supportFragmentManager

        dialogFragment.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }

}