Feature: Hotel Search

  @Prod @HotelSearch
  Scenario Outline: Successfully searching for hotels with different combinations.

    Given I launch the App
    And I launch "Hotels" LOB
    When I search for hotels with following criteria
      | location   | <destination>   |
      | suggestion | <searchSuggest> |
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    Then I can see hotel search results

    Examples:
      | destination | searchSuggest                                 | checkInDate | checkOutDate |
      | SFO         | San Francisco International Airport (SFO), CA | 5           | 10           |
      | Delhi       | Indira Gandhi                                 | 15          | 20           |