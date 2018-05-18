package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.GaiaSuggestion.LocalizedName;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Waypoint;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricRunner.class)
public class StrUtilsTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Before
	public void before() {
		getContext().setTheme(R.style.V2_Theme_Packages);
	}

	@Test
	public void testLegalTextContent() {

		SpannableStringBuilder legalText = StrUtils.generateLegalClickableLink(getContext(), "");

		String expectedText = getLegalText();
		assertEquals(expectedText, legalText.toString());
	}

	@Test
	public void testLegalTextSpans() {
		SpannableStringBuilder legalTextSpanBuilder = StrUtils.generateLegalClickableLink(getContext(), "");

		String rulesText = getContext().getString(R.string.rules_and_restrictions);
		String termsText = getContext().getString(R.string.terms_and_conditions);
		String privacyText = getContext().getString(R.string.privacy_policy);

		String legalText = getLegalText();
		int rulesStart = legalText.indexOf(rulesText);
		int termStart = legalText.indexOf(termsText);
		int privacyStart = legalText.indexOf(privacyText);
		int rulesEnd = rulesStart + rulesText.length();
		int termEnd = termStart + termsText.length();
		int privacyEnd = privacyStart + privacyText.length();

		Object[] rulesSpans = legalTextSpanBuilder.getSpans(rulesStart, rulesEnd, Object.class);
		Object[] termsSpans = legalTextSpanBuilder.getSpans(termStart, termEnd, Object.class);
		Object[] privacySpans = legalTextSpanBuilder.getSpans(privacyStart, privacyEnd, Object.class);

		List<Object[]> spansList = new ArrayList<>();
		spansList.add(rulesSpans);
		spansList.add(termsSpans);
		spansList.add(privacySpans);

		for (Object[] spans : spansList) {
			assertEquals(spans[0].getClass(), LegalClickableSpan.class);
			assertEquals(spans[1].getClass(), StyleSpan.class);
			assertEquals(spans[2].getClass(), UnderlineSpan.class);
			assertEquals(spans[3].getClass(), ForegroundColorSpan.class);
		}
	}

	@Test
	public void testBaggageFeesTextContent() {
		FlightTrip flightTrip = createFlightTrip();
		SpannableStringBuilder spannableStringBuilder = StrUtils.generateBaggageFeesTextWithClickableLinks(getContext(), flightTrip.getLeg(0).getBaggageFeesUrl(), flightTrip.getLeg(1).getBaggageFeesUrl());
		assertEquals("Departure and Return flights have their own baggage fees", spannableStringBuilder.toString());
		assertEquals(spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), LegalClickableSpan.class)[0].getURL(), "www.expedia.com");
		assertEquals(spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), LegalClickableSpan.class)[1].getURL(), "www.travelocity.com");
	}

	private FlightTrip createFlightTrip() {
		FlightTrip flightTrip = new FlightTrip();
		FlightLeg outBoundFlightLeg = new FlightLeg();
		FlightLeg returnFlightLeg = new FlightLeg();
		outBoundFlightLeg.setBaggageFeesUrl("www.expedia.com");
		returnFlightLeg.setBaggageFeesUrl("www.travelocity.com");
		flightTrip.addLeg(outBoundFlightLeg);
		flightTrip.addLeg(returnFlightLeg);
		return flightTrip;
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA })
	public void loyaltyRewardsLegalTextTextIsCorrect() {
		String expectedText = "Join Expedia Rewards today. By joining Expedia Rewards, I accept all Terms and Conditions.";
		assertEquals(expectedText, StrUtils.generateLoyaltyRewardsLegalLink(getContext()).toString());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA })
	public void newAccountCreationTermsTextTextIsCorrect() {
		String expected = "By creating an account, I accept the Expedia Rewards Terms and Conditions, and have read and accept the Terms of Use and the Privacy Policy.";
		assertEquals(expected, StrUtils.generateNewTermsRewardLegalLink(getContext()).toString());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA })
	public void newAccountCreationTermsTextLinksAreCorrect() {
		SpannableStringBuilder termsText = StrUtils.generateNewTermsRewardLegalLink(getContext());

		assertTextIsLinkedToUrl(termsText, "Terms and Conditions",
			"https://www.expedia.com/loyaltyrewards/pages/info-rewards/expediarewards/terms.htm");
		assertTextIsLinkedToUrl(termsText, "Terms of Use", "https://www.expedia.com/p/info-other/legal.htm");
		assertTextIsLinkedToUrl(termsText, "Privacy Policy", "https://www.expedia.com/p/info-other/privacy-policy.htm");
	}

	private void assertTextIsLinkedToUrl(SpannableStringBuilder termsText, String substring, String expectedUrl) {
		int substringStart = termsText.toString().indexOf(substring);
		int substringEnd = substringStart + substring.length();
		LegalClickableSpan[] spans = termsText.getSpans(substringStart, substringEnd, LegalClickableSpan.class);

		assertEquals(1, spans.length);
		assertEquals(expectedUrl, spans[0].getURL());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA })
	public void newAccountCreationTermsTextLinksAreBold() {
		SpannableStringBuilder termsText = StrUtils.generateNewTermsRewardLegalLink(getContext());

		assertTextIsBold(termsText, "Terms and Conditions");
		assertTextIsBold(termsText, "Terms of Use");
		assertTextIsBold(termsText, "Privacy Policy");
	}

	private void assertTextIsBold(SpannableStringBuilder termsText, String boldedText) {
		int substringStart = termsText.toString().indexOf(boldedText);
		int substringEnd = substringStart + boldedText.length();
		StyleSpan[] spans = termsText.getSpans(substringStart, substringEnd, StyleSpan.class);

		assertEquals(1, spans.length);
		assertEquals(Typeface.BOLD, spans[0].getStyle());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA })
	public void loyaltyLegalTextLinksAreCorrect() {
		SpannableStringBuilder loyaltyLegalSpanBuilder = StrUtils.generateLoyaltyRewardsLegalLink(getContext());
		assertTextIsLinkedToUrl(loyaltyLegalSpanBuilder, "Terms and Conditions",
			"https://www.expedia.com/loyaltyrewards/pages/info-rewards/expediarewards/terms.htm");
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA })
	public void loyaltyLegalTextLinksAreBold() {
		SpannableStringBuilder loyaltyLegalSpanBuilder = StrUtils.generateLoyaltyRewardsLegalLink(getContext());
		assertTextIsBold(loyaltyLegalSpanBuilder, "Terms and Conditions");
	}

	@Test
	public void testHTMLFormatting() {
		final String htmlString = "<p>The New/York Pass offers something just right for you.</p>";
		final String pTagOpen = "<p>";
		final String pTagClose = "</p>";
		String formattedString = StrUtils.stripHTMLTags(htmlString);
		Assert.assertThat(formattedString, allOf(not(containsString(pTagOpen)), not(containsString(pTagClose))));
	}

	@Test
	public void testGenerateBulletedList() {
		List<String> items = new ArrayList<>();
		items.add("Item1");
		items.add("Item2");
		items.add("Item3");
		String newline = "\n";
		SpannableStringBuilder stringBuilder = StrUtils.generateBulletedList(items);
		BulletSpan[] bulletSpan = stringBuilder.getSpans(0, stringBuilder.length(), BulletSpan.class);
		assertEquals(items.size(), bulletSpan.length);
		// Check if newline added to each item except the last one.
		assertEquals(items.get(0) + newline, stringBuilder.subSequence(0, 6).toString());
		assertEquals(items.get(1) + newline, stringBuilder.subSequence(6, 12).toString());
		assertEquals(items.get(2), stringBuilder.subSequence(12, 17).toString());
	}

	private String getLegalText() {
		String rulesText = getContext().getString(R.string.rules_and_restrictions);
		String termsText = getContext().getString(R.string.terms_and_conditions);
		String privacyText = getContext().getString(R.string.privacy_policy);

		return getContext().getString(R.string.legal_TEMPLATE, rulesText, termsText, privacyText);
	}

	@Test
	public void testCityStateCountryNameFormatting() {
		String displayNameResponse = "New York, NY, United States (NYC-All Airports)";
		String formattedString = StrUtils.formatCityStateCountryName(displayNameResponse);
		assertEquals(formattedString, "New York, NY, United States");
	}

	@Test
	public void testCityStateNameFormatting() {
		String displayNameResponse1 = "New York, NY(NYC-All Airports)";
		String formattedString1 = StrUtils.formatCityStateName(displayNameResponse1);
		assertEquals(formattedString1, "New York, NY");

		String displayNameResponse2 = "";
		String formattedString2 = StrUtils.formatCityStateName(displayNameResponse2);
		assertEquals(formattedString2, "");

		String displayNameResponse3 = "New York, NY, United States";
		String formattedString3 = StrUtils.formatCityStateName(displayNameResponse3);
		assertEquals(formattedString3, "New York, NY, United States");

		String formattedString4 = StrUtils.formatCityStateName(null);
		assertEquals(formattedString4, "");
	}

	@Test
	public void testAirportNameFormatting() {
		String displayNameResponse = "New York, NY, United States (NYC-All Airports)";
		String formattedString = SuggestionStrUtils.formatAirportName(displayNameResponse);
		assertEquals(formattedString, "NYC - All Airports");
	}

	@Test
	public void testAirportCodeCityNameFormatting() {
		String formattedString = StrUtils.formatAirportCodeCityName(getDummySuggestion());
		assertEquals("(CHI) Chicago", formattedString);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
		MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS})
	public void testWaypointFormatting() {
		Waypoint waypoint = new Waypoint(Waypoint.ACTION_ARRIVAL);
		waypoint.mAirportCode = "YVR";
		waypoint.mCity = "Vancouver";
		assertEquals("Vancouver, Canada" , StrUtils.formatWaypoint(waypoint));

		waypoint.mAirportCode = "ABCDE";
		waypoint.mCity = "London";
		assertEquals("London" , StrUtils.formatWaypoint(waypoint));

		waypoint.mAirportCode = null;
		assertNull(StrUtils.formatWaypoint(waypoint));
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
		MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS})
	public void testAirportFormatting() {
		Airport airport = new Airport();
		airport.mCountryCode = "GB";
		String localizedAirportCity = "London";
		assertEquals("London, United Kingdom" , StrUtils.formatAirport(airport, localizedAirportCity));

		airport.mAirportCode = "YVR";
		airport.mCity = "";
		airport.mCountryCode = "CA";
		assertEquals("YVR, Canada" , StrUtils.formatAirport(airport, null));


		airport.mCountryCode = "US";
		airport.mStateCode = "CA";
		airport.mCity = "Los Angeles";
		assertEquals("Los Angeles, CA" , StrUtils.formatAirport(airport, null));

		airport.mCountryCode = "ABCDE";
		assertEquals("Los Angeles, ABCDE" , StrUtils.formatAirport(airport, null));

		airport.mCountryCode = "";
		airport.mCity = "";
		airport.mAirportCode = "YVR";
		assertEquals("YVR" , StrUtils.formatAirport(airport, null));

	}

	@Test
	public void testCityNameFormatting() {
		String formattedString = StrUtils.formatCityName(getDummySuggestion());
		assertEquals("Chicago", formattedString);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
		MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS})
	public void testRoundOff() {
		float testNumber = 4.0481f;
		assertEquals("4", StrUtils.roundOff(testNumber, 0));
		assertEquals("4.0", StrUtils.roundOff(testNumber, 1));
		assertEquals("4.05", StrUtils.roundOff(testNumber, 2));
		assertEquals("4.048", StrUtils.roundOff(testNumber, 3));
	}

	public void testContentFormatting() {
		String content = "<p>A complimentary breakfast is offered.</p><p>eStone Villa Inn San Mateo is a smoke-free property.</p><ul><li>24-hour front desk</li></ul>";
		String formattedString = StrUtils.getFormattedContent(getContext(), content);
		assertEquals("A complimentary breakfast is offered.eStone Villa Inn San Mateo is a smoke-free property.<br/><br/>• 24-hour front desk<br/>", formattedString);
	}


	private SuggestionV4 getDummySuggestion() {
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.gaiaId = "";
		suggestion.regionNames = new SuggestionV4.RegionNames();
		suggestion.regionNames.displayName = "Chicago, IL";
		suggestion.regionNames.fullName = "Chicago (and vicinity), Illinois, United States of America";
		suggestion.regionNames.shortName = "Chicago (and vicinity)";
		suggestion.hierarchyInfo = new SuggestionV4.HierarchyInfo();
		suggestion.hierarchyInfo.airport = new SuggestionV4.Airport();
		suggestion.hierarchyInfo.airport.airportCode = "CHI";
		return suggestion;
	}

	@Test
	public void testFormatRoomString() {
		assertEquals("1 room", StrUtils.formatRoomString(getContext(), 1));
		assertEquals("3 rooms", StrUtils.formatRoomString(getContext(), 3));
	}

	@Test
	public void testFormatNightsString() {
		assertEquals("1 night", StrUtils.formatNightsString(getContext(), 1));
		assertEquals("3 nights", StrUtils.formatNightsString(getContext(), 3));
	}

	@Test
	public void testFormatLowerCaseGuestString() {
		assertEquals("1 guest", StrUtils.formatLowerCaseGuestString(getContext(), 1));
		assertEquals("3 guests", StrUtils.formatLowerCaseGuestString(getContext(), 3));
	}

	@Test
	public void getRedemptionLocationListTest() {
		List<ActivityDetailsResponse.LXLocation> redemptionLocations = new ArrayList<>();
		String redemptionLocationString;
		List<String> expectedRedemptionLocations = new ArrayList<>();
		List<String> obtainedRedemptionLocations;
		ActivityDetailsResponse.LXLocation redemptionLocation1 = new ActivityDetailsResponse.LXLocation();
		ActivityDetailsResponse.LXLocation redemptionLocation2 = new ActivityDetailsResponse.LXLocation();
		ActivityDetailsResponse.LXLocation redemptionLocation3 = new ActivityDetailsResponse.LXLocation();

		redemptionLocation1.addressName = "California Academy of Sciences";
		redemptionLocation1.street = "55 Music Concourse Dr";
		redemptionLocation1.city = "San Francisco";
		redemptionLocation1.province = "";
		redemptionLocation1.postalCode = "94118";
		redemptionLocations.add(redemptionLocation1);

		redemptionLocation2.addressName = "Aquarium of the Bay";
		redemptionLocation2.street = "2 Beach Street Pier 39";
		redemptionLocation2.city = "San Francisco";
		redemptionLocation2.province = "SFO";
		redemptionLocation2.postalCode = "94133";
		redemptionLocations.add(redemptionLocation2);

		redemptionLocation3.addressName = "Blue & Gold Fleet Bay Cruise";
		redemptionLocation3.street = "Pier 39";
		redemptionLocation3.city = "San Francisco";
		redemptionLocation3.province = "";
		redemptionLocation3.postalCode = "";
		redemptionLocations.add(redemptionLocation3);

		redemptionLocationString = "California Academy of Sciences, 55 Music Concourse Dr, San Francisco, 94118";
		expectedRedemptionLocations.add(redemptionLocationString);
		redemptionLocationString = "Aquarium of the Bay, 2 Beach Street Pier 39, San Francisco, SFO, 94133";
		expectedRedemptionLocations.add(redemptionLocationString);
		redemptionLocationString = "Blue & Gold Fleet Bay Cruise, Pier 39, San Francisco";
		expectedRedemptionLocations.add(redemptionLocationString);

		obtainedRedemptionLocations = StrUtils.getRedemptionLocationList(redemptionLocations);
		assertEquals(obtainedRedemptionLocations, expectedRedemptionLocations);
	}

	@Test
	public void getDisplayNameForGaiaNearbyTest() {
		//When airport name is null for eg: HOTELS LOB
		LocalizedName suggestionName = new LocalizedName(1043, "San Francisco, CA (SFO-San Francisco Intl.)",
			"San Francisco, CA, United States (SFO-San Francisco Intl.)", "San Francisco, CA", null);
		assertEquals("San Francisco, CA",
			StrUtils.getDisplayNameForGaiaNearby(suggestionName.getFriendlyName(), suggestionName.getAirportName()));

		//When airport name is not null for eg: FLIGHTS LOB
		suggestionName = new LocalizedName(1043, "Madrid",
			"Madrid (XTI-Chamartin Train Station)", "Madrid, Spain", "XTI-Chamartin Train Station");
		assertEquals("Madrid, Spain (XTI-Chamartin Train Station)",
			StrUtils.getDisplayNameForGaiaNearby(suggestionName.getFriendlyName(), suggestionName.getAirportName()));
	}

	@Test
	public void testBundleTotalWithTaxesString() {
		assertEquals("Trip total (with taxes & fee)", StrUtils.bundleTotalWithTaxesString(getContext()).toString());
	}

	@Test
	public void testFormatPackageCityName() {
		assertNull(StrUtils.formatPackageCityName(null));
		String formattedString = StrUtils.formatPackageCityName(getDummySuggestion().regionNames.shortName);
		assertEquals("Chicago", formattedString);
		formattedString = StrUtils.formatPackageCityName(getDummySuggestion().regionNames.fullName);
		assertEquals("Chicago", formattedString);
		formattedString = StrUtils.formatPackageCityName(getDummySuggestion().regionNames.displayName);
		assertEquals("Chicago", formattedString);
	}

	@Test
	public void testFormatStateName() {
		assertNull(StrUtils.formatStateName(null));
		String formattedString = StrUtils.formatStateName(getDummySuggestion().regionNames.shortName);
		assertEquals(null, formattedString);
		formattedString = StrUtils.formatStateName("Seattle, WA (SEA-Seattle - Tacoma Intl.)");
		assertEquals("WA", formattedString);

	}

	@Test
	public void testGetAppropriateTermsAndConditionsUrlWithTermsPresentForUS() {
		setPOS(PointOfSaleId.UNITED_STATES);
		String termsUrl = StrUtils.getAppropriateTermsAndConditionsUrl();
		assertEquals(PointOfSale.getPointOfSale().getTermsAndConditionsUrl(), termsUrl);
	}

	@Test
	public void testGetAppropriateTermsAndConditionsUrlWithTermsMissingForGermany() {
		// Ideally we'd just mock/override the termsAndConditions URLs rather than switching the POS here,
		// but that POS class isn't really set up to be mocked
		setPOS(PointOfSaleId.GERMANY);
		String termsUrl = StrUtils.getAppropriateTermsAndConditionsUrl();
		assertEquals(PointOfSale.getPointOfSale().getTermsOfBookingUrl(), termsUrl);
	}

	private void setPOS(PointOfSaleId posId) {
		SettingUtils.save(getContext(), R.string.PointOfSaleKey, Integer.toString(posId.getId()));
		PointOfSale.onPointOfSaleChanged(getContext());
	}
}
