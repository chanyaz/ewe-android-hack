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


  Scenario Outline: Successfully searching for hotels through "Did you mean..."

    Given I launch the App
    And I launch "Hotels" LOB
    And I search for "<destination>" and select the item with the magnifying glass
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I click on "<popupSuggestionOptionText>" within 'Did You Mean...' popup
    And I wait for hotel search results to load

    Examples:
      | destination                | popupSuggestionOptionText             | checkInDate | checkOutDate |
      | Hotel Nikko San Francisco  | 222 Mason St, San Francisco, CA 94102 | 5           | 7            |
