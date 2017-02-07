Feature: Flights Search Results Screen Tests

  @Flights @Search @FlightResults @WIP
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
    Then on FSR the destination is "Delhi"
    And on FSR the date is as user selected
    And on inbound FSR the number of traveller are as user selected
    And I select first flight
    And I verify date is as user selected for inbound flight
    And on outbound FSR the number of traveller are as user selected


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
    Then on FSR the destination is "Delhi"
    And on FSR the date is as user selected
    And on inbound FSR the number of traveller are as user selected


  @Flights @Search @FlightResults
  Scenario Outline: Verifying UI elements and data on each flight cell of FSR
    Given I launch the App
    And I bucket the following tests
      | RoundTripOnFlightsFSR |
      | UrgencyMessegingOnFSR |
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
    Then Validate that flight time field is displayed: true
    And Validate that price field is displayed: true
    And Validate that airline name field is displayed: true
    And Validate that flight duration field is displayed: true
    And Validate that round trip header is displayed: true
    And Name of airline is "<AirlineName>"
    And Price of the flight is <price>
    And Duration of the flight is "<duration>"
    And Timing of the flight is "<timing>"
    And Number of stops are <number>

    Examples:
      | AirlineName    | price | duration | timing             | number |
      | Virgin America | 800   | 4h 35m   | 5:40 pm - 10:15 pm | 1      |

  @Flights @SearchScreen @WIP
  Scenario Outline: POS and locale combination
    Given I launch the App
    And I set the POS to "<POS>"
    And I change the locale to "<LOCALE>"
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
    Then the currency symbol on FSR is "<symbol>"

    Examples:
      | POS              | LOCALE | symbol |
      | Unites States    |   AU   |  US$   |
      | Australia        |   US   |   $    |

