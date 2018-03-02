Feature: Verify Share works on Trip Details page

  @Prod @RC_Trips_ShareItinerary
  Scenario Outline: Verify Share functionality works on the trip details page
    Given I launch the App
    When I tap on "Account" tab
    And I login with user, which has
      | tier | Blue |
      | type | Facebook |
    And I tap on "Trips" tab
    And I wait for trips screen to load
    And I tap on trip item with name "<hotelName>"

    Then I tap on Share icon
    And I tap on <shareVia> and verify the app has opened
    And I press back
    And I tap on back button
    And I kill process and wipe data of <shareVia> app

    Examples:
      | hotelName               | shareVia  |
      #| Longhorn Casino & Hotel | Facebook  |
      | Longhorn Casino & Hotel | Gmail     |
      #| Longhorn Casino & Hotel | KakaoTalk |
      #| Longhorn Casino & Hotel | LINE      |
