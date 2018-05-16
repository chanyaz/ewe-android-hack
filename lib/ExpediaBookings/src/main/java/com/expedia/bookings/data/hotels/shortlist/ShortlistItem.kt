package com.expedia.bookings.data.hotels.shortlist

data class ShortlistItem(
        var id: String? = null,
        var itemId: String? = null,
        var configId: String? = null,
        var guid: String? = null,
        var uid: String? = null,
        var expUserId: String? = null,
        var siteId: String? = null,
        var metadata: ShortlistItemMetadata? = null,
        var lastModifiedDate: LastModifiedDate? = null)
