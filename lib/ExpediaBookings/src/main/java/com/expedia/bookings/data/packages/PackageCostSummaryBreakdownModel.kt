package com.expedia.bookings.data.packages

data class PackageCostSummaryBreakdownModel(val standaloneHotelPrice: String?,
                                            val standaloneFlightsPrice: String?,
                                            val referenceTotalPrice: String?,
                                            val savings: String?,
                                            val totalPrice: String?)
