Feature: This feature file contains all the scenarios applicable for PKG regression.

  @PackageSearchPage @RC @Package @Validate26NightsPackages
  Scenario Outline: Since the business won't allow bookings spanned more than 26 nights validate that the application is adhering to it.

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select "<sourceSearchSuggest>" as source from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select "<destinationSearchSuggest>" as destination from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I validate that the number of days displayed is <days> on the calender
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear

    Examples:
      | buttonCaption     | source | sourceSearchSuggest       | destination | destinationSearchSuggest               | startDate | endDate | days |
      | Vacation Packages | sfo    | SFO - San Francisco Intl. | DTW         | Detroit Metropolitan Airport (DTW), MI | 5         | 35      | 26   |

  @PackageSearchPage @RC @Package @Validate329DaysAdvanceBookingPackages
  Scenario Outline: Validate that the application doesn't allow any booking past 329 days from current date.

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select "<sourceSearchSuggest>" as source from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select "<destinationSearchSuggest>" as destination from the suggestions on packages search page
    Then I select start date as <startDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I look for the alert message saying "<message>"

    Examples:
      | buttonCaption     | source | sourceSearchSuggest       | destination | destinationSearchSuggest               | startDate | message                                                |
      | Vacation Packages | sfo    | SFO - San Francisco Intl. | DTW         | Detroit Metropolitan Airport (DTW), MI | 335       | This date is too far out, please choose a closer date. |

  @PackageSearchPage @RC @Package @ValidateSingleDaySearchPackages
  Scenario Outline: Validate that the application allows package search on the current date(only start date supplied)

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select "<sourceSearchSuggest>" as source from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select "<destinationSearchSuggest>" as destination from the suggestions on packages search page
    Then I select start date as <startDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear

    Examples:
      | buttonCaption     | source | sourceSearchSuggest       | destination | destinationSearchSuggest               | startDate |
      | Vacation Packages | sfo    | SFO - San Francisco Intl. | DTW         | Detroit Metropolitan Airport (DTW), MI | 10        |

  @PackageSearchPage @RC @Package @ValidateMaxTravellersSearchPackages
  Scenario Outline: Validate that the application allows package search with 6 travellers.

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select "<sourceSearchSuggest>" as source from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select "<destinationSearchSuggest>" as destination from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I select travellers on packages search page
    Then I increase trvallers' count to 6 adults
    Then I click on search button on packages search page
    Then I validate that the results appear

    Examples:
      | buttonCaption     | source | sourceSearchSuggest       | destination | destinationSearchSuggest               | startDate | endDate |
      | Vacation Packages | sfo    | SFO - San Francisco Intl. | DTW         | Detroit Metropolitan Airport (DTW), MI | 5         | 10      |


  @PackageSearchPage @RC @Package @ValidateSameOriginDestinationSearchPackages
  Scenario Outline: Validate that we get an alert when the origin and destination are same.

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select "<sourceSearchSuggest>" as source from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select "<destinationSearchSuggest>" as destination from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I look for the alert message saying "<message>"

    Examples:
      | buttonCaption     | source | sourceSearchSuggest       | destination | destinationSearchSuggest | startDate | endDate | message                                           |
      | Vacation Packages | sfo    | SFO - San Francisco Intl. | sfo         | San Francisco, CA        | 5         | 10      | Departure and arrival airports must be different. |