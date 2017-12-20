Feature: Package Launch

  @Packages @Prod @EBOnlySet1
  Scenario Outline: UI validation if Packages Tab is present and clickable on Launch Screen for POSales
    Given I set the POS to "<point_of_sale>"
    And I launch the App
    And I launch Package LOB with "<point_of_sale>" POS
    When I make a packages search with following parameters
      | source              | SFO                       |
      | destination         | LAS                       |
      | source_suggest      | SFO - San Francisco Intl. |
      | destination_suggest | Las Vegas Strip, NV       |
      | start_date          | 20                        |
      | end_date            | 30                        |
      | adults              | 2                         |
      | child               | 2                         |
    Then Validate that hotel SRP screen is displayed
    And I press back following number of times: 3
    Then Validate that Launch screen is displayed

    Examples:
      | point_of_sale  |
      | United States  |
      | United Kingdom |
      | Japan          |
      | Singapore      |
      | Malaysia       |
      | Australia      |
      | New Zealand    |
      | Canada         |

  @Packages @Prod @EBOnlySet2
  Scenario Outline: UI validation if Packages Tab is present and clickable on Launch Screen for POSales under Abacus Test
    Given I set the POS to "<point_of_sale>"
    And I bucket the following tests
      | PackagesNewPOSLaunch |
    And I launch the App
    And I launch Package LOB with "<point_of_sale>" POS
    When I make a packages search with following parameters
      | source              | SFO                       |
      | destination         | LAS                       |
      | source_suggest      | SFO - San Francisco Intl. |
      | destination_suggest | Las Vegas Strip, NV       |
      | start_date          | 20                        |
      | end_date            | 30                        |
      | adults              | 2                         |
      | child               | 2                         |
    Then Validate that hotel SRP screen is displayed
    And I press back following number of times: 3
    Then Validate that Launch screen is displayed

    Examples:
      | point_of_sale |
      | Thailand      |
      | Germany       |