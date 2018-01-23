package com.expedia.bookings.data.lx

enum class LXCategoryType {
    // This will be the always be first in order given it has all the activities.
    AllThingsToDo,
    ThemeParks,
    ShowSportTickets,
    SightseeingPasses,
    Attractions,
    Adventures,
    DayTripsExcursions,
    ToursSightseeing,
    Disney,
    AirBalloonHelicopterTours,
    SharedTransfers,
    WalkingBikeTours,
    CruisesWaterTours,
    Cirque,
    PrivateTransfers,
    HoponHopoff,
    Nightlife,
    PrivateTours,
    MultiDayExtendedTours,
    FoodDrink,
    Spa,
    WeddingCeremonies,
    WinterActivities,
    WaterActivities,
    // This will be the last in order in case we have any unknown categories.
    Unknown;
}
