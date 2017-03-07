Feature: Testing sort and filter button of flight search screen


  @Flights @Search @FlightResults
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
    And I click on sort and filter icon
    Then Validate that default flight duration is set to maximum

  @Flights @Search @FlightResults
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
    And I click on sort and filter icon
    And I move the scrubber by an hour
    Then Validate scrubber moves by an hour

