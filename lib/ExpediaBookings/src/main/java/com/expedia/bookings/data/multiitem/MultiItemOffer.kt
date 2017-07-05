package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.payment.LoyaltyInformation

data class MultiItemOffer(
        val price: MultiItemOfferPrice,
        val searchedOffer: OfferReference,
        val packagedOffers: List<OfferReference>,
        val offer: HotelOffer?,
        val packageDeal: PackageDeal,
        val loyaltyInfo: LoyaltyInformation,
//        val winningDimensions: List<IMultiItemOffer.Dimension>,
        val detailsUrl: String,
        val changeHotelUrl: String,
        val changeRoomUrl: String,
        val changeCarUrl: String,
        val checkoutUrl: String,
        val cancellationPolicy: CancellationPolicy
    )