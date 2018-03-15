package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.payment.LoyaltyInformation

data class MultiItemOffer(
    val price: MultiItemOfferPrice?,
    val searchedOffer: OfferReference,
    val packagedOffers: List<OfferReference>,
    val packageDeal: PackageDeal?,
    val loyaltyInfo: LoyaltyInformation?,
//        val winningDimensions: List<IMultiItemOffer.Dimension>,
    val cancellationPolicy: CancellationPolicy
)
