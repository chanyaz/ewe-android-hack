Feature: Flights Search Results

  @Flights @FlightSearchResults
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
    And I select outbound flight at position 1 and reach inbound FSR
    And I verify date is as user selected for inbound flight
    And on inbound FSR the number of traveller are as user selected


  @Flights @FlightSearchResults
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


  @Flights @FlightSearchResults
  Scenario Outline: Verifying UI elements and data on each flight cell of FSR
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
    Then Validate that flight time field at cell <cellNumber> is displayed: true and isOutBound : true
    And Validate that price field at cell <cellNumber> is displayed: true and isOutBound : true
    And Validate that airline name field at cell <cellNumber> is displayed: true and isOutBound : true
    And Validate that flight duration field at cell <cellNumber> is displayed: true and isOutBound : true
    And Name of airline at cell <cellNumber> is "<AirlineName>" and isOutBound : true
    And Price of the flight at cell <cellNumber> is <price> and isOutBound : true
    And Duration of the flight at cell <cellNumber> is "<duration>" and isOutBound : true
    And Timing of the flight at cell <cellNumber> is "<timing>" and isOutBound : true
    And Number of stops at cell <cellNumber> are <number> and isOutBound : true

    Examples:
      | AirlineName    | price | duration | timing             | number | cellNumber |
      | Virgin America | 800   | 4h 35m   | 5:40 pm - 10:15 pm | 1      | 2          |



  @Flights @CALocale @Prod @EBOnly
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


  @Flights @FlightSearchResults
  Scenario Outline: Data consistency between Outbound and Inbound FSR and cell UI validations
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
    And I select outbound flight at position 1 and reach inbound FSR
    Then Validate that flight time field at cell <cellNumber> is displayed: true and isOutBound : false
    And Validate that price field at cell <cellNumber> is displayed: true and isOutBound : false
    And Validate that airline name field at cell <cellNumber> is displayed: true and isOutBound : false
    And Validate that flight duration field at cell <cellNumber> is displayed: true and isOutBound : false
    And Name of airline at cell <cellNumber> is "<AirlineName>" and isOutBound : false
    And Price of the flight at cell <cellNumber> is <price> and isOutBound : false
    And Duration of the flight at cell <cellNumber> is "<duration>" and isOutBound : false
    And Timing of the flight at cell <cellNumber> is "<timing>" and isOutBound : false
    And Number of stops at cell <cellNumber> are <number> and isOutBound : false

    Examples:
      | AirlineName       | price | duration | timing             | number | cellNumber |
      | American Airlines | 696   | 2h 35m   | 5:40 pm - 8:15 pm  | 0      | 1          |

  @Flights @FlightSearchResults
  Scenario: UI validations on the docked outbound header view on inbound FSR
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
    And I select outbound flight at position 1 and reach inbound FSR
    Then Validate that on the selected outbound docked view Flight label is displayed
    And Validate that on the selected outbound docked view Flight Airline name is displayed
    And Validate the toolbar header text on the selected outbound docked view

  @Flights @FlightSearchResults
  Scenario: Validate urgency message is displayed when seats left is less than 6
    Given I launch the App
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
    Then urgency message on cell 1 isDisplayed : true

  @Flights @FlightSearchResults
  Scenario: Validate urgency message is not displayed when seats left is greater than 6
    Given I launch the App
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
    Then urgency message on cell 2 isDisplayed : false


  @Flights @FlightSearchResults
  Scenario: Verify roundtrip messaging not shown for one way trip
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

  @Flights  @EBOnly
  Scenario: Validate legal compliance messaging FSR for AU POS
    Given I launch the App
    And I set the POS to "Australia"
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
    Then Validate legal compliance messaging on SRP and isOutbound : true
    And Validate the Per person roundtrip text and isOutbound : true
    Then I select outbound flight at position 1
    And Validate legal compliance message on flight detail screen and isOutbound : true
    Then Select outbound flight from Overview
    And Validate legal compliance messaging on SRP and isOutbound : false
    And Validate the Per person roundtrip text and isOutbound : false
    Then I select inbound flight at position 1
    And Validate legal compliance message on flight detail screen and isOutbound : false

  @Flights @FlightSearchResults
  Scenario: Multi-Carrier as airline name text for more than 3 airlines
    Given I launch the App
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
    Then multi carrier text is shown instead of Airline Name on cell 5 isOutbound : true

  @Flights @FlightSearchResults
  Scenario: Validate basic economy is displayed when isBasicEconomy is true in response
    Given I launch the App
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
    Then basic economy on cell 1 isDisplayed : true

  @Flights @EBOnly
  Scenario: Validate that XSell Package Banner is displayed for Round Trip, Economy and UK/US POS
    Given I launch the App
    And I bucket the following tests
      | FlightXSellPackage  |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
      | class               | Economy                                  |
    And I wait for results to load
    Then Validate that XSell Package Banner is displayed with title "Hotel + Flight" and description "Save when you book your flights and hotels together"
    And I tap on XSell Package Banner
    Then Validate Hotel Search Results Screen Header
      | title               | Hotels in Delhi, India                   |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
      | guest               | 1 guest                                  |
    And I press back
    Then Validate that XSell Package Banner is displayed with title "Hotel + Flight" and description "Save when you book your flights and hotels together"

  @Flights  @EBOnly
  Scenario: Validate that XSell Package Banner is not displayed for POS other than UK/US POS
    Given I launch the App
    And I set the POS to "Australia"
    And I bucket the following tests
      | FlightXSellPackage  |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 2                                        |
      | child               | 1                                        |
      | class               | Economy                                  |
    And I wait for results to load
    Then Validate that XSell Package Banner is not displayed

  @Flights @EBOnly
  Scenario: Validate that XSell Package Banner is not displayed for Cabin Class other than Economy
    Given I launch the App
    And I bucket the following tests
      | FlightXSellPackage  |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 2                                        |
      | child               | 1                                        |
      | class               | Business                                 |
    And I wait for results to load
    Then Validate that XSell Package Banner is not displayed

  @Flights @EBOnly
  Scenario: Validate that XSell Package Banner is not displayed for One Way Flights
    Given I launch the App
    And I bucket the following tests
      | FlightXSellPackage  |
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date          | 10                                       |
    And I change travellers count and press done
    And I click on class widget
    And I click on "Economy" as preferred class
    And I click on Done button
    Then I can trigger flights search
    And I wait for results to load
    Then Validate that XSell Package Banner is not displayed

  @Flights @Prod @EBOnly
  Scenario: Intercept getPackages API call after hitting XSell Package Banner and validate request parameters
    Given I launch the App
    And I bucket the following tests
      | FlightXSellPackage  |
    And I want to intercept these calls for packages
      | GetPackagesV1       |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | LHR                                      |
      | destination         | LAS                                      |
      | source_suggest      | London, England, UK                      |
      | destination_suggest | Las Vegas, NV                            |
      | start_date          | 15                                       |
      | end_date            | 20                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
      | class               | Economy                                  |
    And I wait for results to load
    And I tap on XSell Package Banner
    Then Validate the getPackages API request query data for following parameters for packages
      | forceNoRedir        | 1                                        |
      | packageType         | fh                                       |
    Then Validate the getPackages API request form data for following parameters
      | fromDate            | 15                                       |
      | destinationId       | 6139100                                  |
      | ttla                | LAS                                      |
      | ftla                | LHR                                      |
      | packageTripType     | 2                                        |
      | adultsPerRoom[1]    | 1                                        |
      | numberOfRooms       | 1                                        |
      | toDate              | 20                                       |
      | originId            | 178279                                   |

  @Flights @EBOnly
  Scenario: Intercept getPackages API call after hitting XSell Package Banner and validate request parameters with modifying parameters
    Given I launch the App
    And I bucket the following tests
      | FlightXSellPackage  |
    And I want to intercept these calls for packages
      | GetPackagesV1       |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 15                                       |
      | end_date            | 20                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
      | class               | Economy                                  |
    And I wait for results to load
    And I tap on XSell Package Banner
    And I press back
    And I press back
    When I trigger flight search again with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 20                                       |
      | end_date            | 30                                       |
      | adults              | 2                                        |
      | child               | 2                                        |
      | class               | Economy                                  |
    And I wait for results to load
    And I tap on XSell Package Banner
    Then Validate the getPackages API request query data for following parameters for packages
      | forceNoRedir        | 1                                        |
      | packageType         | fh                                       |
    Then Validate the getPackages API request form data for following parameters
      | fromDate            | 20                                       |
      | destinationId       | happy                                    |
      | ttla                | DEL                                      |
      | ftla                | SFO                                      |
      | packageTripType     | 2                                        |
      | adultsPerRoom[1]    | 2                                        |
      | numberOfRooms       | 1                                        |
      | toDate              | 30                                       |
      | originId            | happy                                    |
      | childrenPerRoom[1]  | 2                                        |
      | childAges[1][1]     | 10                                       |
      | childAges[1][2]     | 10                                       |