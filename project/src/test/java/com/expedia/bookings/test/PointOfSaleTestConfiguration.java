package com.expedia.bookings.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.Context;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleConfigHelper;
import com.expedia.bookings.data.pos.PointOfSaleId;

public class PointOfSaleTestConfiguration {
	private static final String ESS_LOCALES_JSON = "[\"en_US\"]";
	private static final String OPTIONAL_POSTCODE_COUNTRY_JSON = "[\"HKG\"]";

	/**
	 * Configures the PointOfSale using the specified file in phone UI mode.
	 *
	 * @param context Robolectric context
	 * @param posConfigFilename filename of JSON file to load configuration from
	 */
	public static void configurePointOfSale(Context context, String posConfigFilename) {
		configurePointOfSale(context, posConfigFilename, false);
	}

	/**
	 * Configures the PointOfSale using the specified file.
	 *
	 * @param context Robolectric context
	 * @param posConfigFilename filename of JSON file to load configuration from
	 * @param isTablet true if in tablet UI mode, false if in phone UI mode
	 */
	public static void configurePointOfSale(Context context, String posConfigFilename, boolean isTablet) {
		configurePOS(context, posConfigFilename, Integer.toString(PointOfSaleId.UNITED_STATES.getId()), isTablet);
	}

	public static void configurePOS(Context context, String posConfigFilename, String posKey, boolean isTablet) {
		PointOfSaleConfigHelper mockConfigHelper = Mockito.mock(PointOfSaleConfigHelper.class);

		Mockito.doAnswer(createInputStreamAnswerForString(ESS_LOCALES_JSON))
			.when(mockConfigHelper).openExpediaSuggestSupportedLocalesConfig();
		Mockito.doAnswer(createInputStreamAnswerForString(OPTIONAL_POSTCODE_COUNTRY_JSON))
			.when(mockConfigHelper).openPaymentPostalCodeOptionalCountriesConfiguration();
		Mockito.doAnswer(createInputStreamAnswerForAssetFile(context, posConfigFilename))
			.when(mockConfigHelper).openPointOfSaleConfiguration();

		PointOfSale.init(mockConfigHelper, posKey, isTablet);
	}


	private static Answer<InputStream> createInputStreamAnswerForString(final String str) {
		return new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return new ByteArrayInputStream(str.getBytes());
			}
		};
	}

	private static Answer<InputStream> createInputStreamAnswerForAssetFile(final Context context, final String filename) {
		return new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return context.getAssets().open(filename);
			}
		};
	}


}
