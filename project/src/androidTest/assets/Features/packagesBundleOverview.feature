Feature: Bundle Overview with packages details

  @Packages @BundleOverview @Prod
  Scenario: Overview screen data when no hotel has been selected - from hotels SRP
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | KTM                   |
      | destination         | LAS                   |
      | source_suggest      | KTM - Tribhuvan Intl. |
      | destination_suggest | Las Vegas Strip, NV   |
      | start_date          | 5                     |
      | end_date            | 10                    |
      | adults              | 2                     |
      | child               | 2                     |
    And I wait for hotels results for packages to load
    And I tap on bundle overview sliding widget bar at the bottom
    Then following information on the bundle overview screen isDisplayed: true
      | Step 1 Text                       | Step 1: Select hotel            |
      | Hotel Bar - Hotel text            | Select hotel in Las Vegas Strip |
      | Hotel Bar - Date                  | 6 - 10                          |
      | Hotel Bar - travelers             | 4 guests                        |
      | Step 2 Text                       | Step 2: Select flights          |
      | Hotel Image                       | Hotel icon Drawable             |
      | Outbound Flight Bar - Flight Text | Flight to Las Vegas             |
      | Outbound Flight Bar - date        | 5                               |
      | Outbound Flight Bar - traveler    | 4 travelers                     |
      | Flight Outbound Image             | Flight Outbound drawable        |
      | Inbound Flight Bar - Flight Text  | Flight to Kathmandu             |
      | Inbound Flight Bar - date         | 10                              |
      | Inbound Flight Bar - traveler     | 4 travelers                     |
      | Flight Inbound Image              | Flight Inblund drawable         |
    And "Outbound Flight Bar" on bundle overview isDisabled: true
    And "Inbound Flight Bar" on bundle overview isDisabled: true


  @Packages @BundleOverview @Prod
  Scenario: Hotels SRP accessible from overiview screen - no hotel selected yet
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | KTM                   |
      | destination         | LAS                   |
      | source_suggest      | KTM - Tribhuvan Intl. |
      | destination_suggest | Las Vegas Strip, NV   |
      | start_date          | 5                     |
      | end_date            | 10                    |
      | adults              | 2                     |
      | child               | 2                     |
    And I wait for hotels results for packages to load
    And I tap on bundle overview sliding widget bar at the bottom
    And I tap on "hotels bar" on bundle overview screen
    Then Hotels SRP is displayed


  @Packages @BundleOverview @Prod
  Scenario: Back Press on Hotels SRP displays bundle overview
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I make a packages search with following parameters
      | source              | KTM                   |
      | destination         | LAS                   |
      | source_suggest      | KTM - Tribhuvan Intl. |
      | destination_suggest | Las Vegas Strip, NV   |
      | start_date          | 5                     |
      | end_date            | 10                    |
      | adults              | 2                     |
      | child               | 2                     |
    And I wait for hotels results for packages to load
    And I press back
    Then Bundle Overview screen is displayed
    And following information on the bundle overview screen isDisplayed: true
      | Step 1 Text                       | Step 1: Select hotel            |
      | Hotel Bar - Hotel text            | Select hotel in Las Vegas Strip |
      | Hotel Bar - Date                  | 6 - 10                          |
      | Hotel Bar - travelers             | 4 guests                        |
      | Step 2 Text                       | Step 2: Select flights          |
      | Hotel Image                       | Hotel icon Drawable             |
      | Outbound Flight Bar - Flight Text | Flight to Las Vegas             |
      | Outbound Flight Bar - date        | 5                               |
      | Outbound Flight Bar - traveler    | 4 travelers                     |
      | Flight Outbound Image             | Flight Outbound drawable        |
      | Inbound Flight Bar - Flight Text  | Flight to Kathmandu             |
      | Inbound Flight Bar - date         | 10                              |
      | Inbound Flight Bar - traveler     | 4 travelers                     |
      | Flight Inbound Image              | Flight Inblund drawable         |
    And "Outbound Flight Bar" on bundle overview isDisabled: true
    And "Inbound Flight Bar" on bundle overview isDisabled: true





