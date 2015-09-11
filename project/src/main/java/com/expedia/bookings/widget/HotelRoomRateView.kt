package com.expedia.bookings.widget

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.*
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribe
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelRoomRateViewModel
import rx.Observer

public class HotelRoomRateView(context: Context, val container: TableLayout, val selectedRoomObserver: Observer<HotelOffersResponse.HotelRoomResponse>) : LinearLayout(context) {

    val PICASSO_HOTEL_ROOM = "HOTEL_ROOMS"

    //views for room row
    val roomType: TextView by bindView(R.id.room_type_text_view)
    val collapsedBedType: TextView by bindView(R.id.collapsed_bed_type_text_view)
    val collapsedUrgency : TextView by bindView(R.id.collapsed_urgency_text_view)
    val expandedBedType: TextView by bindView(R.id.expanded_bed_type_text_view)
    val dailyPricePerNight: TextView by bindView(R.id.daily_price_per_night)
    val totalPricePerNight: TextView by bindView(R.id.total_price_per_night)
    val viewRoom: ToggleButton by bindView (R.id.view_room_button)
    val roomHeaderImage: ImageView by bindView(R.id.room_header_image)
    val roomInformationText: TextView by bindView(R.id.room_info_description_text)
    val roomInfoContainer: RelativeLayout by bindView(R.id.room_info_container)
    val row: View  by bindView(R.id.root)
    val expandedAmenity: TextView by bindView(R.id.expanded_amenity_text_view)
    val freeCancellation: TextView by bindView(R.id.expanded_free_cancellation_text_view)
    val roomInfoHeader: TextView by bindView(R.id.room_info_header_text)
    val divider : View by bindView(R.id.divider)


    var viewmodel: HotelRoomRateViewModel by notNullAndObservable { vm ->
        expandedAmenity.setVisibility(View.GONE)
        viewRoom.subscribeOnCheckChanged(vm.expandCollapseRoomRate)
        vm.roomSelectedObservable.subscribe(selectedRoomObserver)
        roomInfoContainer.subscribeOnClick(vm.expandCollapseRoomRateInfo)

        vm.totalPricePerNightObservable.subscribe(totalPricePerNight)
        vm.roomRateInfoTextObservable.subscribe(roomInformationText)
        vm.roomTypeObservable.subscribe(roomType)
        vm.collapsedBedTypeObservable.subscribe(collapsedBedType)
        vm.expandedBedTypeObservable.subscribe(expandedBedType)
        vm.expandedAmenityObservable.subscribe { text ->
            expandedAmenity.setVisibility(View.VISIBLE)
            expandedAmenity.setText(text)
        }

        vm.collapsedUrgencyObservable.subscribe(collapsedUrgency)
        vm.expandedMessageObservable.subscribe { expandedMessagePair ->
            freeCancellation.setText(expandedMessagePair.first)
            freeCancellation.setCompoundDrawablesWithIntrinsicBounds(expandedMessagePair.second, null, null, null)
        }

        vm.dailyPricePerNightObservable.subscribe(dailyPricePerNight)
        vm.roomHeaderImageObservable.subscribe { imageUrl ->
            val margin = getContext().getResources().getDimension(R.dimen.hotel_room_list_container_margin) * 2 // margin from left and right
            val drawable = Images.makeHotelBitmapDrawable(getContext(), emptyPicassoCallback, container.getWidth() - margin.toInt(), imageUrl, PICASSO_HOTEL_ROOM)
            roomHeaderImage.setImageDrawable(drawable)
        }
        vm.expandRoomObservable.subscribe { expand ->
            val anim: TranslateAnimation = TranslateAnimation(0.0f, 0.0f, row.getHeight().toFloat(), 0.0f);

            roomInfoContainer.setVisibility(if (expand) View.VISIBLE else View.GONE)
            collapsedBedType.setVisibility(if (expand) View.GONE else View.VISIBLE)
            collapsedUrgency.setVisibility(if (expand) View.GONE else View.VISIBLE)
            expandedBedType.setVisibility(if (expand) View.VISIBLE else View.GONE)
            expandedAmenity.setVisibility(if (expand && Strings.isNotEmpty(expandedAmenity.getText())) View.VISIBLE else View.GONE)
            freeCancellation.setVisibility(if (expand) View.VISIBLE else View.GONE)
            totalPricePerNight.setVisibility(if (expand) View.VISIBLE else View.GONE)
            roomHeaderImage.setVisibility(if (expand) View.VISIBLE else View.GONE)
            if (expand) {
                dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                dailyPricePerNight.setTextColor(getResources().getColor(R.color.hotels_primary_color))
                divider.setVisibility(View.VISIBLE)
            } else {
                dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                dailyPricePerNight.setTextColor(getResources().getColor(R.color.hotel_cell_disabled_text))
                divider.setVisibility(View.GONE)
            }

            viewRoom.setChecked(if (expand) true else false)

            if (expand) {
                row.startAnimation(anim)
            }
        }

        vm.collapseRoomObservable.subscribe { collapseIndex ->
            var row: View = container.getChildAt(collapseIndex)
            val roomHeaderImage: View = row.findViewById(R.id.room_header_image)
            val roomInfoContainer = row.findViewById(R.id.room_info_container) as RelativeLayout
            val viewRoom = row.findViewById (R.id.view_room_button) as ToggleButton
            val collapsedBedType = row.findViewById (R.id.collapsed_bed_type_text_view) as TextView
            val collapsedUrgency = row.findViewById (R.id.collapsed_urgency_text_view) as TextView
            val expandedBedType = row.findViewById (R.id.expanded_bed_type_text_view) as TextView
            val expandedAmenity = row.findViewById (R.id.expanded_amenity_text_view) as TextView
            val freeCancellation = row.findViewById (R.id.expanded_free_cancellation_text_view) as TextView
            val totalPricePerNight = row.findViewById(R.id.total_price_per_night) as TextView
            val divider = row.findViewById(R.id.divider)
            Ui.setViewBackground(row.findViewById(R.id.root), null)

            viewRoom.setChecked(false)
            roomHeaderImage.setVisibility(View.GONE)
            roomInfoContainer.setVisibility(View.GONE)
            collapsedBedType.setVisibility(View.VISIBLE)
            collapsedUrgency.setVisibility(View.VISIBLE)
            expandedBedType.setVisibility(View.GONE)
            expandedAmenity.setVisibility(View.GONE)
            freeCancellation.setVisibility(View.GONE)
            totalPricePerNight.setVisibility(View.GONE)
            divider.setVisibility(View.GONE)
        }

        vm.roomInfoObservable.subscribe { visibility ->
            roomInformationText.setVisibility(visibility)
        }

        vm.roomBackgroundViewObservable.subscribe { drawable ->
            row.setBackground(drawable)
        }
    }

    init {
        View.inflate(getContext(), R.layout.hotel_room_row, this)
    }
}

val emptyPicassoCallback = object : HeaderBitmapDrawable.CallbackListener {
    override fun onBitmapLoaded() {
    }

    override fun onBitmapFailed() {
    }

    override fun onPrepareLoad() {
    }
}



