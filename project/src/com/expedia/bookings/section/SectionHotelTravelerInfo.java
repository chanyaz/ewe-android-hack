package com.expedia.bookings.section;

import java.util.ArrayList;

import android.app.DatePickerDialog;
import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.widget.TelephoneSpinner;
import com.expedia.bookings.widget.TelephoneSpinnerAdapter;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public class SectionHotelTravelerInfo extends LinearLayout implements ISection<Traveler>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	ArrayList<SectionField<?, Traveler>> mFields = new ArrayList<SectionField<?, Traveler>>();

	Traveler mTraveler;
	Context mContext;
	boolean mAutoChoosePassportCountry = true;

	public SectionHotelTravelerInfo(Context context) {
		super(context);
		init(context);
	}

	public SectionHotelTravelerInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionHotelTravelerInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		//Display fields
		mFields.add(mDisplayFullName);
		mFields.add(mDisplayPhoneNumberWithCountryCode);

		//Validation Indicator fields
		mFields.add(mValidFirstName);
		mFields.add(mValidMiddleName);
		mFields.add(mValidLastName);
		mFields.add(mValidPhoneNumber);

		//Edit fields
		mFields.add(mEditFirstName);
		mFields.add(mEditMiddleName);
		mFields.add(mEditLastName);
		mFields.add(mEditPhoneNumberCountryCodeSpinner);
		mFields.add(mEditPhoneNumber);
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
					onChange(SectionHotelTravelerInfo.this);
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
					onChange(SectionHotelTravelerInfo.this);
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
					onChange(SectionHotelTravelerInfo.this);
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

	//This class is defined so that we can have both SectionFieldEditable and OnDateSetListener implemented in a single class
	abstract static class SectionFieldEditableWithDateChangeListener<FieldType extends View, Data extends Object>
			extends
			SectionFieldEditable<FieldType, Data> implements DatePickerDialog.OnDateSetListener {
		public SectionFieldEditableWithDateChangeListener(int fieldId) {
			super(fieldId);
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
					onChange(SectionHotelTravelerInfo.this);
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
					onChange(SectionHotelTravelerInfo.this);
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
}