Feature: Flights Overview

  @Flights @FlightsOverview
  Scenario: Verify data consistency through Overview screen for round trip search
    Given I launch the App
    And I bucket the following tests
      | FlightRateDetailExpansion |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    Then validate following information is present on the overview screen for isOutbound : true
      | destination                    | (DEL)                                             |
      | travel date and traveller      | Mar 22 at 9:00 pm, 1 Traveler                     |
      | Flight time                    | 9:00 pm - 11:00 pm                                     |
      | airport names                  | (SFO) SFO - (DEL) DEL                             |
      | airline name                   | happy_round_trip                                  |
      | flight duration                | 2h 0m                                             |
    And validate total duration on flight Overview is "2h 0m" for isOutbound : true
    Then collapse the outbound widget
    Then validate following information is present on the overview screen for isOutbound : false
      | destination                    | (SFO)                                             |
      | travel date and traveller      | Mar 22 at 5:40 pm, 1 Traveler                       |
      | Flight time                    | 5:40 pm - 8:15 pm                                      |
      | airport names                  | (DEL) DEL - (SFO) SFO                             |
      | airline name                   | American Airlines 179                             |
      | flight duration                | 2h 35m                                            |
    And validate total duration on flight Overview is "2h 35m" for isOutbound : false
    And collapse the inbound widget

  @Flights @FlightsOverview
  Scenario: Verify data consistency for multi-leg flights on Overview screen.
    Given I launch the App
    And I bucket the following tests
      | FlightRateDetailExpansion |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select outbound flight at position 2 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    Then validate following flight details for multi-leg flights
      | first-segment-flight time      | 5:40 pm - 8:15 pm                                 |
      | first-segment-airport name     | (SEA) Seattle, USA - (LAX) Los Angeles, USA       |
      | first-segment-airline name     | Virgin America 798                                |
      | first-segment-flight duration  | 2h 35m                                            |
      | second-segment-flight time     | 9:00 pm - 10:15 pm                                |
      | second-segment-airport name    | (LAX) Los Angeles, USA - (SFO) San Francisco, USA |
      | second-segment-airline name    | Virgin America 947                                |
      | second-segment-flight duration | 1h 15m                                            |
    And validate layover of outbound flight is on "(LAX) Los Angeles, USA" for "45m"
    And validate total duration on flight Overview is "4h 35m" for isOutbound : true

  @Flights @FlightsOverview
  Scenario: Verify message on free cancellation and split ticket messaging on Overview screen.
    Given I launch the App
    And I bucket the following tests
      | FlightRateDetailExpansion |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select outbound flight at position 2 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    Then collapse the outbound widget
    And collapse the inbound widget
    Then validate free cancellation message is displayed
    And validate split ticket messaging is displayed

  @Flights @FlightsOverview
  Scenario: Verify price details on cost summary popup on Flights Overview.
    Given I launch the App
    And I bucket the following tests
      | FlightRateDetailExpansion |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select outbound flight at position 2 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    Then collapse the outbound widget
    And collapse the inbound widget
    Then validate total price of the trip is "$64"
    Then I click on trip total link
    Then validate following detailed information is present on cost summary screen
      | Adult 1 details     | $64.00 |
      | Flight              | $48.95 |
      | Taxes & Fees        | $15.05 |
      | Expedia Booking Fee | $0.00  |
      | Total Due Today     | $64.00 |
    And I click on Done button
    And I click on checkout button

  @Flights @FlightsOverview @Prod
  Scenario: Verify cost summary popup for multi-travellers on Flights Overview.
    Given I launch the App
    And I bucket the following tests
      | FlightRateDetailExpansion |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And I click on trip total link
    Then validate price info for multi travellers
    And validate price for "Expedia Booking Fee" is displayed
    And validate price for "Total Due Today" is displayed
