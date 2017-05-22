
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