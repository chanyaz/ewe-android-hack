Feature: Activity Search

  @LXSearch @Prod
  Scenario: Verifying if lx search works for Las Vegas
    Given I launch the App
    And I launch "LX" LOB
    When I enter destination for lx
      | destination              | las               |
      | destination_suggest      | Las Vegas, NV     |
    And I pick dates range for lx
      | start_date  | 5   |
      | end_date    | 11  |
    Then I can trigger lx search
    And I wait for activities results to load
    And Validate that lx search results are displayed

  @LXSearch
  Scenario: Verifying UI fields visibility on lx search form
    Given I launch the App
    When I launch "LX" LOB
    Then Enter Destination field exists for lx search form

  @LXSearch @Prod
  Scenario: Calender validation - Calender widget is displayed after selecting Lx locations
    Given I launch the App
    And I launch "LX" LOB
    When I enter destination for lx
      | destination              | tok               |
      | destination_suggest      | Tokyo, Japan      |
    Then Validate that Calender widget is displayed for lx: true
    And Validate that Current Month calender is displayed for lx
    And Validate that Done button is disabled for lx
    And Validate that Previous month arrow is displayed for lx: false
    And Validate that Next month arrow is displayed for lx: true

  @LXSearch
  Scenario: Previous/Next month button validation of Calender Widget
    Given I launch the App
    And I launch "LX" LOB
    When I Click on Select Dates button for lx
    Then Validate that Calender widget is displayed for lx: true
    Then I click on Next month button for lx
    Then Validate that next month calender is displayed for lx
    Then Validate that Previous month arrow is displayed for lx: true
    Then Validate that Next month arrow is displayed for lx: true
    Then I click on Previous month button for lx
    Then Validate that Current Month calender is displayed for lx
    Then Validate that Previous month arrow is displayed for lx: false
    Then Validate that Next month arrow is displayed for lx: true
