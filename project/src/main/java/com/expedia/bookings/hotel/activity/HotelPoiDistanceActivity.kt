package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.HotelPoiEnum
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.widget.HotelPoiDistanceAdapter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.hotel.widget.HotelPoiDistanceAdapter.POIDataItem
import com.google.android.gms.maps.model.PointOfInterest

class HotelPoiDistanceActivity : AppCompatActivity() {
    private val toolbar by bindView<Toolbar>(R.id.hotel_poi_toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.hotel_poi_recycler_view)

    private val adapter = HotelPoiDistanceAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.hotel_poi_distance_activity)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()

        val data = getListData(intent.getStringExtra(HotelExtras.EXTRA_HOTEL_SELECTED_ID))
        adapter.updateData(data)
    }

    private fun getListData(hotelId: String) : List<POIDataItem> {
        when (hotelId) {
            "116674" -> { //silversmith
                return listOf(POIDataItem("Art Institue of Chicago", "0.1", HotelPoiEnum.LANDMARK.iconId),
                        POIDataItem("Adams & Wabash Red Line Station", "0.1", HotelPoiEnum.TRANSIT.iconId),
                        POIDataItem("Revival Food Hall", "0.1", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("Millennium Park", "0.1", HotelPoiEnum.LANDMARK.iconId),
                        POIDataItem("Buckingham Fountain", "0.2", HotelPoiEnum.LANDMARK.iconId),
                        POIDataItem("Grant Park", "0.2", HotelPoiEnum.LANDMARK.iconId),
                        POIDataItem("Lou Malnati's Pizzeria", "0.3", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("Amtrack Station", "0.3", HotelPoiEnum.TRANSIT.iconId),
                        POIDataItem("Magnificent Mile", "0.4", HotelPoiEnum.SHOPPING.iconId),
                        POIDataItem("Lone Wolf", "0.8", HotelPoiEnum.NIGHTLIFE.iconId),
                        POIDataItem("Maude's Liquor Bar", "0.8", HotelPoiEnum.NIGHTLIFE.iconId))
            }
            "1293276" -> { //Crowne Plaza
                return listOf(POIDataItem("Girl & The Goat", "0.3", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("Au Cheval", "0.2", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("Next Restaurant", "0.6", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("Lone Wolf", "0.3", HotelPoiEnum.NIGHTLIFE.iconId),
                        POIDataItem("Maude's Liquor Bar", "0.5", HotelPoiEnum.NIGHTLIFE.iconId),
                        POIDataItem("The Fig Tree", "0.4", HotelPoiEnum.SHOPPING.iconId),
                        POIDataItem("Notre", "0.3", HotelPoiEnum.SHOPPING.iconId))
            }
            "2784555" -> { //Chicago South Loop
                return listOf(POIDataItem("Acadia", "1.0", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("MingHin Cuisine", "0.9", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("McCormick Place Convention Center", "0.6", HotelPoiEnum.LANDMARK.iconId))
            }
            "1850638" -> { //The Homestead evanston
                return listOf(POIDataItem("Stumble & Relish", "0.5", HotelPoiEnum.SHOPPING.iconId),
                        POIDataItem("gigi BOTTEGA", "0.3", HotelPoiEnum.SHOPPING.iconId),
                        POIDataItem("Evanston Outlets ", "0.7", HotelPoiEnum.SHOPPING.iconId))
            }
            "892669" -> { //City Suite Hotels
                return listOf(POIDataItem("Vic Theatre", "0.1 miles", HotelPoiEnum.LANDMARK.iconId),
                        POIDataItem("Wellington Brown Line Station", "0.2", HotelPoiEnum.TRANSIT.iconId),
                        POIDataItem("Addison Red Line Station", "0.4", HotelPoiEnum.TRANSIT.iconId),
                        POIDataItem("Wrigley Field", "0.4", HotelPoiEnum.LANDMARK.iconId))
            }
            "18278617" -> { //Ray's Bucktown
                return listOf(POIDataItem("Irazu", "0.6", HotelPoiEnum.RESTAURANT.iconId),
                        POIDataItem("The Bristol", "0.3", HotelPoiEnum.RESTAURANT.iconId))
            }
            "983642" -> { //Motel 6
                return listOf(POIDataItem("Biff's Bar & Grill", ".3 miles", HotelPoiEnum.RESTAURANT.iconId))
            }
            "21514796" -> { //Travel Inn
                return listOf()
            }
            "5461400" -> { //Aloft Chicago City Center
                return listOf()
            }
            "23023" -> { //Best Western Grant Park
                return listOf(POIDataItem("Art Institue of Chicago", "0.2", HotelPoiEnum.LANDMARK.iconId),
                        POIDataItem("Adams & Wabash Red Line Station", "0.1", HotelPoiEnum.TRANSIT.iconId),
                        POIDataItem("Buckingham Fountain", "0.1", HotelPoiEnum.LANDMARK.iconId),
                        POIDataItem("Grant Park", "0.0", HotelPoiEnum.LANDMARK.iconId))
            }
        }
        return listOf()
    }
}
