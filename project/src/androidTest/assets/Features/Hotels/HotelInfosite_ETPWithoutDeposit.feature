Feature: HotelInfosite_ETPWithoutDeposit

  @Prod @RC_HotelETPWithoutDeposit
  Scenario Outline: ETP text verification on Hotel infosite in case of without deposit.

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
    And I verify PayNowPayLater Tab is Present
    And I click on Pay Now Button
    And I verify ETP Text is not Displayed

    Examples:
      | destination        | checkInDate | checkOutDate |
      | Pod 39             | 65          | 67           |

