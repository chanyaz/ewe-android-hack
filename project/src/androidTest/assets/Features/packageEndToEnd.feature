Feature: Package End To End

  @Packages @PackageEtoE @Prod
  Scenario: Validate bundle traveler info on all screens
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | SEA                            |
      | destination         | SFO                            |
      | source_suggest      | SEA - Seattle  -  Tacoma Intl. |
      | destination_suggest | San Francisco, CA              |
      | start_date          | 15                             |
      | end_date            | 20                             |
      | adults              | 2                              |
      | child               | 2                              |
    Then on Package Overview Screen validate the toolbar
      | destination       | San Francisco, CA |
      | start_date        | 15                |
      | end_date          | 20                |
      | totalTravelers    | 4 travelers       |
    Then validate HSR screen is displayed with following travel dates and travelers
      | start_date      | 15  |
      | end_date        | 20 |
      | Total_Travelers | 4  |
    And I click on View your bundle
    Then on Package Overview Screen validate the toolbar
      | destination       | San Francisco, CA |
      | start_date        | 15                |
      | end_date          | 20                |
      | totalTravelers    | 4 travelers       |
    And I click to close sliding bundle
    And I select hotel at position 1 on HSR screen
    Then validate HIS screen is displayed with following travel dates and travelers
      | start_date      |  15       |
      | end_date        |  20       |
      | total_guests    |  4 guests |
    And I select first room
    Then on Package Overview Screen validate the toolbar
      | destination       | San Francisco, CA |
      | start_date        | 15                |
      | end_date          | 20                |
      | totalTravelers    | 4 travelers       |
    And validate outbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 15 |
      | Total_Travelers | 4  |
    And I click on View your bundle
    Then on Package Overview Screen validate the toolbar
      | destination       | San Francisco, CA |
      | start_date        | 15                |
      | end_date          | 20                |
      | totalTravelers    | 4 travelers       |
    And I click to close sliding bundle
    And I select outbound flight to destination at position 1 and goto details page
    Then validate package flight detail screen is displayed with following travel dates and travelers
      | travel_date     | 15 |
      | Total_Travelers | 4  |
    And I click select flight on flight details screen
    Then on Package Overview Screen validate the toolbar
      | destination       | San Francisco, CA |
      | start_date        | 15                |
      | end_date          | 20                |
      | totalTravelers    | 4 travelers       |
    And validate inbound FSR screen is displayed with following travel date and travelers
      | travel_date     | 20 |
      | Total_Travelers | 4  |
    And I click on View your bundle
    Then on Package Overview Screen validate the toolbar
      | destination       | San Francisco, CA |
      | start_date        | 15                |
      | end_date          | 20                |
      | totalTravelers    | 4 travelers       |
    And I click to close sliding bundle
    And I select inbound flight to source at position 1 and goto details page
    Then validate package flight detail screen is displayed with following travel dates and travelers
      | travel_date     | 20 |
      | Total_Travelers | 4  |
    And I click select flight on flight details screen
    And Wait for checkout screen to load after createTrip
    And Close price change Alert dialog if it is visible
    Then on Package Overview Screen validate the toolbar when hotel, outbound and inbound flight is selected
      | start_date        | 15                |
      | end_date          | 20                |
      | totalTravelers    | 4 travelers       |
