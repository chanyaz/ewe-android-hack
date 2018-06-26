Feature: Sorting of Flight results

  @Prod @Flights @FlightSortSet1
  Scenario: Verify flight results are sorted by price by default
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | LHR                                      |
      | destination         | Spain                                    |
      | source_suggest      | London, England, UK (LHR - Heathrow)     |
      | destination_suggest | BCN - Barcelona Intl.                    |
      | start_date          | 15                                       |
      | end_date            | 20                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    Then flight results are sorted by "price"
    When I click on sort and filter icon and isOutBound : true
    Then "Price" sorting is shown as selected


  @Prod @Flights @FlightSortSet1
  Scenario: Verify flight results can be sorted by departure time
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | LHR                                      |
      | destination         | Spain                                    |
      | source_suggest      | London, England, UK (LHR - Heathrow)     |
      | destination_suggest | BCN - Barcelona Intl.                    |
      | start_date          | 15                                       |
      | end_date            | 20                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I click on sort and filter icon and isOutBound : true
    And I sort results by "Departure time"
    And save the sort and filter selection
    And I wait for results to load
    Then flight results are sorted by "Departure time"


  @Prod @Flights @FlightSortSet2
  Scenario: Verify flight results can be sorted by arrival time
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | LHR                                      |
      | destination         | Spain                                    |
      | source_suggest      | London, England, UK (LHR - Heathrow)     |
      | destination_suggest | BCN - Barcelona Intl.                    |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I click on sort and filter icon and isOutBound : true
    And I sort results by "Arrival time"
    And save the sort and filter selection
    And I wait for results to load
    Then flight results are sorted by "Arrival time"


  @Prod @Flights @FlightSortSet2
  Scenario: Verify flight results can be sorted by flight duration
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | LHR                                      |
      | destination         | Spain                                    |
      | source_suggest      | London, England, UK (LHR - Heathrow)     |
      | destination_suggest | Barcelona, Spain                         |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 1                                        |
      | child               | 1                                        |
    And I wait for results to load
    And I click on sort and filter icon and isOutBound : true
    And I sort results by "Duration"
    And save the sort and filter selection
    And I wait for results to load
    Then flight results are sorted by "Duration"


