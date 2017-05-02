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



  @Flights @FlightSearchResults @CALocale @Prod
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
    And I select outbound flight at position 1 and reach inbound FSR
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


  @Flights @Search @FlightSearchResults
  Scenario: Validate urgency message is displayed when seats left is less than 6
    Given I launch the App
    And I bucket the following tests
      | UrgencyMessegingOnFSR |
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



  @Flights @Search @FlightSearchResults
  Scenario: Validate urgency message is not displayed when seats left is greater than 6
    Given I launch the App
    And I bucket the following tests
      | UrgencyMessegingOnFSR |
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


  @Flights @Search @FlightSearchResults
  Scenario: Verify roundtrip messaging not shown for one way trip
    Given I launch the App
    And I bucket the following tests
      | RoundTripOnFlightsFSR |
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
    And Validate that round trip header at cell 1 is displayed: false and isOutBound : true

  @Flights @Search @FlightSearchResults @Prod @FlightCheckout
  Scenario: Passport field is mandatory on checkout in international flights

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
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    And I open traveller details
    Then Passport field is present on the traveler info form
    When I fill the following details in the traveller details form:
      | firstName   | Expedia      |
      | lastName    | Automaton    |
      | email       | abc@exp.com  |
      | phoneNumber | 3432234      |
      | year        | 1990         |
      | month       | 3            |
      | date        | 23           |
      | gender      | Male         |
    And I save the traveller details by hitting done
    Then Traveller details are not saved
    And Passport field is shown as a mandatory field


  @Flights @Search @FlightSearchResults
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


  @Flights @Search @FlightSearchResults @Prod @FlightCheckout
  Scenario: Passport field is mandatory on checkout in domestic flights for AirAsia
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | KUL                                      |
      | destination         | PEN                                      |
      | source_suggest      | Kuala Lumpur, Malaysia (KUL - All Airports) |
      | destination_suggest | Penang, Malaysia (PEN - Penang Intl.) |
      | start_date          | 15                                        |
      | end_date            | 25                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I click on sort and filter icon
    And I scroll to Airline Section
    And I select "AirAsia" checkbox
    And I click on sort and filter screen done button
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    And I open traveller details
    Then Passport field is present on the traveler info form
    When I fill the following details in the traveller details form:
      | firstName   | Expedia      |
      | lastName    | Automaton    |
      | email       | abc@exp.com  |
      | phoneNumber | 3432234      |
      | year        | 1990         |
      | month       | 3            |
      | date        | 23           |
      | gender      | Male         |
    And I save the traveller details by hitting done
    Then Traveller details are not saved
    And Passport field is shown as a mandatory field

    
  @Flights @Search @FlightSearchResults
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


  @Flights @Search @FlightSearchResults
  Scenario: Multi-Carrier as airline name text for more than 2 airlines when bucketed for RoundTripOnFlightsFSR AB test
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
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    Then multi carrier text is shown instead of Airline Name on cell 4 isOutbound : true
