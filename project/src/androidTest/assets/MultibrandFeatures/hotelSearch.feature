Feature: Getting hotel results from the App

  @HotelSearch
  Scenario Outline: Successfully searching for hotels with different combinations.

    Given I have the app installed and I open it.
    Then I should be able to click Hotels button to launch the hotel search screen
    Then I enter destination as "<destination>"
    Then I select "<searchSuggest>" as search suggestion
    Then I select <checkInDate> , <checkOutDate> as check in and checkout date
    Then I click on Search Button
    Then I wait for the results to appear

    Examples:
      | destination | searchSuggest                                 | checkInDate | checkOutDate |
      | SFO         | San Francisco International Airport (SFO), CA | 5           | 10           |
      | Delhi       | Indira Gandhi                                 | 15          | 20           |