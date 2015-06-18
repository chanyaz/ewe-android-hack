package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.cars.Suggestion
import org.joda.time.LocalDate

/**
 * Created by malnguyen on 6/18/15.
 */

public class HotelSearchParams {
    var location : Suggestion? = null
    var checkIn: LocalDate? = null
    var checkOut: LocalDate? = null
    var mChildren: String? = null
    var mNumAdults: Int = 0

    public fun getRoomParam() : String {
        var roomParam : String = mNumAdults.toString()
        if (mChildren != "") {
            roomParam += "," + mChildren
        }

        return roomParam
    }
}