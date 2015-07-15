package com.expedia.bookings.utils

import android.content.Context
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TableLayout
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.widget.TextView
import kotlin.properties.Delegates

public class HotelRoomRateRowUtils(val context: Context, container: TableLayout) : HeaderBitmapDrawable.CallbackListener {

    var mainContainer :TableLayout by Delegates.notNull()
    var lastExpanded = 0
    //views for room row
    val PICASSO_HOTEL_ROOM = "HOTEL_ROOMS"

    var row: View by Delegates.notNull()
    var imageView: View by Delegates.notNull()
    var roomInfoContainer : RelativeLayout by Delegates.notNull()
    var viewRoom :ToggleButton by Delegates.notNull()
    var collapsedBedType : TextView by Delegates.notNull()
    var expandedBedType : TextView by Delegates.notNull()
    var expandedAmenity : TextView by Delegates.notNull()
    var freeCancellation : TextView by Delegates.notNull()
    var totalPricePerNight : TextView by Delegates.notNull()

    init {
        mainContainer = container
    }

    fun bindDetails(roomResponseList: List<HotelOffersResponse.HotelRoomResponse>) {
        for (roomResponseIndex in 0..roomResponseList.size() - 1) {
            val tableRow = View.inflate(context, R.layout.hotel_room_row, null)
            val response = roomResponseList.get(roomResponseIndex)
            val roomType = tableRow.findViewById (R.id.room_type_text_view) as TextView
            val collapsedBedType = tableRow.findViewById (R.id.collapsed_bed_type_text_view) as TextView
            val expandedBedType = tableRow.findViewById (R.id.expanded_bed_type_text_view) as TextView
            val dailyPricePerNight = tableRow.findViewById (R.id.daily_price_per_night) as TextView
            val totalPricePerNight = tableRow.findViewById (R.id.total_price_per_night) as TextView
            val viewRoom = tableRow.findViewById (R.id.view_room_button) as ToggleButton
            val roomHeaderImage = tableRow.findViewById(R.id.room_header_image) as ImageView
            val roomInformationText = tableRow.findViewById (R.id.room_info_description_text) as TextView
            val roomInfoContainer = tableRow.findViewById (R.id.room_info_container) as RelativeLayout
            val roomInfoHeader = tableRow.findViewById(R.id.room_info_header_text) as TextView

            viewRoom.setOnClickListener { view ->
                val isChecked = viewRoom.isChecked()
                if (!isChecked) {
                    //TODO :show checkout
                } else {
                    updateState(isChecked, roomResponseIndex);
                }
            }

            roomInfoHeader.setOnClickListener { view ->
                if (roomInformationText.getVisibility() == View.VISIBLE) {
                    roomInformationText.setVisibility(View.GONE)
                } else {
                    roomInformationText.setVisibility(View.VISIBLE)
                }
            }

            roomType.setText(response.roomTypeDescription)
            val currencyCode = response.rateInfo.chargeableRateInfo.currencySymbol

            dailyPricePerNight.setText(currencyCode + response.rateInfo.chargeableRateInfo.averageRate.toInt() + context.getResources().getString(R.string.per_night));
            totalPricePerNight.setText(context.getResources().getString(R.string.cars_total_template, currencyCode + response.rateInfo.chargeableRateInfo.total))

            var bedTypeList: List<HotelOffersResponse.BedTypes> = response.bedTypes
            val sb = StringBuilder()
            for (bedTypeIndex in 0..bedTypeList.size() - 1) {
                sb.append(response.bedTypes.get(bedTypeIndex).description)
            }
            collapsedBedType.setText(sb.toString())
            expandedBedType.setText(sb.toString())

            if (roomResponseIndex == 0) {
                val expandedAmenity = tableRow.findViewById (R.id.expanded_amenity_text_view) as TextView
                val freeCancellation = tableRow.findViewById (R.id.expanded_free_cancellation__text_view) as TextView
                collapsedBedType.setVisibility(View.GONE)
                expandedBedType.setVisibility(View.VISIBLE)
                expandedAmenity.setVisibility(View.VISIBLE)
                freeCancellation.setVisibility(View.VISIBLE)
                totalPricePerNight.setVisibility(View.VISIBLE)

            }

            roomInformationText.setText(response.roomLongDescription)

            //calculating image width
            val margin = context.getResources().getDimension(R.dimen.hotel_room_list_container_margin) * 2 // margin from left and right
            val drawable = Images.makeHotelBitmapDrawable(context, this, mainContainer.getWidth() - margin.toInt(), Images.getMediaHost() + response.roomThumbnailUrl, PICASSO_HOTEL_ROOM)
            roomHeaderImage.setImageDrawable(drawable)

            if (roomResponseIndex != 0) {
                roomHeaderImage.setVisibility(View.GONE)
                roomInfoContainer.setVisibility(View.GONE)
                totalPricePerNight.setVisibility(View.GONE)
            } else {
                viewRoom.setChecked(true)
                Ui.setViewBackground(tableRow.findViewById(R.id.root), context.getResources().getDrawable(R.drawable.card_background))
            }

            mainContainer.addView(tableRow)
        }

    }

    public fun updateState(isChecked: Boolean, roomToBeExpanded: Int) {
        // collapse the last expanded room
        collapseRoomsRates(roomToBeExpanded);
        initializeViews(roomToBeExpanded)
        Ui.setViewBackground(row.findViewById(R.id.root), context.getResources().getDrawable(R.drawable.card_background))

        if (isChecked) {
            val anim: TranslateAnimation = TranslateAnimation(0.0f, 0.0f, row.getHeight().toFloat(), 0.0f);
            imageView.setVisibility(View.VISIBLE);
            roomInfoContainer.setVisibility(View.VISIBLE)
            collapsedBedType.setVisibility(View.GONE)
            expandedBedType.setVisibility(View.VISIBLE)
            expandedAmenity.setVisibility(View.VISIBLE)
            freeCancellation.setVisibility(View.VISIBLE)
            totalPricePerNight.setVisibility(View.VISIBLE)
            row.startAnimation(anim);
            lastExpanded = roomToBeExpanded
        }
    }

    private fun collapseRoomsRates(roomToBeExpanded : Int) {
        initializeViews(lastExpanded)
        Ui.setViewBackground(row.findViewById(R.id.root), null)

        if (lastExpanded != roomToBeExpanded) {
            viewRoom.setChecked(false)
            imageView.setVisibility(View.GONE)
            roomInfoContainer.setVisibility(View.GONE)
            collapsedBedType.setVisibility(View.VISIBLE)
            expandedBedType.setVisibility(View.GONE)
            expandedAmenity.setVisibility(View.GONE)
            freeCancellation.setVisibility(View.GONE)
            totalPricePerNight.setVisibility(View.GONE)
        }

    }

    public fun initializeViews(index: Int) {
        row = mainContainer.getChildAt(index)
        imageView: View = row.findViewById(R.id.room_header_image)
        roomInfoContainer = row.findViewById(R.id.room_info_container) as RelativeLayout
        viewRoom = row.findViewById (R.id.view_room_button) as ToggleButton
        collapsedBedType = row.findViewById (R.id.collapsed_bed_type_text_view) as TextView
        expandedBedType = row.findViewById (R.id.expanded_bed_type_text_view) as TextView
        expandedAmenity = row.findViewById (R.id.expanded_amenity_text_view) as TextView
        freeCancellation = row.findViewById (R.id.expanded_free_cancellation__text_view) as TextView
        totalPricePerNight = row.findViewById(R.id.total_price_per_night) as TextView
    }

    override fun onBitmapLoaded() {
    }

    override fun onBitmapFailed() {
    }

    override fun onPrepareLoad() {
    }

}