package com.expedia.bookings.data.trips

class EventbriteResponse(val events: List<Event>)

class Event(
        val name: Name,
        val start: Start,
        val end: End,
        val description: Description,
        val url: String,
        val is_free: Boolean,
        val venue: Venue,
        val category_id: String,
        val logo: Logo
)

class Name(val text: String)

class Start(val local: String)

class End(val local: String)

class Description(val text: String)

class Logo(val url: String)

class Venue(
        val name: String,
        val latitude: Double,
        val longitude: Double
)