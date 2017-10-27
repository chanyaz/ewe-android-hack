@Prod @RC_HotelSearch
Feature: Hotel Search with name

  Scenario Outline: Successfully searching for hotels with hotel name.

    Given I launch the App
    And I launch "Hotels" LOB
    When I search for hotels with following criteria
      | location   | <destination>   |
      | suggestion | <searchSuggest> |
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I verify the hotel label text is "<destination>"

    Examples:
      | destination                | searchSuggest                                | checkInDate | checkOutDate |
      | Hotel Nikko San Francisco  | Hotel Nikko San Francisco, San Francisco, CA | 5           | 7            |


  Scenario Outline: Successfully searching for hotels through "Did you mean..."

    Given I launch the App
    And I launch "Hotels" LOB
    And I search for hotels and choose a specific location
      | location   | <destination> |
      | suggestion | <searchSuggest> |
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I click on "<popupSuggestionOptionText>" within 'Did You Mean...' popup
    And I wait for hotel search results to load

    Examples:
      | destination                | searchSuggest               | popupSuggestionOptionText             | checkInDate | checkOutDate |
      | Hotel Nikko San Francisco  | "Hotel Nikko San Francisco" | 222 Mason St, San Francisco, CA 94102 | 5           | 7            |
