package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class HotelReviewsApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path

        if (!HotelReviewsApiRequestMatcher.isHotelReviewsRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            HotelReviewsApiRequestMatcher.isHotelReviewsSummaryRequest(urlPath) -> {
                val fileName = if (isUnhappy(urlPath)) "unhappy-summaries" else "happy-summaries"
                return getMockResponse("api/hotelreviews/hotel/$fileName.json")
            }

            HotelReviewsApiRequestMatcher.isHotelReviewsSearchRequest(urlPath) -> {
                val fileName = if (urlPath.contains("PrivateBank")) "happy-search" else "unhappy-search"
                return getMockResponse("api/hotelreviews/hotel/$fileName.json")
            }

            HotelReviewsApiRequestMatcher.isHotelReviewsDetailsRequest(urlPath) -> {
                return getMockResponse("api/hotelreviews/hotel/happy.json")
            }

            HotelReviewsApiRequestMatcher.isHotelReviewTranslationRequest(urlPath) -> {
                return getMockResponse("api/hotelreview/translate/happy.json")
            }
            else -> make404()
        }
    }

    private fun isUnhappy(path: String): Boolean {
        return path.contains("unhappy-summaries")
    }
}

class HotelReviewsApiRequestMatcher {
    companion object {
        fun isHotelReviewsRequest(path: String): Boolean = path.startsWith("/api/hotelreview")

        fun isHotelReviewsSummaryRequest(path: String): Boolean = path.startsWith("/api/hotelreviews") && path.contains("summary")

        fun isHotelReviewsSearchRequest(path: String): Boolean = path.startsWith("/api/hotelreviews") && path.contains("searchTerm")

        fun isHotelReviewsDetailsRequest(path: String): Boolean = path.startsWith("/api/hotelreviews")

        fun isHotelReviewTranslationRequest(path: String): Boolean = path.contains(Regex("/api/hotelreview/\\w+/\\w\\w"))

    }
}
