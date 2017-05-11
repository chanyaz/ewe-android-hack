Feature: Search Suggestions for packages

  @Packages @PackagesTypeahead
  Scenario: Ensure trigger of typeahead calls on typing search query
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I type "de" in the packages source search box
    Then packages suggest typeAhead is not fired
    When I add "l" in the packages source search box
    Then validate "DEL - Indira Gandhi Intl." suggestion is fired for typing "del"
    When I add "h" in the packages source search box
    Then validate "SFO - San Francisco Intl." suggestion is fired for typing "delh"

  @Packages @PackagesTypeahead
  Scenario: Ensure Typeahead works as expected for source and destination locations
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I type "del" in the packages source search box
    And I select "DEL - Indira Gandhi Intl." from suggestions
    And I type "delh" in the packages destination search box
    Then validate "San Francisco, CA (SFO-San Francisco Intl.)" suggestion is fired for typing "delh"
    And I select "San Francisco, CA (SFO-San Francisco Intl.)" from suggestions

  @Packages @PackagesTypeahead @Prod
  Scenario: Typeahead happy path by hitting real service
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I type "TOR" in the packages source search box
    Then packages suggest typeAhead is fired

  @Packages @PackagesTypeahead
  Scenario: Recent searches shown in the suggestion list
    Given I launch the App
    And I launch "Bundle Deals" LOB
    When I type "del" in the packages source search box
    And I select "DEL - Indira Gandhi Intl." from suggestions
    And I type "de" in the packages destination search box
    Then "Delhi, India (DEL-Indira Gandhi Intl.)" is listed at the top of suggestion list as recent search
