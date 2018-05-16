package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class HotelShortlistApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path

        return when {
            HotelShortlistApiRequestMatcher.isHotelShortlistFetchRequest(path) ->
                getMockResponse("api/hotelShortlist/hotelShortlistFetchResponse.json")
            HotelShortlistApiRequestMatcher.isHotelShortlistSaveRequest(path) ->
                getMockResponse("api/hotelShortlist/hotelShortlistSaveResponse.json")
            else ->
                make404()
        }
    }
}

class HotelShortlistApiRequestMatcher {
    companion object {
        fun isHotelShortlistRequest(urlPath: String): Boolean  {
            return urlPath.startsWith("/api/ucs/shortlist/")
        }

        fun isHotelShortlistFetchRequest(urlPath: String): Boolean {
            return urlPath.startsWith("/api/ucs/shortlist/detail/fetch")
        }

        fun isHotelShortlistSaveRequest(urlPath: String): Boolean {
            return urlPath.startsWith("/api/ucs/shortlist/save/")
        }
    }
}
