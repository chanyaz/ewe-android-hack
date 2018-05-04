package com.expedia.bookings.utils

class CountryCodeUtil {

    companion object {

        @JvmField
        val countryCodes = hashSetOf(93,
                355,
                213,
                1,
                376,
                244,
                672,
                54,
                374,
                297,
                61,
                43,
                994,
                973,
                880,
                375,
                32,
                501,
                229,
                975,
                591,
                387,
                267,
                55,
                246,
                673,
                359,
                226,
                257,
                855,
                237,
                238,
                236,
                235,
                56,
                86,
                61,
                61,
                57,
                269,
                242,
                243,
                682,
                506,
                385,
                357,
                420,
                225,
                45,
                253,
                593,
                20,
                503,
                240,
                291,
                372,
                251,
                500,
                298,
                679,
                358,
                33,
                594,
                689,
                241,
                220,
                995,
                49,
                233,
                350,
                30,
                299,
                590,
                502,
                44,
                224,
                245,
                592,
                509,
                379,
                504,
                852,
                36,
                354,
                91,
                62,
                98,
                964,
                353,
                44,
                972,
                39,
                81,
                44,
                962,
                7,
                254,
                686,
                850,
                82,
                965,
                996,
                856,
                371,
                961,
                266,
                231,
                218,
                423,
                370,
                352,
                853,
                389,
                261,
                265,
                60,
                960,
                223,
                356,
                692,
                596,
                222,
                230,
                262,
                52,
                691,
                373,
                377,
                976,
                382,
                212,
                258,
                95,
                264,
                674,
                977,
                31,
                599,
                687,
                64,
                505,
                227,
                234,
                683,
                672,
                47,
                968,
                92,
                680,
                970,
                507,
                675,
                595,
                51,
                63,
                870,
                48,
                351,
                974,
                40,
                7,
                250,
                262,
                590,
                290,
                590,
                508,
                685,
                378,
                239,
                966,
                221,
                381,
                248,
                232,
                65,
                421,
                386,
                677,
                252,
                27,
                500,
                34,
                94,
                249,
                597,
                47,
                268,
                46,
                41,
                963,
                886,
                992,
                255,
                66,
                670,
                228,
                690,
                676,
                216,
                90,
                993,
                688,
                256,
                380,
                971,
                44,
                699,
                598,
                998,
                678,
                58,
                84,
                681,
                212,
                967,
                260,
                263,
                358)

        @JvmStatic
        fun getCountryCode(numberString: String): String {
            var countryCodeRangeToUse = 1..3
            var getCountryCodeStartIndex = 0
            if (numberString.length <= 10) return ""

            if (numberString.substring(0, 1) == "+") {
                countryCodeRangeToUse = 2..4
                getCountryCodeStartIndex = 1
            }
            (countryCodeRangeToUse).map { i ->
                val countryCode = numberString.substring(getCountryCodeStartIndex, i).toInt()
                if (countryCodes.contains(countryCode)) return countryCode.toString()
            }
            return ""
        }
    }
}
