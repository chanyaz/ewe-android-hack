Feature: Flight Details on a seperate screen

  @Flights @FlightDetails
  Scenario: Verify data consistency between outbound search and details screen on flights toolbar

    Given I launch the App
    And I bucket the following tests
      | FlightsSeatClassAndBookingCode |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I click on the flight with airline name "happy_round_trip" at "21:00 - 23:00"
    Then on flight details screen the destination is "Delhi, India"
    And on flight details the traveler count is 5
    And on Flight detail check the date is as user selected
    And price displayed on flight details is "$696.00"
    And on flight details screen the urgency text is "1 seat left"
    And flight time on the flight details is "21:00 - 23:00"
    And airport names on the flight details is "(SFO) SFO - (DEL) DEL"
    And airline name on the flight details is "happy_round_trip"
    And flight duration on the flight details is "2h 0m"
    And flight class info is "Economy (K)"
    And flight total duration on the flight details is "2h 0m"
    And Baggage link "Baggage fee info" is present on the flight details
    And Select button "Select this Flight" is displayed at the bottom of the flight details screen


  @Flights @FlightDetails
  Scenario: Verify multi leg information
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I click on the flight with airline name "Virgin America" at "17:40 - 22:15"
    Then on flight details screen the destination is "Delhi, India"
    And on flight details the traveler count is 5
    And on Flight detail check the date is as user selected
    And price displayed on flight details is "$800.00"
    And flight time for segment 1 on the flight details is "17:40 - 20:15"
    And airport names on the flight details is "(SEA) Seattle, USA - (LAX) Los Angeles, USA"
    And airline name on the flight details is "Virgin America 798"
    And flight duration on the flight details is "2h 35m"
    And flight layover airport is "Layover in (LAX) Los Angeles, USA"
    And flight layover is for "45m"
    And flight time for segment 2 on the flight details is "21:00 - 22:15"
    And airport names on the flight details is "(LAX) Los Angeles, USA - (SFO) San Francisco, USA"
    And airline name on the flight details is "Virgin America 947"
    And flight duration on the flight details is "1h 15m"
    And flight total duration on the flight details is "4h 35m"
    And Baggage link "Baggage fee info" is present on the flight details
    And Select button "Select this Flight" is displayed at the bottom of the flight details screen

  @Flights @FlightDetails
  Scenario: Verify that on tapping back button on flight detail screen user is navigated to Flight search results
      Given I launch the App
      And I launch "Flights" LOB
      When I make a flight search with following parameters
        | source              | SFO                                      |
        | destination         | DEL                                      |
        | source_suggest      | San Francisco, CA                        |
        | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
        | start_date          | 5                                        |
        | end_date            | 25                                       |
        | adults              | 3                                        |
        | child               | 2                                        |
      And I wait for results to load
      And I click on the flight with airline name "happy_round_trip" at "21:00 - 23:00"
      And I press back
      And Validate that flight search results are displayed

  @Flights @FlightDetails
  Scenario: Verify that on selecting different flight, details for the new selection is displayed
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I click on the flight with airline name "happy_round_trip" at "21:00 - 23:00"
    And I press back
    And Validate that flight search results are displayed
    And I click on the flight with airline name "Virgin America" at "17:40 - 22:15"
    Then on flight details screen the destination is "Delhi, India"
    And on flight details the traveler count is 5
    And on Flight detail check the date is as user selected
    And price displayed on flight details is "$800.00"
    And flight time for segment 1 on the flight details is "17:40 - 20:15"
    And airport names on the flight details is "(SEA) Seattle, USA - (LAX) Los Angeles, USA"
    And airline name on the flight details is "Virgin America 798"
    And flight duration on the flight details is "2h 35m"
    And flight layover airport is "Layover in (LAX) Los Angeles, USA"
    And flight layover is for "45m"
    And flight time for segment 2 on the flight details is "21:00 - 22:15"
    And airport names on the flight details is "(LAX) Los Angeles, USA - (SFO) San Francisco, USA"
    And airline name on the flight details is "Virgin America 947"
    And flight duration on the flight details is "1h 15m"
    And flight total duration on the flight details is "4h 35m"