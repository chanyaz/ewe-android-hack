
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
      | number_of_nights   | (5 nights)|
    And Validate that Done button is enabled
    And I click on Done button
    And Validate the date selected on calender button
      | start_date         | 5         |
      | end_date           | 10        |
      | number_of_nights   | (5 nights)|

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
      | destination_suggest | Las Vegas Strip, Las Vegas, NV |
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
      | destination_suggest | Las Vegas Strip, Las Vegas, NV |
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
      | destination_suggest | Las Vegas Strip, Las Vegas, NV |
      | start_date          | 5                              |
      | end_date            | 10                             |
      | adults              | 2                              |
      | child               | 2                              |
    And I select hotel at position 1 on HSR screen
    And I store the hotel name in "varHotelName"
    And I select first room
    And I select outbound flight to destination at position 1
    And I select inbound flight to source at position 1
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
