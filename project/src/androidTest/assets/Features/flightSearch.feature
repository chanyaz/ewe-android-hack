Feature: Flights testing

  @Flights @SearchScreen
  Scenario: Verifying if round trip fields work

    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | sfo                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I change travellers count and press done
    Then I can trigger flights search


  @Flights @SearchScreen
  Scenario: Verifying if one way trip fields work

    Given I launch the App
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 5 |
    And I change travellers count and press done
    Then I can trigger flights search


  @Flights @SearchScreen
  Scenario: Verifying if round trip fields exist and are visible
    Given I launch the App
    When I launch "Flights" LOB
    Then departure field exists for flights search form
    And arrival field exists for flights search form
    And calendar field exists for flights search form

  @Flights @SearchScreen
  Scenario: Verifying if one way trip fields exist and are visible
    Given I launch the App
    And I launch "Flights" LOB
    When I select one way trip
    Then departure field exists for flights search form
    And calendar field exists for one way flights search form
    And arrival field exists for flights search form

  @Flights @SearchScreen @WIP
  Scenario: Verifying data consistency through screens for round trip
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


  @Flights @SearchScreen
  Scenario: Verifying data consistency for one way trip
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


  @Flights @SearchScreen
  Scenario Outline: Validating travellers form adults
    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I click on guest button
    And I increase the adult count to max
    And I press done
    Then <initialNumber> traveler count is as selected by user
    When I click on guest button
    And reduce the travellers count
    And I press done
    Then <laterNumber> traveler count is as selected by user

    Examples:
      | initialNumber | laterNumber |
      | 6             | 5           |


  @Flights @SearchScreen
  Scenario Outline: Validating travellers form children
    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I click on guest button
    And I increase the child count to max
    And equal number of age pickers are shown
    And the default age is 10 years
    And I press done
    Then <initialNumber> traveler count is as selected by user
    When I click on guest button
    And Reduce the child count
    Then corresponding age picker is removed
    When I press done
    Then <laterNumber> traveler count is as selected by user

    Examples:
      | initialNumber | laterNumber |
      | 5             | 4           |


  @Flights @SearchScreen
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

  @Flights @SearchScreen
  Scenario: Calender validation - Calender widget is displayed after selecting Flight locations
    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source                | SFO                                       |
      | destination           | DEL                                       |
      | source_suggest        | San Francisco, CA                     |
      | destination_suggest   | Delhi, India (DEL - Indira Gandhi Intl.)  |
    Then Validate that Calender widget is displayed: true
    And Validate that Current Month calender is displayed
    And Validate that Done button is disabled
    And Validate that Previous month arrow is displayed: false

  @Flights @SearchScreen
  Scenario: Calender fields/text validation for Search Screen when no dates are selected and a Round trip.

    Given I launch the App
    And I launch "Flights" LOB
    And I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And Validate that Current Month calender is displayed
    And Validate that Done button is disabled
    And Validate that Previous month arrow is displayed: false
    And Validate that Next month arrow is displayed: true
    And Validate that "Select departure date" text below calender title is displayed

  @Flights @SearchScreen
  Scenario: Calender fields/text validation for Search Screen when selecting dates and a Round trip

    Given I launch the App
    And I launch "Flights" LOB
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And I choose departure date for flights-roundtrip and validate the tool tip
      | start_date | 5  |
    And I choose return date for flights-roundtrip and validate the tool tip
      | start_date | 5  |
      | end_date   | 10 |
    And Validate that Done button is enabled
    And I Click on Done button
    And Validate the selected date on calender button
      | start_date | 5  |
      | end_date   | 10 |

  @Flights @SearchScreen
  Scenario: Calender fields/text validation for Search Screen when a Round trip and selecting departure date only

    Given I launch the App
    And I launch "Flights" LOB
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And I choose departure date for flights-roundtrip and validate the tool tip
      | start_date | 5  |
    And Validate that Done button is enabled
    And I Click on Done button
    And Validate the selected date on calender button
      | start_date | 5  |

  @Flights @SearchScreen
  Scenario: Calender fields/text validation for Search Screen when selecting dates and a OneWay trip

    Given I launch the App
    And I launch "Flights" LOB
    And I select one way trip
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And I choose departure date for flights-oneway and validate the tool tip
      | start_date | 5  |
    And Validate that Done button is enabled
    And I Click on Done button
    And Validate the selected date on calender button
      | start_date | 5  |
      | isRoundTrip | false  |

  @Flights @SearchScreen
  Scenario: Previous/Next month button validation of Calender Widget

    Given I launch the App
    And I launch "Flights" LOB
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    Then I click on Next month button
    Then Validate that next month calender is displayed
    Then Validate that Previous month arrow is displayed: true
    Then Validate that Next month arrow is displayed: true
    Then I click on Previous month button
    Then Validate that Current Month calender is displayed
    Then Validate that Previous month arrow is displayed: false
    Then Validate that Next month arrow is displayed: true