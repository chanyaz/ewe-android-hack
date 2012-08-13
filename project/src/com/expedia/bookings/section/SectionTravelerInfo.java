package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.FlightPassenger.AssistanceType;
import com.expedia.bookings.data.FlightPassenger.Gender;
import com.expedia.bookings.data.FlightPassenger.MealType;
import com.expedia.bookings.data.FlightPassenger.SeatPreference;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.widget.TelephoneSpinner;
import com.expedia.bookings.widget.TelephoneSpinnerAdapter;
import com.mobiata.android.util.Ui;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.graphics.Color;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class SectionTravelerInfo extends LinearLayout implements ISection<FlightPassenger>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	ArrayList<SectionField<?, FlightPassenger>> mFields = new ArrayList<SectionField<?, FlightPassenger>>();

	FlightPassenger mPassenger;
	Context mContext;

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
		mFields.add(mDisplayFirstName);
		mFields.add(mDisplayMiddleName);
		mFields.add(mDisplayLastName);
		mFields.add(mDisplayPhoneCountryCode);
		mFields.add(mDisplayPhoneNumber);
		mFields.add(mDisplayGender);
		mFields.add(mDisplayBirthDay);
		mFields.add(mDisplayRedressNumber);
		mFields.add(mDisplayPassportCountry);
		mFields.add(mDisplaySpecialAssistance);
		mFields.add(mDisplayPhoneNumberWithCountryCode);

		//Validation Indicator fields
		mFields.add(mValidFirstName);
		mFields.add(mValidMiddleName);
		mFields.add(mValidLastName);
		mFields.add(mValidPhoneNumber);
		mFields.add(mValidGender);
		mFields.add(mValidDateOfBirth);
		mFields.add(mValidRedressNumber);
		mFields.add(mValidPassportCountry);

		//Edit fields
		mFields.add(mEditFirstName);
		mFields.add(mEditMiddleName);
		mFields.add(mEditLastName);
		mFields.add(mEditPhoneNumberCountryCode);
		mFields.add(mEditPhoneNumberCountryCodeSpinner);
		mFields.add(mEditPhoneNumber);
		mFields.add(mEditBirthDate);
		mFields.add(mEditBirthDateTextBtn);
		mFields.add(mEditRedressNumber);
		mFields.add(mEditGender);
		mFields.add(mEditPassportCountry);
		mFields.add(mEditAssistancePreference);//Old radio...
		mFields.add(mEditSeatPreference);//Old radio...
		mFields.add(mEditMealPreference);//Old Radio...
		mFields.add(mEditMealPreferenceSpinner);
		mFields.add(mEditAssistancePreferenceSpinner);
		mFields.add(mEditSeatPreferenceSpinner);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, FlightPassenger> field : mFields) {
			field.bindField(this);
		}

		hasValidInput();
	}

	@Override
	public void bind(FlightPassenger passenger) {
		//Update fields
		mPassenger = passenger;

		if (mPassenger != null) {
			for (SectionField<?, FlightPassenger> field : mFields) {
				field.bindData(mPassenger);
			}
		}
	}

	public boolean hasValidInput() {
		SectionFieldEditable<?, FlightPassenger> editable;
		boolean valid = true;
		for (SectionField<?, FlightPassenger> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				editable = (SectionFieldEditable<?, FlightPassenger>) field;
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

	//////////////////////////////////////
	////// VALIDATION INDICATOR FIELDS
	//////////////////////////////////////

	class ValidationIndicatorColorView<Data extends Object> extends SectionFieldValidIndicator<View, Data> {
		public ValidationIndicatorColorView(int fieldId) {
			super(fieldId);
		}

		@Override
		protected void onPostValidate(View field, boolean isValid) {
			if (isValid) {
				field.setBackgroundColor(Color.GREEN);
			}
			else {
				field.setBackgroundColor(Color.RED);
			}
		}
	}

	ValidationIndicatorColorView<FlightPassenger> mValidFirstName = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_first_name);
	ValidationIndicatorColorView<FlightPassenger> mValidMiddleName = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_middle_name);
	ValidationIndicatorColorView<FlightPassenger> mValidLastName = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_last_name);
	ValidationIndicatorColorView<FlightPassenger> mValidPhoneNumber = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_phone_number);
	ValidationIndicatorColorView<FlightPassenger> mValidGender = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_gender);
	ValidationIndicatorColorView<FlightPassenger> mValidDateOfBirth = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_date_of_birth);
	ValidationIndicatorColorView<FlightPassenger> mValidRedressNumber = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_redress_number);
	ValidationIndicatorColorView<FlightPassenger> mValidPassportCountry = new ValidationIndicatorColorView<FlightPassenger>(
			R.id.valid_passport_country);

	//////////////////////////////////////
	////// DISPLAY FIELDS
	//////////////////////////////////////

	SectionField<TextView, FlightPassenger> mDisplayFullName = new SectionField<TextView, FlightPassenger>(
			R.id.display_full_name) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
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

	SectionField<TextView, FlightPassenger> mDisplayFirstName = new SectionField<TextView, FlightPassenger>(
			R.id.display_first_name) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (!TextUtils.isEmpty(data.getFirstName())) {
				field.setText(data.getFirstName());
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayMiddleName = new SectionField<TextView, FlightPassenger>(
			R.id.display_middle_name) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (!TextUtils.isEmpty(data.getMiddleName())) {
				field.setText(data.getMiddleName());
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayLastName = new SectionField<TextView, FlightPassenger>(
			R.id.display_last_name) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (!TextUtils.isEmpty(data.getLastName())) {
				field.setText(data.getLastName());
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayPhoneCountryCode = new SectionField<TextView, FlightPassenger>(
			R.id.display_phone_number_country_code) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (!TextUtils.isEmpty(data.getPhoneCountryCode())) {
				field.setText(data.getPhoneCountryCode());
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayPhoneNumber = new SectionField<TextView, FlightPassenger>(
			R.id.display_phone_number) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (!TextUtils.isEmpty(data.getPhoneNumber())) {
				field.setText(PhoneNumberUtils.formatNumber(data.getPhoneNumber()));
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayPhoneNumberWithCountryCode = new SectionField<TextView, FlightPassenger>(
			R.id.display_phone_number_with_country_code) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			String formatStr = mContext.getResources().getString(R.string.phone_number_with_country_code_TEMPLATE);
			String retStr = String.format(formatStr,
					data.getPhoneCountryCode() == null ? "" : data.getPhoneCountryCode(),
					data.getPhoneNumber() == null ? "" : PhoneNumberUtils.formatNumber(data.getPhoneNumber()));
			field.setText(retStr);
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayGender = new SectionField<TextView, FlightPassenger>(
			R.id.display_gender) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (data.getGender() != null && !TextUtils.isEmpty(data.getGender().name())) {
				field.setText(data.getGender().name());
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayBirthDay = new SectionField<TextView, FlightPassenger>(
			R.id.display_date_of_birth) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (data.getBirthDate() != null) {
				field.setText(android.text.format.DateUtils.formatDateTime(mContext, data.getBirthDate().getTime()
						.getTime(), android.text.format.DateUtils.FORMAT_NUMERIC_DATE
						| android.text.format.DateUtils.FORMAT_SHOW_DATE));
			}
		}
	};
	SectionField<TextView, FlightPassenger> mDisplayRedressNumber = new SectionField<TextView, FlightPassenger>(
			R.id.display_redress_number) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (!TextUtils.isEmpty(data.getRedressNumber())) {
				field.setText(data.getRedressNumber());
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplayPassportCountry = new SectionField<TextView, FlightPassenger>(
			R.id.display_passport_country) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			if (!TextUtils.isEmpty(data.getPassportCountry())) {
				field.setText(data.getPassportCountry());
			}
		}
	};

	SectionField<TextView, FlightPassenger> mDisplaySpecialAssistance = new SectionField<TextView, FlightPassenger>(
			R.id.display_special_assistance) {
		@Override
		public void onHasFieldAndData(TextView field, FlightPassenger data) {
			String template = mContext.getString(R.string.special_assistance_label_TEMPLATE);
			String val = String.format(template, data.getAssistanceString(mContext));
			field.setText(val);
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, FlightPassenger> mEditFirstName = new SectionFieldEditable<EditText, FlightPassenger>(
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
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getFirstName());

		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidFirstName);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, FlightPassenger> mEditMiddleName = new SectionFieldEditable<EditText, FlightPassenger>(
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
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getMiddleName());
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidMiddleName);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, FlightPassenger> mEditLastName = new SectionFieldEditable<EditText, FlightPassenger>(
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
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidLastName);
			return retArr;
		}

		@Override
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getLastName());
		}
	};

	SectionFieldEditable<EditText, FlightPassenger> mEditPhoneNumberCountryCode = new SectionFieldEditable<EditText, FlightPassenger>(
			R.id.edit_phone_number_country_code) {
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
						getData().setPhoneCountryCode(s.toString());
					}
					onChange(SectionTravelerInfo.this);
				}
			});

		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}

		@Override
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getPhoneCountryCode());
		}
	};

	SectionFieldEditable<TextView, FlightPassenger> mEditBirthDateTextBtn = new SectionFieldEditable<TextView, FlightPassenger>(
			R.id.edit_birth_date_text_btn) {

		@Override
		public void setChangeListener(TextView field) {

			field.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					Calendar defCal = Calendar.getInstance();
					if (hasBoundData()) {
						if (getData().getBirthDate() != null) {
							defCal = getData().getBirthDate();
						}
					}

					int year = defCal.get(Calendar.YEAR);
					int month = defCal.get(Calendar.MONTH);
					int day = defCal.get(Calendar.DAY_OF_MONTH);

					OnDateSetListener dsl = new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							if (hasBoundData()) {
								Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
								getData().setBirthDate(cal);
							}
							refreshText();
							onChange(SectionTravelerInfo.this);
						}
					};

					DatePickerDialog birthDatePicker = new DatePickerDialog(mContext, dsl, year, month, day);
					birthDatePicker.show();
				}
			});

		}

		private void refreshText() {
			if (hasBoundField() && hasBoundData()) {
				onHasFieldAndData(getField(), getData());
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidDateOfBirth);
			return retArr;
		}

		@Override
		protected void onHasFieldAndData(TextView field, FlightPassenger data) {

			Calendar cal = Calendar.getInstance();
			if (data.getBirthDate() != null) {
				cal = data.getBirthDate();
			}
			field.setText(DateUtils.formatDateTime(mContext, cal.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_NUMERIC_DATE));
		}

		Validator<TextView> mValidator = new Validator<TextView>() {

			@Override
			public int validate(TextView obj) {
				// TODO require reasonable date
				return ValidationError.NO_ERROR;
			}

		};

		@Override
		protected Validator<TextView> getValidator() {
			return mValidator;
		}
	};

	SectionFieldEditable<EditText, FlightPassenger> mEditPhoneNumber = new SectionFieldEditable<EditText, FlightPassenger>(
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
						getData().setPhoneNumber(getNumbersOnly(s.toString()));
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
							f.setText(phoneNumberDisplayer(getData().getPhoneNumber()));
						}
					}
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(phoneNumberDisplayer(data.getPhoneNumber()));
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
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

	SectionFieldEditable<EditText, FlightPassenger> mEditRedressNumber = new SectionFieldEditable<EditText, FlightPassenger>(
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
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getRedressNumber());
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidRedressNumber);
			return retArr;
		}
	};

	SectionFieldEditable<DatePicker, FlightPassenger> mEditBirthDate = new SectionFieldEditable<DatePicker, FlightPassenger>(
			R.id.edit_date_of_birth) {

		Validator<DatePicker> mValidator = new Validator<DatePicker>() {
			@Override
			public int validate(DatePicker obj) {
				Calendar cal = new GregorianCalendar(obj.getYear(), obj.getMonth(), obj.getDayOfMonth());
				Calendar now = Calendar.getInstance();
				if (cal.getTimeInMillis() > now.getTimeInMillis()) {
					return ValidationError.ERROR_DATA_INVALID;
				}
				return ValidationError.NO_ERROR;
			}
		};

		@Override
		protected Validator<DatePicker> getValidator() {
			return mValidator;
		}

		@Override
		public void setChangeListener(DatePicker field) {

			Calendar now = Calendar.getInstance();
			field.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH),
					new OnDateChangedListener() {
						@Override
						public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
							if (mPassenger != null) {
								mPassenger.setBirthDate(cal);
							}
							onChange(SectionTravelerInfo.this);
						}
					});
		}

		@Override
		protected void onHasFieldAndData(DatePicker field, FlightPassenger data) {
			if (data.getBirthDate() == null) {
				data.setBirthDate(Calendar.getInstance());
			}
			Calendar bd = data.getBirthDate();
			field.updateDate(bd.get(Calendar.YEAR), bd.get(Calendar.MONTH), bd.get(Calendar.DAY_OF_MONTH));
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidDateOfBirth);
			return retArr;
		}
	};

	SectionFieldEditable<RadioGroup, FlightPassenger> mEditGender = new SectionFieldEditable<RadioGroup, FlightPassenger>(
			R.id.edit_gender_radio) {

		RadioButton mMaleRadio;
		RadioButton mFemaleRadio;

		Validator<RadioGroup> mValidator = new Validator<RadioGroup>() {
			@Override
			public int validate(RadioGroup obj) {
				if (obj.getCheckedRadioButtonId() < 0) {
					return ValidationError.ERROR_DATA_MISSING;
				}
				return ValidationError.NO_ERROR;
			}
		};

		private void setIsMale(boolean isMale) {
			if (mMaleRadio != null && mFemaleRadio != null) {
				mMaleRadio.setChecked(isMale);
				mFemaleRadio.setChecked(!isMale);
			}
		}

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (this.hasBoundField()) {
				mMaleRadio = Ui.findView(getField(), R.id.edit_gender_radio_male);
				mFemaleRadio = Ui.findView(getField(), R.id.edit_gender_radio_female);
			}
		}

		@Override
		protected Validator<RadioGroup> getValidator() {
			return mValidator;
		}

		@Override
		public void setChangeListener(RadioGroup field) {
			field.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (hasBoundData() && mMaleRadio != null && mFemaleRadio != null) {
						if (checkedId == mMaleRadio.getId()) {
							getData().setGender(Gender.MALE);
						}
						else if (checkedId == mFemaleRadio.getId()) {
							getData().setGender(Gender.FEMALE);
						}
					}
					onChange(SectionTravelerInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(RadioGroup field, FlightPassenger data) {
			if (data.getGender() != null) {
				if (data.getGender() == Gender.MALE) {
					setIsMale(true);
				}
				else {
					setIsMale(false);
				}

			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidGender);
			return retArr;
		}
	};

	SectionFieldEditable<Spinner, FlightPassenger> mEditPassportCountry = new SectionFieldEditable<Spinner, FlightPassenger>(
			R.id.edit_passport_country_spinner) {

		ArrayAdapter<CharSequence> mCountryAdapter;

		Validator<Spinner> mValidator = new Validator<Spinner>() {
			@Override
			public int validate(Spinner obj) {
				return ValidationError.NO_ERROR;
			}
		};

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected void onFieldBind() {
			super.onFieldBind();

			mCountryAdapter = ArrayAdapter.createFromResource(mContext, R.array.country_names,
					android.R.layout.simple_spinner_item);
			mCountryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			if (hasBoundField()) {
				getField().setAdapter(mCountryAdapter);
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (mCountryAdapter != null && getData() != null) {
						getData().setPassportCountry(mCountryAdapter.getItem(pos).toString());
					}
					onChange(SectionTravelerInfo.this);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		}

		@Override
		protected void onHasFieldAndData(Spinner field, FlightPassenger data) {
			if (mCountryAdapter != null && !TextUtils.isEmpty(data.getPassportCountry())) {
				for (int i = 0; i < mCountryAdapter.getCount(); i++) {
					if (mCountryAdapter.getItem(i).toString().equalsIgnoreCase(data.getPassportCountry())) {
						getField().setSelection(i);
						break;
					}
				}
			}
			else {
				final String targetCountry = mContext.getString(LocaleUtils.getDefaultCountryResId(mContext));
				for (int i = 0; i < mCountryAdapter.getCount(); i++) {
					if (targetCountry.equalsIgnoreCase(mCountryAdapter.getItem(i).toString())) {
						getField().setSelection(i);
						break;
					}
				}
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> retArr = new ArrayList<SectionFieldValidIndicator<?, FlightPassenger>>();
			retArr.add(mValidPassportCountry);
			return retArr;
		}
	};

	SectionFieldEditable<TelephoneSpinner, FlightPassenger> mEditPhoneNumberCountryCodeSpinner = new SectionFieldEditable<TelephoneSpinner, FlightPassenger>(
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
		protected void onHasFieldAndData(TelephoneSpinner field, FlightPassenger data) {
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
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<RadioGroup, FlightPassenger> mEditMealPreference = new SectionFieldEditable<RadioGroup, FlightPassenger>(
			R.id.edit_meal_radio) {

		RadioButton mNoneRadio;
		RadioButton mVegetarianRadio;
		RadioButton mBoozeRadio;

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (this.hasBoundField()) {
				mNoneRadio = Ui.findView(getField(), R.id.edit_meal_radio_none);
				mVegetarianRadio = Ui.findView(getField(), R.id.edit_meal_radio_vegetarian);
				mBoozeRadio = Ui.findView(getField(), R.id.edit_meal_radio_booze);
			}
		}

		@Override
		protected Validator<RadioGroup> getValidator() {
			return CommonSectionValidators.RADIO_GROUP_HAS_SELECTION;
		}

		@Override
		public void setChangeListener(RadioGroup field) {
			field.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {

					if (hasBoundData()) {
						if (mVegetarianRadio != null && checkedId == mVegetarianRadio.getId()) {
							getData().setMealPreference(MealType.VEGITARIAN);
						}
						else if (mBoozeRadio != null && checkedId == mBoozeRadio.getId()) {
							getData().setMealPreference(MealType.BOOZE);
						}
						else {
							getData().setMealPreference(MealType.NONE);
						}
					}
					onChange(SectionTravelerInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(RadioGroup field, FlightPassenger data) {
			if (data.getMealPreference() != null) {
				MealType mt = data.getMealPreference();
				if (mt == MealType.NONE) {
					mNoneRadio.setChecked(true);
					mVegetarianRadio.setChecked(false);
					mBoozeRadio.setChecked(false);
				}
				else if (mt == MealType.VEGITARIAN) {
					mNoneRadio.setChecked(false);
					mVegetarianRadio.setChecked(true);
					mBoozeRadio.setChecked(false);
				}
				else if (mt == MealType.BOOZE) {
					mNoneRadio.setChecked(false);
					mVegetarianRadio.setChecked(false);
					mBoozeRadio.setChecked(true);
				}
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<RadioGroup, FlightPassenger> mEditSeatPreference = new SectionFieldEditable<RadioGroup, FlightPassenger>(
			R.id.edit_seating_preference_radio) {

		RadioButton mNoneRadio;
		RadioButton mWindowRadio;
		RadioButton mAisleRadio;

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (this.hasBoundField()) {
				mNoneRadio = Ui.findView(getField(), R.id.edit_seating_preference_radio_none);
				mWindowRadio = Ui.findView(getField(), R.id.edit_seating_preference_radio_window);
				mAisleRadio = Ui.findView(getField(), R.id.edit_seating_preference_radio_aisle);
			}
		}

		@Override
		protected Validator<RadioGroup> getValidator() {
			return CommonSectionValidators.RADIO_GROUP_HAS_SELECTION;
		}

		@Override
		public void setChangeListener(RadioGroup field) {
			field.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {

					if (hasBoundData()) {
						if (mWindowRadio != null && checkedId == mWindowRadio.getId()) {
							getData().setSeatPreference(SeatPreference.WINDOW);
						}
						else if (mAisleRadio != null && checkedId == mAisleRadio.getId()) {
							getData().setSeatPreference(SeatPreference.AISLE);
						}
						else {
							getData().setSeatPreference(SeatPreference.NONE);
						}
					}
					onChange(SectionTravelerInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(RadioGroup field, FlightPassenger data) {
			if (data.getMealPreference() != null) {
				SeatPreference sp = data.getSeatPreference();
				if (sp == SeatPreference.WINDOW) {
					mNoneRadio.setChecked(false);
					mWindowRadio.setChecked(true);
					mAisleRadio.setChecked(false);
				}
				else if (sp == SeatPreference.AISLE) {
					mNoneRadio.setChecked(false);
					mWindowRadio.setChecked(false);
					mAisleRadio.setChecked(true);
				}
				else {
					mNoneRadio.setChecked(true);
					mWindowRadio.setChecked(false);
					mAisleRadio.setChecked(false);
				}
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<RadioGroup, FlightPassenger> mEditAssistancePreference = new SectionFieldEditable<RadioGroup, FlightPassenger>(
			R.id.edit_assistance_radio) {

		RadioButton mNoneRadio;
		RadioButton mWheelChairRadio;
		RadioButton mDefibrillatorRadio;

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (this.hasBoundField()) {
				mNoneRadio = Ui.findView(getField(), R.id.edit_assistance_radio_none);
				mWheelChairRadio = Ui.findView(getField(), R.id.edit_assistance_radio_wheelchair);
				mDefibrillatorRadio = Ui.findView(getField(), R.id.edit_assistance_radio_defibrillator);
			}
		}

		@Override
		protected Validator<RadioGroup> getValidator() {
			return CommonSectionValidators.RADIO_GROUP_HAS_SELECTION;
		}

		@Override
		public void setChangeListener(RadioGroup field) {
			field.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {

					if (hasBoundData()) {
						if (mWheelChairRadio != null && checkedId == mWheelChairRadio.getId()) {
							getData().setAssistance(AssistanceType.WHEELCHAIR);
						}
						else if (mDefibrillatorRadio != null && checkedId == mDefibrillatorRadio.getId()) {
							getData().setAssistance(AssistanceType.DEFIBRILLATOR);
						}
						else {
							getData().setAssistance(AssistanceType.NONE);
						}
					}
					onChange(SectionTravelerInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(RadioGroup field, FlightPassenger data) {
			if (data.getMealPreference() != null) {
				AssistanceType at = data.getAssistance();
				if (at == AssistanceType.WHEELCHAIR) {
					mNoneRadio.setChecked(false);
					mWheelChairRadio.setChecked(true);
					mDefibrillatorRadio.setChecked(false);
				}
				else if (at == AssistanceType.DEFIBRILLATOR) {
					mNoneRadio.setChecked(false);
					mWheelChairRadio.setChecked(false);
					mDefibrillatorRadio.setChecked(true);
				}
				else {
					mNoneRadio.setChecked(true);
					mWheelChairRadio.setChecked(false);
					mDefibrillatorRadio.setChecked(false);
				}
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<Spinner, FlightPassenger> mEditMealPreferenceSpinner = new SectionFieldEditable<Spinner, FlightPassenger>(
			R.id.edit_meal_preference_spinner) {

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
				getField().setAdapter(new MealTypeSpinnerAdapter(mContext));
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (getData() != null) {
						MealTypeSpinnerAdapter mealTypes = (MealTypeSpinnerAdapter) parent.getAdapter();
						getData().setMealPreference(mealTypes.getMealTypeValue(position));
					}
					onChange(SectionTravelerInfo.this);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, FlightPassenger data) {
			MealTypeSpinnerAdapter adapter = (MealTypeSpinnerAdapter) field.getAdapter();

			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getMealTypeValue(i) == data.getMealPreference()) {
					field.setSelection(i);
					break;
				}
			}
		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<Spinner, FlightPassenger> mEditSeatPreferenceSpinner = new SectionFieldEditable<Spinner, FlightPassenger>(
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
				getField().setAdapter(new SeatPreferenceSpinnerAdapter(mContext));
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (getData() != null) {
						SeatPreferenceSpinnerAdapter mealTypes = (SeatPreferenceSpinnerAdapter) parent.getAdapter();
						getData().setSeatPreference(mealTypes.getSeatPreferenceValue(position));
					}
					onChange(SectionTravelerInfo.this);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, FlightPassenger data) {
			SeatPreferenceSpinnerAdapter adapter = (SeatPreferenceSpinnerAdapter) field.getAdapter();

			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getSeatPreferenceValue(i) == data.getSeatPreference()) {
					field.setSelection(i);
					break;
				}
			}
		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}
	};

	SectionFieldEditable<Spinner, FlightPassenger> mEditAssistancePreferenceSpinner = new SectionFieldEditable<Spinner, FlightPassenger>(
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
						AssistanceTypeSpinnerAdapter mealTypes = (AssistanceTypeSpinnerAdapter) parent.getAdapter();
						getData().setAssistance(mealTypes.getAssistanceTypeValue(position));
					}
					onChange(SectionTravelerInfo.this);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		@Override
		protected void onHasFieldAndData(Spinner field, FlightPassenger data) {
			AssistanceTypeSpinnerAdapter adapter = (AssistanceTypeSpinnerAdapter) field.getAdapter();

			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getAssistanceTypeValue(i) == data.getAssistance()) {
					field.setSelection(i);
					break;
				}
			}
		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, FlightPassenger>> getPostValidators() {
			return null;
		}
	};

}
