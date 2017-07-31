Feature: Flights Search

  @Flights @FlightSearch @Prod @WIP
  Scenario: Verifying if round trip International search works

    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | sfo                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I change travellers count and press done
    Then I can trigger flights search
    And I wait for results to load
    And Validate that flight search results are displayed


  @Flights @FlightSearch @Prod
  Scenario: Verifying if one way International search works

    Given I launch the App
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 5 |
    And I change travellers count and press done
    Then I can trigger flights search
    And I wait for results to load
    And Validate that flight search results are displayed

  @Flights @Prod @EBOnly
  Scenario: Verifying if round trip International search works for Indonesia

    Given I launch the App
    And I set the POS to "Indonesia"
    And I launch "Flights" LOB
    When I select source location from the dropdown as "BKK"
    And I select destination from the dropdown as "HKG"
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I change travellers count and press done
    Then I can trigger flights search
    And I wait for results to load
    And Validate that flight search results are displayed

  @Flights @Prod @EBOnly
  Scenario: Verifying if one-way trip International search works for Indonesia

    Given I launch the App
    And I set the POS to "Indonesia"
    And I launch "Flights" LOB
    When I select one way trip
    And I select source location from the dropdown as "DPS"
    And I select destination from the dropdown as "SIN"
    And I pick departure date for flights
      | start_date | 5  |
    And I change travellers count and press done
    Then I can trigger flights search
    And I wait for results to load
    And Validate that flight search results are displayed

  @Flights @FlightSearch @Prod @BYOT
    Scenario: Verify International search works with BYOT

    Given I launch the App
    And I bucket the following tests
      | FlightByotSearch |
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I change travellers count and press done
    Then I can trigger flights search
    And I wait for results to load
    And Validate that flight search results are displayed
    And I select outbound flight at position 1 and reach inbound FSR
    And I wait for inbound flights results to load
    And Validate that flight search results are displayed for inbound flights

  @Flights @FlightSearch @Prod @BYOT
  Scenario: Verifying if one way International search works when user is bucketed in BYOT

    Given I launch the App
    And I bucket the following tests
      | FlightByotSearch |
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 5 |
    And I change travellers count and press done
    Then I can trigger flights search
    And I wait for results to load
    And Validate that flight search results are displayed



  @Flights @FlightSearch
  Scenario: Verifying UI fields visibility on round trip search form
    Given I launch the App
    When I launch "Flights" LOB
    Then departure field exists for flights search form
    And arrival field exists for flights search form
    And calendar field with text "Select dates" exists for flights search form

  @Flights @FlightSearch
  Scenario: Verifying UI fields visibility on one way trip search form
    Given I launch the App
    And I launch "Flights" LOB
    When I select one way trip
    Then departure field exists for flights search form
    And calendar field exists for one way flights search form
    And arrival field exists for flights search form


  @Flights @FlightSearch
  Scenario Outline: UI fields validation on travellers form adults
    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I click on guest button
    And I increase the adult count to max
    And I press done
    Then <initialNumber> traveler count is as selected by user
    When I click on guest button
    And reduce the travellers count
    And I press done
    Then <laterNumber> traveler count is as selected by user

    Examples:
      | initialNumber | laterNumber |
      | 6             | 5           |


  @Flights @FlightSearch
  Scenario Outline: UI fields validation on travellers form children
    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I click on guest button
    And I increase the child count to max
    And equal number of age pickers are shown
    And the default age is 10 years
    And I press done
    Then <initialNumber> traveler count is as selected by user
    When I click on guest button
    And Reduce the child count
    Then corresponding age picker is removed
    When I press done
    Then <laterNumber> traveler count is as selected by user

    Examples:
      | initialNumber | laterNumber |
      | 5             | 4           |


  @Flights @FlightSearch
  Scenario: Calender validation - Calender widget is displayed after selecting Flight locations
    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source                | SFO                                       |
      | destination           | DEL                                       |
      | source_suggest        | San Francisco, CA                         |
      | destination_suggest   | Delhi, India (DEL - Indira Gandhi Intl.)  |
    Then Validate that Calender widget is displayed: true
    And Validate that Current Month calender is displayed
    And Validate that Done button is disabled
    And Validate that Previous month arrow is displayed: false

  @Flights @FlightSearch
  Scenario: Calender fields/text validation for Search Screen when no dates are selected and a Round trip.
    Given I launch the App
    And I launch "Flights" LOB
    And I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And Validate that Current Month calender is displayed
    And Validate that Done button is disabled
    And Validate that Previous month arrow is displayed: false
    And Validate that Next month arrow is displayed: true
    And Validate that "Select departure date" text below calender title is displayed

  @Flights @FlightSearch
  Scenario: Calender fields/text validation for Search Screen when selecting dates and a Round trip
    Given I launch the App
    And I launch "Flights" LOB
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And I choose departure date for flights-roundtrip and validate the tool tip
      | start_date | 5  |
    And I choose return date for flights-roundtrip and validate the tool tip
      | start_date | 5  |
      | end_date   | 10 |
    And Validate that Done button is enabled
    And I click on Done button
    And Validate the selected date on calender button
      | start_date | 5  |
      | end_date   | 10 |

  @Flights @FlightSearch
  Scenario: Calender fields/text validation for Search Screen when a Round trip and selecting departure date only
    Given I launch the App
    And I launch "Flights" LOB
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And I choose departure date for flights-roundtrip and validate the tool tip
      | start_date | 5  |
    And Validate that Done button is enabled
    And I click on Done button
    And Validate the selected date on calender button
      | start_date | 5  |

  @Flights @FlightSearch
  Scenario: Calender fields/text validation for Search Screen when selecting dates and a OneWay trip
    Given I launch the App
    And I launch "Flights" LOB
    And I select one way trip
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    And I choose departure date for flights-oneway and validate the tool tip
      | start_date | 5  |
    And Validate that Done button is enabled
    And I click on Done button
    And Validate the selected date on calender button
      | start_date | 5  |
      | isRoundTrip | false  |

  @Flights @FlightSearch
  Scenario: Previous/Next month button validation of Calender Widget
    Given I launch the App
    And I launch "Flights" LOB
    When I Click on Select Dates button for flights
    Then Validate that Calender widget is displayed: true
    Then I click on Next month button
    Then Validate that next month calender is displayed
    Then Validate that Previous month arrow is displayed: true
    Then Validate that Next month arrow is displayed: true
    Then I click on Previous month button
    Then Validate that Current Month calender is displayed
    Then Validate that Previous month arrow is displayed: false
    Then Validate that Next month arrow is displayed: true

  @Flights @FlightSearch @Prod
  Scenario: Verify search form retains detaild of last search at re-search from FSR
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
    And I click on search icon to go to search form
    Then search criteria is retained on the search form
    When I trigger flight search again with following parameters
      | source              | LON                                      |
      | destination         | MAD                                      |
      | source_suggest      | London, England, UK (LON - All Airports) |
      | destination_suggest | Madrid, Spain (MAD - All Airports)       |
      | start_date          | 7                                        |
      | end_date            | 11                                       |
      | adults              | 3                                        |
      | child               | 3                                        |
    And I wait for results to load
    And Validate that flight search results are displayed
    And on FSR the destination is "Madrid"
    And on FSR the date is as user selected
    And on outbound FSR the number of traveller are as user selected

  @Flights @FlightSearch @Prod
  Scenario: Verify user is able to select preferred class while booking flight for round trip under AB test
    Given I launch the App
    And I launch "Flights" LOB
    When I enter source and destination for flights
      | source              | sfo                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick dates for flights
      | start_date | 5  |
      | end_date   | 10 |
    And I click on class widget
    Then Validate "Economy" class is selected by default
    Then I click on "First Class" as preferred class
    And I click on Done button
    And Validate "First Class" preferred class is displayed on search screen
    And Validate Search button is enabled
    And I click on class widget
    And I click on "Business" as preferred class
    And I click on Done button
    And Validate "Business" preferred class is displayed on search screen
    And Validate Search button is enabled
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    And Validate "Premium Economy" preferred class is displayed on search screen
    And Validate Search button is enabled
    And I click on class widget
    And I click on "Economy" as preferred class
    And I click on Done button
    And Validate "Economy" preferred class is displayed on search screen
    And Validate Search button is enabled

  @Flights @FlightSearch @Prod
  Scenario: Verify user is able to select preferred class while booking flight for one way trip under AB test
    Given I launch the App
    And I bucket the following tests
      |FlightPremiumClass|
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | sfo                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 5 |
    And I click on class widget
    Then Validate "Economy" class is selected by default
    Then I click on "First Class" as preferred class
    And I click on Done button
    And Validate "First Class" preferred class is displayed on search screen
    And Validate Search button is enabled
    And I click on class widget
    And I click on "Business" as preferred class
    And I click on Done button
    And Validate "Business" preferred class is displayed on search screen
    And Validate Search button is enabled
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    And Validate "Premium Economy" preferred class is displayed on search screen
    And Validate Search button is enabled
    And I click on class widget
    And I click on "Economy" as preferred class
    And I click on Done button
    And Validate "Economy" preferred class is displayed on search screen
    And Validate Search button is enabled

  @Flights @FlightSearch @Prod
  Scenario: Intercept Flight Search API call and validate request parameters for Premium class
    Given I launch the App
    And I want to intercept these calls
      | FlightSearch |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | LAS                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Las Vegas, NV (LAS - McCarran Intl.) |
      | start_date          | 15                                        |
      | end_date            | 20                                       |
      | adults              | 2                                        |
      | child               | 2                                        |
    And I wait for results to load
    Then Validate the flight Search API request query data for following parameters
      | maxOfferCount                         | 1600                          |
      | cabinClassPreference                  | COACH                         |
      | childTravelerAge                      | 10                            |
      | lccAndMerchantFareCheckoutAllowed     | true                          |
      | sourceType                            | mobileapp                     |
    Then Validate the flight Search API request form data for following parameters
      | infantSeatingInLap                    | false                         |
      | departureAirport                      | SFO                           |
      | arrivalAirport                        | LAS                           |
      | returnDate                            | 20                            |
      | departureDate                         | 15                            |
      | numberOfAdultTravelers                | 2                             |
    And I click on search icon to go to search form
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    Then I can trigger flights search
    And I wait for results to load
    Then Validate the flight Search API request query data for following parameters
      | maxOfferCount                         | 1600                          |
      | cabinClassPreference                  | PREMIUM_COACH                 |
      | childTravelerAge                      | 10                            |
      | lccAndMerchantFareCheckoutAllowed     | true                          |
      | sourceType                            | mobileapp                     |
    Then Validate the flight Search API request form data for following parameters
      | infantSeatingInLap                    | false                         |
      | departureAirport                      | SFO                           |
      | arrivalAirport                        | LAS                           |
      | returnDate                            | 20                            |
      | departureDate                         | 15                            |
      | numberOfAdultTravelers                | 2                             |

  @Flights @FlightSearch
  Scenario: Verify if the previous state is retained
    Given I launch the App
    And I bucket the following tests
      | FlightRetainSearchParams |
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
    And I press back
    And I press back
    And I launch "Flights" LOB
    Then Validate search form retains details of search for flights
      | source              | SFO - San Francisco Intl.                |
      | destination         | DEL - Indira Gandhi Intl.                |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | totalTravelers      | 5 travelers                              |
      | flightClass         | Economy                                  |

  @Flights @FlightSearch
  Scenario: Validate flight params are retained on relaunching LOB for oneway
    Given I launch the App
    And I bucket the following tests
      | FlightRetainSearchParams |
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 10 |
    And I change travellers count and press done
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    Then I can trigger flights search
    And I wait for results to load
    And I press back
    And I press back
    And I launch "Flights" LOB
    Then Validate search form retains details of search for flights
      | source              | SFO - San Francisco Intl.                |
      | destination         | DEL - Indira Gandhi Intl.                |
      | start_date          | 10                                       |
      | totalTravelers      | 4 travelers                              |
      | flightClass         | Premium Economy                          |

  @Flights @FlightSearch
  Scenario: Verify if the previous state is retained on changing parameters.
    Given I launch the App
    And I bucket the following tests
      | FlightRetainSearchParams |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | adults              | 1                                        |
      | child               | 1                                        |
    And I wait for results to load
    And I press back
    And I press back
    And I launch "Flights" LOB
    Then Validate search form retains details of search for flights
      | source              | SFO - San Francisco Intl.                |
      | destination         | DEL - Indira Gandhi Intl.                |
      | start_date          | 5                                        |
      | end_date            | 10                                       |
      | totalTravelers      | 2 travelers                              |
      | flightClass         | Economy                                  |
    When I enter source for flights
      | source              | DEL                                      |
      | source_suggest      | Delhi, India (DEL - Indira Gandhi Intl.) |
    When I enter destination for flights
      | destination         | SFO                                      |
      | destination_suggest | San Francisco, CA                        |
    And I click on calender widget
    And I pick dates for flights
      | start_date | 6  |
      | end_date   | 11 |
    And I change travellers count and press done
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    And Validate Search button is enabled
    Then I can trigger flights search
    And I wait for results to load
    And I press back
    And I press back
    And I launch "Flights" LOB
    Then Validate search form retains details of search for flights
      | source              | DEL - Indira Gandhi Intl.                |
      | destination         | SFO - San Francisco Intl.                |
      | start_date          | 6                                        |
      | end_date            | 11                                       |
      | totalTravelers      | 5 travelers                              |
      | flightClass         | Premium Economy                          |

  @Flights @FlightSearch
  Scenario: Validate previous flight params are retained on updating search params for oneway
    Given I launch the App
    And I bucket the following tests
      | FlightRetainSearchParams |
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 10 |
    And I change travellers count and press done
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    Then I can trigger flights search
    And Validate FSR toolbar is consistent with search params
      | destination                 | Delhi, India             |
      | start_date                  | 10                       |
      | totalTravelers              | 4 travelers              |
    And I wait for results to load
    And I press back
    And I press back
    And I launch "Flights" LOB
    Then Validate search form retains details of search for flights
      | source                      | SFO - San Francisco Intl.|
      | destination                 | DEL - Indira Gandhi Intl.|
      | start_date                  | 10                       |
      | totalTravelers              | 4 travelers              |
      | flightClass                       | Premium Economy          |
    Then I enter source for flights
      | source              | DEL                                      |
      | source_suggest      | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I enter destination for flights
      | destination         | SFO                                      |
      | destination_suggest | San Francisco, CA                        |
    And I Click on Select Dates button for flights
    And I pick departure date for flights
      | start_date | 11 |
    And I change travellers count and press done
    And I click on class widget
    And I click on "First Class" as preferred class
    And I click on Done button
    Then I can trigger flights search
    And I wait for results to load
    And Validate that flight search results are displayed
    And Validate FSR toolbar is consistent with search params
      | destination                 | San Francisco, CA        |
      | start_date                  | 11                       |
      | totalTravelers              | 6 travelers              |
    Then I press back
    And I press back
    And I launch "Flights" LOB
    Then Validate search form retains details of search for flights
      | destination                 | SFO - San Francisco Intl.|
      | source                      | DEL - Indira Gandhi Intl.|
      | start_date                  | 11                       |
      | totalTravelers              | 6 travelers              |
      | flightClass                 | First Class              |

  @Flights @FlightSearch
  Scenario: Intercept Flight Search API call and validate request parameters for changed parameters
    Given I launch the App
    And I bucket the following tests
      | FlightRetainSearchParams |
    And I want to intercept these calls
      | FlightSearch |
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 15                                       |
      | end_date            | 20                                       |
      | adults              | 1                                        |
      | child               | 1                                        |
    And I wait for results to load
    And I press back
    And I press back
    And I launch "Flights" LOB
    When I enter source for flights
      | source              | DEL                                      |
      | source_suggest      | Delhi, India (DEL - Indira Gandhi Intl.) |
    When I enter destination for flights
      | destination         | SFO                                      |
      | destination_suggest | San Francisco, CA                        |
    And I click on calender widget
    And I pick dates for flights
      | start_date | 20 |
      | end_date   | 25 |
    And I change travellers count and press done
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    And Validate Search button is enabled
    Then I can trigger flights search
    And I wait for results to load
    Then Validate the flight Search API request query data for following parameters
      | maxOfferCount                         | 1600                          |
      | cabinClassPreference                  | PREMIUM_COACH                 |
      | lccAndMerchantFareCheckoutAllowed     | true                          |
      | sourceType                            | mobileapp                     |
      | childTravelerAge                      | 10                            |
    Then Validate the flight Search API request form data for following parameters
      | infantSeatingInLap                    | false                         |
      | departureAirport                      | DEL                           |
      | arrivalAirport                        | SFO                           |
      | departureDate                         | 20                            |
      | returnDate                            | 25                            |
      | numberOfAdultTravelers                | 3                             |
    And on FSR the destination is "San Francisco"
    And on FSR validate the date is as user selected
      | start_date                            | 20                           |
    And on FSR Validate the total number of travelers
      | totalTravelers                       | 5 travelers                   |

  @Flight @FlightSearch
  Scenario: Validate the flight search API call for retained flight params for oneway
    Given I launch the App
    And I bucket the following tests
      | FlightRetainSearchParams |
    And I want to intercept these calls
      | FlightSearch |
    And I launch "Flights" LOB
    And I select one way trip
    When I enter source and destination for flights
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I pick departure date for flights
      | start_date | 10 |
    And I change travellers count and press done
    And I click on class widget
    And I click on "Premium Economy" as preferred class
    And I click on Done button
    Then I can trigger flights search
    And I wait for results to load
    Then Validate the flight Search API request query data for following parameters
      | maxOfferCount                         | 1600                          |
      | cabinClassPreference                  | PREMIUM_COACH                 |
      | lccAndMerchantFareCheckoutAllowed     | true                          |
      | sourceType                            | mobileapp                     |
      | childTravelerAge                      | 10                            |
    Then Validate the flight Search API request form data for following parameters
      | infantSeatingInLap                    | false                         |
      | departureAirport                      | SFO                           |
      | arrivalAirport                        | DEL                           |
      | departureDate                         | 10                            |
      | numberOfAdultTravelers                | 3                             |
    And I press back
    And I press back
    And I launch "Flights" LOB
    When I enter destination for flights
      | destination         | SFO                                      |
      | destination_suggest | San Francisco, CA                        |
    When I enter source for flights
      | source              | DEL                                      |
      | source_suggest      | Delhi, India (DEL - Indira Gandhi Intl.) |
    And I Click on Select Dates button for flights
    And I pick departure date for flights
      | start_date | 11 |
    And I change travellers count and press done
    And I click on class widget
    And I click on "First Class" as preferred class
    And I click on Done button
    Then I can trigger flights search
    And I wait for results to load
    Then Validate the flight Search API request query data for following parameters
      | maxOfferCount                         | 1600                          |
      | cabinClassPreference                  | FIRST                         |
      | childTravelerAge                      | 10                            |
      | lccAndMerchantFareCheckoutAllowed     | true                          |
      | sourceType                            | mobileapp                     |
    Then Validate the flight Search API request form data for following parameters
      | infantSeatingInLap                    | false                         |
      | departureAirport                      | DEL                           |
      | arrivalAirport                        | SFO                           |
      | departureDate                         | 11                            |
      | numberOfAdultTravelers                | 5                             |
