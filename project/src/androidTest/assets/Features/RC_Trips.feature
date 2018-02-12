Feature: Bookings on a Trips tab

  @Prod @RC_Trips
  Scenario: Validate that after I click on the Trips tab, I see a hotel booking on the Trips screen
    Given I launch the App
    And I tap on "Account" tab
    And I login with user, which has
      | tier | Blue |
      | type | Facebook |
    And I tap on "Trips" tab
    And I verify that hotel with name "Longhorn Casino & Hotel" is present
