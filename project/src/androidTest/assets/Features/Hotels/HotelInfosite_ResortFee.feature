Feature: HotelInfosite_ResortFee

  @Prod @RC_HotelResortFee
  Scenario Outline: Validate resort fee shown on infosite and checkout

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
    When I click select a room
    And I change date if hotel is sold out
    Then I verify hotel fees appear
    When I book first room
    And I wait for checkout to load
    Then I verify fees breakdown appear
    And I verify fees disclaimer appear

    Examples:
      | destination            | checkInDate | checkOutDate |
      | The Venetian Las Vegas | 7           | 8            |
