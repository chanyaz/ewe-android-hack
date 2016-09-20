package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.section.CountrySpinnerAdapter.CountryDisplayType;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.mobiata.android.validation.MultiValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

public class SectionLocation extends LinearLayout
	implements ISection<Location>, ISectionEditable, InvalidCharacterListener {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<>();
	SectionFieldList<Location> mFields = new SectionFieldList<>();

	Location mLocation;
	Context mContext;
	LineOfBusiness mLineOfBusiness;

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

		mFields.bindFieldsAll(this);
	}

	@Override
	public void bind(Location location) {
		//Update fields
		mLocation = location;

		if (mLocation != null) {
			mFields.bindDataAll(mLocation);
		}
	}

	public void setLineOfBusiness(LineOfBusiness lob) {
		mLineOfBusiness = lob;
	}

	@Override
	public boolean performValidation() {
		if (mLineOfBusiness == null) {
			throw new RuntimeException(
				"Attempting to validate the SectionLocation without knowing the LOB. Proper validation requires a LOB to be set");
		}
		return mFields.hasValidInput();
	}

	public void resetValidation() {
		mFields.setValidationIndicatorState(true);
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

	public boolean isStateRequired() {
		if (mLineOfBusiness != LineOfBusiness.RAILS) {  //Just for rails for now...
			return true;
		}
		CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) mEditCountrySpinner.mField.getAdapter();
		String selectedCountryCode = countryAdapter
			.getItemValue(mEditCountrySpinner.mField.getSelectedItemPosition(), CountryDisplayType.THREE_LETTER);

		List<String> countriesWithStates = Arrays
			.asList(getContext().getResources().getStringArray(R.array.countriesWithStateForBilling));
		return countriesWithStates.contains(selectedCountryCode);
	}

	protected void rebindCountryDependantFields() {
		mEditAddressPostalCode.bindData(mLocation);
	}

	protected void showHideCountryDependantFields() {
		if (isStateRequired()) {
			mFields.setFieldEnabled(mEditAddressState, true);
		}
		else {
			mFields.removeField(mEditAddressState);
		}
	}

	//////////////////////////////////////
	//////INVALID CHARACTER STUFF
	//////////////////////////////////////

	ArrayList<InvalidCharacterListener> mInvalidCharacterListeners = new ArrayList<>();

	@Override
	public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
		for (InvalidCharacterListener listener : mInvalidCharacterListeners) {
			listener.onInvalidCharacterEntered(text, mode);
		}
	}

	public void addInvalidCharacterListener(InvalidCharacterListener listener) {
		mInvalidCharacterListeners.add(listener);
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
					if (addrStr.endsWith(",")) {
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
	ValidationIndicatorExclaimation<Location> mValidAddrLineOne = new ValidationIndicatorExclaimation<>(
		R.id.edit_address_line_one);
	ValidationIndicatorExclaimation<Location> mValidAddrLineTwo = new ValidationIndicatorExclaimation<>(
		R.id.edit_address_line_two);
	ValidationIndicatorExclaimation<Location> mValidCity = new ValidationIndicatorExclaimation<>(
		R.id.edit_address_city);
	ValidationIndicatorExclaimation<Location> mValidState = new ValidationIndicatorExclaimation<>(
		R.id.edit_address_state);
	ValidationIndicatorExclaimation<Location> mValidPostalCode = new ValidationIndicatorExclaimation<>(
		R.id.edit_address_postal_code);

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, Location> mEditAddressLineOne = new SectionFieldEditableFocusChangeTrimmer<EditText, Location>(
		R.id.edit_address_line_one) {

		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> addrValidators = new MultiValidator<>();
			addrValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_ASCII);
			addrValidators.addValidator(CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET);
			return addrValidators;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						List<String> address = getData().getStreetAddress();
						if (address == null) {
							address = new ArrayList<>();
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

			InvalidCharacterHelper
				.generateInvalidCharacterTextWatcher(field, SectionLocation.this, Mode.ADDRESS);
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
			else {
				field.setText("");
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<>();
			retArr.add(mValidAddrLineOne);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressLineTwo = new SectionFieldEditableFocusChangeTrimmer<EditText, Location>(
		R.id.edit_address_line_two) {

		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> addrValidators = new MultiValidator<>();
			addrValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_ASCII);
			addrValidators.addValidator(CommonSectionValidators.ALWAYS_VALID_VALIDATOR_ET);
			return addrValidators;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						List<String> address = getData().getStreetAddress();
						if (address == null) {
							address = new ArrayList<>();
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

			InvalidCharacterHelper
				.generateInvalidCharacterTextWatcher(field, SectionLocation.this, Mode.ADDRESS);
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
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<>();
			retArr.add(mValidAddrLineTwo);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressCity = new SectionFieldEditableFocusChangeTrimmer<EditText, Location>(
		R.id.edit_address_city) {
		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> addrValidators = new MultiValidator<>();
			addrValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_ASCII);
			addrValidators.addValidator(CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET);
			return addrValidators;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setCity(s.toString());
					}
					onChange(SectionLocation.this);
				}
			});

			InvalidCharacterHelper.generateInvalidCharacterTextWatcher(field, SectionLocation.this, Mode.ADDRESS);
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			field.setText((data.getCity() != null) ? data.getCity() : "");
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<>();
			retArr.add(mValidCity);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressState
		= new SectionFieldEditableFocusChangeTrimmer<EditText, Location>(R.id.edit_address_state) {

		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> addrValidators = new MultiValidator<>();
			addrValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_ASCII);
			if (mLineOfBusiness == LineOfBusiness.FLIGHTS || mLineOfBusiness == LineOfBusiness.PACKAGES) {
				addrValidators.addValidator(CommonSectionValidators.ADDRESS_STATE_VALIDATOR);
			}
			else {
				addrValidators.addValidator(CommonSectionValidators.ALWAYS_VALID_VALIDATOR_ET);
			}
			return addrValidators;
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

			InvalidCharacterHelper.generateInvalidCharacterTextWatcher(field, SectionLocation.this, Mode.ADDRESS);
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			field.setText((data.getStateCode() != null) ? data.getStateCode() : "");
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<>();
			retArr.add(mValidState);
			return retArr;
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressPostalCode
		= new SectionFieldEditableFocusChangeTrimmer<EditText, Location>(R.id.edit_address_postal_code) {

		Validator<EditText> mPostalCodeCharacterCountValidator = new Validator<EditText>() {

			//Allow anything between 1 and 15 characters OR blank (this should match the api)
			Pattern mPattern = Pattern.compile("^(.{1,15})?$");

			@Override
			public int validate(EditText obj) {
				if (obj == null || obj.getText() == null) {
					return ValidationError.ERROR_DATA_MISSING;
				}
				else {
					if (mPattern.matcher(obj.getText()).matches()) {
						return ValidationError.NO_ERROR;
					}
					else {
						return ValidationError.ERROR_DATA_INVALID;
					}
				}
			}
		};

		Validator<EditText> mCurrentCountryValidator = new Validator<EditText>() {
			@Override
			public int validate(EditText obj) {
				if (requiresPostalCode()) {
					return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET.validate(obj);
				}
				else {
					return ValidationError.NO_ERROR;
				}
			}

		};

		@Override
		protected Validator<EditText> getValidator() {
			MultiValidator<EditText> postalCodeValidators = new MultiValidator<>();
			postalCodeValidators.addValidator(mPostalCodeCharacterCountValidator);
			postalCodeValidators.addValidator(CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_ASCII);
			postalCodeValidators.addValidator(mCurrentCountryValidator);
			return postalCodeValidators;
		}

		@Override
		public void bindData(Location location) {
			super.bindData(location);
			updatePostalCodeInput();
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

			InvalidCharacterHelper.generateInvalidCharacterTextWatcher(field, SectionLocation.this, Mode.ADDRESS);
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			field.setText((data.getPostalCode() != null) ? data.getPostalCode() : "");
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<>();
			retArr.add(mValidPostalCode);
			return retArr;
		}

		private void updatePostalCodeInput() {
			if (this.hasBoundField()) {
				Location location = this.getData();
				PointOfSaleId posId = PointOfSale.getPointOfSale().getPointOfSaleId();
				//If we set the country to USA (or we dont select a country, but POS is USA) use the number keyboard and set hint to use zip code (instead of postal code)
				if ((location != null && location.getCountryCode() != null
					&& location.getCountryCode().equalsIgnoreCase("USA"))
					|| (!mEditCountrySpinner.hasBoundField() && posId == ProductFlavorFeatureConfiguration.getInstance()
					.getUSPointOfSaleId())) {
					this.getField().setInputType(InputType.TYPE_CLASS_NUMBER);
					if (mLineOfBusiness == LineOfBusiness.PACKAGES) {
						this.getField().setHint(R.string.address_zip_code_hint);
					}
					else {
						this.getField().setHint(R.string.address_postal_code_hint_US);
					}
				}
				else {
					this.getField().setInputType(InputType.TYPE_CLASS_TEXT);
					this.getField().setHint(R.string.address_postal_code_hint);
				}
			}
		}
	};

	/**
	 * Expedia has complicated logic for required payment fields. It differs for the country of payment billing.
	 * Postal code seems to have the most complicated logic, so I encapsulate this logic in a helper method.
	 */
	private boolean requiresPostalCode() {
		// #1056. Postal code check depends on the country, of billing, selected.
		if (mLineOfBusiness == LineOfBusiness.FLIGHTS) {
			CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) mEditCountrySpinner.mField.getAdapter();
			String selectedCountry = countryAdapter.getItemValue(mEditCountrySpinner.mField.getSelectedItemPosition(),
				CountryDisplayType.THREE_LETTER);
			return PointOfSale.countryPaymentRequiresPostalCode(selectedCountry);
		}

		if (mLineOfBusiness == LineOfBusiness.PACKAGES) {
			// TODO Check with product for PACKAGES lob postal code restrictions.
			return true;
		}
		return false;
	}

	SectionFieldEditable<Spinner, Location> mEditCountrySpinner
		= new SectionFieldEditable<Spinner, Location>(R.id.edit_country_spinner) {

		private boolean mSetFieldManually = false;

		Validator<Spinner> mValidator = new Validator<Spinner>() {
			@Override
			public int validate(Spinner obj) {
				return ValidationError.NO_ERROR;
			}
		};

		private void updateData(int position) {
			if (getData() != null && getField() != null) {
				CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) getField().getAdapter();
				getData()
					.setCountryCode(countryAdapter.getItemValue(position, CountryDisplayType.THREE_LETTER));
				showHideCountryDependantFields();
				updateCountryDependantValidation();
				rebindCountryDependantFields();
			}
		}

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				getField().setAdapter(
					new CountrySpinnerAdapter(mContext, CountryDisplayType.FULL_NAME,
						R.layout.simple_spinner_item_18, R.layout.simple_spinner_dropdown_item, false));
			}
		}

		@Override
		public void setChangeListener(Spinner field) {

			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					updateData(position);

					if (!mSetFieldManually) {
						onChange(SectionLocation.this);
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

		protected void updateCountryDependantValidation() {
			// Force the postal code section to update its validator
			mEditAddressPostalCode.onChange(null);

			// Force the State/Province section to update its validator
			mEditAddressState.onChange(null);
		}

		@Override
		protected void onHasFieldAndData(Spinner field, Location data) {
			CountrySpinnerAdapter adapter = (CountrySpinnerAdapter) field.getAdapter();
			if (TextUtils.isEmpty(data.getCountryCode())) {
				int localePosition = adapter.getDefaultLocalePosition();
				field.setSelection(localePosition);
				updateData(localePosition);
				mSetFieldManually = true;
			}
			else {
				for (int i = 0; i < adapter.getCount(); i++) {
					if (adapter.getItemValue(i, CountryDisplayType.THREE_LETTER)
						.equalsIgnoreCase(data.getCountryCode())) {
						field.setSelection(i);
						updateData(i);
						mSetFieldManually = true;
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
