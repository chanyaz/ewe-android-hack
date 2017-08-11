package com.expedia.bookings.widget.itin

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.OccupantSelectedRoomOptions
import com.expedia.bookings.data.trips.TripHotel
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
    val reservedFor: TextView by bindView(R.id.itin_hotel_details_reserved_for)
    val guestName: TextView by bindView(R.id.itin_hotel_details_guest_name)
    val amenitiesContainer: LinearLayout by bindView(R.id.itin_hotel_room_amenities_container)
    val amenitiesDivider: View by bindView(R.id.amenities_summary_divider)
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
                reservedForDetails(itinCardDataHotel.lastHotelRoom)
                setUpAmenities(itinCardDataHotel.lastHotelRoom)
            }
        }
    }

    private fun reservedForDetails(tripHotelRoom: TripHotelRoom) {
        reservedFor.text = context.getString(R.string.itin_hotel_room_details_reserved_for_header)

        val fullName = tripHotelRoom.primaryOccupant?.fullName
        val guestCount = numberOfGuests(tripHotelRoom)

        if (fullName.isNullOrEmpty() && guestCount.isEmpty()) {
            reservedFor.visibility = View.GONE
            guestName.visibility = View.GONE
        } else if (fullName.isNullOrEmpty()) {
            guestName.text = numberOfGuests(tripHotelRoom)
        } else if (guestCount.isEmpty()) {
            guestName.text = tripHotelRoom.primaryOccupant?.fullName
        } else {
            guestName.text = Phrase.from(context, R.string.itin_hotel_room_details_guest_info_TEMPLATE)
                    .put("guestname", tripHotelRoom.primaryOccupant?.fullName)
                    .put("numofguests", numberOfGuests(tripHotelRoom)).format().toString()
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

        if (occupantSelectedRoomOptions != null) {
            if (occupantSelectedRoomOptions.isSmokingPreferenceSelected) {
                if (occupantSelectedRoomOptions.smokingPreference == "NONSMOKING_ROOM") {
                    list.add(context.getString(R.string.non_smoking))
                }
                if (occupantSelectedRoomOptions.smokingPreference == "SMOKING_ROOM") {
                    list.add(context.getString(R.string.smoking))
                }
            }
            if (occupantSelectedRoomOptions.bedTypeName.isNotEmpty()) {
                list.add(occupantSelectedRoomOptions.bedTypeName)
            }
            if (occupantSelectedRoomOptions.accessibilityOptions.isNotEmpty()) {
                list.add(getAccessibleOptionList(occupantSelectedRoomOptions.accessibilityOptions))
            }
            if (occupantSelectedRoomOptions.specialRequest.isNotEmpty()) {
                list.add("\"" + occupantSelectedRoomOptions.specialRequest + "\"")
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
        }

        return list
    }

    private fun getAccessibleOptionList(accessibleOptions: List<String>): String {
        val formattedList = TextUtils.join(", ", accessibleOptions).toString()
        return formattedList
    }

    private fun numberOfGuests(tripHotelRoom: TripHotelRoom): String {
        val list = ArrayList<String>()
        val otherOccupantsInfo = tripHotelRoom.otherOccupantInfo

        if (otherOccupantsInfo != null) {
            if (otherOccupantsInfo.adultCount > 0) {
                list.add(context.resources.getQuantityString(R.plurals.number_of_adults, otherOccupantsInfo.adultCount, otherOccupantsInfo.adultCount).toLowerCase())
            }
            if (otherOccupantsInfo.childCount > 0) {
                list.add(context.resources.getQuantityString(R.plurals.number_of_children, otherOccupantsInfo.childCount, otherOccupantsInfo.childCount).toLowerCase())
            }

            if (otherOccupantsInfo.infantCount > 0) {
                list.add(context.resources.getQuantityString(R.plurals.number_of_infant, otherOccupantsInfo.infantCount, otherOccupantsInfo.infantCount).toLowerCase())
            }
        }
        val formattedList = TextUtils.join(", ", list)
        return formattedList
    }

    fun setUpAmenities(tripHotelRoom: TripHotelRoom) {
        val breakfastAmenityIds = listOf(2, 4, 8, 4096, 8192, 16777216)
        val amenities = tripHotelRoom.amenityIds
        var showAmenities: Boolean = false
        amenitiesContainer.removeAllViews()
        amenities.map {
            if (it in breakfastAmenityIds) breakfastAmenityIds[0] else it
        }.toSet().forEach {
            val amenity = HotelItinRoomAmenity(context)
            when (it) {
                2 -> {
                        amenity.setUp(context.resources.getString(R.string.itin_hotel_room_amenity_free_breakfast),
                                R.drawable.itin_hotel_room_free_breakfast,
                                context.resources.getString(R.string.itin_hotel_room_amenity_free_breakfast_cont_desc))
                        amenitiesContainer.addView(amenity)
                        showAmenities = true
                }
                128 -> {
                    amenity.setUp(context.resources.getString(R.string.itin_hotel_room_amenity_free_parking),
                            R.drawable.itin_hotel_room_parking_icon,
                            context.resources.getString(R.string.itin_hotel_room_amenity_free_parking_cont_desc))
                    amenitiesContainer.addView(amenity)
                    showAmenities = true
                }
                2048 -> {
                    amenity.setUp(context.resources.getString(R.string.itin_hotel_room_amenity_free_wifi),
                            R.drawable.itin_hotel_free_wifi,
                            context.resources.getString(R.string.itin_hotel_room_amenity_free_wifi_cont_desc))
                    amenitiesContainer.addView(amenity)
                    showAmenities = true
                }
            }
        }
        if (!showAmenities) {
            amenitiesContainer.visibility = View.GONE
            amenitiesDivider.visibility = View.GONE
        }
    }
}
