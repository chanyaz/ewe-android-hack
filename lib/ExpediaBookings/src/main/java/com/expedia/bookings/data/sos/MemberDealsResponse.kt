package com.expedia.bookings.data.sos

import com.expedia.bookings.data.BaseDealsResponse

open class MemberDealsResponse : BaseDealsResponse() {
    var destinations: List<DealsDestination> = emptyList()
        protected set
}
