Feature: View Elements on Trip Details page

  @Prod @RC_TripDetails
  Scenario Outline: Verify UI elements on the trip details page of a hotel booking
    Given I launch the App
    And I set bucketing rules for A/B tests as
      | EBAndroidAppTripsMessageHotel | BUCKETED |
      | TripsHotelsM2                 | BUCKETED |

    When I tap on "Account" tab
    And I login with user, which has
      | tier | Blue |
      | type | Facebook |
    And I tap on "Trips" tab
    And I wait for trips screen to load
    And I tap on trip item with name "<hotelName>"

    Then I verify the hotel name in the tool bar is "<hotelName>"
    And I verify the dates in the tool bar are from "<checkInDate>" to "<checkOutDate>"
    And I verify the hotel name in the hotel information section is "<hotelName>"
    And I verify the phone number in the hotel information section is "<phoneNumber>"
    And I verify the phone number button in the hotel information section is clickable
    And I verify the Check-In Date is: "<checkInDate>"
    And I verify the Check-Out Date is: "<checkOutDate>"
    And I verify the Check-In Time is: "<checkInTime>"
    And I verify the Check-Out Time is: "<checkOutTime>"

    Examples:
      | hotelName               | phoneNumber    | checkInDate | checkOutDate | checkInTime | checkOutTime |
      | Longhorn Casino & Hotel | +1-702-4358888 | 2019-05-01  | 2019-05-02   | 3 pm        | 11 am        |
