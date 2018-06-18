Feature: HotelInfosite_DepositAndResortFee

  @Prod @RC_HotelDepositAndResortFee
  Scenario Outline: Validate deposit without resort fee shown on infosite

    Given I launch the App
    And I launch "Hotels" LOB
    And I enter destination as "<destination>"
    And I select hotel with the text "<destination>"
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I wait for hotel search results to load
    And I verify pinned hotel name is <destination>
    And I click on pinned hotel
    And I verify the hotel label text is "<destination>"
    Then I verify the deposit terms text is displayed
    And I verify the resort fees text is not displayed

    Examples:
     | destination        | checkInDate | checkOutDate |
     | 11th Avenue Hostel | 7           | 8            |

  @Prod @RC_HotelDepositAndResortFee
  Scenario Outline: Validate deposit and resort fee shown on infosite

    Given I launch the App
    And I launch "Hotels" LOB
    And I enter destination as "<destination>"
    And I select hotel with the text "<destination>"
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I wait for hotel search results to load
    And I verify pinned hotel name is <destination>
    And I click on pinned hotel
    And I verify the hotel label text is "<destination>"
    Then I verify the deposit terms text is displayed
    And I verify the resort fees text is displayed
    When I click select a room
    Then I click on hotel fees info icon
    Then I verify additional hotel fees screen is displayed
    Then I verify deposit and resort fees on additional hotel fees screen

    Examples:
      | destination                  | checkInDate | checkOutDate |
      | Oasis at Gold Spike          | 7           | 8            |
