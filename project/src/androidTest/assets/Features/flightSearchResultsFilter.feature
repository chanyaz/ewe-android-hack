Feature: Testing sort and filter button of flight search screen

  @Flights @FlightFilters
  Scenario: Verify by default Maximum flight duration is selected

    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 2                                        |
      | child               | 2                                        |
    And I wait for results to load
    And Validate that flight search results are displayed
    And I click on sort and filter icon and isOutBound : true
    Then Validate that default flight duration is set to maximum

  @Flights @FlightFilters
  Scenario: Verify scrubber moves by an hour

    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 2                                        |
      | child               | 2                                        |
    And I wait for results to load
    And Validate that flight search results are displayed
    And I click on sort and filter icon and isOutBound : true
    And I move the scrubber by an hour
    Then Validate scrubber moves by an hour

  @Flights @FlightFilters
  Scenario: Verify number of results after filter applied

    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 2                                        |
      | child               | 2                                        |
    And I wait for results to load
    And Validate that flight search results are displayed
    And I click on sort and filter icon and isOutBound : true
    And I select "1 Stop" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    Then Validate that after filter applied the number of result changes
    And I click on sort and filter icon and isOutBound : true
    And I select "Nonstop" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    Then Validate that after filter applied the number of result changes

  @Flights @FlightFilters
  Scenario: Verify filtered results are as per the criteria

    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 2                                        |
      | child               | 2                                        |
    And I wait for results to load
    And Validate that flight search results are displayed
    And I click on sort and filter icon and isOutBound : true
    And I select "Nonstop" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    Then Validate all results are "Nonstop"
    And I click on sort and filter icon and isOutBound : true
    And I select "Nonstop" checkbox and isOutBound : true
    And I select "1 Stop" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    Then Validate all results are "1 Stop"

  @Flights @FlightFilters
  Scenario: Verify number of results while applying filter

    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 2                                        |
      | child               | 2                                        |
    And I wait for results to load
    And Validate that flight search results are displayed
    And I click on sort and filter icon and isOutBound : true
    And I select "1 Stop" checkbox and isOutBound : true
    And Validate that the Dynamic Feedback TextView is Visible
    Then Validate Number of Results in Dynamic Feedback TextView changes
    And I select "Nonstop" checkbox and isOutBound : true
    Then Validate Number of Results in Dynamic Feedback TextView changes
    And I select "1 Stop" checkbox and isOutBound : true
    Then Validate Number of Results in Dynamic Feedback TextView changes

