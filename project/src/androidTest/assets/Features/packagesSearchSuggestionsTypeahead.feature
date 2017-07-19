Feature: Search Suggestions for packages

  @Packages @PackagesTypeahead
  Scenario: Ensure trigger of typeahead calls on typing search query
    Given I launch the App
    And I launch "Bundle Deals" LOB
    And I click on source search button
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
    And I click on source search button
    When I type "del" in the packages source search box
    And I select "DEL - Indira Gandhi Intl." from suggestions
    And I type "delh" in the packages destination search box
    Then validate "San Francisco, CA (SFO-San Francisco Intl.)" suggestion is fired for typing "delh"
    And I select "San Francisco, CA (SFO-San Francisco Intl.)" from suggestions

  @Packages @PackagesTypeahead @Prod
  Scenario: Typeahead happy path by hitting real service
    Given I launch the App
    And I launch "Bundle Deals" LOB
    And I click on source search button
    When I type "TOR" in the packages source search box
    Then packages suggest typeAhead is fired

  @Packages @PackagesTypeahead
  Scenario: Recent searches shown in the suggestion list
    Given I launch the App
    And I launch "Bundle Deals" LOB
    And I click on source search button
    When I type "del" in the packages source search box
    And I select "DEL - Indira Gandhi Intl." from suggestions
    And I type "de" in the packages destination search box
    Then "Delhi, India (DEL-Indira Gandhi Intl.)" is listed at the top of suggestion list as recent search


  @Packages @PackagesTypeahead @Prod
  Scenario: Flying From field - Typeahead call is made when 3 letters are entered for
    Given I launch the App
    And I want to intercept these calls for packages
      | TypeAheadSFO |
    And I launch "Bundle Deals" LOB
    And I click on source search button
    And I type "sfo" and select the location "SFO - San Francisco Intl."
    Then Validate the "TypeAhead" API request query params for following parameters for packages
      | locale                      | en_US                          |
      | regiontype                  | 95                             |
      | dest                        | false                          |
      | features                    | ta_hierarchy                   |
      | client                      | expedia.app.android.phone      |
      | lob                         | PACKAGES                       |
      | sourceType                  | mobileapp                      |

  @Packages @PackagesTypeahead @Prod
  Scenario: Flying to field - No typeahead call is made when 2 letters are entered
    Given I launch the App
    And I want to intercept these calls for packages
      | TypeAheadSFO |
    And I launch "Bundle Deals" LOB
    And I click on source search button
    When I type "sf" in the packages source search box
    Then Validate that no typeahead call is trigerred for packages

  @Prod
  Scenario: Flying to field - Typeahead call is made when 3 letters are entered for
    Given I launch the App
    And I want to intercept these calls for packages
      | TypeAheadLAS |
    And I launch "Bundle Deals" LOB
    And I click on source search button
    When I type "SFO" in the packages source search box
    And I select "SFO - San Francisco Intl." from suggestions
    And I type "LAS" and select the location "Las Vegas Strip, Las Vegas, NV"
    Then Validate the "TypeAhead" API request query params for following parameters for packages
      | locale                      | en_US                          |
      | regiontype                  | 95                             |
      | dest                        | true                           |
      | features                    | ta_hierarchy                   |
      | client                      | expedia.app.android.phone      |
      | lob                         | PACKAGES                       |
      | sourceType                  | mobileapp                      |

  @Packages @PackagesTypeahead @Prod
  Scenario: Flying From field - No typeahead call is made when 2 letters are entered
    Given I launch the App
    And I want to intercept these calls for packages
      | TypeAheadLAS |
    And I launch "Bundle Deals" LOB
    And I click on source search button
    When I type "sfo" in the packages source search box
    And I select "San Francisco, CA" from suggestions
    And I type "la" in the packages destination search box
    Then Validate that no typeahead call is trigerred for packages
