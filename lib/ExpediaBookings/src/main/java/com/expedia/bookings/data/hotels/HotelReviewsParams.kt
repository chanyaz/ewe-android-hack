package com.expedia.bookings.data.hotels

public class HotelReviewsParams(val hotelId: String, val sortBy: String, val pageNumber: Int, val numReviewsPerPage: Int) {

    class Builder {
        private var hotelId: String? = null
        private var sortBy: String? = null
        private var pageNumber: Int? = null
        private var numReviewsPerPage: Int? = null

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


        fun build(): HotelReviewsParams {
            return HotelReviewsParams(hotelId ?: throw IllegalArgumentException(),
                    sortBy ?: throw IllegalArgumentException(),
                    pageNumber ?: throw IllegalArgumentException(),
                    numReviewsPerPage ?: throw java.lang.IllegalArgumentException())
        }
    }
}