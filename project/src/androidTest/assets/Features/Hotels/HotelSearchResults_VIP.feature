Feature: HotelSearchResults_VIP

  @Prod @RC_HotelSearchResults_VIP
  Scenario Outline: Validate VIP label presence, based on the user tier type
    Given I launch the App
    And I tap on "Shop Travel" tab
    When I launch "Hotels" LOB
    When I search for hotels and choose a specific location
      | location   | <location>      |
      | suggestion | <searchSuggestion> |
    And I select 5 , 7 as check in and checkout date
    And I click on Search Button
    And I wait for hotel search results to load
    Then I scroll to hotel with name <vipHotelName>
    And I verify the hotel <vipHotelName> has a vip label
    And I click on a hotel with name <vipHotelName>
    Then I verify that VIP Access label is present on hotel infosite page
    And I click on a VIP Access label on hotel infosite page
    Then I verify the VIP Access header text
      | headerText | +VIP Access |
    And I verify the body text on VIP Access page
      | bodyText | At +VIP Access hotels, silver and gold members receive free room upgrades and other perks on availability at check-in. |

    Examples:
      | location      | searchSuggestion  | vipHotelName                         |
      | San Francisco | San Francisco, CA | Argonaut Hotel - a Noble House Hotel |

