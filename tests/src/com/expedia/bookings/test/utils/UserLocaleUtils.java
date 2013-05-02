package com.expedia.bookings.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import ErrorsAndExceptions.OutOfPOSException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;

public class UserLocaleUtils extends ActivityInstrumentationTestCase2<SearchActivity> {

	private Resources mRes;
	public static String TAG = "LocaleUtils";
	private String mCurrentLocaleString;

	public UserLocaleUtils(Resources res) {
		super("com.expedia.bookings", SearchActivity.class);
		mRes = res;
	}

	////////////////////////////////////////////////////////
	//// Lists of Locales
	public static Locale[] AMERICAN_LOCALES = new Locale[] {
			new Locale("es", "AR"),
			new Locale("pt", "BR"),
			new Locale("en", "CA"),
			new Locale("fr", "CA"),
			new Locale("es", "MX"),
			new Locale("en", "US"),
	};

	public static Locale[] APAC_LOCALES = new Locale[] {
			new Locale("en", "HK"),
			new Locale("zh", "HK"),
			new Locale("id", "ID"),
			new Locale("en", "IN"),
			new Locale("ja", "JP"),
			new Locale("ko", "KR"),
			new Locale("en", "MY"),
			new Locale("ms", "MY"),
			new Locale("en", "PH"),
			new Locale("en", "SG"),
			new Locale("th", "TH"),
			new Locale("en", "TW"),
			new Locale("zh", "TW"),
			new Locale("vi", "VN"),
			new Locale("tl", "PH"),
			new Locale("zh", "CN"),
	};

	public static Locale[] WESTERN_LOCALES = new Locale[] {
			new Locale("de", "AT"),
			new Locale("en", "AU"),
			new Locale("fr", "BE"),
			new Locale("nl", "BE"),
			new Locale("de", "DE"),
			new Locale("da", "DK"),
			new Locale("es", "ES"),
			new Locale("fr", "FR"),
			new Locale("en", "IE"),
			new Locale("it", "IT"),
			new Locale("nl", "NL"),
			new Locale("nb", "NO"),
			new Locale("en", "NZ"),
			new Locale("sv", "SE"),
			new Locale("en", "UK"),
	};

	public static Locale[] FLIGHTS_LOCALES = new Locale[] {
			AMERICAN_LOCALES[2], //en_CA
			AMERICAN_LOCALES[3], //fr_CA
			AMERICAN_LOCALES[5], //en_US
			WESTERN_LOCALES[9], //it_IT
			WESTERN_LOCALES[7], //fr_FR
			WESTERN_LOCALES[4], //de_DE
			WESTERN_LOCALES[14], //en_UK
			WESTERN_LOCALES[1], //en_AU
			WESTERN_LOCALES[12], //en_NZ
			WESTERN_LOCALES[11], //nb_NO
			WESTERN_LOCALES[5], //da_DK
			WESTERN_LOCALES[2], //fr_BE
			WESTERN_LOCALES[3], //nl_BE
			WESTERN_LOCALES[8], //en_IE
			APAC_LOCALES[4], //ja_JP
	};

	public static final Map<Locale, Integer> LOCALE_TO_COUNTRY = new HashMap<Locale, Integer>();
	static {
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[0], R.string.country_ar);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[1], R.string.country_br);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[2], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[3], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[4], R.string.country_mx);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[5], R.string.country_us);

		LOCALE_TO_COUNTRY.put(APAC_LOCALES[0], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[1], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[2], R.string.country_id);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[3], R.string.country_in);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[4], R.string.country_jp);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[5], R.string.country_kr);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[6], R.string.country_my);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[7], R.string.country_my);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[8], R.string.country_ph);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[9], R.string.country_sg);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[10], R.string.country_th);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[11], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[12], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[13], R.string.country_vn);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[14], R.string.country_ph);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[15], R.string.country_tw);

		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[0], R.string.country_at);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[1], R.string.country_au);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[2], R.string.country_be);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[3], R.string.country_be);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[4], R.string.country_de);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[5], R.string.country_dk);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[6], R.string.country_es);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[7], R.string.country_fr);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[8], R.string.country_ie);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[9], R.string.country_it);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[10], R.string.country_nl);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[11], R.string.country_no);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[12], R.string.country_nz);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[13], R.string.country_se);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[14], R.string.country_gb);
	}

	////////////////////////////////////////////////////////////////
	// Setting Locale

	//Set locale to top locale code in listName file
	//Remove that line from the list afterward
	public Locale selectNextLocaleFromInternalList(String listName)
			throws OutOfPOSException {

		File fileIn = new File(listName);
		File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tempFile");
		PrintWriter fileWriter = null;
		BufferedReader listReader = null;

		Locale newLocale;

		try {

			fileWriter = new PrintWriter(tempFile);
			listReader = new BufferedReader(new FileReader(fileIn));

			//Each line from file is a POS code
			String localeCode = listReader.readLine();

			// Get substrings for new locale, instantiate new locale, set new locale
			// If there are no more POSs listed in the file
			// throw on OutOfPOSException

			try {
				mCurrentLocaleString = localeCode;
				newLocale = new Locale(localeCode.substring(0, 2), localeCode.substring(3, 5));
			}
			catch (NullPointerException e) {
				Log.e(TAG, "Out of locales. Throwing OutOfPOSException");
				throw new OutOfPOSException();
			}
			catch(StringIndexOutOfBoundsException s) {
				Log.e(TAG, "Out of locales. Throwing OutOfPOSException");
				throw new OutOfPOSException();
			}
			setLocale(newLocale);

			//write the rest of the existing file to a temporary file

			String nextLine = listReader.readLine();
			while (nextLine != null) {
				Log.d(TAG, "Writing POS to temp file: " + nextLine);
				fileWriter.write(nextLine + '\n');
				nextLine = listReader.readLine();
			}

			//set old file to be the new file
			//which has one less locale listed
			fileIn.delete();
			tempFile.renameTo(fileIn);

			return newLocale;
		}
		catch(OutOfPOSException o) {
			throw o;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			fileWriter.close();

			try {
				listReader.close();
			}
			catch (IOException e) {
				Log.e(TAG, "Failed closing listReader", e);
			}
		}
	}

	// If a test fails for errors/exceptions, 
	// we do not want to have skipped any POS
	// This method is for adding the POS back to the list
	public void appendCurrentLocaleBackOnToList(String listName) {
		File fileIn = new File(listName);
		File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tempFile");
		PrintWriter fileWriter = null;
		BufferedReader listReader = null;

		try {
			listReader = new BufferedReader(new FileReader(fileIn));
			fileWriter = new PrintWriter(tempFile);

			String nextLine = listReader.readLine();

			while (nextLine != null) {
				Log.d(TAG, "Writing POS to temp file: " + nextLine);
				fileWriter.write(nextLine + '\n');
				nextLine = listReader.readLine();
			}

			fileWriter.println(mCurrentLocaleString);
			fileIn.delete();
			tempFile.renameTo(fileIn);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			fileWriter.close();
			try {
				listReader.close();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	// Select a random locale from the array of locales given
	public Locale setRandomLocale(Locale[] localeList) {
		Random rand = new Random();
		int localeIndex = rand.nextInt(localeList.length);
		Locale selectedLocale = localeList[localeIndex];
		setLocale(selectedLocale);

		return selectedLocale;
	}

	public void setLocale(Locale locale) {
		Configuration config = mRes.getConfiguration(); //get current configuration
		config.locale = locale; //set to locale specified
		Locale.setDefault(locale);
		mRes.updateConfiguration(config, mRes.getDisplayMetrics());
	}

}
