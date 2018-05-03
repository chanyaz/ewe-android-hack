Feature: Package Overview

  @Packages @PackageOverview @Prod
    Scenario: Validate Package Overview bundle total details when user gets back to HSR from HIS
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
      And I press back
      And I click on View your bundle
      And on POS Validate that Package Overview Screen is displayed
      And validate bundle total widget
        | bundle_total_text         | getValueBasedOnBrand           |
        | additional_text           | includes hotel and flights     |
        | bundle_total_value        | $0                             |

  @Packages @PackageOverview
  Scenario: Validate bundle Package Overview Screen details with outbound flight selected
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | sfo                            |
      | destination         | KTM                            |
      | source_suggest      | San Francisco, CA              |
      | destination_suggest | Kathmandu, Nepal (KTM-Tribhuvan Intl.) |
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
    And I select room at position 1
    And validate outbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 5 |
      | Total_Travelers | 4 |
    And I select outbound flight to destination at position 1
    And I click on View your bundle
    And on POS Validate that Package Overview Screen is displayed
    Then on Package Overview Screen validate the toolbar
      | destination       | Kathmandu, Nepal |
      | start_date        | 5                     |
      | end_date          | 10                    |
      | totalTravelers    | 4 travelers           |
    And validate hotel selection step label
      | info_text         | Step 1: Select hotel|
    And validate "varHotelName" is same as user selected on package overview screen
    Then validate hotel widget data on hotel overview widget
      | info_text | May 7 - May 10, 4 guests |
    And validate hotel widget luggage image icon is checked
    Then I click on hotel widget details icon
    And verify hotel widget detail view is displayed
    And verify hotel widget detail data
      | room_info           | flight_outbound_happy |
      | room_type           | 2 twin beds           |
      | hotel_address       | Lazimpat              |
      | hotel_city          | Kathmandu, Nepal      |
      | cancellation_status | Free cancellation     |
      | sale_status         |                       |
    And I click on hotel widget details icon
    And validate outbound flight selection label
      | info_text           | Step 2: Select flights|
    And validate package outbound flight icon is checked
    And verify package outbound flight widget view is displayed : false
    And I click on package flight details icon
    And verify package outbound flight widget view is displayed : true
    And verify package outbound flight icon is displayed
    And validate package outbound flight data on package outbound flight widget
      | flight_to               | Flight to (KTM) Kathmandu      |
      | info_text               | May 5 at 2:15 pm, 4 travelers  |
    And validate package outbound flight details
      | departure_arrival_time    | 2:15 pm - 10:45 pm +1d                |
      | departure_arrival_airport | (SFO) San Francisco - (CAN) Guangzhou |
      | airline                   | China Southern Airlines 660 • Boeing 777-300er |
      | airplane_type             | Boeing 777                            |
      | flight_duration           | 17h 30m                               |
    And validate package outbound flight total duration
      | flight_duration           | Total Duration: 31h 43m                |
    And I click on package flight details icon
    And validate bundle total widget
      | bundle_total_text         | getValueBasedOnBrand                    |
      | additional_text           | includes hotel and flights              |
      | bundle_total_value        | $1,244.98                               |
      | savings                   | $70.47 Saved                            |
    And validate package inbound flight widget view
      | header                    | Select flight to San Francisco        |
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
      | source              | sfo                            |
      | destination         | KTM                            |
      | source_suggest      | San Francisco, CA              |
      | destination_suggest | Kathmandu, Nepal (KTM-Tribhuvan Intl.)          |
      | start_date          | 5                              |
      | end_date            | 10                             |
      | adults              | 2                              |
      | child               | 2                              |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select room at position 1
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
    And Validate that Package Overview screen is displayed
    Then on Package Overview Screen validate the toolbar when hotel, outbound and inbound flight is selected
      | destination       | Kathmandu, Nepal      |
      | start_date        | 5                     |
      | end_date          | 10                    |
      | totalTravelers    | 4 travelers           |
    And validate hotel selection step label
      | info_text         | Hotel in Kathmandu - 1 room, 3 nights |
     And validate "varHotelName" is same as user selected on package overview screen
     Then validate hotel widget data on hotel overview widget
       | info_text | May 7 - May 10, 4 guests |
     And validate hotel widget luggage image icon is checked
     Then I click on hotel widget details icon
     And verify hotel widget detail view is displayed
     And verify hotel widget detail data
       | room_info           | flight_outbound_happy |
       | room_type           | 2 twin beds           |
       | hotel_address       | Lazimpat              |
       | hotel_city          | Kathmandu, Nepal      |
       | cancellation_status | Free cancellation     |
       | sale_status         |                       |
     And I click on hotel widget details icon
     And validate outbound flight selection label
       | info_text           | Flights - happy to KTM, round trip|
     And validate package outbound flight data on package outbound flight widget
       | flight_to               | Flight to (KTM) Kathmandu      |
       | info_text               | May 5 at 2:15 pm, 4 travelers  |
     And validate package outbound flight icon is checked
     And I click on package flight details icon
     And verify package outbound flight widget view is displayed : true
     And verify package outbound flight icon is displayed
     And validate package outbound flight details
       | departure_arrival_time    | 2:15 pm - 10:45 pm +1d                |
       | departure_arrival_airport | (SFO) San Francisco - (CAN) Guangzhou |
       | airline                   | China Southern Airlines 660 • Boeing 777-300er |
       | airplane_type             | Boeing 777                            |
       | flight_duration           | 17h 30m                               |
     And validate package outbound flight total duration
       | flight_duration           | Total Duration: 31h 43m               |
     And I click on package flight details icon
     And validate package inbound flight icon is checked
     And validate package inbound flight data on package inbound flight widget
       | flight_from             | Flight to (SFO) San Francisco        |
       | info_text_inbound       | May 10 at 4:15 pm, 4 travelers       |
     And verify package inbound flight widget view is displayed : false
     And I click on package flight inbound details icon
     And verify package inbound flight widget view is displayed : true
     And verify package inbound flight icon is displayed
     And validate package inbound flight details
       | departure_arrival_time    | 4:15 pm - 9:25 pm                       |
       | departure_arrival_airport | (KTM) Kathmandu - (KMG) Kunming         |
       | airline                   | China Eastern Airlines 758 • Boeing 737 |
       | airplane_type             | China Eastern Airlines 758 • Boeing 737 |
       | flight_duration           | 2h 52m                                  |
     And validate package inbound flight total duration
       | flight_duration           | Total Duration: 29h 57m               |
     And validate bundle total widget
       | bundle_total_text         | getValueBasedOnBrand                  |
       | additional_text           | includes hotel and flights            |
       | bundle_total_value        | $1,244.98                             |
       | savings                   | $70.47 Saved                          |

  @Packages @PackageOverview
  Scenario: Verify price details on cost summary popup on Bundle Overview.
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | sfo                                                |
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
      | bundle_total_value        | $1,244.98                             |
      | savings                   | $70.47 Saved                          |
    And I click on checkout button

  @Packages @PackageOverview
  Scenario: Validate that close and edit icons are present
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | sfo                                                |
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
    And I set bucketing rules for A/B tests as
      | PackagesBackFlowFromOverview | CONTROL                                     |
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | sfo                                                |
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
    And I set bucketing rules for A/B tests as
      | PackagesBackFlowFromOverview | BUCKETED                                    |
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source                | sfo                                                |
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

  @Packages @PackageOverview
  Scenario: Validate bundle Package Overview Screen details shows seating class
      Given I launch the App
      And I launch "Bundle Deals" LOB
      When I make a packages search with following parameters
        | source              | sfo                            |
        | destination         | KTM                            |
        | source_suggest      | San Francisco, CA              |
        | destination_suggest | Kathmandu, Nepal (KTM-Tribhuvan Intl.)          |
        | start_date          | 5                              |
        | end_date            | 10                             |
        | adults              | 2                              |
        | child               | 2                              |
      And I select hotel at position 1 on HSR screen
      And I select room at position 1
      And I select outbound flight to destination at position 1
      And I click on View your bundle
      And on POS Validate that Package Overview Screen is displayed
      And validate outbound flight selection label
        | info_text           | Step 2: Select flights|
      And validate package outbound flight icon is checked
      And verify package outbound flight widget view is displayed : false
      And I click on package flight details icon
      And verify package outbound flight widget view is displayed : true
      And verify package outbound flight icon is displayed
      And validate package outbound flight data on package outbound flight widget
        | flight_to               | Flight to (KTM) Kathmandu        |
        | info_text               | May 5 at 2:15 pm, 4 travelers |
      And validate package outbound flight details
        | departure_arrival_time    | 2:15 pm - 10:45 pm +1d                |
        | departure_arrival_airport | (SFO) San Francisco - (CAN) Guangzhou |
        | airline                   | China Southern Airlines 660 • Boeing 777-300er |
        | airplane_type             | Boeing 777                            |
        | flight_duration           | 17h 30m                               |
      And validate package outbound flight details seating class
        | seatingClass              | Economy (S)                           |
