package com.expedia.bookings.activity;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.FileCipher;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.validation.PatternValidator.EmailValidator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.TextViewErrorHandler;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.ValidationProcessor;
import com.mobiata.android.validation.Validator;
import com.mobiata.hotellib.Params;
import com.mobiata.hotellib.data.BillingInfo;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Location;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Policy;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.RateBreakdown;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.utils.JSONUtils;
import com.mobiata.hotellib.utils.StrUtils;

public class BookingInfoActivity extends Activity {

	private static final String SAVED_INFO_FILENAME = "booking.dat";

	// Kind of pointless when this is just stored as a static field, but at least protects
	// against someone getting the plaintext file but not the app itself.
	private static final String PASSWORD = "7eGeDr4jaD6jut9aha3hAyupAC6ZE9a";

	private SearchParams mSearchParams;
	private Property mProperty;
	private Rate mRate;

	private BillingInfo mBillingInfo;

	private LayoutInflater mInflater;

	private boolean mFormHasBeenFocused;

	// Cached data from arrays
	private String[] mStateCodes;
	private String[] mCountryCodes;

	// Cached views
	private EditText mFirstNameEditText;
	private EditText mLastNameEditText;
	private EditText mTelephoneEditText;
	private EditText mEmailEditText;
	private EditText mAddress1EditText;
	private EditText mAddress2EditText;
	private EditText mCityEditText;
	private EditText mPostalCodeEditText;
	private Spinner mStateSpinner;
	private EditText mStateEditText;
	private Spinner mCountrySpinner;
	private EditText mCardNumberEditText;
	private EditText mExpirationMonthEditText;
	private EditText mExpirationYearEditText;
	private EditText mSecurityCodeEditText;
	private Button mConfirmationButton;

	private TextView mSecurityCodeTipTextView;
	private TextView mChargeDetailsTextView;

	// Validation
	private static final int ERROR_INVALID_CARD_NUMBER = 101;
	private static final int ERROR_INVALID_MONTH = 102;
	private static final int ERROR_EXPIRED_YEAR = 103;
	private static final int ERROR_SHORT_SECURITY_CODE = 104;
	private ValidationProcessor mValidationProcessor;
	private TextViewErrorHandler mErrorHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mInflater = getLayoutInflater();
		mValidationProcessor = new ValidationProcessor();

		setContentView(R.layout.activity_booking_info);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);
		mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent,
				Codes.SEARCH_PARAMS,
				SearchParams.class);
		mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);

		// Retrieve some data we keep using
		Resources r = getResources();
		mStateCodes = r.getStringArray(R.array.state_codes);
		mCountryCodes = r.getStringArray(R.array.country_codes);

		// Retrieve views that we need for the form fields
		mFirstNameEditText = (EditText) findViewById(R.id.first_name_edit_text);
		mLastNameEditText = (EditText) findViewById(R.id.last_name_edit_text);
		mTelephoneEditText = (EditText) findViewById(R.id.telephone_edit_text);
		mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
		mAddress1EditText = (EditText) findViewById(R.id.address1_edit_text);
		mAddress2EditText = (EditText) findViewById(R.id.address2_edit_text);
		mCityEditText = (EditText) findViewById(R.id.city_edit_text);
		mPostalCodeEditText = (EditText) findViewById(R.id.postal_code_edit_text);
		mStateSpinner = (Spinner) findViewById(R.id.state_spinner);
		mStateEditText = (EditText) findViewById(R.id.state_edit_text);
		mCountrySpinner = (Spinner) findViewById(R.id.country_spinner);
		mCardNumberEditText = (EditText) findViewById(R.id.card_number_edit_text);
		mExpirationMonthEditText = (EditText) findViewById(R.id.expiration_month_edit_text);
		mExpirationYearEditText = (EditText) findViewById(R.id.expiration_year_edit_text);
		mSecurityCodeEditText = (EditText) findViewById(R.id.security_code_edit_text);
		mConfirmationButton = (Button) findViewById(R.id.confirm_book_button);

		// Other cached views
		mSecurityCodeTipTextView = (TextView) findViewById(R.id.security_code_tip_text_view);
		mChargeDetailsTextView = (TextView) findViewById(R.id.charge_details_text_view);

		// Configure the layout
		configureTicket();
		configureForm();
		configureFooter();

		// Retrieve previous instance
		Instance lastInstance = (Instance) getLastNonConfigurationInstance();
		if (lastInstance != null) {
			this.mBillingInfo = lastInstance.mBillingInfo;
			this.mFormHasBeenFocused = lastInstance.mFormHasBeenFocused;

			if (this.mFormHasBeenFocused) {
				onFormFieldFocus();
			}

			syncFormFields();
		}
		else {
			// Try loading saved billing info
			if (loadSavedBillingInfo()) {
				syncFormFields();
			}
			else {
				mBillingInfo = new BillingInfo();
			}

			mFormHasBeenFocused = false;
		}
	}

	private class Instance {
		public BillingInfo mBillingInfo;
		public boolean mFormHasBeenFocused;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		syncBillingInfo();

		Instance instance = new Instance();
		instance.mBillingInfo = this.mBillingInfo;
		instance.mFormHasBeenFocused = this.mFormHasBeenFocused;
		return instance;
	}

	/**
	 * Syncs the local BillingInfo with data from the form fields.  Should be used before you want to access
	 * the local BillingInfo's data.
	 */
	private void syncBillingInfo() {
		// Start off with a clean slate
		mBillingInfo = new BillingInfo();

		mBillingInfo.setFirstName(mFirstNameEditText.getText().toString());
		mBillingInfo.setLastName(mLastNameEditText.getText().toString());
		mBillingInfo.setTelephone(mTelephoneEditText.getText().toString());
		mBillingInfo.setEmail(mEmailEditText.getText().toString());

		Location location = new Location();
		List<String> streetAddress = new ArrayList<String>();
		streetAddress.add(mAddress1EditText.getText().toString());
		String address2 = mAddress2EditText.getText().toString();
		if (address2 != null && address2.length() > 0) {
			streetAddress.add(address2);
		}
		location.setStreetAddress(streetAddress);
		location.setCity(mCityEditText.getText().toString());
		location.setPostalCode(mPostalCodeEditText.getText().toString());
		if (useStateSpinner()) {
			location.setStateCode(mStateCodes[mStateSpinner.getSelectedItemPosition()]);
		}
		else {
			location.setStateCode(mStateEditText.getText().toString());
		}
		location.setCountryCode(mCountryCodes[mCountrySpinner.getSelectedItemPosition()]);
		mBillingInfo.setLocation(location);

		mBillingInfo.setNumber(mCardNumberEditText.getText().toString());
		String expirationMonth = mExpirationMonthEditText.getText().toString();
		String expirationYear = mExpirationYearEditText.getText().toString();
		if (expirationMonth != null && expirationMonth.length() > 0 && expirationYear != null
				&& expirationYear.length() > 0) {
			Calendar cal = new GregorianCalendar(Integer.parseInt(expirationYear),
					Integer.parseInt(expirationMonth) - 1, 15);
			mBillingInfo.setExpirationDate(cal);
		}
		mBillingInfo.setSecurityCode(mSecurityCodeEditText.getText().toString());
	}

	/**
	 * Syncs the form fields with data from local BillingInfo.  Should be used when creating or
	 * restoring the Activity.
	 */
	private void syncFormFields() {
		mFirstNameEditText.setText(mBillingInfo.getFirstName());
		mLastNameEditText.setText(mBillingInfo.getLastName());
		mTelephoneEditText.setText(mBillingInfo.getTelephone());
		mEmailEditText.setText(mBillingInfo.getEmail());

		Location loc = mBillingInfo.getLocation();
		mAddress1EditText.setText(loc.getStreetAddress().get(0));
		if (loc.getStreetAddress().size() > 1) {
			mAddress2EditText.setText(loc.getStreetAddress().get(1));
		}
		mCityEditText.setText(loc.getCity());
		mPostalCodeEditText.setText(loc.getPostalCode());
		setSpinnerSelection(mCountrySpinner, mCountryCodes, loc.getCountryCode());
		if (useStateSpinner()) {
			setSpinnerSelection(mStateSpinner, mStateCodes, loc.getStateCode());
		}
		else {
			mStateEditText.setText(loc.getStateCode());
		}

		mCardNumberEditText.setText(mBillingInfo.getNumber());
		Calendar cal = mBillingInfo.getExpirationDate();
		if (cal != null) {
			mExpirationMonthEditText.setText((mBillingInfo.getExpirationDate().get(Calendar.MONTH) + 1) + "");
			mExpirationYearEditText.setText((mBillingInfo.getExpirationDate().get(Calendar.YEAR) % 100) + "");
		}
		mSecurityCodeEditText.setText(mBillingInfo.getSecurityCode());
	}

	public boolean useStateSpinner() {
		String countryCode = mCountryCodes[mCountrySpinner.getSelectedItemPosition()];
		return countryCode.equals(getString(R.string.country_code_us))
				|| countryCode.equals(getString(R.string.country_code_ca));
	}

	private void configureTicket() {
		// Configure the booking summary at the top of the page
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (mProperty.getThumbnail() != null) {
			ImageCache.getInstance().loadImage(mProperty.getThumbnail().getUrl(), thumbnailView);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(mProperty.getName());

		Location location = mProperty.getLocation();
		TextView address1View = (TextView) findViewById(R.id.address1_text_view);
		address1View.setText(StrUtils.formatAddressStreet(location));
		TextView address2View = (TextView) findViewById(R.id.address2_text_view);
		address2View.setText(StrUtils.formatAddressCity(location));

		// Configure the details
		ViewGroup detailsLayout = (ViewGroup) findViewById(R.id.details_layout);
		addDetail(detailsLayout, R.string.room_type, mRate.getRoomDescription());

		addDetail(detailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(this, mSearchParams));

		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(this);
		String start = medDf.format(mSearchParams.getCheckInDate().getTime());
		String end = medDf.format(mSearchParams.getCheckOutDate().getTime());
		int numDays = (int) Math.round((mSearchParams.getCheckOutDate().getTimeInMillis() - mSearchParams
				.getCheckInDate().getTimeInMillis()) / (1000 * 60 * 60 * 24));
		String numNights = (numDays == 1) ? getString(R.string.stay_duration_one_night) : getString(
				R.string.stay_duration_template, numDays);
		addDetail(detailsLayout, R.string.CheckIn, start);
		addDetail(detailsLayout, R.string.CheckOut, end + "\n" + numNights);

		// If there's a breakdown list, show that; otherwise, show the nightly mRate
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		if (mRate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : mRate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dateFormat.format(date);
				addDetail(detailsLayout, getString(R.string.room_rate_template, dateStr), breakdown.getAmount()
						.getFormattedMoney());
			}
		}
		else if (mRate.getDailyAmountBeforeTax() != null) {
			addDetail(detailsLayout, R.string.RatePerRoomPerNight, mRate.getDailyAmountBeforeTax().getFormattedMoney());
		}

		Money taxesAndFeesPerRoom = mRate.getTaxesAndFeesPerRoom();
		if (taxesAndFeesPerRoom != null && taxesAndFeesPerRoom.getFormattedMoney() != null
				&& taxesAndFeesPerRoom.getFormattedMoney().length() > 0) {
			addDetail(detailsLayout, R.string.TaxesAndFees, taxesAndFeesPerRoom.getFormattedMoney());
		}

		// Configure the total cost
		Money totalAmountAfterTax = mRate.getTotalAmountAfterTax();
		TextView totalView = (TextView) findViewById(R.id.total_cost_text_view);
		if (totalAmountAfterTax != null && totalAmountAfterTax.getFormattedMoney() != null
				&& totalAmountAfterTax.getFormattedMoney().length() > 0) {
			totalView.setText(totalAmountAfterTax.getFormattedMoney());
		}
		else {
			totalView.setText("Dan didn't account for no total info, tell him");
		}
	}

	private void addDetail(ViewGroup parent, int labelStrId, String value) {
		addDetail(parent, getString(labelStrId), value);
	}

	private void addDetail(ViewGroup parent, String label, String value) {
		View detailRow = mInflater.inflate(R.layout.snippet_booking_detail, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		parent.addView(detailRow);
	}

	private void configureForm() {
		// Setup automatic filling of state/country information based on city entered.
		// Works for some popular cities.
		mCityEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			@Override
			public void afterTextChanged(Editable s) {
				String key = s.toString().toLowerCase();
				if (COMMON_US_CITIES.containsKey(key)) {
					setSpinnerSelection(mStateSpinner, getString(COMMON_US_CITIES.get(key)));
					setSpinnerSelection(mCountrySpinner, getString(R.string.country_us));
				}
			}
		});

		// Set the default country as USA
		setSpinnerSelection(mCountrySpinner, getString(R.string.country_us));

		mCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (useStateSpinner()) {
					mStateSpinner.setVisibility(View.VISIBLE);
					mStateEditText.setVisibility(View.GONE);
				}
				else {
					mStateSpinner.setVisibility(View.GONE);
					mStateEditText.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Should not happen, do nothing
			}
		});

		// Configure form validation
		// Setup validators and error handlers
		TextViewValidator requiredFieldValidator = new TextViewValidator();
		TextViewErrorHandler errorHandler = mErrorHandler = new TextViewErrorHandler(getString(R.string.required_field));
		errorHandler.addResponse(ValidationError.ERROR_DATA_INVALID, getString(R.string.invalid_field));
		errorHandler.addResponse(ERROR_INVALID_CARD_NUMBER, getString(R.string.invalid_card_number));
		errorHandler.addResponse(ERROR_INVALID_MONTH, getString(R.string.invalid_month));
		errorHandler.addResponse(ERROR_EXPIRED_YEAR, getString(R.string.invalid_expiration_year));
		errorHandler.addResponse(ERROR_SHORT_SECURITY_CODE, getString(R.string.invalid_security_code));

		// Add all the validators
		mValidationProcessor.add(mFirstNameEditText, requiredFieldValidator);
		mValidationProcessor.add(mLastNameEditText, requiredFieldValidator);
		mValidationProcessor.add(mTelephoneEditText, new TextViewValidator(new TelephoneValidator()));
		mValidationProcessor.add(mEmailEditText, new TextViewValidator(new EmailValidator()));
		mValidationProcessor.add(mAddress1EditText, requiredFieldValidator);
		mValidationProcessor.add(mCityEditText, requiredFieldValidator);
		mValidationProcessor.add(mCardNumberEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence number) {
				return (!FormatUtils.isValidCreditCardNumber(number)) ? ERROR_INVALID_CARD_NUMBER : 0;
			}
		}));
		mValidationProcessor.add(mExpirationMonthEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence obj) {
				int month = Integer.parseInt(obj.toString());
				return (month < 1 || month > 12) ? ERROR_INVALID_MONTH : 0;
			}
		}));
		mValidationProcessor.add(mExpirationYearEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence obj) {
				int thisYear = Calendar.getInstance().get(Calendar.YEAR);
				int year = Integer.parseInt(obj.toString());
				return (thisYear % 100 > year) ? ERROR_EXPIRED_YEAR : 0;
			}
		}));
		mValidationProcessor.add(mSecurityCodeEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence obj) {
				return (obj.length() < 3) ? ERROR_SHORT_SECURITY_CODE : 0;
			}
		}));

		// Configure the bottom of the page form stuff
		mConfirmationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bookProperty();
			}
		});

		// Setup a focus change listener that changes the bottom from "enter booking info"
		// to "confirm & book", plus the text
		OnFocusChangeListener l = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					onFormFieldFocus();
				}
				else {
					saveBillingInfo();
				}
			}
		};

		mFirstNameEditText.setOnFocusChangeListener(l);
		mLastNameEditText.setOnFocusChangeListener(l);
		mTelephoneEditText.setOnFocusChangeListener(l);
		mEmailEditText.setOnFocusChangeListener(l);
		mAddress1EditText.setOnFocusChangeListener(l);
		mAddress2EditText.setOnFocusChangeListener(l);
		mCityEditText.setOnFocusChangeListener(l);
		mPostalCodeEditText.setOnFocusChangeListener(l);
		mStateSpinner.setOnFocusChangeListener(l);
		mStateEditText.setOnFocusChangeListener(l);
		mCountrySpinner.setOnFocusChangeListener(l);
		mCardNumberEditText.setOnFocusChangeListener(l);
		mExpirationMonthEditText.setOnFocusChangeListener(l);
		mExpirationYearEditText.setOnFocusChangeListener(l);
		mSecurityCodeEditText.setOnFocusChangeListener(l);
		mConfirmationButton.setOnFocusChangeListener(l);
	}

	private void configureFooter() {
		// Configure the cancellation policy
		TextView cancellationPolicyView = (TextView) findViewById(R.id.cancellation_policy_text_view);
		boolean foundCancellationPolicy = false;
		for (Policy policy : mRate.getRateRules().getPolicies()) {
			if (policy.getType() == Policy.TYPE_CANCEL) {
				foundCancellationPolicy = true;
				cancellationPolicyView.setText(policy.getDescription());
			}
		}
		if (!foundCancellationPolicy) {
			cancellationPolicyView.setVisibility(View.GONE);
		}
	}

	private void setSpinnerSelection(Spinner spinner, String target) {
		spinner.setSelection(findAdapterIndex(spinner.getAdapter(), target));
	}

	private int findAdapterIndex(SpinnerAdapter adapter, String target) {
		int numItems = adapter.getCount();
		for (int n = 0; n < numItems; n++) {
			String name = (String) adapter.getItem(n);
			if (name.equalsIgnoreCase(target)) {
				return n;
			}
		}
		return -1;
	}

	private void setSpinnerSelection(Spinner spinner, String[] codes, String targetCode) {
		for (int n = 0; n < codes.length; n++) {
			if (targetCode.equals(codes[n])) {
				spinner.setSelection(n);
				return;
			}
		}
	}

	private void onFormFieldFocus() {
		mFormHasBeenFocused = true;

		// Change the button text
		mConfirmationButton.setText(R.string.confirm_book);

		// Add the charge details text
		mChargeDetailsTextView.setText(getString(R.string.charge_details_template, mRate.getTotalAmountAfterTax()
				.getFormattedMoney()));
	}

	private boolean saveBillingInfo() {
		if (Params.isLoggingEnabled()) {
			Log.d(Params.getLoggingTag(), "Saving user's billing info.");
		}

		// Initialize a cipher
		FileCipher fileCipher = new FileCipher(PASSWORD);

		if (!fileCipher.isInitialized()) {
			return false;
		}

		// Gather all the data to be saved
		syncBillingInfo();

		JSONObject data = mBillingInfo.toJson();

		// Remove sensitive data
		data.remove("brandName");
		data.remove("brandCode");
		data.remove("number");
		data.remove("securityCode");

		return fileCipher.saveSecureData(getFileStreamPath(SAVED_INFO_FILENAME), data.toString());
	}

	private boolean loadSavedBillingInfo() {
		if (Params.isLoggingEnabled()) {
			Log.d(Params.getLoggingTag(), "Loading saved billing info.");
		}

		// Check that the saved billing info file exists
		File f = getFileStreamPath(SAVED_INFO_FILENAME);
		if (!f.exists()) {
			return false;
		}

		// Initialize a cipher
		FileCipher fileCipher = new FileCipher(PASSWORD);
		if (!fileCipher.isInitialized()) {
			return false;
		}

		String results = fileCipher.loadSecureData(f);
		if (results == null || results.length() == 0) {
			return false;
		}

		try {
			JSONObject obj = new JSONObject(results);
			mBillingInfo = new BillingInfo();
			mBillingInfo.fromJson(obj);
			return true;
		}
		catch (JSONException e) {
			if (Params.isLoggingEnabled()) {
				Log.e(Params.getLoggingTag(), "Could not restore saved billing info.", e);
			}
			return false;
		}
	}

	private void bookProperty() {
		boolean valid = mValidationProcessor.validate(mErrorHandler);

		// TODO: Handle invalid and valid responses
	}

	// Static data that auto-fills states/countries
	@SuppressWarnings("serial")
	public static final HashMap<CharSequence, Integer> COMMON_US_CITIES = new HashMap<CharSequence, Integer>() {
		{
			put("new york", R.string.state_new_york);
			put("los angeles", R.string.state_california);
			put("chicago", R.string.state_illinois);
			put("houston", R.string.state_texas);
			put("philadelphia", R.string.state_pennsylvania);
			put("phoenix", R.string.state_arizona);
			put("san antonio", R.string.state_texas);
			put("san diego", R.string.state_california);
			put("dallas", R.string.state_texas);
			put("san jose", R.string.state_california);
			put("jacksonville", R.string.state_florida);
			put("indianapolis", R.string.state_indiana);
			put("san francisco", R.string.state_california);
			put("austin", R.string.state_texas);
			put("columbus", R.string.state_ohio);
			put("fort worth", R.string.state_texas);
			put("charlotte", R.string.state_north_carolina);
			put("detroit", R.string.state_michigan);
			put("el paso", R.string.state_texas);
			put("memphis", R.string.state_tennessee);
			put("baltimore", R.string.state_maryland);
			put("boston", R.string.state_massachusetts);
			put("seattle", R.string.state_washington);
			put("washington", R.string.state_district_of_columbia);
			put("nashville", R.string.state_tennessee);
			put("denver", R.string.state_colorado);
			put("louisville", R.string.state_kentucky);
			put("milwaukee", R.string.state_wisconsin);
			put("portland", R.string.state_oregon);
			put("las vegas", R.string.state_nevada);
			put("oklahoma city", R.string.state_oklahoma);
			put("albuquerque", R.string.state_new_mexico);
			put("tucson", R.string.state_arizona);
			put("fresno", R.string.state_california);
			put("sacramento", R.string.state_california);
			put("long beach", R.string.state_california);
			put("kansas city", R.string.state_missouri);
			put("mesa", R.string.state_arizona);
			put("virginia beach", R.string.state_virginia);
			put("atlanta", R.string.state_georgia);
			put("colorado springs", R.string.state_colorado);
			put("omaha", R.string.state_nebraska);
			put("raleigh", R.string.state_north_carolina);
			put("miami", R.string.state_florida);
			put("cleveland", R.string.state_ohio);
			put("tulsa", R.string.state_oklahoma);
			put("oakland", R.string.state_california);
			put("minneapolis", R.string.state_minnesota);
			put("wichita", R.string.state_kansas);
			put("arlington", R.string.state_texas);
			put("bakersfield", R.string.state_california);
			put("new orleans", R.string.state_louisiana);
			put("honolulu", R.string.state_hawaii);
			put("anaheim", R.string.state_california);
			put("tampa", R.string.state_florida);
			put("aurora", R.string.state_colorado);
			put("santa ana", R.string.state_california);
			put("st louis", R.string.state_missouri);
			put("pittsburgh", R.string.state_pennsylvania);
			put("corpus christi", R.string.state_texas);
			put("riverside", R.string.state_california);
			put("cincinnati", R.string.state_ohio);
			put("lexington", R.string.state_kentucky);
			put("anchorage", R.string.state_alaska);
			put("stockton", R.string.state_california);
			put("toledo", R.string.state_ohio);
			put("st paul", R.string.state_minnesota);
			put("newark", R.string.state_new_jersey);
			put("greensboro", R.string.state_north_carolina);
			put("buffalo", R.string.state_new_york);
			put("plano", R.string.state_texas);
			put("lincoln", R.string.state_nebraska);
			put("henderson", R.string.state_nevada);
			put("fort wayne", R.string.state_indiana);
			put("jersey city", R.string.state_new_jersey);
			put("st petersburg", R.string.state_florida);
			put("chula vista", R.string.state_california);
			put("norfolk", R.string.state_virginia);
			put("orlando", R.string.state_florida);
			put("chandler", R.string.state_arizona);
			put("laredo", R.string.state_texas);
			put("madison", R.string.state_wisconsin);
			put("winston-salem", R.string.state_north_carolina);
			put("lubbock", R.string.state_texas);
			put("baton rouge", R.string.state_louisiana);
			put("durham", R.string.state_north_carolina);
			put("garland", R.string.state_texas);
			put("glendale", R.string.state_arizona);
			put("reno", R.string.state_nevada);
			put("hialeah", R.string.state_florida);
			put("paradise", R.string.state_nevada);
			put("chesapeake", R.string.state_virginia);
			put("scottsdale", R.string.state_arizona);
			put("north las vegas", R.string.state_nevada);
			put("irving", R.string.state_texas);
			put("fremont", R.string.state_california);
			put("irvine", R.string.state_california);
			put("birmingham", R.string.state_alabama);
			put("rochester", R.string.state_new_york);
			put("san bernardino", R.string.state_california);
			put("spokane", R.string.state_washington);
			put("gilbert", R.string.state_arizona);
			put("arlington", R.string.state_virginia);
			put("montgomery", R.string.state_alabama);
			put("boise", R.string.state_idaho);
			put("richmond", R.string.state_virginia);
			put("des moines", R.string.state_iowa);
			put("modesto", R.string.state_california);
			put("fayetteville", R.string.state_north_carolina);
			put("shreveport", R.string.state_louisiana);
			put("akron", R.string.state_ohio);
			put("tacoma", R.string.state_washington);
			put("aurora", R.string.state_illinois);
			put("oxnard", R.string.state_california);
			put("fontana", R.string.state_california);
			put("yonkers", R.string.state_new_york);
			put("augusta", R.string.state_georgia);
			put("mobile", R.string.state_alabama);
			put("little rock", R.string.state_arkansas);
			put("moreno valley", R.string.state_california);
			put("glendale", R.string.state_california);
			put("amarillo", R.string.state_texas);
			put("huntington beach", R.string.state_california);
			put("columbus", R.string.state_georgia);
			put("grand rapids", R.string.state_michigan);
			put("salt lake city", R.string.state_utah);
			put("tallahassee", R.string.state_florida);
			put("worcester", R.string.state_massachusetts);
			put("newport news", R.string.state_virginia);
			put("huntsville", R.string.state_alabama);
			put("knoxville", R.string.state_tennessee);
			put("providence", R.string.state_rhode_island);
			put("santa clarita", R.string.state_california);
			put("grand prairie", R.string.state_texas);
			put("brownsville", R.string.state_texas);
			put("jackson", R.string.state_mississippi);
			put("overland park", R.string.state_kansas);
			put("garden grove", R.string.state_california);
			put("santa rosa", R.string.state_california);
			put("chattanooga", R.string.state_tennessee);
			put("oceanside", R.string.state_california);
			put("fort lauderdale", R.string.state_florida);
			put("rancho cucamonga", R.string.state_california);
			put("port st. lucie", R.string.state_florida);
			put("ontario", R.string.state_california);
			put("vancouver", R.string.state_washington);
			put("tempe", R.string.state_arizona);
			put("springfield", R.string.state_missouri);
			put("lancaster", R.string.state_california);
			put("eugene", R.string.state_oregon);
			put("pembroke pines", R.string.state_florida);
			put("salem", R.string.state_oregon);
			put("cape coral", R.string.state_florida);
			put("peoria", R.string.state_arizona);
			put("sioux falls", R.string.state_south_dakota);
			put("springfield", R.string.state_massachusetts);
			put("elk grove", R.string.state_california);
			put("rockford", R.string.state_illinois);
			put("palmdale", R.string.state_california);
			put("corona", R.string.state_california);
			put("salinas", R.string.state_california);
			put("pomona", R.string.state_california);
			put("pasadena", R.string.state_texas);
			put("joliet", R.string.state_illinois);
			put("paterson", R.string.state_new_jersey);
			put("kansas city", R.string.state_kansas);
			put("torrance", R.string.state_california);
			put("syracuse", R.string.state_new_york);
			put("bridgeport", R.string.state_connecticut);
			put("hayward", R.string.state_california);
			put("fort collins", R.string.state_colorado);
			put("escondido", R.string.state_california);
			put("lakewood", R.string.state_colorado);
			put("naperville", R.string.state_illinois);
			put("dayton", R.string.state_ohio);
			put("hollywood", R.string.state_florida);
			put("sunnyvale", R.string.state_california);
			put("alexandria", R.string.state_virginia);
			put("mesquite", R.string.state_texas);
			put("hampton", R.string.state_virginia);
			put("pasadena", R.string.state_california);
			put("orange", R.string.state_california);
			put("savannah", R.string.state_georgia);
			put("cary", R.string.state_north_carolina);
			put("fullerton", R.string.state_california);
			put("warren", R.string.state_michigan);
			put("clarksville", R.string.state_tennessee);
			put("mckinney", R.string.state_texas);
			put("mcallen", R.string.state_texas);
			put("new haven", R.string.state_connecticut);
			put("sterling heights", R.string.state_michigan);
			put("west valley city", R.string.state_utah);
			put("columbia", R.string.state_south_carolina);
			put("killeen", R.string.state_texas);
			put("topeka", R.string.state_kansas);
			put("thousand oaks", R.string.state_california);
			put("cedar rapids", R.string.state_iowa);
			put("olathe", R.string.state_kansas);
			put("elizabeth", R.string.state_new_jersey);
			put("waco", R.string.state_texas);
			put("hartford", R.string.state_connecticut);
			put("visalia", R.string.state_california);
			put("gainesville", R.string.state_florida);
			put("simi valley", R.string.state_california);
			put("stamford", R.string.state_connecticut);
			put("bellevue", R.string.state_washington);
			put("concord", R.string.state_california);
			put("miramar", R.string.state_florida);
			put("coral springs", R.string.state_florida);
			put("lafayette", R.string.state_louisiana);
			put("charleston", R.string.state_south_carolina);
			put("carrollton", R.string.state_texas);
			put("roseville", R.string.state_california);
			put("thornton", R.string.state_colorado);
			put("beaumont", R.string.state_texas);
			put("allentown", R.string.state_pennsylvania);
			put("surprise", R.string.state_arizona);
			put("evansville", R.string.state_indiana);
			put("abilene", R.string.state_texas);
			put("frisco", R.string.state_texas);
			put("independence", R.string.state_missouri);
			put("santa clara", R.string.state_california);
			put("springfield", R.string.state_illinois);
			put("vallejo", R.string.state_california);
			put("victorville", R.string.state_california);
			put("athens", R.string.state_georgia);
			put("peoria", R.string.state_illinois);
			put("lansing", R.string.state_michigan);
			put("ann arbor", R.string.state_michigan);
			put("el monte", R.string.state_california);
			put("denton", R.string.state_texas);
			put("berkeley", R.string.state_california);
			put("provo", R.string.state_utah);
			put("downey", R.string.state_california);
			put("midland", R.string.state_texas);
			put("norman", R.string.state_oklahoma);
			put("waterbury", R.string.state_connecticut);
			put("costa mesa", R.string.state_california);
			put("inglewood", R.string.state_california);
			put("manchester", R.string.state_new_hampshire);
			put("murfreesboro", R.string.state_tennessee);
			put("columbia", R.string.state_missouri);
			put("elgin", R.string.state_illinois);
			put("clearwater", R.string.state_florida);
			put("miami gardens", R.string.state_florida);
			put("rochester", R.string.state_minnesota);
			put("pueblo", R.string.state_colorado);
			put("lowell", R.string.state_massachusetts);
			put("wilmington", R.string.state_north_carolina);
			put("arvada", R.string.state_colorado);
			put("ventura", R.string.state_california);
			put("westminster", R.string.state_colorado);
			put("west covina", R.string.state_california);
			put("gresham", R.string.state_oregon);
			put("fargo", R.string.state_north_dakota);
			put("norwalk", R.string.state_california);
			put("carlsbad", R.string.state_california);
			put("fairfield", R.string.state_california);
			put("cambridge", R.string.state_massachusetts);
			put("wichita falls", R.string.state_texas);
			put("high point", R.string.state_north_carolina);
			put("billings", R.string.state_montana);
			put("green bay", R.string.state_wisconsin);
			put("west jordan", R.string.state_utah);
			put("richmond", R.string.state_california);
			put("murrieta", R.string.state_california);
			put("burbank", R.string.state_california);
			put("palm bay", R.string.state_florida);
			put("everett", R.string.state_washington);
			put("flint", R.string.state_michigan);
			put("antioch", R.string.state_california);
			put("erie", R.string.state_pennsylvania);
			put("south bend", R.string.state_indiana);
			put("daly city", R.string.state_california);
			put("centennial", R.string.state_colorado);
			put("temecula", R.string.state_california);
		}
	};
}
