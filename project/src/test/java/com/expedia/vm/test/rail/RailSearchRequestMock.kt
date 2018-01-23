package com.expedia.vm.test.rail

import com.expedia.bookings.data.SuggestionV4
import org.joda.time.LocalDate

object RailSearchRequestMock {
    @JvmStatic fun origin(regionShortName: String): SuggestionV4 {
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = regionShortName
        val suggestion = SuggestionV4()
        suggestion.regionNames = regionName
        return suggestion
    }

    @JvmStatic fun destination(regionShortName: String): SuggestionV4 {
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = regionShortName
        val suggestion = SuggestionV4()
        suggestion.regionNames = regionName
        return suggestion
    }

    @JvmStatic fun departDate(): LocalDate {
        return LocalDate()
    }

    @JvmStatic fun returnDate(): LocalDate {
        return LocalDate()
    }

    @JvmStatic fun departTime(): Int {
        return 0
    }
}
