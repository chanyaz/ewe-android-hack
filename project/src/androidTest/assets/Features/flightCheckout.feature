Feature: Flights Checkout

  @Flights @FlightCheckout
  Scenario: Verify that Main traveler and credit card (if single stored) should auto -populate for Logged in User
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
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select first flight
    And I wait for inbound flights results to load
    And I select first inbound flight
    When I click on checkout button
    Then I login with user having single stored card at checkout screen
    And I wait for checkout screen to load
    And Validate that Main traveller "single card" is selected by default
    And Validate that Credit card "Visa 1111" is selected by default
    And I click on Payment Info
    And Validate that Credit card "Saved Visa 1111" is shown selected at Payment Method screen


  @Flights @FlightCheckout
  Scenario: Verify that Main traveler and credit card (if multiple stored) should auto-populate for Logged in User
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
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select first flight
    And I wait for inbound flights results to load
    And I select first inbound flight
    When I click on checkout button
    Then I login with user having multiple stored card at checkout screen
    And I wait for checkout screen to load
    And Validate that Main traveller "Mock Web Server" is selected by default
    And Validate that Credit card "AmexTesting" is selected by default
    And I click on Payment Info
    And Validate that Credit card "Saved AmexTesting" is shown selected at Payment Method screen
