@Flights @FlightsCheckout
Feature: Flights Checkout

  @Flights @FlightsCheckoutSet1
  Scenario: Verify that cardholder name field is present

    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
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

  @Flights @FlightsCheckoutSet1
  Scenario: Verify that cardholder field cannot be left blank
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
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
    Then I verify that error hint "Enter name as it appears on the card" is displayed for cardholder name

  @Flights @FlightsCheckoutSet1
  Scenario: Verify that on entering only first or last name will result in error
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
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
    Then I verify that error hint "Enter name as it appears on the card" is displayed for cardholder name

  @Flights @FlightsCheckoutSet1
  Scenario: Verify that user can enter both the first name and last name
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
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

  @Flights @FlightsCheckoutSet1
  Scenario: Verify that Main traveler and credit card (if single stored) should auto -populate for Logged in User
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
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


  @Flights @FlightsCheckoutSet1
  Scenario: Verify that Main traveler and credit card (if multiple stored) should auto-populate for Logged in User
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
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

  @Flights @Prod @FlightsCheckoutSet1
  Scenario: Passport field is mandatory on checkout in international flights

    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 20                                        |
      | end_date            | 30                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
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
    Then I verify that error hint "Select a passport country" is displayed for Passport

  @Flights @Prod @FlightsCheckoutSet1
  Scenario: Passport field is mandatory on checkout in domestic flights for AirAsia
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | KUL                                      |
      | destination         | PEN                                      |
      | source_suggest      | KUL - Kuala Lumpur Intl. |
      | destination_suggest | Penang, Malaysia (PEN - Penang Intl.) |
      | start_date          | 20                                        |
      | end_date            | 30                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I click on sort and filter icon and isOutBound : true
    And I scroll to Airline Section
    And I select "AirAsia" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
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
    Then I verify that error hint "Select a passport country" is displayed for Passport

  @Flights @FlightsCheckoutSet2 @Prod
  Scenario: Verify that traveler details and card details are erased on each create trip from checkout page for round trip flights.
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 15                                        |
      | end_date            | 20                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
    When I click on checkout button
    And I wait for checkout screen to load
    And I open traveller details
    When I fill the following details in the traveller details form:
      | firstName   | Mobiata      |
      | lastName    | Auto         |
      | email       | abc@exp.com  |
      | phoneNumber | 3432234      |
      | year        | 1990         |
      | month       | 3            |
      | date        | 23           |
      | gender      | Male         |
      | passport    | United States|
    And I save the traveller details by hitting done
    And I click on Payment Info
    And I fill the payment details
    And Validate that Main traveller "Mobiata Auto" is selected by default
    And Validate that Credit card "Visa …1111" is selected by default
    And I press back
    And I press back
    And I press back
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
    When I click on checkout button
    And I wait for checkout screen to load
    And Validate that Main traveller "Enter traveler details" is selected by default
    And Validate that Credit card "Enter payment details" is selected by default

  @Flights @FlightsCheckoutSet2 @Prod
  Scenario: Verify that traveler details and card details are erased on each create trip from checkout page for one-way trip flights.
    Given I launch the App
    And I put following tests in control
      | FlightsCrossSellPackage |
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | sfo                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 5 |
    And I can trigger flights search
    And I wait for results to load
    And I select outbound flight at position 1 and reach inbound FSR
    And Close price change Alert dialog if it is visible
    When I click on checkout button
    And I wait for checkout screen to load
    And I open traveller details
    When I fill the following details in the traveller details form:
      | firstName   | Mobiata      |
      | lastName    | Auto         |
      | email       | abc@exp.com  |
      | phoneNumber | 3432234      |
      | year        | 1990         |
      | month       | 3            |
      | date        | 23           |
      | gender      | Male         |
      | passport    | United States|
    And I save the traveller details by hitting done
    And I click on Payment Info
    And I fill the payment details
    And Validate that Main traveller "Mobiata Auto" is selected by default
    And Validate that Credit card "Visa …1111" is selected by default
    And I press back
    And I press back
    And I press back
    And I select outbound flight at position 2 and reach inbound FSR
    And Wait for checkout screen to load after createTrip
    And Close price change Alert dialog if it is visible
    When I click on checkout button
    And I wait for checkout screen to load
    And Validate that Main traveller "Enter traveler details" is selected by default
    And Validate that Credit card "Enter payment details" is selected by default

  @Flights @FlightsCheckoutSet2
    Scenario: Verify that error message should be displayed if card is not accepted by certain flight on payment screen.
      Given I launch the App
      And I launch "Flights" LOB
      When I make a flight search with following parameters
        | source              | SFO                                      |
        | destination         | DEL                                      |
        | source_suggest      | San Francisco, CA                        |
        | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
        | start_date          | 15                                       |
        | end_date            | 20                                       |
        | adults              | 1                                        |
        | child               | 0                                        |
      And I wait for results to load
      And I select outbound flight at position 2 and reach inbound FSR
      And I wait for inbound flights results to load
      And I select inbound flight at position 1 and reach overview
      And Close price change Alert dialog if it is visible
      When I click on checkout button
      And I wait for checkout screen to load
      And I click on Payment Info
      Then I verify that field to enter credit card is present on the payment details form
      Then I enter the card number
        | card_number              | 343434343434343                    |
      Then Validate the error message displayed
        | error_message            | Airline does not accept American Express    |
      And Also verify the credit card image when card is not accepted for payment
