Feature: Flights testing

  @Flights @SearchScreen
  Scenario: Verifying if round trip fields work

    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source                | sfo                                       |
      | destination           | DEL                                       |
      | source_suggest        | San Francisco, CA                         |
      | destination_suggest   | Delhi, India (DEL - Indira Gandhi Intl.)  |
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
      | start_date | 5  |
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
  Scenario: Verifying if round trip fields exists
    Given I launch the App
    And I launch "Flights" LOB
    When I select one way trip
    Then departure field exists for flights search form
    And calendar field exists for one way flights search form
    And arrival field exists for flights search form

  @Flights @SearchScreen
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
