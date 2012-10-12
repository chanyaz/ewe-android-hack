package com.expedia.bookings.section;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
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
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.CountrySpinnerAdapter.CountryDisplayType;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TelephoneSpinner;
import com.expedia.bookings.widget.TelephoneSpinnerAdapter;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public class SectionTravelerInfo extends LinearLayout implements ISection<Traveler>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	ArrayList<SectionField<?, Traveler>> mFields = new ArrayList<SectionField<?, Traveler>>();

	Traveler mTraveler;
	Context mContext;
	boolean mAutoChoosePassportCountry = true;

	public SectionTravelerInfo(Context context) {
		super(context);
		init(context);
	}

	public SectionTravelerInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

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

		//Validation Indicator fields
		mFields.add(mValidFirstName);
		mFields.add(mValidMiddleName);
		mFields.add(mValidLastName);
		mFields.add(mValidPhoneNumber);
		mFields.add(mValidDateOfBirth);
		mFields.add(mValidRedressNumber);

		//Edit fields
		mFields.add(mEditFirstName);
		mFields.add(mEditMiddleName);
		mFields.add(mEditLastName);
		mFields.add(mEditPhoneNumberCountryCodeSpinner);
		mFields.add(mEditPhoneNumber);
		mFields.add(mEditBirthDateTextBtn);
		mFields.add(mEditRedressNumber);
		mFields.add(mEditGenderSpinner);
		mFields.add(mEditPassportCountryListView);
		mFields.add(mEditAssistancePreferenceSpinner);
		mFields.add(mEditSeatPreferenceSpinner);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, Traveler> field : mFields) {
			field.bindField(this);
		}
	}

	@Override
	public void bind(Traveler traveler) {
		//Update fields
		mTraveler = traveler;

		if (mTraveler != null) {
			for (SectionField<?, Traveler> field : mFields) {
				field.bindData(mTraveler);
			}
		}
	}

	public boolean hasValidInput() {
		SectionFieldEditable<?, Traveler> editable;
		boolean valid = true;
		for (SectionField<?, Traveler> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				editable = (SectionFieldEditable<?, Traveler>) field;
				boolean newIsValid = editable.isValid();
				valid = (valid && newIsValid);
			}
		}
		return valid;
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
	 * @param enabled
	 */
	public void setAutoChoosePassportCountryEnabled(boolean enabled) {
		mAutoChoosePassportCountry = enabled;
	}

	public String phoneToStringHelper(Phone phone) {
		if (phone != null) {
			String number = (phone.getAreaCode() == null ? "" : phone.getAreaCode())
					+ (phone.getNumber() == null ? "" : phone.getNumber());
			return number;
		}
		return "";
	}

	//////////////////////////////////////
	////// VALIDATION INDICATOR FIELDS
	//////////////////////////////////////

	ValidationIndicatorTextColorExclaimation<Traveler> mValidFirstName = new ValidationIndicatorTextColorExclaimation<Traveler>(
			R.id.edit_first_name);

	ValidationIndicatorTextColorExclaimation<Traveler> mValidMiddleName = new ValidationIndicatorTextColorExclaimation<Traveler>(
			R.id.edit_middle_name);
	ValidationIndicatorTextColorExclaimation<Traveler> mValidLastName = new ValidationIndicatorTextColorExclaimation<Traveler>(
			R.id.edit_last_name);
	ValidationIndicatorTextColorExclaimation<Traveler> mValidPhoneNumber = new ValidationIndicatorTextColorExclaimation<Traveler>(
			R.id.edit_phone_number);
	ValidationIndicatorTextColorExclaimation<Traveler> mValidDateOfBirth = new ValidationIndicatorTextColorExclaimation<Traveler>(
			R.id.edit_birth_date_text_btn);
	ValidationIndicatorTextColorExclaimation<Traveler> mValidRedressNumber = new ValidationIndicatorTextColorExclaimation<Traveler>(
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

			if (!TextUtils.isEmpty(fullNameStr)) {
				field.setText(fullNameStr);
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
				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
				String bdayStr = df.format(data.getBirthDateInMillis());//DateFormat.MEDIUM
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
			else {
				//TODO: Slow but it already has all the data we need... sigh...
				CountrySpinnerAdapter adapter = new CountrySpinnerAdapter(getContext(), CountryDisplayType.FULL_NAME);
				int pos = adapter.getPositionByCountryThreeLetterCode(data.getPrimaryPassportCountry());
				if (pos > 0) {
					field.setText(adapter.getItemValue(pos, CountryDisplayType.FULL_NAME));
				}
				else {
					field.setText("");
				}
			}
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

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, Traveler> mEditFirstName = new SectionFieldEditable<EditText, Traveler>(
			R.id.edit_first_name) {
		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET;
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

	SectionFieldEditable<EditText, Traveler> mEditMiddleName = new SectionFieldEditable<EditText, Traveler>(
			R.id.edit_middle_name) {
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
						getData().setMiddleName(s.toString());
					}
					onChange(SectionTravelerInfo.this);
				}
			});
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

	SectionFieldEditable<EditText, Traveler> mEditLastName = new SectionFieldEditable<EditText, Traveler>(
			R.id.edit_last_name) {
		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET;
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

	/*
	 * This is our date picker DialogFragment. The coder has a responsibility to set setOnDateSetListener after any sort of creation event (including rotaiton)
	 * 
	 * Note this DatePickerFragment uses a regular datePickerDialog which depends on Calendar objects, meaning that the OnDateSet call will return off by one months...
	 */
	public static class DatePickerFragment extends DialogFragment
	{
		private OnDateSetListener mListener = null;
		private int mDay;
		private int mMonth;//0 indexed like calendar
		private int mYear;

		private static final String DAY_TAG = "DAY_TAG";
		private static final String MONTH_TAG = "MONTH_TAG";
		private static final String YEAR_TAG = "YEAR_TAG";

		public static DatePickerFragment newInstance(Date cal, OnDateSetListener listener) {
			DatePickerFragment frag = new DatePickerFragment();
			frag.setOnDateSetListener(listener);
			frag.setDate(cal.getDayOfMonth(), cal.getMonth() - 1, cal.getYear());
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

			// Create a new instance of DatePickerDialog and return it
			DatePickerDialog dialog = new DatePickerDialog(getActivity(), mListener, mYear, mMonth, mDay);

			if (AndroidUtils.getSdkVersion() >= 11) {
				//We set a max date for new apis, if we are stuck with an old api, they will be allowed to choose any date, but validation will fail
				dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
			}

			return dialog;
		}

		@Override
		public void onSaveInstanceState(Bundle out) {

			out.putInt(DAY_TAG, mDay);
			out.putInt(MONTH_TAG, mMonth);
			out.putInt(YEAR_TAG, mYear);

			super.onSaveInstanceState(out);
		}
	}

	//This class is defined so that we can have both SectionFieldEditable and OnDateSetListener implemented in a single class
	abstract static class SectionFieldEditableWithDateChangeListener<FieldType extends View, Data extends Object>
			extends
			SectionFieldEditable<FieldType, Data> implements DatePickerDialog.OnDateSetListener {
		public SectionFieldEditableWithDateChangeListener(int fieldId) {
			super(fieldId);
		}
	};

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
						Date date = new Date(1970, 1, 1);
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
			}
			else {
				Log.e("The Birthday picker is expecting a FragmentActivity to be the context. In it's current state, this will do nohting if the context is not a FragmentActivity");
			}
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			monthOfYear++;//DatePicker uses calendars...
			if (hasBoundData()) {
				Date date = getData().getBirthDate();
				if (date == null) {
					date = new Date(year, monthOfYear, dayOfMonth);
				}
				else {
					date.setDate(year, monthOfYear, dayOfMonth);
				}
				getData().setBirthDate(date);
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
			if (data.getBirthDate() != null) {
				String formatStr = mContext.getString(R.string.born_on_colored_TEMPLATE);
				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
				String bdayStr = df.format(data.getBirthDateInMillis());//DateFormat.MEDIUM
				btnTxt = String.format(formatStr, bdayStr);
			}
			field.setText(Html.fromHtml(btnTxt));
		}

		Validator<TextView> mValidator = new Validator<TextView>() {

			@Override
			public int validate(TextView obj) {
				int retVal = ValidationError.NO_ERROR;
				if (hasBoundData()) {
					if (getData().getBirthDate() != null) {
						long birthDate = getData().getBirthDateInMillis();
						long now = Calendar.getInstance().getTimeInMillis();
						if (birthDate > now) {
							retVal = ValidationError.ERROR_DATA_INVALID;
						}
						else {
							retVal = ValidationError.NO_ERROR;
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

	SectionFieldEditable<EditText, Traveler> mEditPhoneNumber = new SectionFieldEditable<EditText, Traveler>(
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
						//TODO:This is assuming that the first 3 digits are the area code. This may or may not cause issues down the  line.
						String numbersOnly = getNumbersOnly(s.toString());
						if (numbersOnly.length() <= 3) {
							getData().getOrCreatePrimaryPhoneNumber().setAreaCode(numbersOnly);
							getData().getOrCreatePrimaryPhoneNumber().setNumber("");
						}
						else {
							getData().getOrCreatePrimaryPhoneNumber().setAreaCode(numbersOnly.substring(0, 3));
							getData().getOrCreatePrimaryPhoneNumber().setNumber(numbersOnly.substring(3));
						}
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

	SectionFieldEditable<EditText, Traveler> mEditRedressNumber = new SectionFieldEditable<EditText, Traveler>(
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

	SectionFieldEditable<ListView, Traveler> mEditPassportCountryListView = new SectionFieldEditable<ListView, Traveler>(
			R.id.edit_passport_country_listview) {

		CountrySpinnerAdapter mCountryAdapter;

		Validator<ListView> mValidator = new Validator<ListView>() {
			@Override
			public int validate(ListView obj) {
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

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Traveler>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Traveler>> retArr = new ArrayList<SectionFieldValidIndicator<?, Traveler>>();
			return retArr;
		}
	};

	SectionFieldEditable<TelephoneSpinner, Traveler> mEditPhoneNumberCountryCodeSpinner = new SectionFieldEditable<TelephoneSpinner, Traveler>(
			R.id.edit_phone_number_country_code_spinner) {

		Validator<TelephoneSpinner> mValidator = new Validator<TelephoneSpinner>() {
			@Override
			public int validate(TelephoneSpinner obj) {
				return ValidationError.NO_ERROR;
			}
		};

		@Override
		public void setChangeListener(TelephoneSpinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (getData() != null) {
						TelephoneSpinner spinner = (TelephoneSpinner) parent;
						String countryCode = "" + spinner.getSelectedTelephoneCountryCode();
						getData().setPhoneCountryCode(countryCode);
					}
					onChange(SectionTravelerInfo.this);
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
				final String targetCountryCode = data.getPhoneCountryCode();
				for (int i = 0; i < adapter.getCount(); i++) {
					if (targetCountryCode.equalsIgnoreCase("" + adapter.getCountryCode(i))) {
						field.setSelection(i);
						break;
					}
				}
			}
			else {
				final String targetCountry = mContext.getString(LocaleUtils.getDefaultCountryResId(mContext));
				for (int i = 0; i < adapter.getCount(); i++) {
					if (targetCountry.equalsIgnoreCase(adapter.getCountryName(i))) {
						getField().setSelection(i);
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
				return ValidationError.NO_ERROR;
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
					if (getData() != null) {
						GenderSpinnerAdapter genderAdapter = (GenderSpinnerAdapter) parent.getAdapter();
						getData().setGender(genderAdapter.getGender(position));
					}
					onChange(SectionTravelerInfo.this);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, Traveler data) {
			GenderSpinnerAdapter adapter = (GenderSpinnerAdapter) field.getAdapter();
			if (!TextUtils.isEmpty(data.getFirstName())) {
				String newFormatStr = String.format(mContext.getString(R.string.gender_name_TEMPLATE),
						data.getFirstName(), "%s");
				adapter.setFormatString(newFormatStr);
			}
			int currentPos = adapter.getGenderPosition(data.getGender());
			if (currentPos >= 0) {
				field.setSelection(currentPos);
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
					onChange(SectionTravelerInfo.this);
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
					onChange(SectionTravelerInfo.this);
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

}
