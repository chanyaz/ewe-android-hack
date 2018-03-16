
Feature: Package Search

  @Packages @PackageSearch
  Scenario: Verify that calendar widget is displayed after selecting package source and destination.
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    Then Validate that Calender widget is displayed: true
    And Validate that Current Month calender is displayed
    And Validate that Done button is disabled
    And Validate that Previous month arrow is displayed: false

  @Packages @PackageSearch
  Scenario: Calender fields/text validation for Search Screen when no dates are selected
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    And Validate that Next month arrow is displayed: true
    And Validate that "Select departure date" text below calender title is displayed

  @Packages @PackageSearch
  Scenario: Calender fields/text validation for Search Screen when selecting only the departure date.
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    Then Validate that Calender widget is displayed: true
    And I choose date from calendar widget
      | start_date | 5  |
    And Validate dates selected are correct
      | start_date | 5  |
    And Validate that Done button is enabled
    And I click on Done button
    And Validate the date selected on calender button
      | start_date | 5  |

  @Packages @PackageSearch
  Scenario: Calender fields/text validation for Search Screen when selecting the departure and return dates.
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    Then Validate that Calender widget is displayed: true
    And I choose date from calendar widget
      | start_date         | 5         |
      | end_date           | 10        |
    And Validate dates selected are correct
      | start_date         | 5         |
      | end_date           | 10        |
      | number_of_nights   | (5 nights)|
    And Validate that Done button is enabled
    And I click on Done button
    And Validate the date selected on calender button
      | start_date         | 5         |
      | end_date           | 10        |
      | number_of_nights   | (5 nights)|

  @Packages @PackageSearch
  Scenario: User should not be able to select more than 26 days in calendar for package search.
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    Then Validate that Calender widget is displayed: true
    And I choose date from calendar widget
      | start_date         | 5          |
      | end_date           | 40         |
    And Validate dates selected are correct
      | start_date         | 5          |
      | end_date           | 31         |
      | number_of_nights   | (26 nights)|

  @Packages @PackageSearch
  Scenario: User should not be able to select dates more than 329 days from now.
    Given I launch the App
    And I launch "Bundle Deals" LOB
    And I open calendar widget
    Then Validate that max end date selectable is 329 days from now

  @Packages @PackageSearch
  Scenario: Previous/Next month button validation of Calender Widget
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    Then Validate that Calender widget is displayed: true
    Then I click on Next month button
    Then Validate that next month calender is displayed
    Then Validate that Previous month arrow is displayed: true
    Then Validate that Next month arrow is displayed: true
    Then I click on Previous month button
    Then Validate that Current Month calender is displayed
    Then Validate that Previous month arrow is displayed: false
    Then Validate that Next month arrow is displayed: true

  @Packages @PackageSearch
  Scenario Outline: UI fields validation on travellers form adults
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source                | SFO                                                |
      | destination           | DET                                                |
      | source_suggest        | San Francisco, CA                                  |
      | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    And I choose date from calendar widget
      | start_date         | 5         |
      | end_date           | 10        |
    And Validate dates selected are correct
      | start_date         | 5         |
      | end_date           | 10        |
      | number_of_nights   | (5 nights)|
    And I click on Done button
    And I click on guest button
    And I increase the adult count to max
    And Validate plus icon for Adults is disabled
    And Validate plus icon for Children is disabled
    And I press done
    Then <initialNumber> traveler count is as selected by user
    When I click on guest button
    And reduce the travellers count
    And I press done
    Then <laterNumber> traveler count is as selected by user

    Examples:
      | initialNumber | laterNumber |
      | 6             | 5           |

  @Packages @PackageSearch
  Scenario Outline: UI fields validation on travellers form children
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
        | source                | SFO                                                |
        | destination           | DET                                                |
        | source_suggest        | San Francisco, CA                                  |
        | destination_suggest   | Detroit, MI (DTW-Detroit Metropolitan Wayne County)|
    And I choose date from calendar widget
      | start_date         | 5         |
      | end_date           | 10        |
    And Validate dates selected are correct
      | start_date         | 5         |
      | end_date           | 10        |
      | number_of_nights   | (5 nights)|
    And I click on Done button
    And I click on guest button
    And I increase the child count to max
    And Validate plus icon for Children is disabled
    Then I increase the adult count
    And Validate plus icon for Adults is disabled
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
        | 6             | 5           |

  @Packages @PackageSearch @Prod
  Scenario: Verify data consistency while loading through package overview screen
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SFO                            |
      | destination         | LAS                            |
      | source_suggest      | SFO - San Francisco Intl.      |
      | destination_suggest | Las Vegas Strip, NV            |
      | start_date          | 5                              |
      | end_date            | 10                             |
      | adults              | 2                              |
      | child               | 2                              |
    Then validate hotels loading on package overview screen
    And I select hotel at position 1 on HSR screen
    And I select first room
    And validate outbound flights loading on package overview screen
    And I select outbound flight to destination at position 1
    And validate inbound flights loading on package overview screen
    And I select inbound flight to source at position 1
    And Close price change Alert dialog if it is visible
    And I click on checkout button
    And I validate that checkout screen is displayed

  @Packages @PackageSearch @Prod
  Scenario: Verify consistency of traveler details on HSR and FSR toolbar of packages
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SFO                            |
      | destination         | LAS                            |
      | source_suggest      | SFO - San Francisco Intl.      |
      | destination_suggest | Las Vegas Strip, NV            |
      | start_date          | 5                              |
      | end_date            | 10                             |
      | adults              | 2                              |
      | child               | 2                              |
    And validate HSR screen is displayed with following travel dates and travelers
      | start_date      |  5 |
      | end_date        | 10 |
      | Total_Travelers |  4 |
    And I select hotel at position 1 on HSR screen
    And I select first room
    And validate outbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 5 |
      | Total_Travelers | 4 |
    And I select outbound flight to destination at position 1
    And validate inbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 10 |
      | Total_Travelers |  4 |
    And I select inbound flight to source at position 1
    And Close price change Alert dialog if it is visible
    And I click on checkout button
    And I validate that checkout screen is displayed

  @Packages @PackageSearch @Prod
  Scenario: Verify consistency of traveler details on package overview screen
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SFO                            |
      | destination         | LAS                            |
      | source_suggest      | SFO - San Francisco Intl.      |
      | destination_suggest | Las Vegas Strip, NV            |
      | start_date          | 5                              |
      | end_date            | 10                             |
      | adults              | 2                              |
      | child               | 2                              |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select first room
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
    And Close price change Alert dialog if it is visible
    And Wait for checkout button to display
    And validate "varHotelName" is same as user selected on package overview screen
    And validate hotel widget of overview screen with following details
      | start_date      |  5 |
      | end_date        | 10 |
      | Total_Travelers |  4 |
    And validate flight outbound widget of overview screen with following details
      | destination     | LAS |
      | travel_date     |   5 |
      | Total_Travelers |   4 |
    And validate flight inbound widget of overview screen with following details
      | source          | SFO |
      | travel_date     |  10 |
      | Total_Travelers |   4 |

  @Packages @PackageSearch @Prod
  Scenario: Intercept getPackages API call after hitting search button and validate request paramaters
    Given I launch the App
    And I launch "Bundle Deals" LOB
    And I want to intercept these calls for packages
      | GetPackagesV1 |
    When I make a packages search with following parameters
      | source              | SFO                            |
      | destination         | LAS                            |
      | source_suggest      | SFO - San Francisco Intl.      |
      | destination_suggest | Las Vegas Strip, NV            |
      | start_date          | 20                             |
      | end_date            | 30                             |
      | adults              | 2                              |
      | child               | 2                              |
    Then Validate the getPackages API request query data for following parameters for packages
      | forceNoRedir                | 1                          |
      | packageType                 | fh                         |
    Then Validate the getPackages API request form data for following parameters
      | fromDate                | 20                          |
      | destinationId           | 800045                      |
      | ttla                    | LAS                         |
      | ftla                    | SFO                         |
      | packageTripType         | 2                           |
      | adultsPerRoom[1]        | 2                           |
      | numberOfRooms           | 1                           |
      | toDate                  | 30                          |
      | originId                | 5195347                      |
      | childrenPerRoom[1]      | 2                           |
      | childAges[1][1]         | 10                          |
      | childAges[1][2]         | 10                          |


  @Packages @PackageSearch @Prod
  Scenario: Verify search form selection are retained for packages
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SFO                            |
      | destination         | LAS                            |
      | source_suggest      | SFO - San Francisco Intl.      |
      | destination_suggest | Las Vegas Strip, NV            |
      | start_date          | 15                             |
      | end_date            | 20                             |
      | adults              | 2                              |
      | child               | 2                              |
    And validate HSR screen is displayed with following travel dates and travelers
      | start_date      | 15 |
      | end_date        | 20 |
      | Total_Travelers |  4 |
    And I press back
    Then Validate that Package Overview screen is displayed
    And I press back
    Then Validate search form retains details of search for packages
      | source              | SFO - San Francisco Intl.      |
      | destination         | Las Vegas Strip, NV            |
      | start_date          | 15                             |
      | end_date            | 20                             |
      | numberOfNights      | (5 nights)                     |
      | totalTravelers      | 4 travelers                    |

  @Packages @PackageSearch @Prod
  Scenario: Validate Search form Default state for packages
    Given I launch the App
    And I launch "Bundle Deals" LOB
    Then Validate toolbar title is "Bundle Deals" for packages
    Then Validate search form default state for packages
      | source              | Flying from      |
      | destination         | Flying to        |
      | calendar            | Select dates     |
      | totalTravelers      | 1 traveler       |

  @Packages @PackageSearch @Prod
  Scenario: Verify that tapping on change hotel should take us to hotel srp
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SFO                            |
      | destination         | LAS                            |
      | source_suggest      | SFO - San Francisco Intl.      |
      | destination_suggest | Las Vegas Strip, NV            |
      | start_date          | 5                              |
      | end_date            | 10                             |
      | adults              | 2                              |
      | child               | 2                              |
    And validate HSR screen is displayed with following travel dates and travelers
      | start_date      |  5 |
      | end_date        | 10 |
      | Total_Travelers |  4 |
    And I select hotel at position 1 on HSR screen
    And I select first room
    And validate outbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 5 |
      | Total_Travelers | 4 |
    And I select outbound flight to destination at position 1
    And validate inbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 10 |
      | Total_Travelers |  4 |
    And I select inbound flight to source at position 1
    And Close price change Alert dialog if it is visible
    And Wait for checkout button to display
    And I click on edit icon and select "Change hotel"
    Then Validate that hotel SRP screen is displayed
    Then Validate that number of results shown and present are equal

  @Packages @PackageSearch @Prod
  Scenario: Verify if the previous state is retained in Packages even when user navigates between different LOBs
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SFO                                    |
      | destination         | KTM                                    |
      | source_suggest      | SFO - San Francisco Intl.              |
      | destination_suggest | Kathmandu Valley, Nepal                |
      | start_date          | 5                                      |
      | end_date            | 10                                     |
      | adults              | 2                                      |
      | child               | 2                                      |
    Then Validate that hotel SRP screen is displayed
    And I press back following number of times: 3
    And I launch "Flights" LOB
    And I press back
    And I launch "Bundle Deals" LOB
    Then Validate search form retains details of search for packages
      | source              | SFO - San Francisco Intl.              |
      | destination         | Kathmandu Valley, Nepal                |
      | start_date          | 5                                      |
      | end_date            | 10                                     |
      | numberOfNights      | (5 nights)                             |
      | totalTravelers      | 4 travelers                            |

  @Packages @PackageSearch
  Scenario: UI fields validation for travellers on new revamp Traveler form
    Given I launch the App
    And I set bucketing rules for A/B tests as
      | EBAndroidAppFlightTravelerFormRevamp | BUCKETED              |
    And I launch "Bundle Deals" LOB
    When I enter source and destination for packages
      | source              | SFO                                    |
      | destination         | KTM                                    |
      | source_suggest      | SFO - San Francisco Intl.              |
      | destination_suggest | Kathmandu, Nepal (KTM-Tribhuvan Intl.) |
    And I choose date from calendar widget
      | start_date          | 5                                      |
      | end_date            | 10                                     |
    And I click on Done button
    And I click on guest button
    And I increase the adult count by: 3
    When I press done
    Then 4 traveler count is as selected by user
    And I click on guest button
    And I decrease the adult count by: 2
    When I press done
    Then 2 traveler count is as selected by user
    And I click on guest button
    And I increase the youth count by: 2
    When I press done
    Then 4 traveler count is as selected by user
    And I click on guest button
    And I decrease the youth count by: 1
    When I press done
    Then 3 traveler count is as selected by user
    And I click on guest button
    And I increase the child count by: 2
    When I press done
    Then 5 traveler count is as selected by user
    And I click on guest button
    And I decrease the child count by: 1
    When I press done
    Then 4 traveler count is as selected by user
    And I click on guest button
    And I increase the infant count by: 2
    When I press done
    Then 6 traveler count is as selected by user
    And I click on guest button
    And I decrease the infant count by: 1
    When I press done
    Then 5 traveler count is as selected by user

  @Packages @Prod
  Scenario: Verify docked outbound flight on inbound flight results screen
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SFO                       |
      | destination         | LAS                       |
      | source_suggest      | SFO - San Francisco Intl. |
      | destination_suggest | Las Vegas Strip, NV       |
      | start_date          | 5                         |
      | end_date            | 10                        |
      | adults              | 2                         |
      | child               | 2                         |
    And I select hotel at position 1 on HSR screen
    And I select first room
    And I select outbound flight to destination at position 1
    And Validate that there is a docked outbound flight

    @Packages @Prod
      Scenario: Verify Hotel + Flight + Car Tab on search form screen
        Given I launch the App
        And I set bucketing rules for A/B tests as
          | EBAndroidAppPackagesWebviewFHC | BUCKETED       |
        And I launch "Bundle Deals" LOB
        And I select FHC package
        When I make a packages search with following parameters
          | source              | SFO                       |
          | destination         | LAS                       |
          | source_suggest      | SFO - San Francisco Intl. |
          | destination_suggest | Las Vegas Strip, NV       |
          | start_date          | 5                         |
          | end_date            | 10                        |
          | adults              | 2                         |
          | child               | 2                         |
        Then Validate that FHC WebView screen is displayed
