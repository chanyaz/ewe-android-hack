Feature: Hotel Search with name

  @Prod @RC_HotelResortFee
  Scenario Outline: Successfully searching for hotels with hotel name.

    Given I launch the App
    And I launch "Hotels" LOB
    And I enter destination as "<destination>"
    And I select hotel with the text "<destination>"
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I click on Select a Room Button
    And I book first Room
    And I verify resort fee disclaimer text is displayed


    Examples:
      | destination            | checkInDate | checkOutDate |
      | Hooters Casino Hotel   | 5           | 7            |

