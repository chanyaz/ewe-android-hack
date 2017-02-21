Feature: Flights Search Results Screen Tests

  @Flights @Search @FlightResults
  Scenario: Verifying data consistency through Search and FSR screens for round trip search
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
    And Validate that flight search results are displayed
    Then on FSR the destination is "Delhi"
    And on FSR the date is as user selected
    And on outbound FSR the number of traveller are as user selected
    And I select first flight
    And I verify date is as user selected for inbound flight
    And on inbound FSR the number of traveller are as user selected


  @Flights @Search @FlightResults
  Scenario: Verifying data consistency for one way trip through Search and FSR screens
    Given I launch the App
    And I launch "Flights" LOB
    And I select one way trip
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
    And Validate that flight search results are displayed
    Then on FSR the destination is "Delhi"
    And on FSR the date is as user selected
    And on outbound FSR the number of traveller are as user selected


  @Flights @Search @FlightResults
  Scenario Outline: Verifying UI elements and data on each flight cell of FSR
    Given I launch the App
    And I bucket the following tests
      | RoundTripOnFlightsFSR |
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
    And Validate that flight search results are displayed
    Then Validate that flight time field at cell <cellNumber> is displayed: true and isOutBound : true
    And Validate that price field at cell <cellNumber> is displayed: true and isOutBound : true
    And Validate that airline name field at cell <cellNumber> is displayed: true and isOutBound : true
    And Validate that flight duration field at cell <cellNumber> is displayed: true and isOutBound : true
    And Validate that round trip header at cell <cellNumber> is displayed: true and isOutBound : true
    And Name of airline at cell <cellNumber> is "<AirlineName>" and isOutBound : true
    And Price of the flight at cell <cellNumber> is <price> and isOutBound : true
    And Duration of the flight at cell <cellNumber> is "<duration>" and isOutBound : true
    And Timing of the flight at cell <cellNumber> is "<timing>" and isOutBound : true
    And Number of stops at cell <cellNumber> are <number> and isOutBound : true

    Examples:
      | AirlineName    | price | duration | timing             | number | cellNumber |
      | Virgin America | 800   | 4h 35m   | 5:40 pm - 10:15 pm | 1      | 2          |



  @Flights @SearchScreen @CALocale @Prod
  Scenario: POS and locale combination
    Given I launch the App
    And I set the POS to "Canada"
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
    And Validate that flight search results are displayed
    Then the currency symbol at cell 2 on FSR is "$" and isOutBound : true


  @Flights @Search @FlightResults
  Scenario Outline: Data consistency between Outbound and Inbound FSR and cell UI validations
    Given I launch the App
    And I bucket the following tests
      | RoundTripOnFlightsFSR |
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
    And I select first flight
    Then Validate that flight time field at cell <cellNumber> is displayed: true and isOutBound : false
    And Validate that price field at cell <cellNumber> is displayed: true and isOutBound : false
    And Validate that airline name field at cell <cellNumber> is displayed: true and isOutBound : false
    And Validate that flight duration field at cell <cellNumber> is displayed: true and isOutBound : false
    And Validate that round trip header at cell <cellNumber> is displayed: true and isOutBound : false
    And Name of airline at cell <cellNumber> is "<AirlineName>" and isOutBound : false
    And Price of the flight at cell <cellNumber> is <price> and isOutBound : false
    And Duration of the flight at cell <cellNumber> is "<duration>" and isOutBound : false
    And Timing of the flight at cell <cellNumber> is "<timing>" and isOutBound : false
    And Number of stops at cell <cellNumber> are <number> and isOutBound : false

    Examples:
      | AirlineName       | price | duration | timing             | number | cellNumber |
      | American Airlines | 696   | 2h 35m   | 5:40 pm - 8:15 pm  | 0      | 1          |

