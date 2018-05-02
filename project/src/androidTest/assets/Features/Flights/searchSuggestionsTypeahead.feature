Feature: Search Suggestions for Flights

  @Flights @FlightsTypeahead
  Scenario: Ensure trigger of typeahead calls on typing search query
    Given I launch the App
    And I launch "Flights" LOB
    When I type "lo" in the flights search box
    Then flights suggest typeAhead is not fired
    When I add "n" to the query in flights search box
    Then flights suggest typeAhead is fired for "lon"
    When I add "d" to the query in flights search box
    Then flights suggest typeAhead is fired for "lond"

  @Flights @FlightsTypeahead
  Scenario: Ensure Typeahead works as expected for To and From locations Roundtrip
    Given I launch the App
    And I launch "Flights" LOB
    When I type "lon" in the flights search box
    And I select "London, England, UK (LON - All Airports)" from suggestions
    And I type "lond" in the flights destination search box
    Then flights suggest typeAhead is fired for "lond"

  @Flights @FlightsTypeahead
  Scenario: Ensure Typeahead works as expected for One way trip
    Given I launch the App
    And I launch "Flights" LOB
    And I select one way trip
    When I type "lo" in the flights search box
    Then flights suggest typeAhead is not fired
    When I add "n" to the query in flights search box
    Then flights suggest typeAhead is fired for "lon"
    When I add "d" to the query in flights search box
    Then flights suggest typeAhead is fired for "lond"

  @Flights @FlightsTypeahead @Prod
  Scenario: Typeahead happy path by hitting real service
    Given I launch the App
    And I launch "Flights" LOB
    When I type "TOR" in the flights search box
    Then flights suggest typeAhead is fired

  @Flights @FlightsTypeahead
  Scenario: Recent searches shown in the suggestion list
    Given I launch the App
    And I launch "Flights" LOB
    When I type "lon" in the flights search box
    And I select "London, England, UK (LON - All Airports)" from suggestions
    And I press back following number of times: 2
    And I type "lo" in the flights search box
    Then "London, England, UK (LON - All Airports)" is listed at the top of suggestion list as recent search

  @Flights @FlightsTypeahead
  Scenario: Multi-city metro code type hierarchy shown properly on the UI
    Given I launch the App
    And I launch "Flights" LOB
    When I type "lon" in the flights search box
    Then flights suggest typeAhead is fired for "lon"
    And the results are listed in hierarchy


