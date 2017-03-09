Feature: Flight Details on a seperate screen

  @Flights @FlightDetails
  Scenario: Verify data consistency between outbound search and details screen on flights toolbar

    Given I launch the App
    And I launch "Flights" LOB
    When I make a flight search with following parameters
      | source              | SFO                                      |
      | destination         | DEL                                      |
      | source_suggest      | San Francisco, CA                        |
      | destination_suggest | Delhi, India (DEL - Indira Gandhi Intl.) |
      | start_date          | 5                                        |
      | end_date            | 25                                       |
      | adults              | 3                                        |
      | child               | 2                                        |
    And I wait for results to load
    And I click on the flight with airline name "happy_round_trip" at "9:00 pm - 11:00 pm"
    Then on flight details screen the destination is "Delhi, India"
    And on flight details the traveler count is 5
    And on Flight detail check the date is as user selected
    And price displayed on flight details is "$696.00"
    And flight time on the flight details is "9:00 pm - 11:00 pm"
    And airport names on the flight details is "(SFO) SFO - (DEL) DEL"
    And airline name on the flight details is "happy_round_trip"
    And flight duration on the flight details is "2h 0m"
    And flight total duration on the flight details is "2h 0m"
    And Baggage link "Baggage fee info" is present on the flight details
    And Select button "Select this Flight" is displayed at the bottom of the flight details screen



