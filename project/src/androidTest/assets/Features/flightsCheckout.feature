@Flights @FlightsCheckout
Feature: Flights Checkout

  @Flights @FlightsCheckout
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
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    And I tap on payment details
    Then I verify that cardholder name field is present on the payment details form

  @Flights @FlightsCheckout
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
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    And I tap on payment details
    Then I tap on the cardholder name field
    Then I tap on some other field say Address field
    Then I verify that a red exclamation is displayed on cardholder name

  @Flights @FlightsCheckout
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
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    And I tap on payment details
    Then I tap on the cardholder name field
    Then I enter the first name
    Then I tap on some other field say Address field
    Then I verify that a red exclamation is displayed on cardholder name

  @Flights @FlightsCheckout
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
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    And I tap on payment details
    Then I tap on the cardholder name field
    Then I enter the first name and last name
    Then I tap on some other field say Address field
    Then I verify that no red exclamation is displayed on cardholder name

  @Flights @FlightsCheckout
  Scenario: Verify that Main traveler and credit card (if single stored) should auto -populate for Logged in User
    Given I launch the App
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
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    Then I login with user having single stored card at checkout screen
    And I wait for checkout screen to load
    And Validate that Main traveller "single card" is selected by default
    And Validate that Credit card "Visa 1111" is selected by default
    And I click on Payment Info
    And Validate that Credit card "Saved Visa 1111" is shown selected at Payment Method screen


  @Flights @FlightsCheckout
  Scenario: Verify that Main traveler and credit card (if multiple stored) should auto-populate for Logged in User
    Given I launch the App
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
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    When I click on checkout button
    Then I login with user having multiple stored card at checkout screen
    And I wait for checkout screen to load
    And Validate that Main traveller "Mock Web Server" is selected by default
    And Validate that Credit card "AmexTesting" is selected by default
    And I click on Payment Info
    And Validate that Credit card "Saved AmexTesting" is shown selected at Payment Method screen

  @Flights @Prod @FlightsCheckout
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

  @Flights @Prod @FlightsCheckout
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

