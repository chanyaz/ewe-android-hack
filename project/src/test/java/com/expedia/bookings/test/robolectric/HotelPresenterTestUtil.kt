package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import org.joda.time.LocalDate

class HotelPresenterTestUtil {

    companion object {

        @JvmStatic
        fun getDummyHotelSearchParams(context: Context): HotelSearchParams {
            return HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                    context.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
                    .destination(getDummySuggestion())
                    .adults(2)
                    .children(listOf(10, 10, 10))
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(1)).build() as HotelSearchParams
        }

        @JvmStatic
        fun getDummySuggestion(): SuggestionV4 {
            val suggestion = SuggestionV4()
            suggestion.gaiaId = ""
            suggestion.regionNames = SuggestionV4.RegionNames()
            suggestion.regionNames.displayName = ""
            suggestion.regionNames.fullName = ""
            suggestion.regionNames.shortName = ""
            return suggestion
        }
    }
}
