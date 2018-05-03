Feature: Package Hotel Results

  @Packages @PackageHotelResults
  Scenario: Validate Unreal deals are shown in hotel results
      Given I launch the App
      And I launch "Bundle Deals" LOB
      When I make a packages search with following parameters
        | source              | sfo                            |
        | destination         | KTM                            |
        | source_suggest      | San Francisco, CA              |
        | destination_suggest | Kathmandu, Nepal (KTM-Tribhuvan Intl.)             |
        | start_date          | 5                              |
        | end_date            | 10                             |
        | adults              | 2                              |
        | child               | 2                              |
      Then Validate unreal deal is displayed
        | position | 3                                       |
        | title    | Unreal Deal                             |
        | message  | Book this and save 100% on your flight. |
      Then Validate unreal deal is displayed
        | position | 4                                       |
        | title    | Unreal Deal                             |
        | message  | Book this and save 549.62 (23.86%)      |

  @Packages @PackageHotelResults @Prod
    Scenario: Validate Accessibility aspects of hotel results description header
        Given I launch the App
        And I launch "Bundle Deals" LOB
        When I make a packages search with following parameters
          | source              | SFO                            |
          | destination         | LAS                            |
          | source_suggest      | SFO - San Francisco Intl.      |
          | destination_suggest | Las Vegas Strip, NV            |
          | start_date          | 15                             |
          | end_date            | 20                             |
          | adults              | 2                              |
          | child               | 2                              |
        And validate HSR screen is displayed with following travel dates and travelers
          | start_date      |  15 |
          | end_date        |  20 |
          | Total_Travelers |  4  |
        Then Validate content description of hotel results description header
