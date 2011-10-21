package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.appwidget.ExpediaBookingsService;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.fragment.BookingInfoValidation;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.RoomTypeActivityHandler;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.DialogUtils;
import com.mobiata.android.validation.PatternValidator.EmailValidator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.RequiredValidator;
import com.mobiata.android.validation.TextViewErrorHandler;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.ValidationProcessor;
import com.mobiata.android.validation.Validator;
import com.omniture.AppMeasurement;

public class BookingInfoActivity extends Activity implements Download, OnDownloadComplete {

	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.booking";

	private static final int DIALOG_CLEAR_PRIVATE_DATA = 4;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private Context mContext;
	private ExpediaBookingApp mApp;

	// Room type handler

	private RoomTypeActivityHandler mRoomTypeHandler;

	// Data pertaining to this booking

	private Session mSession;
	private SearchParams mSearchParams;
	private Property mProperty;
	private Rate mRate;

	// The data that the user has entered for billing info
	private BillingInfo mBillingInfo;
	private CreditCardType mCreditCardType;

	// The state of the form
	private boolean mFormHasBeenFocused;
	private boolean mGuestsExpanded;
	private boolean mBillingExpanded;

	// Cached data from arrays
	private String[] mCountryCodes;

	// Cached views
	private ViewGroup mGuestSavedLayout;
	private ViewGroup mGuestFormLayout;
	private EditText mFirstNameEditText;
	private EditText mLastNameEditText;
	private EditText mTelephoneEditText;
	private EditText mEmailEditText;
	private ViewGroup mBillingSavedLayout;
	private ViewGroup mBillingFormLayout;
	private EditText mAddress1EditText;
	private EditText mAddress2EditText;
	private EditText mCityEditText;
	private EditText mPostalCodeEditText;
	private EditText mStateEditText;
	private Spinner mCountrySpinner;
	private EditText mCardNumberEditText;
	private EditText mExpirationMonthEditText;
	private EditText mExpirationYearEditText;
	private EditText mSecurityCodeEditText;
	private TextView mConfirmationButton;
	private CheckBox mRulesRestrictionsCheckbox;
	private View mReceipt;

	// Cached views (non-interactive)
	private ScrollView mScrollView;
	private ImageView mCreditCardImageView;
	private TextView mSecurityCodeTipTextView;
	private ImageView mChargeDetailsImageView;
	private TextView mChargeDetailsTextView;
	private TextView mRulesRestrictionsTextView;
	private ViewGroup mRulesRestrictionsLayout;

	// Validation
	private ValidationProcessor mValidationProcessor;
	private TextViewErrorHandler mErrorHandler;

	// This is a tracking variable to solve a nasty problem.  The problem is that Spinner.onItemSelectedListener()
	// fires wildly when you set the Spinner's position manually (sometimes twice at a time).  We only want to track
	// when a user *explicitly* clicks on a new country.  What this does is keep track of what the system thinks
	// is the selected country - only the user can get this out of alignment, thus causing tracking.
	private int mSelectedCountryPosition;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	// Errors from a bad booking
	List<ServerError> mErrors;

	// Instance keys
	private static final int INSTANCE_BILLING_INFO = 1;
	private static final int INSTANCE_FORM_HAS_BEEN_FOCUSED = 2;
	private static final int INSTANCE_GUESTS_EXPANDED = 3;
	private static final int INSTANCE_BILLING_EXPANDED = 4;
	private static final int INSTANCE_GUESTS_COMPLETED = 5;
	private static final int INSTANCE_BILLING_COMPLETED = 6;
	private static final int INSTANCE_CARD_COMPLETED = 7;
	private static final int INSTANCE_ERRORS = 8;

	private BookingInfoValidation mBookingInfoValidation;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	// Lifecycle events

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mApp = (ExpediaBookingApp) getApplication();

		mValidationProcessor = new ValidationProcessor();
		mBookingInfoValidation = new BookingInfoValidation();

		setContentView(R.layout.activity_booking_info);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mSession = (Session) JSONUtils.parseJSONableFromIntent(intent, Codes.SESSION, Session.class);
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);
		mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
				SearchParams.class);
		mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);

		// This code allows us to test the BookingInfoActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				mSearchParams = new SearchParams();
				mSearchParams.fillWithTestData();
				mProperty = new Property();
				mProperty.fillWithTestData();
				mRate = new Rate();
				mRate.fillWithTestData();
			}
			catch (JSONException e) {
				Log.e("Couldn't create dummy data!", e);
			}
		}

		// Retrieve some data we keep using
		Resources r = getResources();
		mCountryCodes = r.getStringArray(R.array.country_codes);

		// Retrieve views that we need for the form fields
		mGuestSavedLayout = (ViewGroup) findViewById(R.id.saved_guest_info_layout);
		mGuestFormLayout = (ViewGroup) findViewById(R.id.guest_info_layout);
		mFirstNameEditText = (EditText) findViewById(R.id.first_name_edit_text);
		mLastNameEditText = (EditText) findViewById(R.id.last_name_edit_text);
		mTelephoneEditText = (EditText) findViewById(R.id.telephone_edit_text);
		mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
		mBillingSavedLayout = (ViewGroup) findViewById(R.id.saved_billing_info_layout);
		mBillingFormLayout = (ViewGroup) findViewById(R.id.billing_info_layout);
		mAddress1EditText = (EditText) findViewById(R.id.address1_edit_text);
		mAddress2EditText = (EditText) findViewById(R.id.address2_edit_text);
		mCityEditText = (EditText) findViewById(R.id.city_edit_text);
		mPostalCodeEditText = (EditText) findViewById(R.id.postal_code_edit_text);
		mStateEditText = (EditText) findViewById(R.id.state_edit_text);
		mCountrySpinner = (Spinner) findViewById(R.id.country_spinner);
		mCardNumberEditText = (EditText) findViewById(R.id.card_number_edit_text);
		mExpirationMonthEditText = (EditText) findViewById(R.id.expiration_month_edit_text);
		mExpirationYearEditText = (EditText) findViewById(R.id.expiration_year_edit_text);
		mSecurityCodeEditText = (EditText) findViewById(R.id.security_code_edit_text);
		mConfirmationButton = (TextView) findViewById(R.id.confirm_book_button);
		mRulesRestrictionsCheckbox = (CheckBox) findViewById(R.id.rules_restrictions_checkbox);

		// Other cached views
		mScrollView = (ScrollView) findViewById(R.id.scroll_view);
		mCreditCardImageView = (ImageView) findViewById(R.id.credit_card_image_view);
		mSecurityCodeTipTextView = (TextView) findViewById(R.id.security_code_tip_text_view);
		mChargeDetailsImageView = (ImageView) findViewById(R.id.charge_details_lock_image_view);
		mChargeDetailsTextView = (TextView) findViewById(R.id.charge_details_text_view);
		mRulesRestrictionsTextView = (TextView) findViewById(R.id.rules_restrictions_text_view);
		mRulesRestrictionsLayout = (ViewGroup) findViewById(R.id.rules_restrictions_layout);
		mReceipt = findViewById(R.id.receipt);
		// Configure the room type handler
		mRoomTypeHandler = new RoomTypeActivityHandler(this, getIntent(), mProperty, mSearchParams, mRate);

		// Configure the layout
		BookingReceiptUtils.configureTicket(this, mReceipt, mProperty, mSearchParams, mRate, mRoomTypeHandler);
		configureForm();
		configureFooter();

		// Retrieve previous instance
		SparseArray<Object> lastInstance = (SparseArray<Object>) getLastNonConfigurationInstance();
		if (lastInstance != null) {
			this.mBillingInfo = (BillingInfo) lastInstance.get(INSTANCE_BILLING_INFO);
			mBookingInfoValidation.setGuestsSectionCompleted((Boolean) lastInstance.get(INSTANCE_GUESTS_COMPLETED));
			mBookingInfoValidation.setBillingSectionCompleted((Boolean) lastInstance.get(INSTANCE_BILLING_COMPLETED));
			mBookingInfoValidation.setCardSectionCompleted((Boolean) lastInstance.get(INSTANCE_CARD_COMPLETED));
			this.mErrors = (List<ServerError>) lastInstance.get(INSTANCE_ERRORS);

			if ((Boolean) lastInstance.get(INSTANCE_FORM_HAS_BEEN_FOCUSED)) {
				onFormFieldFocus();
			}

			syncFormFields();

			if ((Boolean) lastInstance.get(INSTANCE_GUESTS_EXPANDED)) {
				expandGuestsForm(false);
			}
			if ((Boolean) lastInstance.get(INSTANCE_BILLING_EXPANDED)) {
				expandBillingForm(false);
			}
		}
		else {
			mBookingInfoValidation.markAllSectionsAsIncomplete();

			// Try loading saved billing info
			if (loadSavedBillingInfo()) {
				syncFormFields();

				// Determine which form sections could be collapsed
				checkSectionsCompleted(false);

				if (!mBookingInfoValidation.isGuestsSectionCompleted()) {
					expandGuestsForm(false);
				}

				if (!mBookingInfoValidation.isBillingSectionCompleted()) {
					expandBillingForm(false);
				}
			}
			else {
				mBillingInfo = new BillingInfo();
				expandGuestsForm(false);
				expandBillingForm(false);
			}

			mFormHasBeenFocused = false;

			onPageLoad();
		}
		mRoomTypeHandler.onCreate(null);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		syncBillingInfo();

		SparseArray<Object> instance = new SparseArray<Object>();
		instance.put(INSTANCE_BILLING_INFO, mBillingInfo);
		instance.put(INSTANCE_FORM_HAS_BEEN_FOCUSED, mFormHasBeenFocused);
		instance.put(INSTANCE_GUESTS_EXPANDED, mGuestsExpanded);
		instance.put(INSTANCE_BILLING_EXPANDED, mBillingExpanded);
		instance.put(INSTANCE_GUESTS_COMPLETED, mBookingInfoValidation.isGuestsSectionCompleted());
		instance.put(INSTANCE_BILLING_COMPLETED, mBookingInfoValidation.isBillingSectionCompleted());
		instance.put(INSTANCE_CARD_COMPLETED, mBookingInfoValidation.isCardSectionCompleted());
		instance.put(INSTANCE_ERRORS, mErrors);

		mRoomTypeHandler.onRetainNonConfigurationInstance(instance);

		return instance;
	}

	@Override
	protected void onStop() {
		super.onStop();

		mWasStopped = true;

		// Save the billing info, in case a user quit activity with a field focused
		if (isFinishing()) {
			saveBillingInfo();
			checkSectionsCompleted(true);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			onPageLoad();
			mWasStopped = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// If we were booking, re-hook the download 
		BackgroundDownloader downloader = BackgroundDownloader.getInstance();
		if (downloader.isDownloading(DOWNLOAD_KEY)) {
			downloader.registerDownloadCallback(DOWNLOAD_KEY, this);
		}

		mRoomTypeHandler.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mRoomTypeHandler.onDestroy();

		super.onDestroy();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Dialogs

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case BookingInfoUtils.DIALOG_BOOKING_PROGRESS: {
			ProgressDialog pd = new ProgressDialog(this);
			pd.setMessage(getString(R.string.booking_loading));
			pd.setCancelable(false);
			return pd;
		}
		case BookingInfoUtils.DIALOG_BOOKING_NULL: {
			return DialogUtils.createSimpleDialog(this, BookingInfoUtils.DIALOG_BOOKING_NULL,
					R.string.error_booking_title, R.string.error_booking_null);
		}
		case BookingInfoUtils.DIALOG_BOOKING_ERROR: {
			// Gather the error message
			String errorMsg = "";
			int numErrors = mErrors.size();
			for (int a = 0; a < numErrors; a++) {
				if (a > 0) {
					errorMsg += "\n";
				}
				errorMsg += mErrors.get(a).getPresentableMessage(this);
			}

			return DialogUtils.createSimpleDialog(this, BookingInfoUtils.DIALOG_BOOKING_ERROR,
					getString(R.string.error_booking_title), errorMsg);
		}
		case DIALOG_CLEAR_PRIVATE_DATA: {
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_clear_private_data_title);
			builder.setMessage(R.string.dialog_clear_private_data_msg);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Delete private data and clear form
					mBillingInfo.delete(mContext);

					mFirstNameEditText.setText(null);
					mLastNameEditText.setText(null);
					mTelephoneEditText.setText(null);
					mEmailEditText.setText(null);
					mAddress1EditText.setText(null);
					mAddress2EditText.setText(null);
					mCityEditText.setText(null);
					mPostalCodeEditText.setText(null);
					mStateEditText.setText(null);
					setSpinnerSelection(mCountrySpinner, getString(R.string.country_us));
					mCardNumberEditText.setText(null);
					mExpirationMonthEditText.setText(null);
					mExpirationYearEditText.setText(null);
					mSecurityCodeEditText.setText(null);
					mRulesRestrictionsCheckbox.setChecked(false);

					expandGuestsForm(false);
					expandBillingForm(false);

					// Inform the men
					Toast.makeText(mContext, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
		}

		return super.onCreateDialog(id);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Menus

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_booking, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_private_data:
			showDialog(DIALOG_CLEAR_PRIVATE_DATA);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// BackgroundDownloader interface

	@Override
	public void onDownload(Object results) {
		removeDialog(BookingInfoUtils.DIALOG_BOOKING_PROGRESS);

		if (results == null) {
			showDialog(BookingInfoUtils.DIALOG_BOOKING_NULL);
			TrackingUtils.trackErrorPage(this, "ReservationRequestFailed");
			return;
		}

		BookingResponse response = (BookingResponse) results;
		if (response.hasErrors()) {
			mErrors = response.getErrors();
			showDialog(BookingInfoUtils.DIALOG_BOOKING_ERROR);
			TrackingUtils.trackErrorPage(this, "ReservationRequestFailed");
			return;
		}

		mSession = response.getSession();

		Intent intent = new Intent(this, ConfirmationActivity.class);
		intent.fillIn(getIntent(), 0);
		intent.putExtra(Codes.BOOKING_RESPONSE, response.toJson().toString());
		intent.putExtra(Codes.SESSION, mSession.toJson().toString());
		mRoomTypeHandler.saveToIntent(intent);

		// Create a BillingInfo that lacks the user's security code (for safety)
		JSONObject billingJson = mBillingInfo.toJson();
		billingJson.remove("securityCode");
		intent.putExtra(Codes.BILLING_INFO, billingJson.toString());

		startActivity(intent);
	}

	@Override
	public Object doDownload() {
		ExpediaServices services = new ExpediaServices(this, mSession);
		return services.reservation(mSearchParams, mProperty, mRate, mBillingInfo);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	// Activity configuration

	private void configureForm() {
		mGuestSavedLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				expandGuestsForm(true);
			}
		});

		mBillingSavedLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				expandBillingForm(true);
			}
		});

		// Setup automatic filling of state/country information based on city entered.
		// Works for some popular cities.
		mCityEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			public void afterTextChanged(Editable s) {
				String key = s.toString().toLowerCase();
				if (BookingInfoUtils.COMMON_US_CITIES.containsKey(key)) {
					mStateEditText.setText(BookingInfoUtils.COMMON_US_CITIES.get(key));
					mStateEditText.setError(null);
					setSpinnerSelection(mCountrySpinner, getString(R.string.country_us));
				}
			}
		});

		// Set the default country as USA
		setSpinnerSelection(mCountrySpinner, getString(R.string.country_us));
		mCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Adjust the postal code textview.  Do this regardless of how the country spinner changed selection
				if (mCountryCodes[mCountrySpinner.getSelectedItemPosition()].equals("US")) {
					mPostalCodeEditText.setInputType(InputType.TYPE_CLASS_NUMBER
							| InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
				}
				else {
					mPostalCodeEditText.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				}

				// See description of mSelectedCountryPosition to understand why we're doing this
				if (mSelectedCountryPosition != position) {
					if (mFormHasBeenFocused) {
						BookingInfoUtils.focusAndOpenKeyboard(BookingInfoActivity.this, mPostalCodeEditText);
					}
					BookingInfoUtils.onCountrySpinnerClick(BookingInfoActivity.this);

					// Once a user has explicitly changed the country, track every change thereafter
					mSelectedCountryPosition = -1;
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});

		// Configure card number - detection
		// Also configure the lock appearing/disappearing
		mCardNumberEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			@Override
			public void afterTextChanged(Editable s) {
				mCreditCardType = CurrencyUtils.detectCreditCardBrand(mContext, s.toString());
				if (mCreditCardType != null) {
					mCreditCardImageView.setImageResource(BookingInfoUtils.CREDIT_CARD_ICONS.get(mCreditCardType));
					mSecurityCodeTipTextView.setText(BookingInfoUtils.CREDIT_CARD_SECURITY_LOCATION
							.get(mCreditCardType));
				}
				else {
					mCreditCardImageView.setImageResource(R.drawable.ic_cc_unknown);
					mSecurityCodeTipTextView.setText(R.string.security_code_tip_back);
				}

				// If there is text, don't show the lock icon
				int lockIcon = (s == null || s.length() == 0) ? R.drawable.credit_card_lock : 0;
				Drawable dr[] = mCardNumberEditText.getCompoundDrawables();
				if (dr != null) {
					Drawable lockIconDrawable = lockIcon == 0 ? null : getResources().getDrawable(lockIcon);
					mCardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(lockIconDrawable, dr[1], dr[2], dr[3]);
				}
				else {
					mCardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(lockIcon, 0, 0, 0);
				}
			}
		});

		mCardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.credit_card_lock, 0, 0, 0);
		mCardNumberEditText.setCompoundDrawablePadding(Math.round(6 * getResources().getDisplayMetrics().density));

		// Only display the checkbox if we're in a locale that requires its display
		if (RulesRestrictionsUtils.requiresRulesRestrictionsCheckbox()) {
			mRulesRestrictionsCheckbox.setVisibility(View.VISIBLE);
			mRulesRestrictionsCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					buttonView.setError(null);
				}
			});

			// Configure credit card security code field to point towards the checkbox
			mSecurityCodeEditText.setNextFocusDownId(R.id.rules_restrictions_checkbox);
			mSecurityCodeEditText.setNextFocusRightId(R.id.rules_restrictions_checkbox);

			mSecurityCodeEditText.setImeOptions(mSecurityCodeEditText.getImeOptions() | EditorInfo.IME_ACTION_NEXT);
			mSecurityCodeEditText.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_NEXT) {
						focusRulesRestrictions();
						return true;
					}
					return false;
				}
			});
		}
		else {
			mRulesRestrictionsCheckbox.setVisibility(View.GONE);
		}

		// Setup the correct text (and link enabling) on the terms & conditions textview
		mRulesRestrictionsTextView.setText(RulesRestrictionsUtils.getRulesRestrictionsConfirmation(this));
		mRulesRestrictionsTextView.setMovementMethod(LinkMovementMethod.getInstance());

		// Configure form validation
		// Setup validators and error handlers
		final String userCurrency = CurrencyUtils.getCurrencyCode(mContext);
		TextViewValidator requiredFieldValidator = new TextViewValidator();
		Validator<TextView> usValidator = new Validator<TextView>() {
			public int validate(TextView obj) {
				if (mBillingInfo.getLocation().getCountryCode().equals("US")) {
					return RequiredValidator.getInstance().validate(obj.getText());
				}
				return 0;
			}
		};
		TextViewErrorHandler errorHandler = mErrorHandler = new TextViewErrorHandler(getString(R.string.required_field));
		errorHandler.addResponse(ValidationError.ERROR_DATA_INVALID, getString(R.string.invalid_field));
		errorHandler.addResponse(BookingInfoValidation.ERROR_INVALID_CARD_NUMBER,
				getString(R.string.invalid_card_number));
		errorHandler.addResponse(BookingInfoValidation.ERROR_INVALID_MONTH, getString(R.string.invalid_month));
		errorHandler.addResponse(BookingInfoValidation.ERROR_EXPIRED_YEAR, getString(R.string.invalid_expiration_year));
		errorHandler.addResponse(BookingInfoValidation.ERROR_SHORT_SECURITY_CODE,
				getString(R.string.invalid_security_code));
		errorHandler.addResponse(BookingInfoValidation.ERROR_INVALID_CARD_TYPE, getString(R.string.invalid_card_type));
		errorHandler.addResponse(BookingInfoValidation.ERROR_AMEX_BAD_CURRENCY,
				getString(R.string.invalid_currency_for_amex, userCurrency));
		errorHandler.addResponse(BookingInfoValidation.ERROR_NO_TERMS_CONDITIONS_AGREEMEMT,
				getString(R.string.error_no_user_agreement));

		// Add all the validators
		mValidationProcessor.add(mFirstNameEditText, requiredFieldValidator);
		mValidationProcessor.add(mLastNameEditText, requiredFieldValidator);
		mValidationProcessor.add(mTelephoneEditText, new TextViewValidator(new TelephoneValidator()));
		mValidationProcessor.add(mEmailEditText, new TextViewValidator(new EmailValidator()));
		mValidationProcessor.add(mAddress1EditText, requiredFieldValidator);
		mValidationProcessor.add(mCityEditText, requiredFieldValidator);
		mValidationProcessor.add(mStateEditText, usValidator);
		mValidationProcessor.add(mPostalCodeEditText, usValidator);
		mValidationProcessor.add(mCardNumberEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence number) {
				if (mCreditCardType == null) {
					TrackingUtils.trackErrorPage(mContext, "CreditCardNotSupported");
					return BookingInfoValidation.ERROR_INVALID_CARD_TYPE;
				}
				else if (!FormatUtils.isValidCreditCardNumber(number)) {
					TrackingUtils.trackErrorPage(mContext, "CreditCardNotSupported");
					return BookingInfoValidation.ERROR_INVALID_CARD_NUMBER;
				}
				else if (mCreditCardType == CreditCardType.AMERICAN_EXPRESS
						&& !CurrencyUtils.currencySupportedByAmex(mContext, userCurrency)) {
					TrackingUtils.trackErrorPage(mContext, "CurrencyNotSupported");
					return BookingInfoValidation.ERROR_AMEX_BAD_CURRENCY;
				}
				return 0;
			}
		}));
		mValidationProcessor.add(mExpirationMonthEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence obj) {
				int month = Integer.parseInt(obj.toString());
				return (month < 1 || month > 12) ? BookingInfoValidation.ERROR_INVALID_MONTH : 0;
			}
		}));
		mValidationProcessor.add(mExpirationYearEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence obj) {
				int thisYear = Calendar.getInstance().get(Calendar.YEAR);
				int year = Integer.parseInt(obj.toString());
				return (thisYear % 100 > year) ? BookingInfoValidation.ERROR_EXPIRED_YEAR : 0;
			}
		}));
		mValidationProcessor.add(mSecurityCodeEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence obj) {
				return (obj.length() < 3) ? BookingInfoValidation.ERROR_SHORT_SECURITY_CODE : 0;
			}
		}));
		mValidationProcessor.add(mRulesRestrictionsCheckbox, new Validator<CheckBox>() {
			public int validate(CheckBox obj) {
				if (RulesRestrictionsUtils.requiresRulesRestrictionsCheckbox() && !obj.isChecked()) {
					return BookingInfoValidation.ERROR_NO_TERMS_CONDITIONS_AGREEMEMT;
				}
				return 0;
			}
		});

		// Configure the bottom of the page form stuff
		final BookingInfoActivity activity = this;
		mConfirmationButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				syncBillingInfo();

				// Just to make sure, save the billing info when the user clicks submit
				saveBillingInfo();

				List<ValidationError> errors = mValidationProcessor.validate();
				int numErrors = errors.size();

				if (!mFormHasBeenFocused) {
					// Since the user hasn't even focused the form yet, instead push them towards the first
					// invalid field to enter
					if (numErrors > 0) {
						View firstErrorView = (View) errors.get(0).getObject();
						BookingInfoUtils.focusAndOpenKeyboard(BookingInfoActivity.this, firstErrorView);
					}
					return;
				}

				if (numErrors > 0) {
					for (ValidationError error : errors) {
						mErrorHandler.handleError(error);
					}

					// Request focus on the first field that was invalid
					View firstErrorView = (View) errors.get(0).getObject();
					if (firstErrorView == mRulesRestrictionsCheckbox) {
						focusRulesRestrictions();
					}
					else {
						BookingInfoUtils.focusAndOpenKeyboard(BookingInfoActivity.this, firstErrorView);
					}
				}
				else {
					BookingInfoUtils.onClickSubmit(BookingInfoActivity.this);
					showDialog(BookingInfoUtils.DIALOG_BOOKING_PROGRESS);
					BackgroundDownloader.getInstance().startDownload(DOWNLOAD_KEY, activity, activity);
				}
			}
		});

		// Setup a focus change listener that changes the bottom from "enter booking info"
		// to "confirm & book", plus the text
		OnFocusChangeListener l = new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					onFormFieldFocus();
				}
				else {
					saveBillingInfo();

					checkSectionsCompleted(true);
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
		mStateEditText.setOnFocusChangeListener(l);
		mCountrySpinner.setOnFocusChangeListener(l);
		mCardNumberEditText.setOnFocusChangeListener(l);
		mExpirationMonthEditText.setOnFocusChangeListener(l);
		mExpirationYearEditText.setOnFocusChangeListener(l);
		mSecurityCodeEditText.setOnFocusChangeListener(l);
		mConfirmationButton.setOnFocusChangeListener(l);
	}

	private void configureFooter() {
		// 9226: Only display the Expedia Points disclaimer if the user is in the US.
		// (This may change in the future as more countries support points.)
		int visibility = ("us".equals(Locale.getDefault().getCountry().toLowerCase())) ? View.VISIBLE : View.GONE;
		TextView pointsDisclaimerView = (TextView) findViewById(R.id.expedia_points_disclaimer_text_view);
		pointsDisclaimerView.setVisibility(visibility);

		// Configure the cancellation policy
		TextView cancellationPolicyView = (TextView) findViewById(R.id.cancellation_policy_text_view);
		Policy cancellationPolicy = mRate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			cancellationPolicyView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
		}
		else {
			cancellationPolicyView.setVisibility(View.GONE);
		}
	}

	private void setSpinnerSelection(Spinner spinner, String target) {
		mSelectedCountryPosition = findAdapterIndex(spinner.getAdapter(), target);
		spinner.setSelection(mSelectedCountryPosition);
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
				mSelectedCountryPosition = n;
				spinner.setSelection(n);
				return;
			}
		}
	}

	private void onFormFieldFocus() {
		if (!mFormHasBeenFocused) {
			// Change the button text
			mConfirmationButton.setText(R.string.confirm_book);

			// Reveal the charge lock icon
			mChargeDetailsImageView.setVisibility(View.VISIBLE);

			// Add the charge details text
			CharSequence text = getString(R.string.charge_details_template, mRate.getTotalAmountAfterTax()
					.getFormattedMoney());
			mChargeDetailsTextView.setText(text);

			mFormHasBeenFocused = true;
		}
	}

	// Interactivity when expanding saved billing info

	private void expandGuestsForm(boolean animateAndFocus) {
		if (!mGuestsExpanded) {
			mGuestsExpanded = true;

			mGuestSavedLayout.setVisibility(View.GONE);
			mGuestFormLayout.setVisibility(View.VISIBLE);

			// Fix focus movement
			fixFocus();

			if (animateAndFocus) {
				BookingInfoUtils.focusAndOpenKeyboard(this, mFirstNameEditText);
			}

			// TODO: Animation if animated
		}
	}

	private void expandBillingForm(boolean animateAndFocus) {
		if (!mBillingExpanded) {
			mBillingExpanded = true;

			mBillingSavedLayout.setVisibility(View.GONE);
			mBillingFormLayout.setVisibility(View.VISIBLE);

			// Fix focus movement
			fixFocus();

			if (animateAndFocus) {
				BookingInfoUtils.focusAndOpenKeyboard(this, mAddress1EditText);
			}

			// TODO: Animation if animated
		}
	}

	// Fixes focus based on expanding form fields
	private void fixFocus() {
		// Handle where guest forms are pointing down (if expanded)
		if (mGuestsExpanded) {
			int nextId = (mBillingExpanded) ? R.id.address1_edit_text : R.id.card_number_edit_text;
			mEmailEditText.setNextFocusDownId(nextId);
			mEmailEditText.setNextFocusRightId(nextId);
		}

		// Handle where card info is pointing up
		int nextId = (mBillingExpanded) ? R.id.postal_code_edit_text : R.id.email_edit_text;
		mCardNumberEditText.setNextFocusUpId(nextId);
		mCardNumberEditText.setNextFocusLeftId(nextId);
		mExpirationMonthEditText.setNextFocusUpId(nextId);
		mExpirationYearEditText.setNextFocusUpId(nextId);
	}

	// Focusing the rules & restrictions is special for two reasons:
	// 1. It needs to focus the containing layout, so users can view the entire rules & restrictions.
	// 2. It doesn't need to open the soft keyboard.
	private void focusRulesRestrictions() {
		mScrollView.requestChildFocus(mRulesRestrictionsLayout, mRulesRestrictionsLayout);
		mScrollView.scrollBy(0, (int) getResources().getDisplayMetrics().density * 15);
	}

	// BillingInfo syncing and saving/loading

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
		location.setStateCode(mStateEditText.getText().toString());
		location.setCountryCode(mCountryCodes[mCountrySpinner.getSelectedItemPosition()]);
		mBillingInfo.setLocation(location);

		mBillingInfo.setNumber(mCardNumberEditText.getText().toString());
		String expirationMonth = mExpirationMonthEditText.getText().toString();
		String expirationYear = mExpirationYearEditText.getText().toString();
		if (expirationMonth != null && expirationMonth.length() > 0 && expirationYear != null
				&& expirationYear.length() > 0) {
			Calendar cal = new GregorianCalendar(Integer.parseInt(expirationYear) + 2000,
					Integer.parseInt(expirationMonth) - 1, 15);
			mBillingInfo.setExpirationDate(cal);
		}
		mBillingInfo.setSecurityCode(mSecurityCodeEditText.getText().toString());

		if (mCreditCardType != null) {
			mBillingInfo.setBrandCode(mCreditCardType.getCode());
		}
	}

	/**
	 * Syncs the form fields with data from local BillingInfo.  Should be used when creating or
	 * restoring the Activity.
	 */
	private void syncFormFields() {
		// Sync the saved guest fields
		String firstName = mBillingInfo.getFirstName();
		String lastName = mBillingInfo.getLastName();
		if (firstName != null && lastName != null) {
			TextView fullNameView = (TextView) findViewById(R.id.full_name_text_view);
			fullNameView.setText(firstName + " " + lastName);
		}

		TextView telephoneView = (TextView) findViewById(R.id.telephone_text_view);
		telephoneView.setText(mBillingInfo.getTelephone());

		TextView emailView = (TextView) findViewById(R.id.email_text_view);
		emailView.setText(mBillingInfo.getEmail());

		// Sync the editable guest fields
		mFirstNameEditText.setText(firstName);
		mLastNameEditText.setText(lastName);
		mTelephoneEditText.setText(mBillingInfo.getTelephone());
		mEmailEditText.setText(mBillingInfo.getEmail());

		// Sync the saved billing info fields
		String address = "";
		Location loc = mBillingInfo.getLocation();
		if (loc != null) {
			address = StrUtils.formatAddress(loc);
			String countryCode = loc.getCountryCode();
			if (countryCode != null) {
				for (int n = 0; n < mCountryCodes.length; n++) {
					if (mCountryCodes[n].equals(countryCode)) {
						address += "\n" + getResources().getStringArray(R.array.country_names)[n];
						break;
					}
				}
			}

			TextView addressView = (TextView) findViewById(R.id.address_text_view);
			addressView.setText(address);
		}

		// Sync the editable billing info fields
		mAddress1EditText.setText(loc.getStreetAddress().get(0));
		if (loc.getStreetAddress().size() > 1) {
			mAddress2EditText.setText(loc.getStreetAddress().get(1));
		}
		mCityEditText.setText(loc.getCity());
		mPostalCodeEditText.setText(loc.getPostalCode());
		setSpinnerSelection(mCountrySpinner, mCountryCodes, loc.getCountryCode());
		mStateEditText.setText(loc.getStateCode());

		// Sync the editable credit card info fields
		mCardNumberEditText.setText(mBillingInfo.getNumber());
		Calendar cal = mBillingInfo.getExpirationDate();
		if (cal != null) {
			mExpirationMonthEditText.setText((mBillingInfo.getExpirationDate().get(Calendar.MONTH) + 1) + "");
			mExpirationYearEditText.setText((mBillingInfo.getExpirationDate().get(Calendar.YEAR) % 100) + "");
		}
		mSecurityCodeEditText.setText(mBillingInfo.getSecurityCode());
	}

	private boolean saveBillingInfo() {
		// Gather all the data to be saved
		syncBillingInfo();

		// Save the hashed email, just for tracking purposes
		TrackingUtils.saveEmailForTracking(this, mBillingInfo.getEmail());

		return mBillingInfo.save(this);
	}

	private boolean loadSavedBillingInfo() {
		BillingInfo tmpInfo = new BillingInfo();
		if (tmpInfo.load(this)) {
			mBillingInfo = tmpInfo;
			return true;
		}

		return false;
	}

	private void checkSectionsCompleted(boolean trackCompletion) {
		boolean isGuestsCompleted = mBookingInfoValidation.isGuestsSectionCompleted();
		boolean isBillingCompleted = mBookingInfoValidation.isBillingSectionCompleted();
		boolean isCardCompleted = mBookingInfoValidation.isCardSectionCompleted();

		checkSectionsCompleted(true);

		if (trackCompletion) {
			if (isGuestsCompleted && mBookingInfoValidation.isGuestsSectionCompleted()) {
				BookingInfoUtils.onCompletedSection(this, "CKO.BD.CompletedGuestInfo");
			}
			if (isBillingCompleted && mBookingInfoValidation.isBillingSectionCompleted()) {
				BookingInfoUtils.onCompletedSection(this, "CKO.BD.CompletedBillingInfo");
			}
			if (isCardCompleted && mBookingInfoValidation.isCardSectionCompleted()) {
				BookingInfoUtils.onCompletedSection(this, "CKO.BD.CompletedCreditCard");
			}

		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Log.d("Tracking \"App.Hotels.Checkout.Payment\" pageLoad");

		AppMeasurement s = new AppMeasurement(getApplication());

		TrackingUtils.addStandardFields(this, s);

		s.pageName = "App.Hotels.Checkout.Payment";

		s.events = "event34";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Products
		TrackingUtils.addProducts(s, mProperty);

		// If any sections were already complete, fill them in here
		String referrerId = null;
		if (mBookingInfoValidation.isGuestsSectionCompleted() && mBookingInfoValidation.isBillingSectionCompleted()) {
			referrerId = "CKO.BD.CompletedGuestInfo|CKO.BD.CompletedBillingInfo";
		}
		else if (mBookingInfoValidation.isGuestsSectionCompleted()) {
			referrerId = "CKO.BD.CompletedGuestInfo";
		}
		else if (mBookingInfoValidation.isBillingSectionCompleted()) {
			referrerId = "CKO.BD.CompletedBillingInfo";
		}

		s.eVar28 = s.prop16 = referrerId;

		// Send the tracking data
		s.track();
	}

}
