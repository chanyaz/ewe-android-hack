@Flights @FlightsCheckout
Feature: Flights Checkout

  Scenario: Verify that cardholder name field is present

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
    And I select first flight
    And I wait for inbound flights results to load
    And I select first inbound flight
    When I click on checkout button
    And I tap on payment details
    Then I verify that cardholder name field is present on the payment details form

  Scenario: Verify that cardholder field cannot be left blank
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
    And I select first flight
    And I wait for inbound flights results to load
    And I select first inbound flight
    When I click on checkout button
    And I tap on payment details
    Then I tap on the cardholder name field
    Then I tap on some other field say Address field
    Then I verify that a red exclamation is displayed on cardholder name

  Scenario: Verify that on entering only first or last name will result in error
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
    And I select first flight
    And I wait for inbound flights results to load
    And I select first inbound flight
    When I click on checkout button
    And I tap on payment details
    Then I tap on the cardholder name field
    Then I enter the first name
    Then I tap on some other field say Address field
    Then I verify that a red exclamation is displayed on cardholder name

  Scenario: Verify that user can enter both the first name and last name
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
    And I select first flight
    And I wait for inbound flights results to load
    And I select first inbound flight
    When I click on checkout button
    And I tap on payment details
    Then I tap on the cardholder name field
    Then I enter the first name and last name
    Then I tap on some other field say Address field
    Then I verify that no red exclamation is displayed on cardholder name
