Feature: Sorting of Flight results

  @Prod @Flights @FlightSort
  Scenario: Verify flight results are sorted by price by default
    Given I launch the App
    And I bucket the following tests
      | FlightStaticSortFilter  |
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
    Then flight results are sorted by "price"
    When I click on sort and filter icon
    Then "Price" sorting is shown as selected


  @Prod @Flights @FlightSort
  Scenario: Verify flight results can be sorted by departure time
    Given I launch the App
    And I bucket the following tests
      | FlightStaticSortFilter  |
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
    And I click on sort and filter icon
    And I sort results by "Departure time"
    And save the sort and filter selection
    And I wait for results to load
    Then flight results are sorted by "Departure time"


  @Prod @Flights @FlightSort
  Scenario: Verify flight results can be sorted by arrival time
    Given I launch the App
    And I bucket the following tests
      | FlightStaticSortFilter  |
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
    And I click on sort and filter icon
    And I sort results by "Arrival time"
    And save the sort and filter selection
    And I wait for results to load
    Then flight results are sorted by "Arrival time"


  @Prod @Flights @FlightSort
  Scenario: Verify flight results can be sorted by flight duration
    Given I launch the App
    And I bucket the following tests
      | FlightStaticSortFilter  |
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
    And I click on sort and filter icon
    And I sort results by "Duration"
    And save the sort and filter selection
    And I wait for results to load
    Then flight results are sorted by "Duration"


