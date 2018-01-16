package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale

class HotelNameEntryView(context: Context, attrs: AttributeSet?) : NameEntryView(context, attrs) {

    override fun getLayout(): Int {
        return if (PointOfSale.getPointOfSale().showLastNameFirst()) {
            R.layout.material_reversed_name_entry_view
        } else {
            R.layout.material_name_entry_view_no_middle_name
        }
    }
}
