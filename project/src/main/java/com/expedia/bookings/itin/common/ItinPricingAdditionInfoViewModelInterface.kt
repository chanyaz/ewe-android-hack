package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

interface ItinPricingAdditionInfoViewModelInterface {
    val toolbarTitleSubject: PublishSubject<String>
    val additionalInfoItemSubject: PublishSubject<List<ItinAdditionalInfoItem>>
}

data class ItinAdditionalInfoItem(val heading: String, val content: String)
