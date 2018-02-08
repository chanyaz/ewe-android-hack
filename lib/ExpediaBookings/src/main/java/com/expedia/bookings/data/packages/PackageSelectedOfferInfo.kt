package com.expedia.bookings.data.packages

data class PackageSelectedOfferInfo(var hotelId: String? = null,
                                    var ratePlanCode: String? = null,
                                    var roomTypeCode: String? = null,
                                    var inventoryType: String? = null,

                                    var productOfferPrice: PackageOfferModel.PackagePrice? = null,

                                    var flightPIID: String? = null,
                                    var isSplitTicketFlights: Boolean = false,
                                    var outboundFlightBaggageFeesUrl: String? = null,
                                    var inboundFlightBaggageFeesUrl: String? = null)
