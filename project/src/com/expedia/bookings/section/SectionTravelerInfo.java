package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.impl.cookie.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.FlightPassenger.Gender;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.widget.TelephoneSpinner;
import com.expedia.bookings.widget.TelephoneSpinnerAdapter;
import com.mobiata.android.util.Ui;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
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

		//Edit fields
		mFields.add(mEditFirstName);
		mFields.add(mEditMiddleName);
		mFields.add(mEditLastName);
		mFields.add(mEditPhoneNumberCountryCode);
		mFields.add(mEditPhoneNumberCountryCodeSpinner);
		mFields.add(mEditPhoneNumber);
		mFields.add(mEditBirthDate);
		mFields.add(mEditRedressNumber);
		mFields.add(mEditGender);
		mFields.add(mEditPassportCountry);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, FlightPassenger> field : mFields) {
			field.bindField(this);
		}
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
		boolean valid = true;
		for (SectionField<?, FlightPassenger> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				SectionFieldEditable<?, FlightPassenger> editable = (SectionFieldEditable<?, FlightPassenger>) field;
				if (field.hasBoundData()) {
					valid = valid && editable.isValid();
				}
			}
		}
		return valid;
	}

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
				field.setText(data.getPhoneNumber());
			}
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
				field.setText(DateUtils.formatDate(data.getBirthDate().getTime()));
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
					SectionTravelerInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getFirstName());
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
					SectionTravelerInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getMiddleName());
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
					SectionTravelerInfo.this.onChange();
				}
			});
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
					SectionTravelerInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getPhoneCountryCode());
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
					SectionTravelerInfo.this.onChange();
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
					SectionTravelerInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, FlightPassenger data) {
			field.setText(data.getRedressNumber());
		}
	};

	SectionFieldEditable<DatePicker, FlightPassenger> mEditBirthDate = new SectionFieldEditable<DatePicker, FlightPassenger>(
			R.id.edit_date_of_birth) {

		@Override
		protected Validator<DatePicker> getValidator() {
			return new Validator<DatePicker>() {
				@Override
				public int validate(DatePicker obj) {
					return ValidationError.NO_ERROR;
				}
			};
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
							onChange();
						}
					});
		}

		@Override
		protected void onHasFieldAndData(DatePicker field, FlightPassenger data) {
			if (data.getBirthDate() != null) {
				Calendar bd = data.getBirthDate();
				field.updateDate(bd.get(Calendar.YEAR), bd.get(Calendar.MONTH), bd.get(Calendar.DAY_OF_MONTH));
			}

		}
	};

	SectionFieldEditable<RadioGroup, FlightPassenger> mEditGender = new SectionFieldEditable<RadioGroup, FlightPassenger>(
			R.id.edit_gender_radio) {

		RadioButton mMaleRadio;
		RadioButton mFemaleRadio;

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
			return new Validator<RadioGroup>() {
				@Override
				public int validate(RadioGroup obj) {
					if (obj.getCheckedRadioButtonId() < 0) {
						return ValidationError.ERROR_DATA_MISSING;
					}
					return ValidationError.NO_ERROR;
				}
			};
		}

		@Override
		public void setChangeListener(RadioGroup field) {
			field.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (hasBoundData() && mMaleRadio != null && mFemaleRadio != null) {
						//TODO: We may want to do this in a way that doesn't care about the radio button text, 
						// but that is difficult because our sections don't check for subviews so we'd have to introduce some new logic
						if (checkedId == mMaleRadio.getId()) {
							getData().setGender(Gender.MALE);
						}
						else if (checkedId == mFemaleRadio.getId()) {
							getData().setGender(Gender.FEMALE);
						}
					}
					SectionTravelerInfo.this.onChange();
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
	};

	SectionFieldEditable<Spinner, FlightPassenger> mEditPassportCountry = new SectionFieldEditable<Spinner, FlightPassenger>(
			R.id.edit_passport_country_spinner) {

		ArrayAdapter<CharSequence> countryAdapter;

		@Override
		protected Validator<Spinner> getValidator() {
			return new Validator<Spinner>() {
				@Override
				public int validate(Spinner obj) {
					return ValidationError.NO_ERROR;
				}
			};
		}

		@Override
		protected void onFieldBind() {
			super.onFieldBind();

			countryAdapter = ArrayAdapter.createFromResource(mContext, R.array.country_names,
					android.R.layout.simple_spinner_item);
			countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			if (hasBoundField()) {
				getField().setAdapter(countryAdapter);
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (countryAdapter != null && getData() != null) {
						getData().setPassportCountry(countryAdapter.getItem(pos).toString());
					}
					onChange();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		}

		@Override
		protected void onHasFieldAndData(Spinner field, FlightPassenger data) {
			if (countryAdapter != null && !TextUtils.isEmpty(data.getPassportCountry())) {
				for (int i = 0; i < countryAdapter.getCount(); i++) {
					if (countryAdapter.getItem(i).toString().equalsIgnoreCase(data.getPassportCountry())) {
						getField().setSelection(i);
						break;
					}
				}
			}
			else {
				final String targetCountry = mContext.getString(LocaleUtils.getDefaultCountryResId(mContext));
				for (int i = 0; i < countryAdapter.getCount(); i++) {
					if (targetCountry.equalsIgnoreCase(countryAdapter.getItem(i).toString())) {
						getField().setSelection(i);
						break;
					}
				}
			}
		}
	};

	SectionFieldEditable<TelephoneSpinner, FlightPassenger> mEditPhoneNumberCountryCodeSpinner = new SectionFieldEditable<TelephoneSpinner, FlightPassenger>(
			R.id.edit_phone_number_country_code_spinner) {

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
			return new Validator<TelephoneSpinner>() {
				@Override
				public int validate(TelephoneSpinner obj) {
					return ValidationError.NO_ERROR;
				}
			};
		}
	};

}
