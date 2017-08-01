package com.expedia.bookings.widget.itin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class HotelItinRoomDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val roomDetailsText: TextView by bindView(R.id.itin_hotel_details_room_details_text)
    val roomDetailsChevron: ImageView by bindView(R.id.itin_hotel_room_details_chevron)
    val expandedRoomDetails: LinearLayout by bindView(R.id.expanded_room_details_container)
    val collapsedRoomDetails: LinearLayout by bindView(R.id.itin_hotel_details_room_collapsed_view)
    var isRowClickable = true

    init {
        View.inflate(context, R.layout.widget_hotel_itin_room_details, this)
        collapsedRoomDetails.setOnClickListener {
            if (!isRowClickable) {
                return@setOnClickListener
            }
            else {
                if(expandedRoomDetails.visibility == View.GONE) {
                    expandRoomDetailsView()
                }
                else
                {
                    collapseRoomDetailsView()
                }
            }
        }
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.isSharedItin) {
            visibility = View.GONE
        }
        else {
            roomDetailsText.text = Phrase.from(context, R.string.itin_hotel_details_room_details_text_TEMPLATE)
                    .put("roomtype", itinCardDataHotel.property.itinRoomType)
                    .put("bedtype", itinCardDataHotel.property.itinBedType).format().toString()
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
}
