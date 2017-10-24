package com.expedia.bookings.data.trips

class TcsResponse(val sections: TcsSections)

class TcsSections(val poi: TcsPoi)

class TcsPoi(val data: List<TcsData>)

class TcsData(
        val geo: TcsGeo,
        val descriptions: TcsDescription,
        val images: TcsImages
)

class TcsGeo(
        val latitude: String,
        val longitude: String
)

class TcsDescription(val data: List<TcsDescriptionData>)

class TcsDescriptionData(val value: String)

class TcsImages(val data: List<TcsImagesData>)

class TcsImagesData(
        val url: String,
        val alt: String,
        val caption: String
)