#!/bin/bash

export TERM=dumb

internal_artifact() {
tar -czvf ~/artifacts/uitests-$BUILD_NUMBER.tar.gz project/build/fork
}

trap internal_artifact EXIT
./gradlew --no-daemon clean --continue
./gradlew --no-daemon clean

#unistall old apks
./tools/uninstall.sh com.expedia.bookings

TESTS=""
function add_test() {
TESTS+="$1,"
}

#Happy path tests
add_test "ItinPhoneHappyPathTest"
add_test "HotelPhoneHappyPathTest"
add_test "FlightPhoneHappyPathTest"
add_test "CarPhoneHappyPathTest"
add_test "LxPhoneHappyPathTest"
add_test "NewHotelPhoneHappyPathTest"

#Cars tests
add_test "CarFilterTest"
add_test "CarSearchPresenterTest"
add_test "CarSearchErrorTest"
add_test "CarCreateTripErrorTest"
add_test "CarCheckoutErrorTest"
add_test "CarCreditCardTest"
add_test "CarDetailsTest"
add_test "CarCheckoutViewTest"

#Flights tests
add_test "FlightCheckoutUserInfoTest"
add_test "FlightFieldValidationTest"
add_test "FlightTravelerErrorHandlingTest"
add_test "FlightChildTravelersTest"
add_test "FlightSearchTest"

#Hotels tests
add_test "HotelDetailsTest"
add_test "HotelCheckoutUserInfoTest"
add_test "CreditCardsInfoEditTest"
add_test "HotelConfirmationTest"
add_test "HotelRoomsAndRatesTest"
add_test "HotelFieldValidationTest"
add_test "HotelDateAcrossMonthsTest"
add_test "HotelCouponErrorTest"
add_test "HotelCheckoutPriceChangeTest"
add_test "HotelResultsPresenterTests"

#LX tests
add_test "LXCurrentLocationErrorTest"
add_test "LXSearchParamsTest"
add_test "LXDetailsPresenterTest"
add_test "LXResultsPresenterTest"
add_test "LXCheckoutPresenterTest"
add_test "LXCheckoutErrorTest"
add_test "LXCreateTripErrorTest"
add_test "LXInfositeTest"
add_test "LxSearchResultsTest"
add_test "LXCreditCardTest"

./gradlew --no-daemon -Dfork.tablet=true -Dandroid.test.classes="${TESTS}" aED aEDAT forkExpediaDebug
