package com.expedia.bookings.hotel.widget

class TranslationResponse(val data: TranslationData)
class TranslationData(val translations: List<Translation>)
class Translation(val translatedText: String)
