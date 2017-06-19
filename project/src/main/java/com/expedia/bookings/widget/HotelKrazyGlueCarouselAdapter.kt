package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import org.joda.time.LocalDate
import rx.subjects.PublishSubject

class HotelKrazyGlueCarouselAdapter (hotels: List<Hotel>, hotelSubject: PublishSubject<Hotel>) : HotelMapCarouselAdapter(hotels, hotelSubject) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_marker_preview_cell, parent, false)
        setDimensions(view)
        return HotelViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder?, position: Int) {
        if (position == 3) {
//            stuff
            val holder: HotelViewHolder = given as HotelViewHolder
            val root = holder.itemView.findViewById(R.id.root)
            root.setBackgroundResource(R.drawable.hotel_see_more)
            holder.loyaltyAppliedMessageContainer.visibility =View.GONE
            holder.shadowOnLoyaltyMessageContainer.visibility = View.GONE
            holder.hotelGuestRating.visibility = View.GONE
            holder.hotelGuestRecommend.visibility = View.GONE
            holder.hotelNoGuestRating.visibility =View.GONE
            holder.hotelPreviewImage.visibility = View.GONE
            holder.itemView.findViewById(R.id.hotel_preview_container).visibility = View.GONE
            holder.itemView.findViewById(R.id.crazy_glue_see_more_image_container).visibility = View.VISIBLE
            holder.itemView.setOnClickListener{
//                deep link to show hotel search results
                val sp = HotelSearchParams(
                        suggestion = getSuggestion(),
                        checkIn = LocalDate.now().plusWeeks(2),
                        checkOut = LocalDate.now().plusWeeks(2).plusDays(1),
                        shopWithPoints = false,
                        sortType = "",
                        adults = 1,
                        children = emptyList(),
                        filterUnavailable = false)
                goToHotels(root.context, sp)
            }
        } else {
            super.onBindViewHolder(given, position)
            val holder: HotelViewHolder = given as HotelViewHolder
            holder.itemView.setOnClickListener {
                val hotel = hotels[position]
                val sp = HotelSearchParams(
                        suggestion = getSuggestion(hotel.hotelId),
                        checkIn = LocalDate.now().plusWeeks(2),
                        checkOut = LocalDate.now().plusWeeks(2).plusDays(1),
                        shopWithPoints = false,
                        sortType = "",
                        adults = 1,
                        children = emptyList(),
                        filterUnavailable = false)
                goToHotels(holder.itemView.context, sp)
            }
        }
    }

    private fun setDimensions(view: View): View {
        val screen = Ui.getScreenSize(view.context)
        val lp = view.findViewById(R.id.root).layoutParams
        lp.width = (screen.x - (3 * getDp(view))).toInt()
        val margins = view.layoutParams as ViewGroup.MarginLayoutParams
        margins.rightMargin = (0.25 * getDp(view)).toInt()
        return view
    }


    private fun getDp(parent: View) : Float {
        val float= 16.0f;
        val scale = parent.resources.displayMetrics.density;
        return (float * scale + 0.5f);
    }

    private fun getSuggestion( hotelId: String?=null ) : SuggestionV4 {
        val location = SuggestionV4()
        val displayName = "Las Vegas"
        val shortName = "LAS (All-Airports)"
        location.regionNames = SuggestionV4.RegionNames()
        location.regionNames.displayName = displayName
        location.regionNames.shortName= shortName
        location.coordinates = SuggestionV4.LatLng()
        location.coordinates.lat = 36.1699
        location.coordinates.lng = -115.1398
        location.hierarchyInfo = SuggestionV4.HierarchyInfo()
        location.hotelId = hotelId
        location.type =  ""
        return location
    }

    private fun goToHotels(context: Context, sp: HotelSearchParams) {
        HotelNavUtils.goToHotelsV2Params(context, sp, null, NavUtils.FLAG_DEEPLINK)
        val activity = context as AppCompatActivity
        activity.finish()
    }
}