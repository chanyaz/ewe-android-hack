package com.expedia.bookings.widget.itin

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripHotelRoom
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class HotelItinRoomDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    @VisibleForTesting
    val roomDetailsText: TextView by bindView(R.id.itin_hotel_details_room_details_text)
    val roomDetailsChevron: ImageView by bindView(R.id.itin_hotel_room_details_chevron)
    val expandedRoomDetails: LinearLayout by bindView(R.id.expanded_room_details_container)
    val roomRequestsText: TextView by bindView(R.id.hotel_itin_room_details_requests)
    var isRowClickable = true

    private val collapsedRoomDetails: LinearLayout by bindView(R.id.itin_hotel_details_room_collapsed_view)


    init {
        View.inflate(context, R.layout.widget_hotel_itin_room_details, this)
        if (isExpandableRoomDetailsOn()) {
            collapsedRoomDetails.setOnClickListener {
                if (!isRowClickable) {
                    return@setOnClickListener
                } else {
                    if (expandedRoomDetails.visibility == View.GONE) {
                        expandRoomDetailsView()
                    } else {
                        collapseRoomDetailsView()
                    }
                }
            }
        }
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.isSharedItin) {
            visibility = View.GONE
        } else {
            roomDetailsText.text = Phrase.from(context, R.string.itin_hotel_details_room_details_text_TEMPLATE)
                    .put("roomtype", itinCardDataHotel.property.itinRoomType)
                    .put("bedtype", itinCardDataHotel.property.itinBedType).format().toString()
            if (itinCardDataHotel.lastHotelRoom != null) {
                roomRequestsText.text = buildRoomRequests(itinCardDataHotel.lastHotelRoom)
            }
        }
    }

    fun expandRoomDetailsView() {
        expandedRoomDetails.visibility = View.VISIBLE
        AnimUtils.rotate(roomDetailsChevron)
    }

    fun collapseRoomDetailsView() {
        expandedRoomDetails.visibility = View.GONE
        AnimUtils.reverseRotate(roomDetailsChevron)
        roomDetailsChevron.clearAnimation()
    }

    fun isExpandableRoomDetailsOn(): Boolean {
        if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_expandable_hotel_itin_room_details)) {
            return true
        }
        return false
    }

    private fun buildRoomRequests(tripHotelRoom: TripHotelRoom): String {
        val requestString = ArrayList<String>()
        if (gatherRequestStrings(tripHotelRoom).isNotEmpty()) {
            val requests = gatherRequestStrings(tripHotelRoom)
            for (request in requests) {
                requestString.add(request)
            }
            val formattedList = TextUtils.join("\n", requestString)
            return formattedList.toString()
        } else {
            return context.getString(R.string.itin_hotel_room_details_no_requests_selected)
        }
    }

    private fun gatherRequestStrings(tripHotelRoom: TripHotelRoom): List<String> {
        val list = ArrayList<String>()
        val occupantSelectedRoomOptions = tripHotelRoom.occupantSelectedRoomOptions

        if (occupantSelectedRoomOptions.isSmokingPreferenceSelected) {
            list.add(occupantSelectedRoomOptions.smokingPreference)
        }
        if (occupantSelectedRoomOptions.bedTypeName != "") {
            list.add(occupantSelectedRoomOptions.bedTypeName)
        }
        if (occupantSelectedRoomOptions.accessibilityOptions != emptyList<String>()) {
            list.add(getAccessibleOptionList(tripHotelRoom))
        }
        if (occupantSelectedRoomOptions.specialRequest != "") {
            val strbuilder = StringBuilder()
            val specialRequestFormatted = strbuilder.append("\"" + occupantSelectedRoomOptions.specialRequest + "\"")
            list.add(specialRequestFormatted.toString())
        }
        if (occupantSelectedRoomOptions.hasExtraBedAdult) {
            list.add(context.getString(R.string.itin_hotel_room_request_extra_adult_bed))
        }
        if (occupantSelectedRoomOptions.hasExtraBedChild) {
            list.add(context.getString(R.string.itin_hotel_room_request_extra_child_bed))
        }
        if (occupantSelectedRoomOptions.hasExtraBedInfant) {
            list.add(context.getString(R.string.itin_hotel_room_request_extra_infant_bed))
        }

        return list
    }

    private fun getAccessibleOptionList(tripHotelRoom: TripHotelRoom): String {
        val accessibleOptions = tripHotelRoom.occupantSelectedRoomOptions.accessibilityOptions
        val accessibleList = ArrayList<String>()
        for (option in accessibleOptions) {
            accessibleList.add(option)
        }
        val formattedList = TextUtils.join(", ", accessibleList).toString()
        return formattedList
    }
}
