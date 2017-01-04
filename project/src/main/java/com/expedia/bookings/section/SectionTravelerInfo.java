package com.expedia.bookings.section;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.section.CountrySpinnerAdapter.CountryDisplayType;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TelephoneSpinner;
import com.expedia.bookings.widget.TelephoneSpinnerAdapter;
import com.mobiata.android.Log;
import com.mobiata.android.validation.MultiValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public class SectionTravelerInfo extends LinearLayout implements ISection<Traveler>,
	ISectionEditable, InvalidCharacterListener {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	SectionFieldList<Traveler> mFields = new SectionFieldList<Traveler>();

	Context mContext;

	private Traveler mTraveler;

	private FlightSearchParams mFlightSearchParams;

	boolean mAutoChoosePassportCountry = true;

	public SectionTravelerInfo(Context context) {
		super(context);
		init(context);
	}

	public SectionTravelerInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@SuppressLint("NewApi")
	public SectionTravelerInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		//Display fields
		mFields.add(mDisplayFullName);
		mFields.add(mDisplayPassportCountry);
		mFields.add(mDisplaySpecialAssistance);
		mFields.add(mDisplayPhoneNumberWithCountryCode);
		mFields.add(mDisplaySpeatPreference);
		mFields.add(mDisplayLongFormBirthDay);
		mFields.add(mDisplayCurrentTravelerWithNameColored);
		mFields.add(mDisplayEmailDisclaimer);

		//Validation Indicator fields
		mFields.add(mValidFirstName);
		mFields.add(mValidMiddleName);
		mFields.add(mValidLastName);
		mFields.add(mValidPhoneNumber);
		mFields.add(mValidEmail);
		mFields.add(mValidDateOfBirth);
		mFields.add(mValidRedressNumber);

		//Edit fields
		mFields.add(mEditFirstName);
		mFields.add(mEditMiddleName);
		mFields.add(mEditLastName);
		mFields.add(mEditPhoneNumberCountryCodeSpinner);
		mFields.add(mEditPhoneNumber);
		mFields.add(mEditEmailAddress);
		mFields.add(mEditBirthDateTextBtn);
		mFields.add(mEditRedressNumber);
		mFields.add(mEditGenderSpinner);
		mFields.add(mEditPassportCountrySpinner);
		mFields.add(mEditPassportCountryListView);
		mFields.add(mEditAssistancePreferenceSpinner);
		mFields.add(mEditSeatPreferenceSpinner);
	}

	public void setViewEnabled(int viewId, boolean enabled) {

	}

	@Override
	public void onFinishInflate() {
		preFinishInflate();

		super.onFinishInflate();

		mFields.bindFieldsAll(this);
		postFinishInflate();
	}

	@Override
	public void bind(Traveler traveler) {
		//Update fields
		mTraveler = traveler;

		if (mTraveler != null) {
			mFields.bindDataAll(traveler);
		}
	}

	public void bind(Traveler traveler, int travelerIndex) {
		setPhoneFieldsEnabled(travelerIndex);
		bind(traveler);
	}

	public void bind(Traveler traveler, FlightSearchParams params) {
		mFlightSearchParams = params;
		bind(traveler);
	}

	public void bind(Traveler traveler, int travelerIndex, FlightSearchParams params) {
		mFlightSearchParams = params;
		bind(traveler, travelerIndex);
	}

	protected void preFinishInflate() {
		//Load the pos specific name edit fields if we have the container for them.
		ViewGroup nameContainer = Ui.findView(this, R.id.edit_names_container);
		if (nameContainer != null) {
			PointOfSale pos = PointOfSale.getPointOfSale();
			if (pos.showLastNameFirst()) {
				View.inflate(mContext, R.layout.include_edit_traveler_names_reversed, nameContainer);
			}
			else {
				View.inflate(mContext, R.layout.include_edit_traveler_names, nameContainer);
			}
		}
	}

	protected void postFinishInflate() {
		removeFieldsForPos();
		removeFieldsForLoggedIn();
	}

	//If we have the middle name field, but it isnt supported by this pos
	//then we hide it and remove it from our list of fields
	private void removeFieldsForPos() {
		PointOfSale pos = PointOfSale.getPointOfSale();
		if (pos.hideMiddleName()) {
			mFields.removeField(mEditMiddleName);
		}
	}

	// Remove email fields if user is logged in
	private void removeFieldsForLoggedIn() {
		if (User.isLoggedIn(mContext)) {
			mFields.removeField(mEditEmailAddress);
			mFields.removeField(mDisplayEmailDisclaimer);
		}
	}

	public void setPhoneFieldsEnabled(int travelerIndex) {
		boolean enabled = travelerIndex == 0;
		mFields.setFieldEnabled(mEditPhoneNumber, enabled);
		mFields.setFieldEnabled(mEditPhoneNumberCountryCodeSpinner, enabled);
		mFields.setFieldEnabled(mDisplayPhoneNumberWithCountryCode, enabled);
		setPhoneContainerVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	public void refreshOnLoginStatusChange() {
		if (User.isLoggedIn(mContext)) {
			mFields.removeField(mEditEmailAddress);
		}
		else {
			mFields.setFieldEnabled(mEditEmailAddress, true);
		}
		onChange();
	}

	private void setPhoneContainerVisibility(int visibility) {
		View container = Ui.findView(this, R.id.phone_edit_container);
		if (container != null) {
			container.setVisibility(visibility);
		}
	}

	public boolean performValidation() {
		return mFields.hasValidInput();
	}

	@Override
	public void onChange() {
		for (SectionChangeListener listener : mChangeListeners) {
			listener.onChange();
		}
	}

	@Override
	public void addChangeListener(SectionChangeListener listener) {
		mChangeListeners.add(listener);
	}

	@Override
	public void removeChangeListener(SectionChangeListener listener) {
		mChangeListeners.remove(listener);
	}

	@Override
	public void clearChangeListeners() {
		mChangeListeners.clear();
	}

	/**
	 * Helper for validation purposes, this must be called before bind
	 *
	 * @param enabled
	 */
	public void setAutoChoosePassportCountryEnabled(boolean enabled) {
		mAutoChoosePassportCountry = enabled;
	}

	public String phoneToStringHelper(Phone phone) {
		if (phone != null) {
			String number = phone.getNumber() == null ? "" : phone.getNumber();
			return number;
		}
		return "";
	}

	public Traveler getTraveler() {
		return mTraveler;
	}

	//////////////////////////////////////
	//////INVALID CHARACTER STUFF
	//////////////////////////////////////

	ArrayList<InvalidCharacterListener> mInvalidCharacterListeners = new ArrayList<InvalidCharacterListener>();

	@Override
	public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
		for (InvalidCharacterListener listener : mInvalidCharacterListeners) {
			listener.onInvalidCharacterEntered(text, mode);
		}
	}

	public void addInvalidCharacterListener(InvalidCharacterListener listener) {
		mInvalidCharacterListeners.add(listener);
	}

	public void removeInvalidCharacterListener(InvalidCharacterListener listener) {
		mInvalidCharacterListeners.remove(listener);
	}

	//////////////////////////////////////
	////// VALIDATION INDICATOR FIELDS
	//////////////////////////////////////

	/**
	 * This will set all validators to their valid state
	 */
	public void resetValidation() {
		mFields.setValidationIndicatorState(true);
	}

	ValidationIndicatorExclaimation<Traveler> mValidFirstName = new ValidationIndicatorExclaimation<Traveler>(
		R.id.edit_first_name);

	ValidationIndicatorExclaimation<Traveler> mValidMiddleName = new ValidationIndicatorExclaimation<Traveler>(
		R.id.edit_middle_name);
	ValidationIndicatorExclaimation<Traveler> mValidLastName = new ValidationIndicatorExclaimation<Traveler>(
		R.id.edit_last_name);
	ValidationIndicatorExclaimation<Traveler> mValidPhoneNumber = new ValidationIndicatorExclaimation<Traveler>(
		R.id.edit_phone_number);
	ValidationIndicatorExclaimation<Traveler> mValidDateOfBirth = new ValidationIndicatorExclaimation<Traveler>(
		R.id.edit_birth_date_text_btn);
	ValidationIndicatorExclaimation<Traveler> mValidEmail = new ValidationIndicatorExclaimation<Traveler>(
		R.id.edit_email_address);
	ValidationIndicatorExclaimation<Traveler> mValidRedressNumber = new ValidationIndicatorExclaimation<Traveler>(
		R.id.edit_redress_number);

	//////////////////////////////////////
	////// DISPLAY FIELDS
	//////////////////////////////////////

	SectionField<TextView, Traveler> mDisplayFullName = new SectionField<TextView, Traveler>(
		R.id.display_full_name) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			String fullNameStr = "";
			fullNameStr += (data.getFirstName() != null) ? data.getFirstName() + " " : "";
			fullNameStr += (data.getMiddleName() != null) ? data.getMiddleName() + " " : "";
			fullNameStr += (data.getLastName() != null) ? data.getLastName() + " " : "";
			fullNameStr = fullNameStr.trim();
			fullNameStr = fullNameStr.replaceAll("\\s+", " ");//we only want one space character at a time

			if (!TextUtils.isEmpty(fullNameStr)) {
				field.setText(fullNameStr);
			}
		}
	};

	SectionField<TextView, Traveler> mDisplayCurrentTravelerWithNameColored = new SectionField<TextView, Traveler>(
		R.id.display_current_traveler_with_name_colored) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			if (data.hasName()) {
				String formatStr = mContext.getString(R.string.current_traveler_TEMPLATE);
				String formatted = String.format(formatStr, data.getFirstName().trim(), data.getLastName().trim());
				SpannableString stringToSpan = new SpannableString(formatted);
				int color = mContext.getResources().getColor(R.color.checkout_card_brand_color);
				Ui.setTextStyleNormalText(stringToSpan, color, 0, formatted.indexOf(data.getFirstName()));

				field.setText(stringToSpan);
			}
			else {
				field.setText("");
			}
		}
	};

	SectionField<TextView, Traveler> mDisplayPhoneNumberWithCountryCode = new SectionField<TextView, Traveler>(
		R.id.display_phone_number_with_country_code) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			String formatStr = mContext.getResources().getString(R.string.phone_number_with_country_code_TEMPLATE);
			String number = phoneToStringHelper(data.getOrCreatePrimaryPhoneNumber());
			String retStr = String.format(formatStr,
				data.getPhoneCountryCode() == null ? "" : data.getPhoneCountryCode(),
				number.trim().compareToIgnoreCase("") == 0 ? "" : PhoneNumberUtils.formatNumber(number));

			field.setText(retStr);
		}
	};

	SectionField<TextView, Traveler> mDisplayLongFormBirthDay = new SectionField<TextView, Traveler>(
		R.id.display_born_on) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			if (data.getBirthDate() != null) {
				String formatStr = mContext.getString(R.string.born_on_TEMPLATE);
				String bdayStr = JodaUtils.formatLocalDate(mContext, data.getBirthDate(),
					DateFormatUtils.FLAGS_MEDIUM_DATE_FORMAT);
				String bornStr = String.format(formatStr, bdayStr);
				field.setText(bornStr);
			}
			else {
				field.setText("");
			}
		}
	};

	SectionField<TextView, Traveler> mDisplayPassportCountry = new SectionField<TextView, Traveler>(
		R.id.display_passport_country) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			if (TextUtils.isEmpty(data.getPrimaryPassportCountry())) {
				field.setText("");
			}
			else { // set primary passport country
				CountrySpinnerAdapter adapter = new CountrySpinnerAdapter(getContext(), CountryDisplayType.FULL_NAME);
				int pos = adapter.getPositionByCountryThreeLetterCode(data.getPrimaryPassportCountry());
				field.setText(adapter.getItemValue(pos, CountryDisplayType.FULL_NAME));
			}
			onChange();
		}
	};

	SectionField<TextView, Traveler> mDisplaySpecialAssistance = new SectionField<TextView, Traveler>(
		R.id.display_special_assistance) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			String template = mContext.getString(R.string.special_assistance_label_TEMPLATE);
			String val = String.format(template, data.getAssistanceString(mContext));
			field.setText(val);
		}
	};

	SectionField<TextView, Traveler> mDisplaySpeatPreference = new SectionField<TextView, Traveler>(
		R.id.display_seat_preference) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			String template = mContext.getString(R.string.prefers_seat_TEMPLATE);
			String val = String.format(template, data.getSeatPreferenceString(mContext));
			field.setText(val);
		}
	};

	SectionField<TextView, Traveler> mDisplayEmailDisclaimer = new SectionField<TextView, Traveler>(
		R.id.email_disclaimer) {
		@Override
		public void onHasFieldAndData(TextView field, Traveler data) {
			field.setText(R.string.email_disclaimer);
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, Traveler> mEditFirstName = new SectionFieldEditableFocusChangeTrimmer<EditText, Traveler>(
		R.id.edit_first_name) {
		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> nameValidators = new MultiValidator<EditText>();
			nameValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES);
			nameValidators.addValidator(CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET);
			return nameValidators;
		}

		@Override
		public void setChangeListener(EditText field) {

			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setFirstName(s.toString());
					}
					onChange(SectionTravelerInfo.this);
				}
			});

			InvalidCharacterHelper
				.generateInvalidCharacterTextWatcher(field, SectionTravelerInfo.this, Mode.NAME);
		}

		@Override
		protected void onHasFieldAndData(EditText field, Traveler data) {
			field.setText(data.getFirstName());
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			retArr.add(mValidFirstName);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Traveler> mEditMiddleName = new SectionFieldEditableFocusChangeTrimmer<EditText, Traveler>(
		R.id.edit_middle_name) {
		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> nameValidators = new MultiValidator<EditText>();
			nameValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES);
			nameValidators.addValidator(CommonSectionValidators.ALWAYS_VALID_VALIDATOR_ET);
			return nameValidators;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setMiddleName(s.toString());
					}
					onChange(SectionTravelerInfo.this);
				}
			});

			InvalidCharacterHelper
				.generateInvalidCharacterTextWatcher(field, SectionTravelerInfo.this, Mode.NAME);
		}

		@Override
		protected void onHasFieldAndData(EditText field, Traveler data) {
			field.setText(data.getMiddleName());
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			retArr.add(mValidMiddleName);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Traveler> mEditLastName = new SectionFieldEditableFocusChangeTrimmer<EditText, Traveler>(
		R.id.edit_last_name) {

		Validator<EditText> mValidator = new Validator<EditText>() {
			@Override
			public int validate(EditText obj) {
				if (obj == null) {
					return ValidationError.ERROR_DATA_MISSING;
				}
				else {
					String text = obj.getText().toString();
					if (text.length() < 2) {
						return ValidationError.ERROR_DATA_INVALID;
					}
				}
				return ValidationError.NO_ERROR;
			}
		};

		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> nameValidators = new MultiValidator<EditText>();
			nameValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES);
			nameValidators.addValidator(mValidator);
			return nameValidators;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setLastName(s.toString());
					}
					onChange(SectionTravelerInfo.this);
				}
			});

			InvalidCharacterHelper
				.generateInvalidCharacterTextWatcher(field, SectionTravelerInfo.this, Mode.NAME);
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			retArr.add(mValidLastName);
			return retArr;
		}

		@Override
		protected void onHasFieldAndData(EditText field, Traveler data) {
			field.setText(data.getLastName());
		}
	};

	SectionFieldEditable<EditText, Traveler> mEditEmailAddress = new SectionFieldEditableFocusChangeTrimmer<EditText, Traveler>(
		R.id.edit_email_address) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setEmail(s.toString());
					}
					onChange(SectionTravelerInfo.this);
				}
			});

			InvalidCharacterHelper
				.generateInvalidCharacterTextWatcher(field, SectionTravelerInfo.this, Mode.EMAIL);
		}

		@Override
		protected void onHasFieldAndData(EditText field, Traveler data) {
			if (!TextUtils.isEmpty(data.getEmail())) {
				field.setText(data.getEmail());
			}
			else {
				field.setText("");
			}
		}

		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> emailValidators = new MultiValidator<EditText>();
			emailValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_ASCII);
			emailValidators.addValidator(CommonSectionValidators.EMAIL_VALIDATOR_STRICT);
			return emailValidators;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			retArr.add(mValidEmail);
			return retArr;
		}

	};

	/*
	 * This is our date picker DialogFragment. The coder has a responsibility to set setOnDateSetListener after any sort of creation event (including rotaiton)
	 *
	 * Note this DatePickerFragment uses a regular datePickerDialog which depends on Calendar objects, meaning that the OnDateSet call will return off by one months...
	 */
	public static class DatePickerFragment extends DialogFragment {
		private OnDateSetListener mListener = null;
		private int mDay;
		private int mMonth;//0 indexed like calendar
		private int mYear;

		private static final String DAY_TAG = "DAY_TAG";
		private static final String MONTH_TAG = "MONTH_TAG";
		private static final String YEAR_TAG = "YEAR_TAG";

		public static DatePickerFragment newInstance(LocalDate cal, OnDateSetListener listener) {
			DatePickerFragment frag = new DatePickerFragment();
			frag.setOnDateSetListener(listener);
			frag.setDate(cal.getDayOfMonth(), cal.getMonthOfYear() - 1, cal.getYear());
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		private void setDate(int day, int month, int year) {
			mDay = day;
			mMonth = month;
			mYear = year;
		}

		public void setOnDateSetListener(final OnDateSetListener listener) {
			//We chain the listener
			OnDateSetListener tListener = new OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					mDay = dayOfMonth;
					mMonth = monthOfYear;
					mYear = year;
					listener.onDateSet(view, year, monthOfYear, dayOfMonth);
				}
			};
			mListener = tListener;
		}

		@Override
		public void onDestroyView() {
			//This is a workaround for a rotation bug: http://code.google.com/p/android/issues/detail?id=17423
			if (getDialog() != null && getRetainInstance()) {
				getDialog().setDismissMessage(null);
			}
			super.onDestroyView();
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

		}

		@SuppressLint("NewApi")
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			setRetainInstance(true);

			if (savedInstanceState != null) {
				if (savedInstanceState.containsKey(DAY_TAG) && savedInstanceState.containsKey(MONTH_TAG)
					&& savedInstanceState.containsKey(YEAR_TAG)) {
					setDate(savedInstanceState.getInt(DAY_TAG), savedInstanceState.getInt(MONTH_TAG),
						savedInstanceState.getInt(YEAR_TAG));
				}
			}

			DatePickerDialog dialog = new DatePickerDialog(getActivity(), mListener, mYear, mMonth, mDay) {

				// The Compat lib has a bug that causes savedInstanceState to be null on old versions of android
				// Because we are retaining instance, we can set the dates in the onDateChanged function to fix this issue
				// however a bug in ICS causes super.onDateChanged() to unset the listener. Basically Pre ICS, ICS, And > JB all behave differently

				@Override
				public void onDateChanged(DatePicker view, int year, int month, int day) {
					super.onDateChanged(view, year, month, day);

					mYear = year;
					mMonth = month;
					mDay = day;
				}

				//old versions of onDateChanged call updateTitle (which we don't have access to)
				public void customUpdateTitle(int year, int month, int day) {
					//e.g. Tue, Apr 4, 1978
					LocalDate localDate = new LocalDate(year, month + 1, day);
					String formattedDate = JodaUtils.formatLocalDate(getContext(), localDate,
						DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
					);
					setTitle(formattedDate);
				}
			};

			dialog.updateDate(mYear, mMonth, mDay);

			//We set a max date for new apis, if we are stuck with an old api, they will be allowed to choose any date, but validation will fail
			dialog.getDatePicker().setMaxDate(DateTime.now().getMillis());

			return dialog;
		}

		@Override
		public void onSaveInstanceState(Bundle out) {

			super.onSaveInstanceState(out);

			out.putInt(DAY_TAG, mDay);
			out.putInt(MONTH_TAG, mMonth);
			out.putInt(YEAR_TAG, mYear);

		}
	}

	//This class is defined so that we can have both SectionFieldEditable and OnDateSetListener implemented in a single class
	abstract static class SectionFieldEditableWithDateChangeListener<FieldType extends View, Data extends Object>
		extends
		SectionFieldEditable<FieldType, Data> implements DatePickerDialog.OnDateSetListener {
		public SectionFieldEditableWithDateChangeListener(int fieldId) {
			super(fieldId);
		}
	}

	private FlightSearchParams getFlightSearchParams() {
		if (mFlightSearchParams == null) {
			throw new RuntimeException("Tried to get nonexistent FlightSearchParams. Should have been set in the bind() method!");
		}
		return mFlightSearchParams;
	}

	private boolean mIsBirthdateAligned = true;

	public boolean isBirthdateAligned() {
		return mIsBirthdateAligned;
	}

	SectionFieldEditable<TextView, Traveler> mEditBirthDateTextBtn = new SectionFieldEditableWithDateChangeListener<TextView, Traveler>(
		R.id.edit_birth_date_text_btn) {

		private final static String TAG_DATE_PICKER = "TAG_DATE_PICKER";

		@Override
		public void setChangeListener(TextView field) {
			//We are using a fragmentDialog so we need a fragmentActivity...
			if (mContext instanceof FragmentActivity) {
				final FragmentActivity fa = (FragmentActivity) mContext;
				final DatePickerDialog.OnDateSetListener listener = this;

				//If we already have created the fragment, we need to set the listener again
				DatePickerFragment datePickerFragment = Ui.findSupportFragment(fa, TAG_DATE_PICKER);
				if (datePickerFragment != null) {
					datePickerFragment.setOnDateSetListener(listener);
				}

				//Finally set the on click listener that shows the dialog
				field.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LocalDate date = new LocalDate(1970, 1, 1);
						if (hasBoundData()) {
							if (getData().getBirthDate() != null) {
								date = getData().getBirthDate();
							}
						}

						DatePickerFragment datePickerFragment = Ui.findSupportFragment(fa, TAG_DATE_PICKER);
						if (datePickerFragment == null) {
							datePickerFragment = DatePickerFragment.newInstance(date, listener);
						}
						datePickerFragment.show(fa.getSupportFragmentManager(), TAG_DATE_PICKER);
					}
				});

				field.addTextChangedListener(new AfterChangeTextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
						//Fixes rotation bug...
						onChange(SectionTravelerInfo.this);
					}
				});
			}
			else {
				Log.e(
					"The Birthday picker is expecting a FragmentActivity to be the context. In it's current state, this will do nohting if the context is not a FragmentActivity");
			}
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			monthOfYear++;//DatePicker uses calendars...
			if (hasBoundData()) {
				getData().setBirthDate(new LocalDate(year, monthOfYear, dayOfMonth));
			}
			refreshText();
			onChange(SectionTravelerInfo.this);
		}

		private void refreshText() {
			if (hasBoundField() && hasBoundData()) {
				onHasFieldAndData(getField(), getData());
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			retArr.add(mValidDateOfBirth);
			return retArr;
		}

		@Override
		protected void onHasFieldAndData(TextView field, Traveler data) {
			String btnTxt = "";
			Spannable stringToSpan = null;
			if (data.getBirthDate() != null) {
				String formatStr = mContext.getString(R.string.born_on_colored_TEMPLATE);
				String bdayStr = JodaUtils.formatLocalDate(mContext, data.getBirthDate(),
					DateFormatUtils.FLAGS_MEDIUM_DATE_FORMAT);
				btnTxt = String.format(formatStr, bdayStr);
				stringToSpan = new SpannableString(btnTxt);
				int color = mContext.getResources().getColor(R.color.checkout_traveler_birth_color);
				Ui.setTextStyleNormalText(stringToSpan, color, 0, btnTxt.indexOf(bdayStr));

			}
			field.setText(stringToSpan != null ? stringToSpan : btnTxt);
		}

		Validator<TextView> mValidator = new Validator<TextView>() {

			@Override
			public int validate(TextView obj) {
				int retVal = ValidationError.NO_ERROR;
				if (hasBoundData()) {
					if (getData().getBirthDate() != null) {
						LocalDate birthDate = getData().getBirthDate();
						FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
						PassengerCategory passengerCategory = getData().getPassengerCategory(searchParams);
						if (birthDate.isAfter(LocalDate.now())) {
							retVal = ValidationError.ERROR_DATA_INVALID;
						}
						else if (!PassengerCategory.isDateWithinPassengerCategoryRange(birthDate, getFlightSearchParams(), passengerCategory)) {
							retVal = ValidationError.ERROR_DATA_INVALID;
							mIsBirthdateAligned = false;
						}
						else {
							retVal = ValidationError.NO_ERROR;
							mIsBirthdateAligned = true;
						}
					}
					else {
						retVal = ValidationError.ERROR_DATA_MISSING;
					}
				}
				else {
					retVal = ValidationError.ERROR_DATA_MISSING;
				}
				return retVal;
			}
		};

		@Override
		protected Validator<TextView> getValidator() {
			return mValidator;
		}

	};

	SectionFieldEditable<EditText, Traveler> mEditPhoneNumber = new SectionFieldEditableFocusChangeTrimmer<EditText, Traveler>(
		R.id.edit_phone_number) {
		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.TELEPHONE_NUMBER_VALIDATOR_ET;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						String numbersOnly = getNumbersOnly(s.toString());
						getData().getOrCreatePrimaryPhoneNumber().setNumber(numbersOnly);
					}
					onChange(SectionTravelerInfo.this);
				}
			});

			field.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						if (v instanceof EditText) {
							EditText f = (EditText) v;
							String number = phoneToStringHelper(getData().getOrCreatePrimaryPhoneNumber());
							f.setText(phoneNumberDisplayer(number));
						}
					}
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Traveler data) {
			field.setText(phoneNumberDisplayer(phoneToStringHelper(data.getOrCreatePrimaryPhoneNumber())));
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			retArr.add(mValidPhoneNumber);
			return retArr;
		}

		private String phoneNumberDisplayer(String phoneNumString) {
			if (phoneNumString != null) {
				String numbersOnly = getNumbersOnly(phoneNumString);
				return PhoneNumberUtils.formatNumber(numbersOnly);
			}
			return phoneNumString;

		}

		private String getNumbersOnly(String input) {
			if (input != null) {
				return input.replaceAll("\\D", "");
			}
			return input;
		}
	};

	SectionFieldEditable<EditText, Traveler> mEditRedressNumber = new SectionFieldEditableFocusChangeTrimmer<EditText, Traveler>(
		R.id.edit_redress_number) {

		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.ALWAYS_VALID_VALIDATOR_ET;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setRedressNumber(s.toString());
					}
					onChange(SectionTravelerInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Traveler data) {
			field.setText(data.getRedressNumber());
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			retArr.add(mValidRedressNumber);
			return retArr;
		}
	};

	SectionFieldEditable<Spinner, Traveler> mEditPassportCountrySpinner = new SectionFieldEditable<Spinner, Traveler>(
		R.id.edit_passport_country_spinner) {

		private boolean mSetFieldManually = false;
		CountrySpinnerAdapter mCountryAdapter;
		Validator<Spinner> mValidator = new Validator<Spinner>() {
			@Override
			public int validate(Spinner obj) {
				if (getData() == null) {
					return ValidationError.ERROR_DATA_MISSING;
				}

				boolean hasMoreThanOnePassport = (getData().getPassportCountries().size() > 1);
				if (obj.getSelectedItemPosition() == 0 || getData().getPrimaryPassportCountry() == null) {
					return ValidationError.ERROR_DATA_MISSING;
				}
				else if (hasMoreThanOnePassport && !getData().isChangedPrimaryPassportCountry()) {
					// customer has more than one passport && they haven't selected a passport
					return ValidationError.ERROR_DATA_MISSING;
				}
				else {
					return ValidationError.NO_ERROR;
				}
			}
		};

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				mCountryAdapter = new CountrySpinnerAdapter(mContext, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME, android.R.layout.simple_list_item_1, android.R.layout.simple_list_item_1, true);
				getField().setAdapter(mCountryAdapter);
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (mCountryAdapter != null && getData() != null) {
						getData().setPrimaryPassportCountry(
							mCountryAdapter.getItemValue(position, CountryDisplayType.THREE_LETTER));
					}

					if (!mSetFieldManually) {
						onChange(SectionTravelerInfo.this);
					}
					else {
						mSetFieldManually = false;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, Traveler data) {
			getField().setSelection(0, false);
			onChange(SectionTravelerInfo.this);

			boolean travelerHasMultiplePassports = (data.getPassportCountries().size() > 1);
			if (!travelerHasMultiplePassports) {
				// only auto-populate passport country spinner/drop down when traveler has 1 or no stored passports

				if (mCountryAdapter != null && !TextUtils.isEmpty(data.getPrimaryPassportCountry())) {
					for (int i = 0; i < mCountryAdapter.getCount(); i++) {
						if (mCountryAdapter.getItemValue(i, CountryDisplayType.THREE_LETTER).equalsIgnoreCase(
							data.getPrimaryPassportCountry())) {
							getField().setSelection(i);
							break;
						}
					}
				}
				else if (mCountryAdapter != null && mAutoChoosePassportCountry) {
					int pos = mCountryAdapter.getDefaultLocalePosition();
					getField().setSelection(pos);
					getData().setPrimaryPassportCountry(mCountryAdapter.getItemValue(pos, CountryDisplayType.THREE_LETTER));
				}
			}
			else { // traveler has multiple passports
				boolean countrySelected = (field.getSelectedItemPosition() > 0);
				boolean dataAndFieldMatch = mCountryAdapter.getItemValue(field.getSelectedItemPosition(), CountryDisplayType.THREE_LETTER).equalsIgnoreCase(
					data.getPrimaryPassportCountry()) && countrySelected;

				if (!dataAndFieldMatch) {
					if (data.isChangedPrimaryPassportCountry() && (data.getPrimaryPassportCountry() != null)) {
						// use the primary passport country recently selected
						for (int i = 0; i < mCountryAdapter.getCount(); i++) {
							if (mCountryAdapter.getItemValue(i, CountryDisplayType.THREE_LETTER).equalsIgnoreCase(
								data.getPrimaryPassportCountry())) {
								getField().setSelection(i);
								break;
							}
						}
					}
					else { // reset drop down to 0
						getField().setSelection(0, false);
					}
				}
			}
		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<ListView, Traveler> mEditPassportCountryListView = new SectionFieldEditable<ListView, Traveler>(
		R.id.edit_passport_country_listview) {

		CountrySpinnerAdapter mCountryAdapter;

		Validator<ListView> mValidator = new Validator<ListView>() {
			@Override
			public int validate(ListView obj) {
				boolean hasMoreThanOnePassport = (getData().getPassportCountries().size() > 1);

				if (hasMoreThanOnePassport) {
					if (getData().isChangedPrimaryPassportCountry() && obj.getCheckedItemPosition() != ListView.INVALID_POSITION) {
						return ValidationError.NO_ERROR;
					}
					else {
						return ValidationError.ERROR_DATA_MISSING;
					}
				}
				else {
					if (obj.getCheckedItemPosition() != ListView.INVALID_POSITION) {
						return ValidationError.NO_ERROR;
					}
					else if (getData() != null && getData().getPrimaryPassportCountry() != null) {
						//referring to data instead of gui elements sort of breaks our paradigm, but meh
						return ValidationError.NO_ERROR;
					}
					else {
						return ValidationError.ERROR_DATA_MISSING;
					}
				}
			}
		};

		@Override
		protected Validator<ListView> getValidator() {
			return mValidator;
		}

		@Override
		protected void onFieldBind() {
			super.onFieldBind();

			mCountryAdapter = new CountrySpinnerAdapter(mContext, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
				android.R.layout.simple_list_item_single_choice);

			if (hasBoundField()) {
				getField().setAdapter(mCountryAdapter);
			}
		}

		@Override
		public void setChangeListener(ListView field) {
			field.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
					if (mCountryAdapter != null && getData() != null) {
						getData().setPrimaryPassportCountry(
							mCountryAdapter.getItemValue(pos, CountryDisplayType.THREE_LETTER));
					}
					onChange(SectionTravelerInfo.this);
				}
			});

		}

		@Override
		protected void onHasFieldAndData(ListView field, Traveler data) {
			getField().setSelection(AdapterView.INVALID_POSITION);
			onChange(SectionTravelerInfo.this);

			boolean travelerHasMultiplePassports = (data.getPassportCountries().size() > 1);
			if (!travelerHasMultiplePassports) {
				if (mCountryAdapter != null && !TextUtils.isEmpty(data.getPrimaryPassportCountry())) {
					for (int i = 0; i < mCountryAdapter.getCount(); i++) {
						if (mCountryAdapter.getItemValue(i, CountryDisplayType.THREE_LETTER).equalsIgnoreCase(
							data.getPrimaryPassportCountry())) {
							getField().setItemChecked(i, true);
							getField().setSelection(i);
							break;
						}
					}
				}
				else if (mAutoChoosePassportCountry) {
					int pos = mCountryAdapter.getDefaultLocalePosition();
					getField().setItemChecked(pos, true);
					getField().setSelection(pos);
					getData().setPrimaryPassportCountry(mCountryAdapter.getItemValue(pos, CountryDisplayType.THREE_LETTER));
				}
			}
			else { // traveler has multiple passports
				int countryIndex = (field.getSelectedItemPosition() == AdapterView.INVALID_POSITION) ? 0 : field.getSelectedItemPosition();
				boolean dataAndFieldMatch = mCountryAdapter.getItemValue(countryIndex, CountryDisplayType.THREE_LETTER).equalsIgnoreCase(
					data.getPrimaryPassportCountry());

				if (!dataAndFieldMatch) {
					if (data.isChangedPrimaryPassportCountry()) {
						// use the primary passport country recently selected
						for (int i = 0; i < mCountryAdapter.getCount(); i++) {
							if (mCountryAdapter.getItemValue(i, CountryDisplayType.THREE_LETTER).equalsIgnoreCase(
								data.getPrimaryPassportCountry())) {
								getField().setSelection(i);
								getField().setItemChecked(i, true);
								break;
							}
						}
					}
					else {
						getField().setSelection(AdapterView.INVALID_POSITION);
						getField().setItemChecked(AdapterView.INVALID_POSITION, true);
						onChange(SectionTravelerInfo.this);
					}
				}
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			return retArr;
		}
	};

	SectionFieldEditable<TelephoneSpinner, Traveler> mEditPhoneNumberCountryCodeSpinner = new SectionFieldEditable<TelephoneSpinner, Traveler>(
		R.id.edit_phone_number_country_code_spinner) {

		private boolean mSetFieldManually = false;

		Validator<TelephoneSpinner> mValidator = new Validator<TelephoneSpinner>() {
			@Override
			public int validate(TelephoneSpinner obj) {
				return ValidationError.NO_ERROR;
			}
		};

		private void updateData() {
			if (getData() != null && getField() != null) {
				TelephoneSpinner spinner = (TelephoneSpinner) getField();
				String countryName = spinner.getSelectedTelephoneCountry();
				String countryCode = "" + spinner.getSelectedTelephoneCountryCode();
				getData().setPhoneCountryCode(countryCode);
				getData().setPhoneCountryName(countryName);
			}
		}

		@Override
		public void setChangeListener(TelephoneSpinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					updateData();
					TelephoneSpinner spinner = (TelephoneSpinner) getField();
					spinner.updateText();

					if (!mSetFieldManually) {
						onChange(SectionTravelerInfo.this);
					}
					else {
						mSetFieldManually = false;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}


		@Override
		protected void onHasFieldAndData(TelephoneSpinner field, Traveler data) {
			TelephoneSpinnerAdapter adapter = (TelephoneSpinnerAdapter) field.getAdapter();
			if (!TextUtils.isEmpty(data.getPhoneCountryCode())) {
				//Look up the country based on Country code, and if available the country name
				String targetCountryCode = data.getPhoneCountryCode();
				String targetCountryName = data.getPhoneCountryName();
				for (int i = 0; i < adapter.getCount(); i++) {
					if (targetCountryCode.equalsIgnoreCase("" + adapter.getCountryCode(i))
						&& (TextUtils.isEmpty(targetCountryName)
						|| targetCountryName.equals(adapter.getCountryName(i)))) {
						field.setSelection(i);
						updateData();
						mSetFieldManually = true;
						break;
					}
				}
			}
			else {
				String targetCountry = mContext.getString(PointOfSale.getPointOfSale()
					.getCountryNameResId());
				for (int i = 0; i < adapter.getCount(); i++) {
					if (targetCountry.equalsIgnoreCase(adapter.getCountryName(i))) {
						field.setSelection(i);
						updateData();
						mSetFieldManually = true;
						break;
					}
				}
			}
		}

		@Override
		protected Validator<TelephoneSpinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<Spinner, Traveler> mEditGenderSpinner = new SectionFieldEditable<Spinner, Traveler>(
		R.id.edit_gender_spinner) {

		Validator<Spinner> mValidator = new Validator<Spinner>() {
			@Override
			public int validate(Spinner obj) {
				GenderSpinnerAdapter adapter = (GenderSpinnerAdapter) obj.getAdapter();
				if (obj.getSelectedItemPosition() == 0) {
					adapter.setErrorVisible(true);
					return ValidationError.ERROR_DATA_MISSING;
				}
				else {
					adapter.setErrorVisible(false);
					return ValidationError.NO_ERROR;
				}
			}
		};

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				getField().setAdapter(new GenderSpinnerAdapter(mContext));
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (hasBoundData()) {
						GenderSpinnerAdapter genderAdapter = (GenderSpinnerAdapter) parent.getAdapter();
						getData().setGender(genderAdapter.getGender(position));
					}

					onChange(SectionTravelerInfo.this);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// ignore
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, Traveler data) {
			GenderSpinnerAdapter adapter = (GenderSpinnerAdapter) field.getAdapter();
			int currentPos = adapter.getGenderPosition(data.getGender());
			if (currentPos >= 0) {
				field.setSelection(currentPos);
			}
			else {
				// Default to Male
				field.setSelection(adapter.getGenderPosition(Traveler.Gender.MALE));
			}
		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<Spinner, Traveler> mEditSeatPreferenceSpinner = new SectionFieldEditable<Spinner, Traveler>(
		R.id.edit_seat_preference_spinner) {

		private boolean mSetFieldManually = false;

		Validator<Spinner> mValidator = new Validator<Spinner>() {
			@Override
			public int validate(Spinner obj) {
				return ValidationError.NO_ERROR;
			}
		};

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				SeatPreferenceSpinnerAdapter adapter = new SeatPreferenceSpinnerAdapter(mContext);
				adapter.setFormatString(mContext.getString(R.string.prefers_seat_colored_TEMPLATE));
				adapter.setSpanColor(R.color.checkout_traveler_birth_color);
				getField().setAdapter(adapter);
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (getData() != null) {
						SeatPreferenceSpinnerAdapter adapter = (SeatPreferenceSpinnerAdapter) parent.getAdapter();
						getData().setSeatPreference(adapter.getSeatPreference(position));
					}

					if (!mSetFieldManually) {
						onChange(SectionTravelerInfo.this);
					}
					else {
						mSetFieldManually = false;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, Traveler data) {
			SeatPreferenceSpinnerAdapter adapter = (SeatPreferenceSpinnerAdapter) field.getAdapter();
			int pos = adapter.getSeatPreferencePosition(data.getSeatPreference());
			if (pos >= 0) {
				field.setSelection(pos);
				mSetFieldManually = true;
			}

		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<Spinner, Traveler> mEditAssistancePreferenceSpinner = new SectionFieldEditable<Spinner, Traveler>(
		R.id.edit_assistance_preference_spinner) {

		private boolean mSetFieldManually = false;

		Validator<Spinner> mValidator = new Validator<Spinner>() {
			@Override
			public int validate(Spinner obj) {
				return ValidationError.NO_ERROR;
			}
		};

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				getField().setAdapter(new AssistanceTypeSpinnerAdapter(mContext));
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (getData() != null) {
						AssistanceTypeSpinnerAdapter assistanceTypeAdapter = (AssistanceTypeSpinnerAdapter) parent
							.getAdapter();
						getData().setAssistance(assistanceTypeAdapter.getAssistanceType(position));
					}

					if (!mSetFieldManually) {
						onChange(SectionTravelerInfo.this);
					}
					else {
						mSetFieldManually = false;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, Traveler data) {
			AssistanceTypeSpinnerAdapter adapter = (AssistanceTypeSpinnerAdapter) field.getAdapter();
			int pos = adapter.getAssistanceTypePosition(data.getAssistance());
			if (pos >= 0) {
				field.setSelection(pos);
				mSetFieldManually = true;
			}
		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			return null;
		}

	};

	public void setFirstNameValid(boolean valid) {
		setFieldValid(mValidFirstName, valid);
	}

	public void setLastNameValid(boolean valid) {
		setFieldValid(mValidLastName, valid);
	}

	public void setPhoneValid(boolean valid) {
		setFieldValid(mValidPhoneNumber, valid);
	}

	private void setFieldValid(SectionFieldValidIndicator mValidFirstName,boolean isValid) {
		mValidFirstName.onPostValidate(mValidFirstName.getField(), isValid, true);
	}
}
