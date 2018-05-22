Feature: Happy Path through Checkout Web View

  @Prod @HotelsCheckoutWebView
  Scenario: Navigate to Checkout Web View through hotels and verify user can interact with web view.
    Given I set bucketing rules for A/B tests as
    | EBAndroidAppHotelsWebCheckout | BUCKETED |
    And I launch the App

    When I launch "Hotels" LOB
    And I search for hotels and choose a specific location
      | location   | San Francisco     |
      | suggestion | San Francisco, CA |
    And I select 5 , 10 as check in and checkout date
    And I click on Search Button
    And I can see hotel search results
    And I select hotel at position 1 on HSR screen
    And I book first room
    And I wait for checkout webview to load

    Then I populate contact name field with 'EspressoWebView FirstLastName'
    And I verify contact name field contains 'EspressoWebView FirstLastName'

    And I decline insurance
    And I verify insurance has been declined
