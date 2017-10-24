package com.expedia.bookings.data.payment

data class CampaignDetails(val tripId: String, val message: String?, val title: String, val fundsRequested: Int,
                           val fundsAvailable: Int, val imageURL: String, val tuid: String, val donationList: List<DonorInformation>?)

data class DonorInformation(val donorTUID: String, val donorName: String, val recieverTUID: String, val amount: String)