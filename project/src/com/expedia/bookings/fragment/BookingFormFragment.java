package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.BookingFragmentActivity;
import com.expedia.bookings.activity.BookingFragmentActivity.InstanceFragment;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.RoomTypeFragmentHandler;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.validation.PatternValidator.EmailValidator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.RequiredValidator;
import com.mobiata.android.validation.TextViewErrorHandler;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.ValidationProcessor;
import com.mobiata.android.validation.Validator;

public class BookingFormFragment extends DialogFragment {

	public static BookingFormFragment newInstance() {
		BookingFormFragment dialog = new BookingFormFragment();
		return dialog;
	}

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
	private View mConfirmBookButton;
	private View mReceipt;
	private View mCloseFormButton;

	// Cached data from arrays
	private String[] mCountryCodes;

	// Cached views (non-interactive)
	private ImageView mCreditCardImageView;
	private TextView mSecurityCodeTipTextView;
	private TextView mRulesRestrictionsTextView;
	private CheckBox mRulesRestrictionsCheckbox;

	// Validation
	private ValidationProcessor mValidationProcessor;
	private TextViewErrorHandler mErrorHandler;

	// The data that the user has entered for billing info
	private CreditCardType mCreditCardType;

	// The state of the form
	private boolean mFormHasBeenFocused;
	private boolean mGuestsExpanded;
	private boolean mBillingExpanded;

	private RoomTypeFragmentHandler mRoomTypeHandler;

	// This is a tracking variable to solve a nasty problem.  The problem is that Spinner.onItemSelectedListener()
	// fires wildly when you set the Spinner's position manually (sometimes twice at a time).  We only want to track
	// when a user *explicitly* clicks on a new country.  What this does is keep track of what the system thinks
	// is the selected country - only the user can get this out of alignment, thus causing tracking.
	private int mSelectedCountryPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mValidationProcessor = new ValidationProcessor();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_booking_form, null);

		Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Fullscreen_Panel);
		dialog.requestWindowFeature(STYLE_NO_TITLE);
		dialog.setContentView(view);

		// Retrieve views that we need for the form fields
		mGuestSavedLayout = (ViewGroup) view.findViewById(R.id.saved_guest_info_layout);
		mGuestFormLayout = (ViewGroup) view.findViewById(R.id.guest_info_layout);
		mFirstNameEditText = (EditText) view.findViewById(R.id.first_name_edit_text);
		mLastNameEditText = (EditText) view.findViewById(R.id.last_name_edit_text);
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
		mCardNumberEditText = (EditText) view.findViewById(R.id.card_number_edit_text);
		mExpirationMonthEditText = (EditText) view.findViewById(R.id.expiration_month_edit_text);
		mExpirationYearEditText = (EditText) view.findViewById(R.id.expiration_year_edit_text);
		mSecurityCodeEditText = (EditText) view.findViewById(R.id.security_code_edit_text);
		mCreditCardImageView = (ImageView) view.findViewById(R.id.credit_card_image_view);
		mSecurityCodeTipTextView = (TextView) view.findViewById(R.id.security_code_tip_text_view);
		mRulesRestrictionsCheckbox = (CheckBox) view.findViewById(R.id.rules_restrictions_checkbox);
		mRulesRestrictionsTextView = (TextView) view.findViewById(R.id.rules_restrictions_text_view);
		mConfirmBookButton = view.findViewById(R.id.confirm_book_button);
		mReceipt = view.findViewById(R.id.receipt);
		mCloseFormButton = view.findViewById(R.id.close_booking_form);

		InstanceFragment instance = getInstance();

		// Retrieve some data we keep using
		Resources r = getResources();
		mCountryCodes = r.getStringArray(R.array.country_codes);
		BookingInfoValidation bookingInfoValidation = instance.mBookingInfoValidation;
		configureForm();
		
		if (getBillingInfo().doesExistOnDisk()) {
			syncFormFields(view);

			bookingInfoValidation.checkBookingSectionsCompleted(mValidationProcessor);

			if (!bookingInfoValidation.isGuestsSectionCompleted()) {
				expandGuestsForm(false);
			}

			if (!bookingInfoValidation.isBillingSectionCompleted()) {
				expandBillingForm(false);
			}

		}
		else {
			expandGuestsForm(false);
			expandBillingForm(false);
		}
		mFormHasBeenFocused = false;

		dialog.setCanceledOnTouchOutside(false);

		mRoomTypeHandler = new RoomTypeFragmentHandler(getActivity(), mReceipt, instance.mProperty,
				instance.mSearchParams, instance.mRate);
		mRoomTypeHandler.onCreate(savedInstanceState);

		// set the window of the dialog to have a transparent background
		// so that the window is not visible through the edges of the dialog.
		ColorDrawable drawable = new ColorDrawable(0);
		dialog.getWindow().setBackgroundDrawable(drawable);
		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		return dialog;
	}

	@Override
	public void onResume() {
		super.onResume();
		configureTicket(mReceipt);
		mRoomTypeHandler.updateRoomDetails(getInstance().mRate);
	}

	@Override
	public void onAttach(Activity activity) {
		if (mRoomTypeHandler != null) {
			mRoomTypeHandler.onAttach();
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		mRoomTypeHandler.onDetach();
		super.onDetach();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mRoomTypeHandler.saveToBundle(outState);
	}

	private void configureTicket(View receipt) {
		InstanceFragment instance = getInstance();
		ViewGroup detailsLayout = (ViewGroup) receipt.findViewById(R.id.details_layout);
		detailsLayout.removeAllViews();
		BookingReceiptUtils.configureTicket(getActivity(), receipt, instance.mProperty, instance.mSearchParams,
				instance.mRate, mRoomTypeHandler);
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
		if (RulesRestrictionsUtils.requiresRulesRestrictionsCheckbox()) {
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

		mConfirmBookButton.setOnClickListener(new OnClickListener() {

			@Override
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
						BookingInfoUtils.focusAndOpenKeyboard(getActivity(), firstErrorView);
					}
					return;
				}

				if (numErrors > 0) {
					for (ValidationError error : errors) {
						mErrorHandler.handleError(error);
					}

					BookingInfoUtils.focusAndOpenKeyboard(getActivity(), (View) errors.get(0).getObject());
				}
				else {
					BookingInfoUtils.onClickSubmit(getActivity());
					((BookingFragmentActivity) getActivity()).bookingCompleted();
					dismiss();
				}

			}
		});

		mCloseFormButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				saveBillingInfo();
				getInstance().mBookingInfoValidation.checkBookingSectionsCompleted(mValidationProcessor);
				dismiss();
			}
		});

		// Setup the correct text (and link enabling) on the terms & conditions textview
		mRulesRestrictionsTextView.setText(RulesRestrictionsUtils.getRulesRestrictionsConfirmation(getActivity()));
		mRulesRestrictionsTextView.setMovementMethod(LinkMovementMethod.getInstance());

		// Configure form validation
		// Setup validators and error handlers
		final String userCurrency = CurrencyUtils.getCurrencyCode(getActivity());
		TextViewValidator requiredFieldValidator = new TextViewValidator();
		Validator<TextView> usValidator = new Validator<TextView>() {
			public int validate(TextView obj) {
				if (getBillingInfo().getLocation().getCountryCode().equals("US")) {
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

		// Setup a focus change listener that changes the bottom from "enter booking info"
		// to "confirm & book", plus the text
		OnFocusChangeListener l = new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mFormHasBeenFocused = true;
				}
				else {
					saveBillingInfo();

					getInstance().mBookingInfoValidation.checkBookingSectionsCompleted(mValidationProcessor);
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
		mConfirmBookButton.setOnFocusChangeListener(l);

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

	/**
	 * Syncs the local BillingInfo with data from the form fields.  Should be used before you want to access
	 * the local BillingInfo's data.
	 */
	private void syncBillingInfo() {
		// Start off with a clean slate
		BillingInfo billingInfo = getInstance().mBillingInfo = new BillingInfo();

		billingInfo.setFirstName(mFirstNameEditText.getText().toString());
		billingInfo.setLastName(mLastNameEditText.getText().toString());
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
	private void syncFormFields(View view) {
		BillingInfo billingInfo = getBillingInfo();
		
		// Sync the saved guest fields
		String firstName = billingInfo.getFirstName();
		String lastName = billingInfo.getLastName();
		if (firstName != null && lastName != null) {
			TextView fullNameView = (TextView) view.findViewById(R.id.full_name_text_view);
			fullNameView.setText(firstName + " " + lastName);
		}

		TextView telephoneView = (TextView) view.findViewById(R.id.telephone_text_view);
		telephoneView.setText(billingInfo.getTelephone());

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

			TextView addressView = (TextView) view.findViewById(R.id.address_text_view);
			addressView.setText(address);

			// Sync the editable billing info fields
			mAddress1EditText.setText(loc.getStreetAddress().get(0));
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
	}

	private boolean saveBillingInfo() {
		// Gather all the data to be saved
		syncBillingInfo();

		// Save the hashed email, just for tracking purposes
		TrackingUtils.saveEmailForTracking(getActivity(), getBillingInfo().getEmail());

		return getBillingInfo().save(getActivity());
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public BookingFragmentActivity.InstanceFragment getInstance() {
		return ((BookingFragmentActivity) getActivity()).mInstance;
	}
	
	public BillingInfo getBillingInfo() {
		return getInstance().mBillingInfo;
	}
}