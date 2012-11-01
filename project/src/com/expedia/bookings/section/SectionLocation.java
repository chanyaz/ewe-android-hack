package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.CountrySpinnerAdapter.CountryDisplayType;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.mobiata.android.Log;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public class SectionLocation extends LinearLayout implements ISection<Location>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	ArrayList<SectionField<?, Location>> mFields = new ArrayList<SectionField<?, Location>>();

	Location mLocation;
	Context mContext;

	public SectionLocation(Context context) {
		super(context);
		init(context);
	}

	public SectionLocation(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionLocation(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		//Display fields
		mFields.add(this.mDisplayAddressCountry);
		mFields.add(this.mDisplayAddressBothLines);
		mFields.add(this.mDisplayCityStateZipOneLine);

		//Validation Indicators
		mFields.add(mValidAddrLineOne);
		mFields.add(mValidAddrLineTwo);
		mFields.add(mValidCity);
		mFields.add(mValidState);
		mFields.add(mValidPostalCode);

		//Edit fields
		mFields.add(this.mEditAddressLineOne);
		mFields.add(this.mEditAddressLineTwo);
		mFields.add(this.mEditAddressCity);
		mFields.add(this.mEditAddressState);
		mFields.add(this.mEditAddressPostalCode);
		mFields.add(this.mEditCountrySpinner);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, Location> field : mFields) {
			field.bindField(this);
		}
	}

	@Override
	public void bind(Location location) {
		//Update fields
		mLocation = location;

		if (mLocation != null) {
			for (SectionField<?, Location> field : mFields) {
				field.bindData(mLocation);
			}
		}
	}

	public boolean hasValidInput() {
		boolean valid = true;
		SectionFieldEditable<?, Location> editable;
		for (SectionField<?, Location> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				editable = (SectionFieldEditable<?, Location>) field;
				boolean newIsValid = editable.isValid();
				valid = (valid && newIsValid);
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

	SectionField<TextView, Location> mDisplayAddressBothLines = new SectionField<TextView, Location>(
			R.id.display_address_single_line) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			List<String> address = data.getStreetAddress();

			if (address != null) {
				if (address.size() == 1) {
					field.setText((address.get(0) != null) ? address.get(0) : "");
				}
				else {
					String addrStr = String.format(
							mContext.getResources().getString(R.string.single_line_street_address_TEMPLATE),
							address.get(0), address.get(1));
					addrStr = addrStr.trim();
					if(addrStr.endsWith(",")){
						addrStr = addrStr.substring(0, addrStr.length() - 1);
					}
					field.setText(addrStr);
				}
			}
		}
	};

	SectionField<TextView, Location> mDisplayCityStateZipOneLine = new SectionField<TextView, Location>(
			R.id.display_address_city_state_zip_one_line) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			Resources res = mContext.getResources();
			String formatStr = res.getString(R.string.single_line_city_state_zip_TEMPLATE);
			String retStr = String.format(formatStr, data.getCity() == null ? "" : data.getCity(),
					data.getStateCode() == null ? "" : data.getStateCode(),
					data.getPostalCode() == null ? "" : data.getPostalCode());
			field.setText(retStr);
		}
	};

	SectionField<TextView, Location> mDisplayAddressCountry = new SectionField<TextView, Location>(
			R.id.display_address_country) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			field.setText((data.getCountryCode() != null) ? data.getCountryCode() : "");
		}
	};

	//////////////////////////////////////
	////// VALIDATION INDICATOR FIELDS
	//////////////////////////////////////
	ValidationIndicatorTextColorExclaimation<Location> mValidAddrLineOne = new ValidationIndicatorTextColorExclaimation<Location>(
			R.id.edit_address_line_one);
	ValidationIndicatorTextColorExclaimation<Location> mValidAddrLineTwo = new ValidationIndicatorTextColorExclaimation<Location>(
			R.id.edit_address_line_two);
	ValidationIndicatorTextColorExclaimation<Location> mValidCity = new ValidationIndicatorTextColorExclaimation<Location>(
			R.id.edit_address_city);
	ValidationIndicatorTextColorExclaimation<Location> mValidState = new ValidationIndicatorTextColorExclaimation<Location>(
			R.id.edit_address_state);
	ValidationIndicatorTextColorExclaimation<Location> mValidPostalCode = new ValidationIndicatorTextColorExclaimation<Location>(
			R.id.edit_address_postal_code);

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, Location> mEditAddressLineOne = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_line_one) {

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
						List<String> address = getData().getStreetAddress();
						if (address == null) {
							address = new ArrayList<String>();
						}
						if (address.size() < 1) {
							address.add("");
						}
						address.set(0, s.toString());
						getData().setStreetAddress(address);
					}
					onChange(SectionLocation.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			if (data.getStreetAddress() != null) {
				List<String> address = data.getStreetAddress();
				if (address.size() > 0) {
					field.setText((address.get(0) != null) ? address.get(0) : "");
				}
				else {
					field.setText("");
				}
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<SectionFieldValidIndicator<?, Location>>();
			retArr.add(mValidAddrLineOne);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressLineTwo = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_line_two) {

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
						List<String> address = getData().getStreetAddress();
						if (address == null) {
							address = new ArrayList<String>();
						}
						if (address.size() < 1) {
							address.add("");
						}
						if (address.size() < 2) {
							address.add("");
						}
						address.set(1, s.toString());
						getData().setStreetAddress(address);
					}
					onChange(SectionLocation.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			if (data.getStreetAddress() != null) {
				List<String> address = data.getStreetAddress();
				if (address.size() > 1) {
					field.setText((address.get(1) != null) ? address.get(1) : "");
				}
				else {
					field.setText("");
				}
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<SectionFieldValidIndicator<?, Location>>();
			retArr.add(mValidAddrLineTwo);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressCity = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_city) {
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
						getData().setCity(s.toString());

						//Autofill state and country if major US city is chosen
						String key = s.toString().toLowerCase();
						if (BookingInfoUtils.COMMON_US_CITIES.containsKey(key)) {
							//Set the state
							if (mEditAddressState.hasBoundField()) {
								mEditAddressState.getField().setText(BookingInfoUtils.COMMON_US_CITIES.get(key));
							}

							//Set the country to us
							if (mEditCountrySpinner.hasBoundField()) {
								CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) mEditCountrySpinner
										.getField().getAdapter();
								int pos = countryAdapter.getPositionByCountryName(mContext
										.getString(R.string.country_us));
								if (pos >= 0) {
									mEditCountrySpinner.getField().setSelection(pos);
								}
							}
						}

					}
					onChange(SectionLocation.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			field.setText((data.getCity() != null) ? data.getCity() : "");
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<SectionFieldValidIndicator<?, Location>>();
			retArr.add(mValidCity);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressState = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_state) {
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
						getData().setStateCode(s.toString());
					}
					onChange(SectionLocation.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			field.setText((data.getStateCode() != null) ? data.getStateCode() : "");
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<SectionFieldValidIndicator<?, Location>>();
			retArr.add(mValidState);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressPostalCode = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_postal_code) {
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
						getData().setPostalCode(s.toString());
					}
					onChange(SectionLocation.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			field.setText((data.getPostalCode() != null) ? data.getPostalCode() : "");
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<SectionFieldValidIndicator<?, Location>>();
			retArr.add(mValidPostalCode);
			return retArr;
		}
	};

	SectionFieldEditable<Spinner, Location> mEditCountrySpinner = new SectionFieldEditable<Spinner, Location>(
			R.id.edit_country_spinner) {

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
				getField().setAdapter(new CountrySpinnerAdapter(mContext, CountryDisplayType.FULL_NAME, R.layout.simple_spinner_item_18, R.layout.simple_spinner_dropdown_item));
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (getData() != null) {
						CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) parent.getAdapter();
						getData().setCountryCode(countryAdapter.getItemValue(position, CountryDisplayType.THREE_LETTER));
						updatePostalCodeFormat();
					}
					onChange(SectionLocation.this);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

		}

		protected void updatePostalCodeFormat() {
			if (getData() != null && mEditAddressPostalCode != null) {
				if (mEditAddressPostalCode.hasBoundField()) {
					Log.i("CountryCode:" + getData().getCountryCode());
					if (!TextUtils.isEmpty(getData().getCountryCode()) && getData().getCountryCode().equalsIgnoreCase("USA")) {
						mEditAddressPostalCode.getField().setInputType(InputType.TYPE_CLASS_PHONE);
					}
					else {
						mEditAddressPostalCode.getField().setInputType(InputType.TYPE_CLASS_TEXT);
					}
				}
			}
		}

		@Override
		protected void onHasFieldAndData(Spinner field, Location data) {
			CountrySpinnerAdapter adapter = (CountrySpinnerAdapter) field.getAdapter();
			if (TextUtils.isEmpty(data.getCountryCode())) {
				field.setSelection(adapter.getDefaultLocalePosition());
			}
			else {
				for (int i = 0; i < adapter.getCount(); i++) {
					if (adapter.getItemValue(i, CountryDisplayType.THREE_LETTER)
							.equalsIgnoreCase(data.getCountryCode())) {
						field.setSelection(i);
						break;
					}
				}
			}
		}

		@Override
		protected Validator<Spinner> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			return null;
		}
	};

}
