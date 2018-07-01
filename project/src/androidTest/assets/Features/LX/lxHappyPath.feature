Feature: Activity Happy Path

  @LXEndToEnd @Prod @SetLXTestComponent
  Scenario: Verifying LX happy path
  Given I launch the App
  And I enable Satallite features flag for "lxMultipleDatesSearch"
  And I launch "LX" LOB
  When I enter destination for lx
  | destination              | tok             |
  | destination_suggest      | Tokyo, Japan    |
  And I pick dates range for lx
  | start_date  | 5   |
  | end_date    | 11  |
  Then I can trigger lx search
  And I wait for activities results to load
  And Validate that lx search results are displayed
  And Validate that toolbar detail text is displayed for lx: true
  And Validate that toolbar subtitle text is displayed for lx: true
  And Validate that sort & filter is displayed: true
  And I store the activity name in "varActivityName"
  And I store the activity date range in "varActivityDateRange"
  Then I select 2 Activity
  And I wait for activity detail to load
  And validate activity name "varActivityName" is same as user selected on ASR
  And validate date range "varActivityDateRange" is same as user selected on ASR
  And Validate that highlights is displayed
  And Validate that description is not empty
  And Validate that location is not empty
  And Validate that cancellation policy is not empty
  Then I select activity 1 offer
  And I trigger Book Now button
