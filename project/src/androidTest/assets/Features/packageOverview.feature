Feature: Package Overview

  @Packages @PackageOverview
  Scenario: Validate bundle Package Overview Screen details with outbound flight selected
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | KTM                            |
      | destination         | SFO                            |
      | source_suggest      | KTM - Tribhuvan Intl.          |
      | destination_suggest | San Francisco, CA              |
      | start_date          | 5                              |
      | end_date            | 10                             |
      | adults              | 2                              |
      | child               | 2                              |
    And validate HSR screen is displayed with following travel dates and travelers
      | start_date      |  5 |
      | end_date        | 10 |
      | Total_Travelers |  4 |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select first room
    And validate outbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 5 |
      | Total_Travelers | 4 |
    And I select outbound flight to destination at position 1
    And I click on View your bundle
    And on POS Validate that Package Overview Screen is displayed
    Then on Package Overview Screen validate the toolbar
      | destination       | San Francisco, CA |
      | start_date        | 5                 |
      | end_date          | 10                |
      | totalTravelers    | 4 Travelers       |
    And validate hotel selection step label
      | info_text         | Step 1: Select Hotel|
    And validate "varHotelName" is same as user selected on package overview screen
    Then validate hotel widget data on hotel overview widget
      | info_text | Jul 10 - Jul 16, 4 Guests |
    And validate hotel widget luggage image icon is checked
    Then I click on hotel widget details icon
    And verify hotel widget detail view is displayed
    And verify hotel widget detail data
      | room_info           | happy_outbound_flight |
      | room_type           | 1 king bed2 king bed  |
      | hotel_address       | 1000 Brush Avenue     |
      | hotel_city          | Detroit, MI           |
      | cancellation_status | Free Cancellation     |
      | sale_status         | Sale!                 |
    And I click on hotel widget details icon
    And validate outbound flight selection label
      | info_text           | Step 2: Select Flights|
    And validate package outbound flight icon is checked
    And verify package outbound flight widget view is displayed : false
    And I click on package flight details icon
    And verify package outbound flight widget view is displayed : true
    And verify package outbound flight icon is displayed
    And validate package outbound flight data on package outbound flight widget
      | flight_to               | Flight to (DTW) Detroit        |
      | info_text               | Jul 10 at 9:00 am, 4 Travelers |
    And validate package outbound flight details
      | departure_arrival_time    | 9:00 am - 11:12 am                    |
      | departure_arrival_airport | (SFO) San Francisco - (HNL) Honolulu  |
      | airline                   | United 1175                           |
      | airplane_type             | Boeing 777                            |
      | flight_duration           | 5h 12m                                |
    And validate package outbound flight total duration
      | flight_duration           | Total Duration: 5h 12m                |
    And validate bundle total widget
      | bundle_total_text         | Bundle total                          |
      | additional_text           | Includes flights + hotel              |
      | bundle_total_value        | $4,211.90                             |
      | savings                   | $540.62 Saved                         |
    And validate package inbound flight widget view
      | header                    | Select flight to Kathmandu            |
      | end_date                  | 10                                    |
      | travelers                 | 4 Travelers                           |
    And validate package inbound flight icon is unchecked
    And validate package inbound flight next icon is displayed
    And I click on package inbound flight next icon
    And validate inbound flights loading on package overview screen
