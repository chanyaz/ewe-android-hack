package com.expedia.bookings.data.hotels

class HotelReviewsParams(
        val hotelId: String, val sortBy: String, val pageNumber: Int, val numReviewsPerPage: Int, val locale: String?, val searchTerm: String?) {

    class Builder {
        private var hotelId: String? = null
        private var sortBy: String? = null
        private var pageNumber: Int? = null
        private var numReviewsPerPage: Int? = null
        private var locale: String? = null
        private var searchTerm: String? = null

        fun hotelId(hotelId: String?): HotelReviewsParams.Builder {
            this.hotelId = hotelId
            return this
        }

        fun sortBy(sortBy: String?): HotelReviewsParams.Builder {
            this.sortBy = sortBy
            return this
        }

        fun pageNumber(pageNumber: Int?): HotelReviewsParams.Builder {
            this.pageNumber = pageNumber
            return this
        }

        fun numReviewsPerPage(numReviewsPerPage: Int?): HotelReviewsParams.Builder {
            this.numReviewsPerPage = numReviewsPerPage
            return this
        }

        fun locale(locale: String?): HotelReviewsParams.Builder {
            this.locale = locale
            return this
        }

        fun searchTerm(searchTerm: String?): HotelReviewsParams.Builder {
            this.searchTerm = searchTerm
            return this
        }

        fun build(): HotelReviewsParams {
            return HotelReviewsParams(hotelId ?: throw IllegalArgumentException(),
                    sortBy ?: throw IllegalArgumentException(),
                    pageNumber ?: throw IllegalArgumentException(),
                    numReviewsPerPage ?: throw IllegalArgumentException(),
                    locale,
                    searchTerm)
        }
    }
}
