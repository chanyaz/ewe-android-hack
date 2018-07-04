Feature: Package Launch

  @Packages @Prod @EBOnlySet3
  Scenario Outline: UI validation if Packages Tab is present and clickable on Launch Screen for POSales
    Given I set the POS to "<point_of_sale>"
    And I launch the App
    And I launch Package LOB with "<point_of_sale>" POS
    When I make a packages search with following parameters
      | source              | Delhi                                  |
      | destination         | London                                 |
      | source_suggest      | DEL - Indira Gandhi Intl.              |
      | destination_suggest | London, England, UK (LON-All Airports) |
      | start_date          | 10                                     |
      | end_date            | 12                                     |
      | adults              | 1                                      |
      | child               | 1                                      |
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
      | Thailand       |
      | Germany        |
      | France         |
      | Italy          |
      | South Korea    |
      | Mexico         |
