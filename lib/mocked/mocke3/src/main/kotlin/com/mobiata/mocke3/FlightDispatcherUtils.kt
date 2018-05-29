package com.mobiata.mocke3

import java.util.HashMap

class FlightDispatcherUtils {

    enum class SearchResultsResponseType(val responseName: String) {
        HAPPY_ONE_WAY("happy_one_way"),
        BYOT_ROUND_TRIP("byot_search"),
        HAPPY_ROUND_TRIP("happy_round_trip"),
        HAPPY_ROUND_TRIP_WITH_INSURANCE_AVAILABLE("happy_round_trip_with_insurance_available"),
        CREATE_TRIP_PRICE_CHANGE("create_trip_price_change")
    }

    enum class SuggestionResponseType(val suggestionString: String) {
        HAPPY_PATH("happy"),
        BYOT_ROUND_TRIP("byot_search"),
        PASSPORT_NEEDED("passport_needed"),
        MAY_CHARGE_OB_FEES("may_charge_ob_fees"),
        SEARCH_ERROR("search_error"),
        EARN("earn"),
        CACHED_BOOKABLE("cached_bookable"),
        CACHED_NON_BOOKABLE("cached_non_bookable"),
        CACHED_NOT_FOUND("cached_not_found");


        companion object {

            private val suggestionResponseTypeMap = HashMap<String, SuggestionResponseType>()

            init {
                for (suggestionResponseType in SuggestionResponseType.values()) {
                    suggestionResponseTypeMap.put(suggestionResponseType.suggestionString, suggestionResponseType)
                }
            }

            fun getValueOf(fileName: String): SuggestionResponseType {
                return suggestionResponseTypeMap[fileName] ?: HAPPY_PATH
            }
        }
    }
}
