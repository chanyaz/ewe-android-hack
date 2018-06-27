Feature: HotelSearch

  @Prod @RC_HotelSearch
  Scenario Outline: Successfully searching for hotels with hotel name, results in a pinned hotel.
    Given I launch the App
    And I launch "Hotels" LOB
    And I enter destination as "<hotelName>"
    And I select hotel with the text "<hotelName>"
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I wait for hotel search results to load
    And I verify pinned hotel name is <hotelName>

    Examples:
      | hotelName                 | checkInDate | checkOutDate |
      | Hotel Nikko San Francisco | 5           | 7            |


  @Prod @HotelSearch @SP
  Scenario Outline: Successfully searching for hotels with hotel name, results in a pinned hotel.
    Given I launch the App
    And I launch "Hotels" LOB
    And I enter destination as "<destination>"
    And I select <searchObjectType> with the text "<searchSuggest>"
    And I select <checkInDate> , <checkOutDate> as check in and checkout date
    And I click on Search Button
    And I wait for hotel search results to load

    Examples:
      | searchObjectType      | destination | searchSuggest                                                         | checkInDate | checkOutDate |
      | location              | SFO         | San Francisco, CA, United States of America (SFO-San Francisco Intl.) | 5           | 10           |
      | location in hierarchy | Delhi       | New Delhi                                                             | 15          | 20           |
