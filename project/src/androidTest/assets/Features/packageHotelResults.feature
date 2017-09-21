Feature: Package Hotel Results

  @Packages @PackageHotelResults
  Scenario: Validate Unreal deals are shown in hotel results
      Given I launch the App
      And I launch "Bundle Deals" LOB
      When I make a packages search with following parameters
        | source              | KTM                            |
        | destination         | SFO                            |
        | source_suggest      | KTM - Tribhuvan Intl.          |
        | destination_suggest | San Francisco, CA              |
        | start_date          | 5                              |
        | end_date            | 10                             |
        | adults              | 2                              |
        | child               | 2                              |
      Then Validate unreal deal is displayed
        | position | 2                             |
        | title    | Unreal Deal                   |
        | message  | Book this and save $110 (22%) |
      Then Validate unreal deal is displayed
        | position | 3                             |
        | title    | Unreal Deal                   |
        | message  | Book this and save $110 (22%) |

  @Packages @PackageHotelResults @WIP @Prod
  Scenario: Validate toolbar details in hotel results
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
