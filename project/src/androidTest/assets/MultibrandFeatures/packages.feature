Feature: This feature file contains all the scenarios applicable for PKG regression.

  @PackageSearchPage @RC @Package @Validate26NightsPackages
  Scenario Outline: Since the business won't allow bookings spanned more than 26 nights validate that the application is adhering to it.

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
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
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
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
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
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
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
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
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I look for the alert message saying "<message>"

    Examples:
      | buttonCaption     | source | sourceSearchSuggest       | destination | destinationSearchSuggest | startDate | endDate | message                                           |
      | Vacation Packages | sfo    | SFO - San Francisco Intl. | sfo         | San Francisco, CA        | 5         | 10      | Departure and arrival airports must be different. |


  @RC @Package @PackageHotelsResultsPage @ValidateFilterButtonWorksBeforeMapLoads
  Scenario Outline: Validate that we filter button works when map is still loading in hotel SRP

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear
    Then I click on filter button on package hotels SRP

    Examples:
      | buttonCaption     | source | sourceSearchSuggest       | destination | destinationSearchSuggest               | startDate | endDate |
      | Vacation Packages | sfo    | SFO - San Francisco Intl. | DTW         | Detroit Metropolitan Airport (DTW), MI | 5         | 10      |


  @RC @Package @PackageHotelDetailsPage @ValidateRenovationNoticeAppear
  Scenario Outline: Validate that the renovation notice appear on hotel details page
    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear
    Then I select hotel with name "<hotelName>"
    Then I search for the renovation notice

    Examples:
      | buttonCaption     | source | sourceSearchSuggest                     | destination | destinationSearchSuggest  | startDate | endDate | hotelName                 |
      | Vacation Packages | DTW    | DTW - Detroit Metropolitan Wayne County | sfo         | San Francisco, CA         | 5         | 10      | Hotel Nikko San Francisco |


  @RC @Package @PackageFlightsResultsPage @ValidateFlightSortFiltersWork
  Scenario Outline: Validate that the flight sort filters work

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear
    Then I select hotel with name "<hotelName>"
    Then I select room in package hotel details page
    Then I wait for flight results to appear in packages
    Then I click on the top filter button on flights results screen
    Then I check if filter by stops works in packages flight filter


    Examples:
      | buttonCaption     | source | sourceSearchSuggest                     | destination | destinationSearchSuggest  | startDate | endDate | hotelName     |
      | Vacation Packages | DTW    | DTW - Detroit Metropolitan Wayne County | sfo         | San Francisco, CA         | 5         | 10      | Serrano Hotel |


  @RC @Package @PackageFlightsResultsPage @ValidateFlightFilterSlidersWork
  Scenario Outline: Validate that the flight filter sliders work as expected

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear
    Then I select hotel with name "<hotelName>"
    Then I select room in package hotel details page
    Then I wait for flight results to appear in packages
    Then I click on the top filter button on flights results screen
    Then I decrease the flight duration to <time> hr in packages flight filter
    Then I set departure time from <DepFrom> to <DepTo> in packages flight filter
    Then I set arrival time from <ArrivalFrom> to <ArrivalTo> in packages flight filter
    Then I click sort to get results in packages flight filter
    Then I select the <number> flight from package flights to go to details page
    Then I select this flight and wait for inbound flights results to appear
    Then I click on the top filter button on flights results screen
    Then I decrease the flight duration to <time> hr in packages flight filter
    Then I set departure time from <DepFrom> to <DepTo> in packages flight filter
    Then I set arrival time from <ArrivalFrom> to <ArrivalTo> in packages flight filter


    Examples:
      | buttonCaption     | source | sourceSearchSuggest                     | destination | destinationSearchSuggest  | startDate | endDate | hotelName     | time  | DepFrom | DepTo | ArrivalFrom | ArrivalTo| number |
      | Vacation Packages | DTW    | DTW - Detroit Metropolitan Wayne County | sfo         | San Francisco, CA         | 5         | 10      | Serrano Hotel | 10    | 8       | 16    | 10          | 20       | 0      |


  @RC @Package @PackageFlightsDetailsPage @ValidatePriceIsPerPersonOnDetailsPage
  Scenario Outline: Validate that the price on details page is per person

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear
    Then I select hotel with name "<hotelName>"
    Then I select room in package hotel details page
    Then I wait for flight results to appear in packages
    Then I select the <number> flight from package flights to go to details page
    Then I check if the price here is given per person
    Then I select this flight and wait for inbound flights results to appear
    Then I select the <number> flight from package flights to go to details page
    Then I check if the price here is given per person

    Examples:
      | buttonCaption     | source | sourceSearchSuggest                     | destination | destinationSearchSuggest  | startDate | endDate | hotelName     | time  | DepFrom | DepTo | ArrivalFrom | ArrivalTo| number |
      | Vacation Packages | DTW    | DTW - Detroit Metropolitan Wayne County | sfo         | San Francisco, CA         | 5         | 10      | Serrano Hotel | 10    | 8       | 11    | 10          | 15       | 0      |


  @RC @Package @PackageFlightsDetailsPage @ValidateBaggageFeeLinkGetsLaunched
  Scenario Outline: Validate that the baggage fee info link is successfully launched

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear
    Then I select hotel with name "<hotelName>"
    Then I select room in package hotel details page
    Then I wait for flight results to appear in packages
    Then I select the <number> flight from package flights to go to details page
    Then I click on the baggage fee info link
    Then I wait for the web info page to load and take screenshot
    Then I close the baggage fee info page to come to the details page
    Then I select this flight and wait for inbound flights results to appear
    Then I select the <number> flight from package flights to go to details page
    Then I click on the baggage fee info link
    Then I wait for the web info page to load and take screenshot
    Then I close the baggage fee info page to come to the details page

    Examples:
      | buttonCaption     | source | sourceSearchSuggest                     | destination | destinationSearchSuggest  | startDate | endDate | hotelName     | time  | DepFrom | DepTo | ArrivalFrom | ArrivalTo| number |
      | Vacation Packages | DTW    | DTW - Detroit Metropolitan Wayne County | sfo         | San Francisco, CA         | 5         | 10      | Serrano Hotel | 10    | 8       | 11    | 10          | 15       | 0      |


  @RC @Package @PackageBundleOverviewPage @ValidateReturnFlightIsNotDisplayedBeforeSelecting
  Scenario Outline: Validate that the return flight time is not displayed as it is not yet selected while changing flights

    Given I have the app installed and I open it.
    Then I should be able to click LOB button with caption "<buttonCaption>" on home page
    Then I enter "<source>" in flying from input box on packages search page
    Then I select source as "<sourceSearchSuggest>" from the suggestions on packages search page
    Then I enter "<destination>" in flying to input box on packages search page
    Then I select destination as "<destinationSearchSuggest>" from the suggestions on packages search page
    Then I select dates as <startDate> and <endDate> on packages search page
    Then I click on calender done button
    Then I click on search button on packages search page
    Then I validate that the results appear
    Then I select hotel with name "<hotelName>"
    Then I select room in package hotel details page
    Then I wait for flight results to appear in packages
    Then I select the <number> flight from package flights to go to details page
    Then I select this flight and wait for inbound flights results to appear
    Then I select the <number> flight from package flights to go to details page
    Then I select the flight and wait for package deal to load
    Then I click on change flights to edit flights
    Then I wait for flight results to appear in packages
    Then I select the <number2> flight from package flights to go to details page
    Then I select this flight and wait for inbound flights results to appear
    Then I select the <number2> flight from package flights to go to details page
    Then I select the flight and wait for package deal to load

    Examples:
      | buttonCaption     | source | sourceSearchSuggest                     | destination | destinationSearchSuggest  | startDate | endDate | hotelName     | number  | time  | DepFrom | DepTo | ArrivalFrom | ArrivalTo| number2 |
      | Vacation Packages | DTW    | DTW - Detroit Metropolitan Wayne County | sfo         | San Francisco, CA         | 5         | 10      | Serrano Hotel | 0       | 10    | 8       | 11    | 10          | 15       | 2       |