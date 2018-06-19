Feature: Hotel Search with name

  @Prod @RC_HotelSearch
  Scenario Outline: Successfully searching for hotels with hotel name.

    Given I launch the App
    And I launch "Hotels" LOB
    And I enter destination as "<destination>"
    And I select hotel with the text "<destination>"
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I verify the hotel label text is "<destination>"

    Examples:
      | destination                | checkInDate | checkOutDate |
      | Hotel Nikko San Francisco  | 5           | 7            |
