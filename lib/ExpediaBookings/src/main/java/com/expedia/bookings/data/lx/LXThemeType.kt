package com.expedia.bookings.data.lx

enum class LXThemeType(val categories: List<LXCategoryType>) {
    TopRatedActivities(emptyList()),
    AdventureAround(arrayListOf(LXCategoryType.Adventures, LXCategoryType.DayTripsExcursions, LXCategoryType.MultiDayExtendedTours)),
    SeeTheSights(arrayListOf(LXCategoryType.SightseeingPasses, LXCategoryType.ToursSightseeing, LXCategoryType.WalkingBikeTours, LXCategoryType.AirBalloonHelicopterTours, LXCategoryType.HoponHopoff)),
    TourTheArea(arrayListOf(LXCategoryType.CruisesWaterTours, LXCategoryType.WalkingBikeTours, LXCategoryType.PrivateTours, LXCategoryType.MultiDayExtendedTours, LXCategoryType.HoponHopoff)),
    FunForTheFamily(arrayListOf(LXCategoryType.WaterActivities, LXCategoryType.WalkingBikeTours, LXCategoryType.ShowSportTickets, LXCategoryType.Cirque, LXCategoryType.HoponHopoff, LXCategoryType.Attractions)),
    PureEntertainment(arrayListOf(LXCategoryType.ShowSportTickets, LXCategoryType.Cirque, LXCategoryType.ThemeParks, LXCategoryType.Attractions)),
    EatPlayEnjoy(arrayListOf(LXCategoryType.FoodDrink, LXCategoryType.Nightlife, LXCategoryType.Spa, LXCategoryType.Attractions, LXCategoryType.WaterActivities, LXCategoryType.WeddingCeremonies)),
    AllThingsToDo(emptyList()),
}
