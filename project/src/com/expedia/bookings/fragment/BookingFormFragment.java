package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.ReceiptWidget;
import com.expedia.bookings.widget.StoredCardSpinnerAdapter;
import com.expedia.bookings.widget.TelephoneSpinner;
import com.expedia.bookings.widget.TelephoneSpinnerAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.validation.PatternValidator.EmailValidator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.RequiredValidator;
import com.mobiata.android.validation.TextViewErrorHandler;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.ValidationProcessor;
import com.mobiata.android.validation.Validator;

public class BookingFormFragment extends DialogFragment {
	private static final String KEY_SIGNIN = "KEY_SIGNIN";

	public static BookingFormFragment newInstance() {
		BookingFormFragment dialog = new BookingFormFragment();
		return dialog;
	}

	private static String GUESTS_EXPANDED = "GUESTS_EXPANDED";
	private static String BILLING_EXPANDED = "BILLING_EXPANDED";
	private static String RULES_RESTRICTIONS_CHECKED = "RULES_RESTRICTIONS_CHECKED";
	private static String USER_PROFILE_IS_FRESH = "USER_PROFILE_IS_FRESH";

	// Cached views
	private View mRootBillingView;
	private ViewGroup mGuestSavedLayout;
	private ViewGroup mGuestFormLayout;
	private EditText mFirstNameEditText;
	private EditText mLastNameEditText;
	private TelephoneSpinner mTelephoneCountryCodeSpinner;
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
	private View mCreditCardInfoContainer;
	private EditText mCardNumberEditText;
	private EditText mExpirationMonthEditText;
	private EditText mExpirationYearEditText;
	private EditText mSecurityCodeEditText;
	private TextView mConfirmBookButton;
	private View mCloseFormButton;
	private ImageView mChargeDetailsImageView;
	private TextView mChargeDetailsTextView;
	private AccountButton mAccountButton;
	private View mStoredCardContainer;
	private Spinner mStoredCardSpinner;

	// Cached data from arrays
	private String[] mCountryCodes;

	// Cached views (non-interactive)
	private ScrollView mScrollView;
	private ImageView mCreditCardImageView;
	private TextView mSecurityCodeTipTextView;
	private TextView mRulesRestrictionsTextView;
	private CheckBox mRulesRestrictionsCheckbox;
	private ViewGroup mRulesRestrictionsLayout;

	// Validation
	private ValidationProcessor mValidationProcessor;
	private ValidationProcessor mGuestInfoValidationProcessor;
	private ValidationProcessor mAddressValidationProcessor;
	private TextViewErrorHandler mErrorHandler;

	// The data that the user has entered for billing info
	private CreditCardType mCreditCardType;
	private StoredCardSpinnerAdapter mCardAdapter;

	// The state of the form
	private boolean mFormHasBeenFocused;
	private boolean mGuestsExpanded;
	private boolean mBillingExpanded;

	private ReceiptWidget mReceiptWidget;

	// This is a tracking variable to solve a nasty problem.  The problem is that Spinner.onItemSelectedListener()
	// fires wildly when you set the Spinner's position manually (sometimes twice at a time).  We only want to track
	// when a user *explicitly* clicks on a new country.  What this does is keep track of what the system thinks
	// is the selected country - only the user can get this out of alignment, thus causing tracking.
	private int mSelectedCountryPosition;

	private BookingInfoValidation mBookingInfoValidation;

	private BookingFormFragmentListener mListener;
	private Activity mActivity;

	private boolean mUserProfileIsFresh = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mValidationProcessor = new ValidationProcessor();
		mGuestInfoValidationProcessor = new ValidationProcessor();
		mAddressValidationProcessor = new ValidationProcessor();
		mBookingInfoValidation = new BookingInfoValidation();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof BookingFormFragment.BookingFormFragmentListener)) {
			throw new RuntimeException("Activity must implement BookingFormFragment.BookingFormFragmentListener!");
		}

		if (!(activity instanceof SignInFragment.SignInFragmentListener)) {
			throw new RuntimeException("Activity must implement SignInFragment.SignInFragmentListener!");
		}

		mListener = (BookingFormFragmentListener) activity;
		mActivity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			loadSavedBillingInfo();
		}

		View view = inflater.inflate(R.layout.fragment_booking_form, null);
		mRootBillingView = view;

		// Retrieve views that we need for the form fields
		mGuestSavedLayout = (ViewGroup) view.findViewById(R.id.saved_guest_info_layout);
		mGuestFormLayout = (ViewGroup) view.findViewById(R.id.guest_info_layout);
		mFirstNameEditText = (EditText) view.findViewById(R.id.first_name_edit_text);
		mLastNameEditText = (EditText) view.findViewById(R.id.last_name_edit_text);
		mTelephoneCountryCodeSpinner = (TelephoneSpinner) view.findViewById(R.id.telephone_country_code_spinner);
		mTelephoneEditText = (EditText) view.findViewById(R.id.telephone_edit_text);
		mEmailEditText = (EditText) view.findViewById(R.id.email_edit_text);
		mBillingSavedLayout = (ViewGroup) view.findViewById(R.id.saved_billing_info_layout);
		mBillingFormLayout = (ViewGroup) view.findViewById(R.id.billing_info_layout);
		mAddress1EditText = (EditText) view.findViewById(R.id.address1_edit_text);
		mAddress2EditText = (EditText) view.findViewById(R.id.address2_edit_text);
		mCityEditText = (EditText) view.findViewById(R.id.city_edit_text);
		mPostalCodeEditText = (EditText) view.findViewById(R.id.postal_code_edit_text);
		mStateEditText = (EditText) view.findViewById(R.id.state_edit_text);
		mCountrySpinner = (Spinner) view.findViewById(R.id.country_spinner);
		mCreditCardInfoContainer = view.findViewById(R.id.credit_card_info_container);
		mCardNumberEditText = (EditText) mCreditCardInfoContainer.findViewById(R.id.card_number_edit_text);
		mExpirationMonthEditText = (EditText) mCreditCardInfoContainer.findViewById(R.id.expiration_month_edit_text);
		mExpirationYearEditText = (EditText) mCreditCardInfoContainer.findViewById(R.id.expiration_year_edit_text);
		mSecurityCodeEditText = (EditText) view.findViewById(R.id.security_code_edit_text);
		mCreditCardImageView = (ImageView) view.findViewById(R.id.credit_card_image_view);
		mSecurityCodeTipTextView = (TextView) view.findViewById(R.id.security_code_tip_text_view);
		mScrollView = (ScrollView) view.findViewById(R.id.scroll_view);
		mRulesRestrictionsCheckbox = (CheckBox) view.findViewById(R.id.rules_restrictions_checkbox);
		mRulesRestrictionsTextView = (TextView) view.findViewById(R.id.rules_restrictions_text_view);
		mRulesRestrictionsLayout = (ViewGroup) view.findViewById(R.id.rules_restrictions_layout);
		mConfirmBookButton = Ui.findView(view, R.id.confirm_book_button);
		mCloseFormButton = view.findViewById(R.id.close_booking_form);
		mChargeDetailsImageView = Ui.findView(view, R.id.charge_details_lock_image_view);
		mChargeDetailsTextView = Ui.findView(view, R.id.charge_details_text_view);

		mStoredCardContainer = view.findViewById(R.id.stored_card_container);
		mStoredCardSpinner = (Spinner) mStoredCardContainer.findViewById(R.id.stored_card_spinner);

		mAccountButton = new AccountButton(getActivity(), mAccountButtonClickListener, view.findViewById(R.id.account_button_root));
		mReceiptWidget = new ReceiptWidget(getActivity(), view.findViewById(R.id.receipt), !getShowsDialog());

		// 10758: rendering the saved layouts on a software layer
		// to avoid the fuzziness of the saved section background
		int sdkVersion = AndroidUtils.getSdkVersion();
		if (sdkVersion >= 11 && sdkVersion <= 13) {
			mGuestSavedLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			mBillingSavedLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			view.findViewById(R.id.credit_card_security_code_container).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		// Retrieve some data we keep using
		Resources r = getResources();
		String[] twoLetterCountryCodes = r.getStringArray(R.array.country_codes);
		String[] threeLetterCountryCodes = new String[twoLetterCountryCodes.length];
		for (int i = 0; i < twoLetterCountryCodes.length; i++){
			threeLetterCountryCodes[i] = LocaleUtils.convertCountryCode(twoLetterCountryCodes[i]);
		}
		mCountryCodes = threeLetterCountryCodes;
		configureForm();

		// Figure out if we are logged in
		if (ExpediaServices.isLoggedIn((Context) mActivity)) {
			if (savedInstanceState != null && savedInstanceState.getBoolean(USER_PROFILE_IS_FRESH)) {
				mUserProfileIsFresh = true;
				mAccountButton.update(false);
				syncFormFieldsFromBillingInfo(view);

				checkSectionsCompleted(false);

				if (!mBookingInfoValidation.isGuestsSectionCompleted()) {
					expandGuestsForm(false);
				}

				if (!checkAddressCompleted()) {
					expandBillingForm(false);
				}
			}
			else {
				syncFormFieldsFromBillingInfo(view);
				if (!mBookingInfoValidation.isGuestsSectionCompleted()) {
					expandGuestsForm(false);
				}
				if (!checkAddressCompleted()) {
					expandBillingForm(false);
				}

				// Show progress spinner
				mAccountButton.update(true);
				// fetch fresh profile
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (bd.isDownloading(KEY_SIGNIN)) {
					bd.cancelDownload(KEY_SIGNIN);
				}
				bd.startDownload(KEY_SIGNIN, mLoginDownload, mLoginCallback);
			}
		}
		else {
			mAccountButton.update(false);

			if (Db.getBillingInfo().doesExistOnDisk()) {
				syncFormFieldsFromBillingInfo(view);

				if (savedInstanceState != null) {
					if (savedInstanceState.getBoolean(GUESTS_EXPANDED)) {
						expandGuestsForm(false);
					}
					if (savedInstanceState.getBoolean(BILLING_EXPANDED)) {
						expandBillingForm(false);
					}
					mRulesRestrictionsCheckbox.setChecked(savedInstanceState.getBoolean(RULES_RESTRICTIONS_CHECKED));
				}
				else {
					checkSectionsCompleted(false);

					if (!mBookingInfoValidation.isGuestsSectionCompleted()) {
						expandGuestsForm(false);
					}

					if (!checkAddressCompleted()) {
						expandBillingForm(false);
					}
				}
			}
			else {
				expandGuestsForm(false);
				expandBillingForm(false);
			}
			mFormHasBeenFocused = false;
		}

		if (savedInstanceState == null) {
			// 12810: Clear out the credit card info if it happens to exist by this point.
			mCardNumberEditText.setText("");
			mSecurityCodeEditText.setText("");
		}

		mReceiptWidget.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate());
		mReceiptWidget.restoreInstanceState(savedInstanceState);

		BookingInfoUtils.determineExpediaPointsDisclaimer(getActivity(), view);
		ConfirmationUtils.determineCancellationPolicy(Db.getSelectedRate(), view);

		if (savedInstanceState == null) {
			Tracker.trackAppHotelsCheckoutPayment(getActivity(), Db.getSelectedProperty(), mBookingInfoValidation);
		}

		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);

		// Make modifications for the dialog implementation of BookingFormFragment here.
		// Any changes that you want 

		dialog.requestWindowFeature(STYLE_NO_TITLE);

		dialog.setCanceledOnTouchOutside(false);

		// set the window of the dialog to have a transparent background
		// so that the window is not visible through the edges of the dialog.
		Window window = dialog.getWindow();
		window.setBackgroundDrawable(new ColorDrawable(0));
		window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		return dialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(GUESTS_EXPANDED, mGuestsExpanded);
		outState.putBoolean(BILLING_EXPANDED, mBillingExpanded);
		outState.putBoolean(RULES_RESTRICTIONS_CHECKED, mRulesRestrictionsCheckbox.isChecked());
		outState.putBoolean(USER_PROFILE_IS_FRESH, mUserProfileIsFresh);

		mReceiptWidget.saveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_SIGNIN, mLoginCallback);
	}

	@Override
	public void onResume() {
		super.onResume();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (! mUserProfileIsFresh && bd.isDownloading(KEY_SIGNIN)) {
			bd.registerDownloadCallback(KEY_SIGNIN, mLoginCallback);
		}
	}

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

		// Set the default country as locale country
		final String targetCountry = getString(LocaleUtils.getDefaultCountryResId(getActivity()));
		setSpinnerSelection(mTelephoneCountryCodeSpinner, targetCountry);
		setSpinnerSelection(mCountrySpinner, targetCountry);
		mCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Adjust the postal code textview.  Do this regardless of how the country spinner changed selection
				if (mCountryCodes[mCountrySpinner.getSelectedItemPosition()].equals("USA")) {
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
						BookingInfoUtils.focusAndOpenKeyboard(getActivity(), mPostalCodeEditText);
					}
					BookingInfoUtils.onCountrySpinnerClick(getActivity());

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
				mCreditCardType = CurrencyUtils.detectCreditCardBrand(getActivity(), s.toString());
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
		if (RulesRestrictionsUtils.requiresRulesRestrictionsCheckbox(getActivity())) {
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
						// TODO: IMPLEMENT THIS
						// .focusOnRulesAndRestrictions();
						return true;
					}
					return false;
				}
			});
		}
		else {
			mRulesRestrictionsCheckbox.setVisibility(View.GONE);
		}

		if (mConfirmBookButton != null) {
			mConfirmBookButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					syncBillingInfo();

					// Just to make sure, save the billing info when the user clicks submit
					saveBillingInfo();
					ValidationProcessor processor = mValidationProcessor;

					if (mUserProfileIsFresh) {
						StoredCreditCard card = mCardAdapter.getSelectedCard();
						if (card != null) {
							// a valid stored CC and not enter new card
							Db.getBillingInfo().setStoredCard(card);
							// just validate the guest info
							processor = mGuestInfoValidationProcessor;
						}
					}

					List<ValidationError> errors = processor.validate();

					if (errors.size() > 0) {
						handleFormErrors(errors);
					}
					else {
						dismissKeyboard(v);
						BookingInfoUtils.onClickSubmit(getActivity());
						mListener.onCheckout();
					}
				}
			});
		}

		if (mCloseFormButton != null) {
			mCloseFormButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismissKeyboard(v);

					saveBillingInfo();

					checkSectionsCompleted(false);

					dismiss();
				}
			});
		}

		// Setup the correct text (and link enabling) on the terms & conditions textview
		mRulesRestrictionsTextView.setText(RulesRestrictionsUtils.getRulesRestrictionsConfirmation(getActivity()));
		mRulesRestrictionsTextView.setMovementMethod(LinkMovementMethod.getInstance());

		// Configure form validation
		// Setup validators and error handlers
		final String userCurrency = "USD"; //TODO: CurrencyUtils.getCurrencyCode(getActivity());
		TextViewValidator requiredFieldValidator = new TextViewValidator();
		Validator<TextView> usValidator = new Validator<TextView>() {
			public int validate(TextView obj) {
				BillingInfo info = Db.getBillingInfo();
				if (info != null && info.getLocation() != null && info.getLocation().getCountryCode().equals("USA")) {
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

		mGuestInfoValidationProcessor.add(mFirstNameEditText, requiredFieldValidator);
		mGuestInfoValidationProcessor.add(mLastNameEditText, requiredFieldValidator);
		mGuestInfoValidationProcessor.add(mTelephoneEditText, new TextViewValidator(new TelephoneValidator()));
		mGuestInfoValidationProcessor.add(mEmailEditText, new TextViewValidator(new EmailValidator()));
		mAddressValidationProcessor.add(mAddress1EditText, requiredFieldValidator);
		mAddressValidationProcessor.add(mCityEditText, requiredFieldValidator);
		mAddressValidationProcessor.add(mStateEditText, usValidator);
		mAddressValidationProcessor.add(mPostalCodeEditText, usValidator);
		mValidationProcessor.add(mCardNumberEditText, new TextViewValidator(new Validator<CharSequence>() {
			public int validate(CharSequence number) {
				if (mCreditCardType == null) {
					TrackingUtils.trackErrorPage(getActivity(), "CreditCardNotSupported");
					return BookingInfoValidation.ERROR_INVALID_CARD_TYPE;
				}
				else if (!FormatUtils.isValidCreditCardNumber(number)) {
					TrackingUtils.trackErrorPage(getActivity(), "CreditCardNotSupported");
					return BookingInfoValidation.ERROR_INVALID_CARD_NUMBER;
				}
				else if (mCreditCardType == CreditCardType.AMERICAN_EXPRESS
						&& !CurrencyUtils.currencySupportedByAmex(getActivity(), userCurrency)) {
					TrackingUtils.trackErrorPage(getActivity(), "CurrencyNotSupported");
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
				if (RulesRestrictionsUtils.requiresRulesRestrictionsCheckbox(getActivity()) && !obj.isChecked()) {
					return BookingInfoValidation.ERROR_NO_TERMS_CONDITIONS_AGREEMEMT;
				}
				return 0;
			}
		});

		// Setup a focus change listener that changes the bottom from "enter booking info"
		// to "confirm & book", plus the text
		OnFocusChangeListener l = new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (!mFormHasBeenFocused) {
						// Change the button text (if not showing as dialog)
						if (!getShowsDialog()) {
							mConfirmBookButton.setText(R.string.confirm_book);
						}

						// Reveal the charge lock icon
						if (mChargeDetailsImageView != null) {
							mChargeDetailsImageView.setVisibility(View.VISIBLE);
						}

						// Add the charge details text
						if (mChargeDetailsTextView != null) {
							CharSequence text = getString(R.string.charge_details_template, Db.getSelectedRate()
									.getTotalAmountAfterTax()
									.getFormattedMoney());
							mChargeDetailsTextView.setText(text);
						}

						mFormHasBeenFocused = true;
					}
				}
				else {
					saveBillingInfo();

					checkSectionsCompleted(true);
				}
			}
		};

		mFirstNameEditText.setOnFocusChangeListener(l);
		mLastNameEditText.setOnFocusChangeListener(l);
		mTelephoneCountryCodeSpinner.setOnFocusChangeListener(l);
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

		if (mConfirmBookButton != null) {
			mConfirmBookButton.setOnFocusChangeListener(l);
		}
	}

	/**
	 * This could be used from the internal InputValidation errors, 
	 * or by the results of an E3 "checkout" call.
	 * @param errors
	 */
	public void handleFormErrors(List<ValidationError> errors) {
		// If the user hasn't even focused the form yet, don't innundate them with 
		// a ton of error messages. Instead just push them towards the first
		// invalid field.
		if (mFormHasBeenFocused) {
			for (ValidationError error : errors) {
				mErrorHandler.handleError(error);
			}
		}

		// Request focus on the first field that was invalid
		View firstErrorView = (View) errors.get(0).getObject();
		if (firstErrorView == mRulesRestrictionsCheckbox) {
			focusRulesRestrictions();
		}
		else {
			BookingInfoUtils.focusAndOpenKeyboard(getActivity(), (View) errors.get(0).getObject());
		}
	}

	private void expandGuestsForm(boolean animateAndFocus) {
		if (!mGuestsExpanded) {
			mGuestsExpanded = true;

			mGuestSavedLayout.setVisibility(View.GONE);
			mGuestFormLayout.setVisibility(View.VISIBLE);

			// Fix focus movement
			fixFocus();

			if (animateAndFocus) {
				BookingInfoUtils.focusAndOpenKeyboard(getActivity(), mFirstNameEditText);
			}

			// TODO: Animation if animated
		}
	}

	private void collapseGuestsForm() {
		if (mGuestsExpanded) {
			mGuestsExpanded = false;
			mGuestSavedLayout.setVisibility(View.VISIBLE);
			mGuestFormLayout.setVisibility(View.GONE);

			fixFocus();
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
				BookingInfoUtils.focusAndOpenKeyboard(getActivity(), mAddress1EditText);
			}

			// TODO: Animation if animated
		}
	}

	private void collapseBillingForm() {
		if (mBillingExpanded) {
			mBillingExpanded = false;

			mBillingSavedLayout.setVisibility(View.VISIBLE);
			mBillingFormLayout.setVisibility(View.GONE);

			fixFocus();
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

	private void setSpinnerSelection(Spinner spinner, String target) {
		final int position = findAdapterIndex(spinner.getAdapter(), target);
		if (!(spinner instanceof TelephoneSpinner)) {
			mSelectedCountryPosition = position;
		}
		spinner.setSelection(position);
	}

	private int findAdapterIndex(SpinnerAdapter adapter, String target) {
		boolean isTelephoneAdapter = (adapter instanceof TelephoneSpinnerAdapter);

		int numItems = adapter.getCount();
		for (int n = 0; n < numItems; n++) {
			String name = isTelephoneAdapter ? ((TelephoneSpinnerAdapter) adapter).getCountryName(n) : (String) adapter
					.getItem(n);

			if (name.equalsIgnoreCase(target)) {
				return n;
			}
		}
		return -1;
	}

	private void setSpinnerSelection(Spinner spinner, String[] codes, String targetCode) {
		for (int n = 0; n < codes.length; n++) {
			if (targetCode.equals(codes[n])) {
				if (!(spinner instanceof TelephoneSpinner)) {
					mSelectedCountryPosition = n;
				}
				spinner.setSelection(n);
				return;
			}
		}
	}

	// Focusing the rules & restrictions is special for two reasons:
	// 1. It needs to focus the containing layout, so users can view the entire rules & restrictions.
	// 2. It doesn't need to open the soft keyboard.
	private void focusRulesRestrictions() {
		mScrollView.requestChildFocus(mRulesRestrictionsLayout, mRulesRestrictionsLayout);
		mScrollView.scrollBy(0, (int) getResources().getDisplayMetrics().density * 15);
	}

	private boolean loadSavedBillingInfo() {
		// Attempt to load the saved billing info
		// TODO: revisit this whole section
		if (Db.loadBillingInfo(getActivity())) {

			BillingInfo billingInfo = Db.getBillingInfo();

			// When upgrading from 1.2.1 to 1.3, country code isn't present. So let's just use the default country.
			if (billingInfo.getTelephoneCountryCode() == null) {

				Resources r = getResources();
				String[] countryCodes = r.getStringArray(R.array.country_codes);
				String[] countryNames = r.getStringArray(R.array.country_names);
				int[] countryPhoneCodes = r.getIntArray(R.array.country_phone_codes);

				String defaultCountryName = getString(LocaleUtils.getDefaultCountryResId(getActivity()));

				for (int n = 0; n < countryCodes.length; n++) {
					if (defaultCountryName.equals(countryNames[n])) {
						billingInfo.setTelephoneCountry(countryCodes[n]);
						billingInfo.setTelephoneCountryCode(Integer.toString(countryPhoneCodes[n]));
						break;
					}
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Syncs the local BillingInfo with data from the form fields.  Should be used before you want to access
	 * the local BillingInfo's data.
	 */
	private void syncBillingInfo() {
		// Start off with a clean slate
		BillingInfo billingInfo = Db.resetBillingInfo();

		billingInfo.setFirstName(mFirstNameEditText.getText().toString());
		billingInfo.setLastName(mLastNameEditText.getText().toString());
		billingInfo.setTelephoneCountryCode(mTelephoneCountryCodeSpinner.getSelectedTelephoneCountryCode() + "");
		billingInfo.setTelephoneCountry(mTelephoneCountryCodeSpinner.getSelectedTelephoneCountry());
		billingInfo.setTelephone(mTelephoneEditText.getText().toString());
		billingInfo.setEmail(mEmailEditText.getText().toString());

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
		billingInfo.setLocation(location);

		billingInfo.setNumber(mCardNumberEditText.getText().toString());
		String expirationMonth = mExpirationMonthEditText.getText().toString();
		String expirationYear = mExpirationYearEditText.getText().toString();
		if (expirationMonth != null && expirationMonth.length() > 0 && expirationYear != null
				&& expirationYear.length() > 0) {
			Calendar cal = new GregorianCalendar(Integer.parseInt(expirationYear) + 2000,
					Integer.parseInt(expirationMonth) - 1, 15);
			billingInfo.setExpirationDate(cal);
		}
		billingInfo.setSecurityCode(mSecurityCodeEditText.getText().toString());

		if (mCreditCardType != null) {
			billingInfo.setBrandCode(mCreditCardType.getCode());
		}
	}

	/**
	 * Syncs the form fields with data from local BillingInfo.  Should be used when creating or
	 * restoring the Activity.
	 */
	private void syncFormFieldsFromBillingInfo(View view) {
		BillingInfo billingInfo;
		if (mUserProfileIsFresh) {
			billingInfo = Db.getUser().toBillingInfo();
		}
		else {
			billingInfo = Db.getBillingInfo();
		}

		// Sync the saved guest fields
		String firstName = billingInfo.getFirstName();
		String lastName = billingInfo.getLastName();
		if (firstName != null && lastName != null) {
			TextView fullNameView = (TextView) view.findViewById(R.id.full_name_text_view);
			fullNameView.setText(firstName + " " + lastName);
		}

		TextView telephoneView = (TextView) view.findViewById(R.id.telephone_text_view);
		String telephoneCountryCode = billingInfo.getTelephoneCountryCode();
		StringBuilder telephoneString = new StringBuilder();
		if (telephoneCountryCode != null) {
			if (telephoneCountryCode.startsWith("+")) {
				telephoneString.append(telephoneCountryCode + " ");
			}
			else {
				telephoneString.append("+" + telephoneCountryCode + " ");
			}
		}
		if (billingInfo.getTelephone() != null) {
			telephoneString.append(billingInfo.getTelephone());
		}
		telephoneView.setText(telephoneString.toString());

		TextView emailView = (TextView) view.findViewById(R.id.email_text_view);
		emailView.setText(billingInfo.getEmail());

		// Sync the editable guest fields
		mFirstNameEditText.setText(firstName);
		mLastNameEditText.setText(lastName);
		mTelephoneEditText.setText(billingInfo.getTelephone());
		mEmailEditText.setText(billingInfo.getEmail());

		// Sync the saved billing info fields
		String address = "";
		Location loc = billingInfo.getLocation();
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

			// Sync the telephone country code spinner
			SpinnerAdapter adapter = mTelephoneCountryCodeSpinner.getAdapter();
			int position = findAdapterIndex(adapter, billingInfo.getTelephoneCountry());
			if (position == -1) {
				position = findAdapterIndex(adapter, getString(LocaleUtils.getDefaultCountryResId(getActivity())));
			}
			if (position != -1) {
				mTelephoneCountryCodeSpinner.setSelection(position);
			}

			TextView addressView = (TextView) view.findViewById(R.id.address_text_view);
			addressView.setText(address);

			// Sync the editable billing info fields
			if (loc.getStreetAddress().size() > 0) {
				mAddress1EditText.setText(loc.getStreetAddress().get(0));
			}
			if (loc.getStreetAddress().size() > 1) {
				mAddress2EditText.setText(loc.getStreetAddress().get(1));
			}
			mCityEditText.setText(loc.getCity());
			mPostalCodeEditText.setText(loc.getPostalCode());
			setSpinnerSelection(mCountrySpinner, mCountryCodes, loc.getCountryCode());
			mStateEditText.setText(loc.getStateCode());

		}

		// Sync the editable credit card info fields
		mCardNumberEditText.setText(billingInfo.getNumber());
		Calendar cal = billingInfo.getExpirationDate();
		if (cal != null) {
			mExpirationMonthEditText.setText((billingInfo.getExpirationDate().get(Calendar.MONTH) + 1) + "");
			mExpirationYearEditText.setText((billingInfo.getExpirationDate().get(Calendar.YEAR) % 100) + "");
		}
		mSecurityCodeEditText.setText(billingInfo.getSecurityCode());

		if (mUserProfileIsFresh && Db.getUser().hasStoredCreditCards()) {
			// otherwise we want them to add a new CC anyways
			mCreditCardInfoContainer.setVisibility(View.GONE);
			mBillingSavedLayout.setVisibility(View.GONE);
			mBillingFormLayout.setVisibility(View.GONE);
			mStoredCardContainer.setVisibility(View.VISIBLE);
			mCardAdapter = new StoredCardSpinnerAdapter((Context) mActivity, Db.getUser().getStoredCreditCards());
			mStoredCardSpinner.setAdapter(mCardAdapter);
			mStoredCardSpinner.setOnItemSelectedListener(new StoredCardOnItemSelectedListener());
		}
		else {
			mCreditCardInfoContainer.setVisibility(View.VISIBLE);
			if (!checkAddressCompleted()) {
				expandBillingForm(false);
			}
			else {
				collapseBillingForm();
			}
			mStoredCardContainer.setVisibility(View.GONE);
		}
	}

	private boolean saveBillingInfo() {
		// Gather all the data to be saved
		syncBillingInfo();

		if (! ExpediaServices.isLoggedIn((Context) mActivity)) {
			// Save the hashed email, just for tracking purposes
			TrackingUtils.saveEmailForTracking(getActivity(), Db.getBillingInfo().getEmail());

			return Db.getBillingInfo().save(getActivity());
		}
		else {
			return true;
		}
	}

	public void clearBillingInfo() {
		final int countryResId = LocaleUtils.getDefaultCountryResId(getActivity());

		mFirstNameEditText.setText(null);
		mLastNameEditText.setText(null);
		setSpinnerSelection(mTelephoneCountryCodeSpinner, getString(countryResId));
		mTelephoneEditText.setText(null);
		mEmailEditText.setText(null);
		mAddress1EditText.setText(null);
		mAddress2EditText.setText(null);
		mCityEditText.setText(null);
		mPostalCodeEditText.setText(null);
		mStateEditText.setText(null);
		setSpinnerSelection(mCountrySpinner, getString(countryResId));
		mCardNumberEditText.setText(null);
		mExpirationMonthEditText.setText(null);
		mExpirationYearEditText.setText(null);
		mSecurityCodeEditText.setText(null);
		mRulesRestrictionsCheckbox.setChecked(false);

		expandGuestsForm(false);
		expandBillingForm(false);
	}

	private void checkSectionsCompleted(boolean trackCompletion) {
		Context context = (trackCompletion) ? getActivity() : null;
		mBookingInfoValidation.checkBookingSectionsCompleted(mValidationProcessor, context);
	}

	private boolean checkAddressCompleted() {
		return mAddressValidationProcessor.validate().size() == 0;
	}

	private void dismissKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	private final Download<SignInResponse> mLoginDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices((Context) mActivity);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SIGNIN, services);
			return services.signIn();
		}
	};

	private final OnDownloadComplete<SignInResponse> mLoginCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse response) {
			if (response == null) {
				// TODO: error
			}
			else if (response.hasErrors()) {
				// TODO: error
			}
			else {
				mUserProfileIsFresh = true;
				Db.setUser(response.getUser());
				mAccountButton.update(false);
				syncFormFieldsFromBillingInfo(mRootBillingView);
				syncBillingInfo();
			}
		}
	};

	private final AccountButtonClickListener mAccountButtonClickListener = new AccountButtonClickListener() {
		public void accountLoginClicked() {
			((SignInFragment.SignInFragmentListener) mActivity).onLoginStarted();
		}

		public void accountLogoutClicked() {
			mUserProfileIsFresh = false;
			ExpediaServices services = new ExpediaServices((Context) mActivity);
			services.signOut();
			mAccountButton.update(false);
			Db.resetBillingInfo();
			Db.getBillingInfo().save(getActivity());
			clearBillingInfo();
			syncFormFieldsFromBillingInfo(mRootBillingView);
		}
	};

	public void loginCompleted() {
		mUserProfileIsFresh = true;
		mAccountButton.update(false);
		syncFormFieldsFromBillingInfo(mRootBillingView);
		syncBillingInfo();
		if (!mBookingInfoValidation.isGuestsSectionCompleted()) {
			expandGuestsForm(false);
		}
		else {
			collapseGuestsForm();
		}
	}

	public class StoredCardOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			mCardAdapter.setSelected(pos);
		}
		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface BookingFormFragmentListener {
		public void onCheckout();
	}
}
