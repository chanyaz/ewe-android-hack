Feature: Flights Overview

  @Flights @FlightsOverviewSet1
  Scenario: Verify data consistency through Overview screen for round trip search
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
    Then toggle the outbound widget
    Then validate following information is present on the overview screen for isOutbound : true
      | destination                    | (DEL)                                             |
      | travel date and traveller      | Mar 22 at 9:00 pm, 1 traveler                     |
      | Flight time                    | 9:00 pm - 11:00 pm                                     |
      | airport names                  | (SFO) SFO - (DEL) DEL                             |
      | airline name                   | happy_round_trip                                  |
      | flight duration                | 2h 0m                                             |
    And validate total duration on flight Overview is "2h 0m" for isOutbound : true
    Then toggle the outbound widget
    Then toggle the inbound widget
    Then validate following information is present on the overview screen for isOutbound : false
      | destination                    | (SFO)                                             |
      | travel date and traveller      | Mar 22 at 5:40 pm, 1 traveler                       |
      | Flight time                    | 5:40 pm - 8:15 pm                                      |
      | airport names                  | (DEL) DEL - (SFO) SFO                             |
      | airline name                   | American Airlines 179                             |
      | flight duration                | 2h 35m                                            |
    And validate total duration on flight Overview is "2h 35m" for isOutbound : false
    And toggle the inbound widget

  @Flights @FlightsOverviewSet1
  Scenario: Verify data consistency for multi-leg flights on Overview screen.
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
    And I select outbound flight at position 2 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And I click on Ok button of Alert dialog
    Then toggle the outbound widget
    Then validate following flight details for multi-leg flights
      | first-segment-flight time      | 5:40 pm - 8:15 pm                          |
      | first-segment-airport name     | (SEA) Seattle, USA - (LAX) Los Angeles, USA       |
      | first-segment-airline name     | Virgin America 798                                |
      | first-segment-flight duration  | 2h 35m                                            |
      | second-segment-flight time     | 9:00 pm - 10:15 pm                                |
      | second-segment-airport name    | (LAX) Los Angeles, USA - (SFO) San Francisco, USA |
      | second-segment-airline name    | Virgin America 947                                |
      | second-segment-flight duration | 1h 15m                                            |
    And validate layover of outbound flight is on "(LAX) Los Angeles, USA" for "45m"
    And validate total duration on flight Overview is "4h 35m" for isOutbound : true

  @Flights @FlightsOverviewSet2 @Prod
  Scenario: Verify message on free cancellation and split ticket messaging on Overview screen.
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | LAS                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | LAS - McCarran Intl.                     |
      | start_date          | 55                                       |
      | end_date            | 60                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I click on sort and filter icon and isOutBound : true
    And I select "Nonstop" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    And I choose the flight with airline name "United" and isOutBound : true
    Then Select outbound flight from Overview
    And I click on sort and filter icon and isOutBound : false
    And I select "Nonstop" checkbox and isOutBound : false
    And I click on sort and filter screen done button
    And I choose the flight with airline name "Virgin America" and isOutBound : false
    And Close price change Alert dialog if it is visible
    And Wait for checkout button to display
    Then validate free cancellation message "Free cancellation within 24 hours" is displayed
    And validate split ticket messaging is displayed

  @Flights @FlightsOverviewSet1
  Scenario: Verify price details on cost summary popup on Flights Overview.
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
    And I select outbound flight at position 2 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
    Then validate total price of the trip is "$64"
    Then I click on trip total link
    Then validate following detailed information is present on cost summary screen
      | Adult 1 details     | $64.00 |
      | Flight              | $48.95 |
      | Taxes & Fees        | $15.05 |
      | Booking Fee | $0.00  |
      | Total Due Today     | $64.00 |
    And I click on Done button
    And I click on checkout button

  @Flights @FlightsOverviewTest @Prod
  Scenario: Verify cost summary popup for multi-travellers on Flights Overview.
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
    And I click on trip total link
    Then validate price info for multi travellers
    And validate Booking Fee text is displayed
    And validate price for "Total Due Today" is displayed

  @Flights @FlightsOverviewSet2
  Scenario: Verify basic economy link on Overview screen is visible
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
    And Wait for checkout button to display
    Then basic economy link with text "Please Read Important Flight Restrictions" isDisplayed : true

  @Flights @Prod @EBOnlySet1
  Scenario: Intercept Flight Search and Overview API call and validate request parameters for SubPub and Flex
    Given I launch the App
    And I set the POS to "Singapore"
    And I bucket the following tests
     | FlightSubpub |
     | FlightFlex   |
    And I launch "Flights" LOB
    And I want to intercept these calls
     | FlightSearch |
    When I make a flight search with following parameters
     | source              | SFO                                      |
     | destination         | DEL                                      |
     | source_suggest      | San Francisco, CA                        |
     | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
     | start_date          | 15                                        |
     | end_date            | 20                                       |
     | adults              | 2                                        |
     | child               | 2                                        |
    And I wait for results to load
    Then Validate the "flight Search" API request query data for following parameters
     | featureOverride                       | SubPub                          |
     | cabinClassPreference                  | COACH                           |
     | maxOfferCount                         | 1600                            |
     | childTravelerAge                      | 10                              |
     | lccAndMerchantFareCheckoutAllowed     | true                            |
     | sourceType                            | mobileapp                       |
    And I wait for results to load
    And I want to intercept these calls
     | FlightCreateTrip |
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    Then Validate the "flight CreateTrip" API request query data for following parameters
     | featureOverride                       | SubPub                          |
     | withInsurance                         | true                            |
     | sourceType                            | mobileapp                       |
     | mobileFlexEnabled                     | true                            |
    And Close price change Alert dialog if it is visible
    And I click on checkout button

  @Flights @FlightsOverviewSet2
  Scenario: Verify price details on cost summary popup on Flights Overview for Subpub.
    Given I launch the App
    And I bucket the following tests
         | FlightSubpub |
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
    And Close price change Alert dialog if it is visible
    Then I click on trip total link
    Then validate following detailed information is present on cost summary screen
      | Adult 1 details     | $689.00 |
      | Flight              | $620.46 |
      | Taxes & Fees        | $68.54  |
      | Booking Fee         | $7.00   |
      | Expedia Discount    | -$1.70  |
      | Total Due Today     | $696.00  |
    And I click on Done button


  @Flights @FlightsOverviewSet2
  Scenario: Verify the price decrease dialog box appears on price change.
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
    And I select outbound flight at position 2
    Then Select outbound flight from Overview
    And Validate that alert Dialog Box with title "Price Change" is visible
    And Validate Price Change to "$696.00" from "$763.00"
    And Close price change Alert dialog
    And Check if Trip total is "$696" on Price Change
    And Check if Cost Summary Dialog Box has "$696.00" as Final Price


  @Flights @FlightsOverviewSet2
  Scenario: Verify the price increase dialog box appears on price change.
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
    And I select outbound flight at position 12
    Then Select outbound flight from Overview
    And Validate that alert Dialog Box with title "Price Change" is visible
    And Validate Price Change to "$896.00" from "$763.00"
    And Close price change Alert dialog
    And Check if Trip total is "$896" on Price Change
    And Check if Cost Summary Dialog Box has "$896.00" as Final Price

    @Flights @FlightsOverviewSet2
    Scenario: Verify the "sold out flights" scenario.
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
      And I select outbound flight at position 4
      Then Select outbound flight from Overview
      Then Validate if error-toolbar has text "Sold Out"
      Then Validate if error image is of "Expedia"
      Then Validate that error-action-button is present and have text "New Search"
      Then Validate that error text is "We're sorry. This flight has sold out."
      And I press back
      And I can trigger flights search
      And I select outbound flight at position 4
      Then Select outbound flight from Overview
      Then Validate if error-toolbar has text "Sold Out"
      Then Validate if error image is of "Expedia"
      Then Validate that error-action-button is present and have text "New Search"
      Then Validate that error text is "We're sorry. This flight has sold out."
      And Click on "New Search" button
      Then Validate search form retains details of search for flights
        | source              | SFO - San Francisco Intl.                |
        | destination         | DEL - Indira Gandhi Intl.                |
        | start_date          | 5                                        |
        | totalTravelers      | 1 traveler                               |
        | flightClass         | Economy                                  |

  @Flights @FlightsOverviewSet2
  Scenario: Verify the "session time out" scenario.
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
    And I select outbound flight at position 6
    Then Select outbound flight from Overview
    Then Validate if error-toolbar has text "Session Expired"
    Then Validate if error image is of "Watch"
    Then Validate that error-action-button is present and have text "New Search"
    Then Validate that error text is "Still there? Your session has expired. Please try your search again."
    And I press back
    And I can trigger flights search
    And I select outbound flight at position 6
    Then Select outbound flight from Overview
    Then Validate if error-toolbar has text "Session Expired"
    Then Validate if error image is of "Watch"
    Then Validate that error-action-button is present and have text "New Search"
    Then Validate that error text is "Still there? Your session has expired. Please try your search again."
    And Click on "New Search" button
    Then Validate search form retains details of search for flights
      | source              | SFO - San Francisco Intl.                |
      | destination         | DEL - Indira Gandhi Intl.                |
      | start_date          | 5                                        |
      | totalTravelers      | 1 traveler                               |
      | flightClass         | Economy                                  |

  @Flights @FlightsOverviewSet1
  Scenario: Verify the "flight unavailable" scenario.
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
    And I select outbound flight at position 5
    Then Select outbound flight from Overview
    Then Validate if error-toolbar has text "Flight Unavailable"
    Then Validate if error image is of "Expedia"
    Then Validate that error-action-button is present and have text "New Search"
    Then Validate that error text is "We're sorry. This flight is no longer available"
    And I press back
    And I can trigger flights search
    And I select outbound flight at position 5
    Then Select outbound flight from Overview
    Then Validate if error-toolbar has text "Flight Unavailable"
    Then Validate if error image is of "Expedia"
    Then Validate that error-action-button is present and have text "New Search"
    Then Validate that error text is "We're sorry. This flight is no longer available"
    And Click on "New Search" button
    Then Validate search form retains details of search for flights
      | source              | SFO - San Francisco Intl.                |
      | destination         | DEL - Indira Gandhi Intl.                |
      | start_date          | 5                                        |
      | totalTravelers      | 1 traveler                               |
      | flightClass         | Economy                                  |

  @Flights @FlightsOverviewSet1
  Scenario: Verify the "unknown error" scenario.
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
    And I select outbound flight at position 3
    Then Select outbound flight from Overview
    Then Validate if error-toolbar has text "Error"
    Then Validate if error image is of "Expedia"
    Then Validate that error-action-button is present and have text "Retry"
    Then Validate that error text is "Whoops. Let's try that again."
    And I press back
    And I can trigger flights search
    And I select outbound flight at position 3
    Then Select outbound flight from Overview
    Then Validate if error-toolbar has text "Error"
    Then Validate if error image is of "Expedia"
    Then Validate that error-action-button is present and have text "Retry"
    Then Validate that error text is "Whoops. Let's try that again."
    And Click on "Retry" button
    Then Validate search form retains details of search for flights
      | source              | SFO - San Francisco Intl.                |
      | destination         | DEL - Indira Gandhi Intl.                |
      | start_date          | 5                                        |
      | totalTravelers      | 1 traveler                               |
      | flightClass         | Economy                                  |

  @Flights @FlightsOverviewSet1 @Prod
  Scenario: Upgrade flight to upper class for round trip
    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | LAS                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | LAS - McCarran Intl.                     |
      | start_date          | 55                                       |
      | end_date            | 60                                       |
      | adults              | 1                                        |
      | child               | 0                                        |
    And I wait for results to load
    And I click on sort and filter icon and isOutBound : true
    And I select "Nonstop" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    And I choose the flight with airline name "United" and isOutBound : true
    Then Select outbound flight from Overview
    And I click on sort and filter icon and isOutBound : false
    And I select "Nonstop" checkbox and isOutBound : false
    And I click on sort and filter screen done button
    And I choose the flight with airline name "United" and isOutBound : false
    And Wait for checkout button to display
    Then Validate that fare family widget card is displayed
    Then Validate fare family widget card info
      | title                 | Upgrade your flights         |
      | subtitle              | (Selected: .*Economy)        |
      | from_label            | From                         |
      | delta_price           | (\+\$\d*.\d\d)               |
    Then I click on fare family widget card
    Then Validate fare family details header info
      | title                 | Select your fare             |
      | location              | SFO - LAS - SFO              |
      | airline               | United                       |
    Then I select flight upgrade at position 2
    And I store the data in "varBundleTotal"
    Then I click on show more amenities at position 2
    Then Validate amenities dialog is visible
    Then I click on Ok button of Alert dialog
    Then I click on done button for upsell
    And Wait for checkout button to display
    Then Validate fare family card title is "(You've selected \w.*)"
    Then Validate fare family card subtitle is "Change fare class"
    Then Validate delta price is displayed : false
    Then Validate from label is displayed : false
    Then Validate bundle total amount is "varBundleTotal"


  @Flights @FlightsOverviewSet2 @Prod
  Scenario: Upgrade flight to upper class for oneway trip
    Given I launch the App
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | LAS                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | LAS - McCarran Intl.                     |
    And I pick departure date for flights
      | start_date | 55 |
    Then I can trigger flights search
    And I wait for results to load
    And I click on sort and filter icon and isOutBound : true
    And I select "Nonstop" checkbox and isOutBound : true
    And I click on sort and filter screen done button
    And I choose the flight with airline name "United" and isOutBound : true
    Then Select outbound flight from Overview
    And Wait for checkout button to display
    Then Validate that fare family widget card is displayed
    Then Validate fare family widget card info
      | title                 | Upgrade your flight          |
      | subtitle              | (Selected: .*Economy)        |
      | from_label            | From                         |
      | delta_price           | (\+\$\d*.\d\d)               |
    Then I click on fare family widget card
    Then Validate fare family details header info
      | title                 | Select your fare             |
      | location              | SFO - LAS                    |
      | airline               | United                       |
    Then I select flight upgrade at position 2
    And I store the data in "varBundleTotal"
    Then I click on show more amenities at position 2
    Then Validate amenities dialog is visible
    Then I click on Ok button of Alert dialog
    Then I click on done button for upsell
    And Wait for checkout button to display
    Then Validate fare family card title is "(You've selected \w.*)"
    Then Validate fare family card subtitle is "Change fare class"
    Then Validate delta price is displayed : false
    Then Validate from label is displayed : false
    Then Validate bundle total amount is "varBundleTotal"

    @Flights @FlightsOverviewSet3
      Scenario: Validate the fare family card on Flights Rate Details Page with multiple traveller
      Given I launch the App
      And I launch "Flights" LOB
      When I make a flight search with following parameters
        | source              | SFO                                      |
        | destination         | DEL                                      |
        | source_suggest      | San Francisco, CA                        |
        | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
        | start_date          | 5                                        |
        | end_date            | 25                                       |
        | adults              | 2                                        |
        | child               | 1                                        |
      And I wait for results to load
      And I select outbound flight at position 4 and reach inbound FSR
      And I wait for inbound flights results to load
      And I select inbound flight at position 1 and reach overview
      And Close price change Alert dialog if it is visible
      Then Validate that fare family widget card is displayed
      Then Validate fare family widget card info
        | title                 | Upgrade your flights         |
        | subtitle              | Selected: Economy,Business   |
        | from_label            | From                         |
        | delta_price           | +$99.00                      |
      Then Validate that traveller number is visible on fare family card : true
      Then Validate that "3 travelers" are displayed on fare family card
      Then I click on fare family widget card
      Then I select flight upgrade at position 2
      Then I click on done button for upsell
      And Wait for checkout button to display
      Then Validate fare family card title is "You've selected First or Business"
      Then Validate fare family card subtitle is "Change fare class"
      Then Validate fare family card icon is displayed
      Then Validate that traveller number is visible on fare family card : false
      Then Validate delta price is displayed : false
      Then Validate from label is displayed : false

  @Flights @FlightsOverviewSet3
  Scenario: Validate the fare family card on Flights Rate Details Page with single traveller
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
    And I select outbound flight at position 4 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
    Then Validate that fare family widget card is displayed
    Then Validate fare family widget card info
      | title                 | Upgrade your flights         |
      | subtitle              | Selected: Economy,Business   |
      | from_label            | From                         |
      | delta_price           | +$99.00                      |
    Then Validate that traveller number is visible on fare family card : false
    Then I click on fare family widget card
    Then I select flight upgrade at position 2
    Then I click on done button for upsell
    And Wait for checkout button to display
    Then Validate fare family card title is "You've selected First or Business"
    Then Validate fare family card subtitle is "Change fare class"
    Then Validate fare family card icon is displayed
    Then Validate that traveller number is visible on fare family card : false
    Then Validate delta price is displayed : false
    Then Validate from label is displayed : false

  @Flights @WIP @FlightsOverviewSet3
  Scenario: Validate the fare family item for round trip with multiple traveller
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
      | child               | 0                                        |
    And I wait for results to load
    And I select outbound flight at position 4 and reach inbound FSR
    And I wait for inbound flights results to load
    And I select inbound flight at position 1 and reach overview
    And Close price change Alert dialog if it is visible
    Then Validate that fare family widget card is displayed
    Then I click on fare family widget card
    Then Validate fare family item info at position 1 is selected: true
    Then Validate fare family item info at position 1
      | title                 | Economy                      |
      | class                 | Cabin: Economy               |
      | price                 | +$0.00                       |
      | round_trip            | true                         |
      | traveler              | 3 travelers                  |
    Then Validate primary amenities at position 3
      | Checked Bags          |  CHARGEABLE                 |
    Then I select flight upgrade at position 2
    Then I click on done button for upsell
    And Wait for checkout button to display
    Then I click on fare family widget card
    Then Validate fare family item info at position 2 is selected: true