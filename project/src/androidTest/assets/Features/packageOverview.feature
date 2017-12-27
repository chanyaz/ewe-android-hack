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
    And I select room at position 2
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
      | totalTravelers    | 4 travelers       |
    And validate hotel selection step label
      | info_text         | Step 1: Select hotel|
    And validate "varHotelName" is same as user selected on package overview screen
    Then validate hotel widget data on hotel overview widget
      | info_text | Jul 10 - Jul 16, 4 guests |
    And validate hotel widget luggage image icon is checked
    Then I click on hotel widget details icon
    And verify hotel widget detail view is displayed
    And verify hotel widget detail data
      | room_info           | happy_outbound_flight |
      | room_type           | 1 king bed2 king bed  |
      | hotel_address       | 1000 Brush Avenue     |
      | hotel_city          | Detroit, MI           |
      | cancellation_status | Free cancellation     |
      | sale_status         | Sale!                 |
    And I click on hotel widget details icon
    And validate outbound flight selection label
      | info_text           | Step 2: Select flights|
    And validate package outbound flight icon is checked
    And verify package outbound flight widget view is displayed : false
    And I click on package flight details icon
    And verify package outbound flight widget view is displayed : true
    And verify package outbound flight icon is displayed
    And validate package outbound flight data on package outbound flight widget
      | flight_to               | Flight to (DTW) Detroit        |
      | info_text               | Jul 10 at 9:00 am, 4 travelers |
    And validate package outbound flight details
      | departure_arrival_time    | 9:00 am - 11:12 am                    |
      | departure_arrival_airport | (SFO) San Francisco - (HNL) Honolulu  |
      | airline                   | United 1175                           |
      | airplane_type             | Boeing 777                            |
      | flight_duration           | 5h 12m                                |
    And validate package outbound flight total duration
      | flight_duration           | Total Duration: 5h 12m                |
    And validate bundle total widget
      | bundle_total_text         | getValueBasedOnBrand                    |
      | additional_text           | includes hotel and flights              |
      | bundle_total_value        | $4,211.90                             |
      | savings                   | $540.62 Saved                         |
    And validate package inbound flight widget view
      | header                    | Select flight to Kathmandu            |
      | end_date                  | 10                                    |
      | travelers                 | 4 travelers                           |
    And validate package inbound flight icon is unchecked
    And validate package inbound flight next icon is displayed
    And I click on package inbound flight next icon
    And validate inbound flights loading on package overview screen

  @Packages @PackageOverview
  Scenario: Validate bundle Package Overview Screen details when hotel, outbound and inbound flight is selected
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
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select room at position 2
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
    And Validate that Package Overview screen is displayed
    Then on Package Overview Screen validate the toolbar when hotel, outbound and inbound flight is selected
      | destination       | Detroit, MI       |
      | start_date        | 5                 |
      | end_date          | 10                |
      | totalTravelers    | 4 travelers       |
    And validate hotel selection step label
      | info_text         | Hotel in Detroit - 1 room, 2 nights|
     And validate "varHotelName" is same as user selected on package overview screen
     Then validate hotel widget data on hotel overview widget
       | info_text | Feb 2 - Feb 4, 4 guests |
     And validate hotel widget luggage image icon is checked
     Then I click on hotel widget details icon
     And verify hotel widget detail view is displayed
     And verify hotel widget detail data
       | room_info           | happy_outbound_flight |
       | room_type           | 1 king bed2 king bed  |
       | hotel_address       | 1000 Brush Avenue     |
       | hotel_city          | Detroit, MI           |
       | cancellation_status | Free cancellation     |
       | sale_status         | Sale!                 |
     And I click on hotel widget details icon
     And validate outbound flight selection label
       | info_text           | Flights - KTM to SFO, round trip|
     And validate package outbound flight data on package outbound flight widget
       | flight_to               | Flight to (DTW) Detroit        |
       | info_text               | Jul 10 at 9:00 am, 4 travelers |
     And validate package outbound flight icon is checked
     And I click on package flight details icon
     And verify package outbound flight widget view is displayed : true
     And verify package outbound flight icon is displayed
     And validate package outbound flight details
       | departure_arrival_time    | 9:00 am - 11:12 am                    |
       | departure_arrival_airport | (SFO) San Francisco - (HNL) Honolulu  |
       | airline                   | United 1175                           |
       | airplane_type             | Boeing 777                            |
       | flight_duration           | 5h 12m                                |
     And validate package outbound flight total duration
       | flight_duration           | Total Duration: 5h 12m                |
     And I click on package flight details icon
     And validate package inbound flight icon is checked
     And validate package inbound flight data on package inbound flight widget
       | flight_from             | Flight to (SFO) San Francisco        |
       | info_text_inbound       | Jul 16 at 1:45 pm, 4 travelers       |
     And verify package inbound flight widget view is displayed : false
     And I click on package flight inbound details icon
     And verify package inbound flight widget view is displayed : true
     And verify package inbound flight icon is displayed
     And validate package inbound flight details
       | departure_arrival_time    | 1:45 pm - 10:00 pm                    |
       | departure_arrival_airport | (HNL) Honolulu - (SFO) San Francisco  |
       | airline                   | Hawaiian Airlines 12                  |
       | airplane_type             | Airbus Industrie A330-200             |
       | flight_duration           | 5h 15m                                |
     And validate package inbound flight total duration
       | flight_duration           | Total Duration: 5h 15m                |
     And validate bundle total widget
       | bundle_total_text         | getValueBasedOnBrand                    |
       | additional_text           | includes hotel and flights              |
       | bundle_total_value        | $2,538.62                             |
       | savings                   | $56.50 Saved                          |

  @Packages @PackageOverview
  Scenario: Verify price details on cost summary popup on Bundle Overview.
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
      | start_date            | 5                                                  |
      | end_date              | 10                                                 |
      | adults                | 1                                                  |
      | child                 | 0                                                  |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select first room
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
    And Validate that Package Overview screen is displayed
    And validate bundle total widget
      | bundle_total_text         | getValueBasedOnBrand                    |
      | additional_text           | includes hotel and flights              |
      | bundle_total_value        | $2,538.62                             |
      | savings                   | $56.50 Saved                          |
    Then I click on trip total link
    Then validate following detailed information is present on bundle overview cost summary screen
      | Hotel + Flights              | $2,595.12                       |
      | room                         | 1 room, 2 nights, 1 guest       |
      | Taxes                        | Taxes & Fees Included ($278.12) |
      | Bundle Discount              | -$56.50                         |
      | Bundle total                 | $2,538.62                       |
    And I click on Done button
    And I click on checkout button

  @Packages @PackageOverview
  Scenario: Validate that close and edit icons are present
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
      | start_date            | 5                                                  |
      | end_date              | 10                                                 |
      | adults                | 2                                                  |
      | child                 | 2                                                  |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select first room
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
    And Validate that Package Overview screen is displayed
    And Validate that close icon is present on top left
    And Validate that edit icon is present on top right

  @Packages @PackageOverview
  Scenario: Validate that close and edit icons are present when PackagesBackFlowFromOverview is controlled
    Given I launch the App
    And I put following tests in control
      | PackagesBackFlowFromOverview |
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
      | start_date            | 5                                                  |
      | end_date              | 10                                                 |
      | adults                | 2                                                  |
      | child                 | 2                                                  |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select first room
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
    And Validate that Package Overview screen is displayed
    And Validate that close icon is present on top left
    And Validate that edit icon is present on top right

  @Packages @PackageOverview
  Scenario: Validate that Back and edit icons are present when PackagesBackFlowFromOverview is bucketed
    Given I launch the App
    And I bucket the following tests
      | PackagesBackFlowFromOverview |
    And I enable following features
      | preference_packages_back_flow_from_overview |
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
      | start_date            | 5                                                  |
      | end_date              | 10                                                 |
      | adults                | 2                                                  |
      | child                 | 2                                                  |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select first room
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
    And Validate that Package Overview screen is displayed
    And Validate that back icon is present on top left
    And Validate that edit icon is present on top right