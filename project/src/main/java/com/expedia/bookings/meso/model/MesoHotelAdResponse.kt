package com.expedia.bookings.meso.model

import com.google.android.gms.ads.formats.NativeAd

data class MesoHotelAdResponse (val background: NativeAd.Image?,
                           val headline: CharSequence?,
                           val hotelId: CharSequence?,
                           val hotelName: CharSequence?,
                           val offerPrice: CharSequence?,
                           val percentageOff: CharSequence?,
                           val propertyLocation: CharSequence?,
                           val regionId: CharSequence?,
                           val StrikethroughPrice: CharSequence?)