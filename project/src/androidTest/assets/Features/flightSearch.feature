Feature: Flights testing

#  @Flights @SearchScreen
#  Scenario: Verifying if round trip fields work
#
#    Given I launch the App
#    And I launch "Flights" LOB
#    When I enter source and destination for flights
#      | source              | sfo                                      |
#      | destination         | DEL                                      |
#      | source_suggest      | San Francisco, CA                        |
#      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
#    And I pick dates for flights
#      | start_date | 5  |
#      | end_date   | 10 |
#    And I change travellers count and press done
#    Then I can trigger flights search
#
#
#  @Flights @SearchScreen
#  Scenario: Verifying if one way trip fields work
#
#    Given I launch the App
#    And I launch "Flights" LOB
#    And I select one way trip
#    When I enter source and destination for flights
#      | source              | SFO                                      |
#      | destination         | DEL                                      |
#      | source_suggest      | San Francisco, CA                        |
#      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
#    And I pick departure date for flights
#      | start_date | 5 |
#    And I change travellers count and press done
#    Then I can trigger flights search
#
#
#  @Flights @SearchScreen
#  Scenario: Verifying if round trip fields exist and are visible
#    Given I launch the App
#    When I launch "Flights" LOB
#    Then departure field exists for flights search form
#    And arrival field exists for flights search form
#    And calendar field exists for flights search form
#
#
#  @Flights @SearchScreen
#  Scenario: Verifying if round trip fields exists
#    Given I launch the App
#    And I launch "Flights" LOB
#    When I select one way trip
#    Then departure field exists for flights search form
#    And calendar field exists for one way flights search form
#    And arrival field exists for flights search form
#
#  @Flights @SearchScreen
#  Scenario: Verifying data consistency through screens for round trip
#    Given I launch the App
#    And I launch "Flights" LOB
#    When I make a flight search with following parameters
#      | source              | SFO                                      |
#      | destination         | DEL                                      |
#      | source_suggest      | San Francisco, CA                        |
#      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
#      | start_date          | 5                                        |
#      | end_date            | 25                                       |
#      | adults              | 3                                        |
#      | child               | 2                                        |
#    Then on FSR the destination is "Delhi"
#    And on FSR the date is as user selected
#    And on inbound FSR the number of traveller are as user selected
#    And I select first flight
#    And I verify date is as user selected for inbound flight
#    And on outbound FSR the number of traveller are as user selected
#
#
#  @Flights @SearchScreen
#  Scenario: Verifying data consistency for one way trip
#    Given I launch the App
#    And I launch "Flights" LOB
#    And I select one way trip
#    When I make a flight search with following parameters
#      | source              | SFO                                      |
#      | destination         | DEL                                      |
#      | source_suggest      | San Francisco, CA                        |
#      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
#      | start_date          | 5                                        |
#      | end_date            | 10                                       |
#      | adults              | 3                                        |
#      | child               | 2                                        |
#    Then on FSR the destination is "Delhi"
#    And on FSR the date is as user selected
#    And on inbound FSR the number of traveller are as user selected
#
#
#  @Flights @SearchScreen
#  Scenario Outline: Validating travellers form adults
#
#    Given I launch the App
#    And I launch "Flights" LOB
#    When I enter source and destination for flights
#      | source              | SFO                                      |
#      | destination         | DEL                                      |
#      | source_suggest      | San Francisco, CA                        |
#      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
#    And I pick dates for flights
#      | start_date | 5  |
#      | end_date   | 10 |
#    And I click on guest button
#    And I increase the adult count to max
#    And I press done
#    Then <initialNumber> traveler count is as selected by user
#    When I click on guest button
#    And reduce the travellers count
#    And I press done
#    Then <laterNumber> traveler count is as selected by user
#
#    Examples:
#      | initialNumber | laterNumber |
#      | 6             | 5           |
#
#
#  @Flights @SearchScreen
#  Scenario Outline: Validating travellers form children
#
#    Given I launch the App
#    And I launch "Flights" LOB
#    When I enter source and destination for flights
#      | source              | SFO                                      |
#      | destination         | DEL                                      |
#      | source_suggest      | San Francisco, CA                        |
#      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
#    And I pick dates for flights
#      | start_date | 5  |
#      | end_date   | 10 |
#    And I click on guest button
#    And I increase the child count to max
#    And equal number of age pickers are shown
#    And the default age is 10 years
#    And I press done
#    Then <initialNumber> traveler count is as selected by user
#    When I click on guest button
#    And Reduce the child count
#    Then corresponding age picker is removed
#    When I press done
#    Then <laterNumber> traveler count is as selected by user
#
#    Examples:
#      | initialNumber | laterNumber |
#      | 5             | 4           |


  @Flights @SearchScreen @WIP
  Scenario Outline: Verifying data consistency through screens for round trip
    Given I launch the App
    Then I set Flight Round Trip AB test to "<variant>"
    And 2 AB test
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
      | variant  | AirlineName    | price | duration | timing             | number |
      | bucketed | Virgin America | 800   | 4h 35m   | 5:40 pm - 10:15 pm | 1      |
