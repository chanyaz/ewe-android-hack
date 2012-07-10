package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.FlightPassenger.Gender;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class SectionTravelerInfo extends LinearLayout implements ISection<FlightPassenger>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	ArrayList<SectionField<?, FlightPassenger>> mFields = new ArrayList<SectionField<?, FlightPassenger>>();

	FlightPassenger mPassenger;

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
		//Display fields
		mFields.add(mDisplayFullName);

		//Edit fields
		mFields.add(mEditFirstName);
		mFields.add(mEditLastName);
		mFields.add(mEditPhoneNumber);
		mFields.add(mEditBirthDate);
		mFields.add(mEditRedressNumber);
		mFields.add(mEditGender);
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

			if (!fullNameStr.isEmpty()) {
				field.setText(fullNameStr);
			}
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, FlightPassenger> mEditFirstName = new SectionFieldEditable<EditText, FlightPassenger>(
			R.id.edit_first_name) {
		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				if (TextUtils.isEmpty(field.getText())) {
					return false;
				}
			}
			return true;
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

	SectionFieldEditable<EditText, FlightPassenger> mEditLastName = new SectionFieldEditable<EditText, FlightPassenger>(
			R.id.edit_last_name) {
		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				if (TextUtils.isEmpty(field.getText())) {
					return false;
				}
			}
			return true;
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

	SectionFieldEditable<EditText, FlightPassenger> mEditPhoneNumber = new SectionFieldEditable<EditText, FlightPassenger>(
			R.id.edit_phone_number) {
		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				String fieldText = field.getText().toString();
				if (TextUtils.isEmpty(fieldText)) {
					return false;
				}
				if (getNumbersOnly(fieldText).length() != 10) {
					return false;
				}
			}
			return true;
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
				if (numbersOnly.length() == 10) {
					String areaCode = numbersOnly.substring(0, 3);
					String start = numbersOnly.substring(3, 6);
					String end = numbersOnly.substring(6, 10);

					String displayPhoneNumber = String.format("(%s) %s - %s", areaCode, start, end);
					return displayPhoneNumber;
				}
				else {
					return phoneNumString;
				}
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
		protected boolean hasValidInput(EditText field) {
			if (field != null) {

			}
			return true;
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
		protected boolean hasValidInput(DatePicker field) {
			// TODO Auto-generated method stub
			return true;
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
		protected boolean hasValidInput(RadioGroup field) {
			if (field != null) {
				if (field.getCheckedRadioButtonId() < 0) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void setChangeListener(RadioGroup field) {
			field.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (hasBoundData() && mMaleRadio != null && mFemaleRadio != null) {
						//TODO: We may want to do this in a way that doesn't care about the radio button text, but that is difficult because our sections don't check for subviews so we'd have to introduce some new logic
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

}
