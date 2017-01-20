package com.expedia.bookings.section;

import com.expedia.bookings.data.extensions.LobExtensionsKt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
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
import com.expedia.bookings.data.RailLocation;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.data.rail.responses.RailTicketDeliveryOption;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.section.CountrySpinnerAdapter.CountryDisplayType;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.widget.SpinnerAdapterWithHint;
import com.mobiata.android.validation.MultiValidator;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

import rx.subjects.BehaviorSubject;

public class SectionLocation extends LinearLayout
	implements ISection<Location>, ISectionEditable, InvalidCharacterListener {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<>();
	SectionFieldList<Location> mFields = new SectionFieldList<>();
	List<String> mCountriesWithStates = Arrays
		.asList(getContext().getResources().getStringArray(R.array.countriesWithStateForBilling));

	Location mLocation;
	Context mContext;
	LineOfBusiness mLineOfBusiness;
	public BehaviorSubject countrySubject = BehaviorSubject.create();

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
		mFields.add(mDisplayAddressCountry);
		mFields.add(mDisplayAddressBothLines);
		mFields.add(mDisplayCityStateZipOneLine);

		//Validation Indicators
		mFields.add(mValidAddrLineOne);
		mFields.add(mValidAddrLineTwo);
		mFields.add(mValidCity);
		mFields.add(mValidState);
		mFields.add(mValidPostalCode);
		mFields.add(mValidDeliveryOption);

		//Edit fields
		mFields.add(mEditAddressLineOne);
		mFields.add(mEditAddressLineTwo);
		mFields.add(mEditAddressCity);
		mFields.add(mEditAddressState);
		mFields.add(mEditAddressPostalCode);
		mFields.add(mEditCountrySpinner);
		mFields.add(mEditDeliveryOptionSpinner);
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

	public Location getLocation() {
		return mLocation;
	}

	@Override
	public boolean performValidation() {
		if (mLineOfBusiness == null) {
			throw new RuntimeException(
				"Attempting to validate the SectionLocation without knowing the LOB. Proper validation requires a LOB to be set");
		}
		return mFields.hasValidInput();
	}

	public int getNumberOfInvalidFields() {
		return mFields.getNumberOfInvalidFields();
	}

	public boolean validateField(int fieldId) {
		return mFields.hasValidInput(fieldId);
	}

	public void resetValidation() {
		mFields.setValidationIndicatorState(true);
	}

	public void resetValidation(int fieldID, boolean status) {
		mFields.setValidationIndicatorState(fieldID, status);
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
		if (mLineOfBusiness == LineOfBusiness.FLIGHTS) {
			return true;
		}
		CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) mEditCountrySpinner.mField.getAdapter();
		String selectedCountryCode = countryAdapter
			.getItemValue(mEditCountrySpinner.mField.getSelectedItemPosition(), CountryDisplayType.THREE_LETTER);
		return mCountriesWithStates.contains(selectedCountryCode);
	}

	protected void rebindCountryDependantFields() {
		mEditAddressPostalCode.bindData(mLocation);
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
	ValidationIndicatorExclamationSpinner<Location> mValidDeliveryOption = new ValidationIndicatorExclamationSpinner<>(
		R.id.edit_delivery_option_spinner);

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
			if (isStateRequired()) {
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
			field.setFilters(new InputFilter[] { new InputFilter() {
					public CharSequence filter(CharSequence src, int start,
						int end, Spanned dst, int dstart, int dend) {
						if (src.equals("")) { // for backspace
							return src;
						}
						if (src.toString().matches("[a-zA-Z]+")) {
							return src;
						}
						return "";
					}
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

	private boolean isRails() {
		return mLineOfBusiness.equals(LineOfBusiness.RAILS);
	}

	SectionFieldEditable<EditText, Location> mEditAddressPostalCode
		= new SectionFieldEditableFocusChangeTrimmer<EditText, Location>(R.id.edit_address_postal_code) {

		Validator<EditText> mPostalCodeCharacterCountValidator = new Validator<EditText>() {

//			Allow anything between 1 and 20 characters if required based on billing country
			Pattern mPattern = Pattern.compile("^(.{1,20})?$");

			@Override
			public int validate(EditText obj) {
				if (obj == null) {
					return ValidationError.ERROR_DATA_MISSING;
				}
				else {
					String text = obj.getText().toString();
					if (isRails()) {
						if (text.isEmpty() || text.length() > 15) {
							return ValidationError.ERROR_DATA_INVALID;
						}
					}
					else {
						if (!mPattern.matcher(text).matches()) {
							return ValidationError.ERROR_DATA_INVALID;
						}
					}
					return ValidationError.NO_ERROR;
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
		if (LobExtensionsKt.hasBillingInfo(mLineOfBusiness)) {
			CountrySpinnerAdapter countryAdapter = (CountrySpinnerAdapter) mEditCountrySpinner.mField.getAdapter();
			String selectedCountry = countryAdapter.getItemValue(mEditCountrySpinner.mField.getSelectedItemPosition(),
				CountryDisplayType.THREE_LETTER);
			return PointOfSale.countryPaymentRequiresPostalCode(selectedCountry);
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
				updateCountryDependantValidation();
				rebindCountryDependantFields();
				if (mEditAddressState.mField != null) {
					String countryCode = getData().getCountryCode();
					countrySubject.onNext(countryCode);
					if (countryCode.equals(mCountriesWithStates.get(0))) {
						mEditAddressState.mField.setHint(R.string.address_state_hint);
						mEditAddressState.mField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
					}
					else if (countryCode.equals(mCountriesWithStates.get(1))) {
						mEditAddressState.mField.setHint(R.string.address_province_hint);
						mEditAddressState.mField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
					}
					else {
						mEditAddressState.mField.setHint(R.string.address_county_hint);
						mEditAddressState.mField.setInputType(InputType.TYPE_CLASS_TEXT);
					}
				}
			}
		}

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				CountrySpinnerAdapter countryAdapter = new CountrySpinnerAdapter(mContext, CountryDisplayType.FULL_NAME,
					R.layout.simple_spinner_item_18, R.layout.simple_spinner_dropdown_item, false);
				getField().setAdapter(countryAdapter);
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
			if (data instanceof RailLocation && ((RailLocation) data).getTicketDeliveryCountryCodes() != null) {
				adapter.dataSetChanged(((RailLocation) data).getTicketDeliveryCountryCodes());
			}
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

	SectionFieldEditable<RailDeliverySpinnerWithValidationIndicator, Location> mEditDeliveryOptionSpinner
		= new SectionFieldEditable<RailDeliverySpinnerWithValidationIndicator, Location>(R.id.edit_delivery_option_spinner) {

		Validator<RailDeliverySpinnerWithValidationIndicator> mValidator = new Validator<RailDeliverySpinnerWithValidationIndicator>() {
			@Override
			public int validate(RailDeliverySpinnerWithValidationIndicator spinnerWithValidationIndicator) {
				SpinnerAdapterWithHint.SpinnerItem selection = (SpinnerAdapterWithHint.SpinnerItem) spinnerWithValidationIndicator
					.getSpinner().getSelectedItem();
				if (hint.equals(selection.getValue())) {
					return ValidationError.ERROR_DATA_MISSING;
				}
				else {
					return ValidationError.NO_ERROR;
				}
			}
		};

		String hint = getContext().getResources().getString(R.string.address_mail_delivery_option_hint);

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				SpinnerAdapterWithHint deliveryOptionsAdapter = new SpinnerAdapterWithHint(mContext, hint,
					R.layout.snippet_rail_delivery_option_text_view, R.layout.rail_delivery_option_dropdown_item,
					R.id.rail_delivery_drop_down_text);
				getField().getSpinner().setAdapter(deliveryOptionsAdapter);
			}
		}

		@Override
		public void setChangeListener(RailDeliverySpinnerWithValidationIndicator field) {

			field.getSpinner().setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					SpinnerAdapterWithHint deliveryOptionsAdapter = (SpinnerAdapterWithHint) getField().getSpinner().getAdapter();
					SpinnerAdapterWithHint.SpinnerItem selected = deliveryOptionsAdapter.getItem(position);
					RailLocation railLocation = (RailLocation) getData();
					if (hint.equals(selected.getValue())) {
						railLocation.setTicketDeliveryOptionSelected(null);
					}
					else {
						railLocation.setTicketDeliveryOptionSelected(
							(RailTicketDeliveryOption) selected.getItem());
					}
					onChange(SectionLocation.this);

					mEditAddressLineOne.getField().requestFocus();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		}

		@Override
		protected void onHasFieldAndData(final RailDeliverySpinnerWithValidationIndicator field, Location data) {
			Spinner spinner = field.getSpinner();
			RailLocation railLocation = (RailLocation) data;
			if (railLocation.getTickerDeliveryOptions() != null) {
				final SpinnerAdapterWithHint deliveryOptionsAdapter = (SpinnerAdapterWithHint) spinner.getAdapter();
				deliveryOptionsAdapter.dataSetChanged(railLocation.getTickerDeliveryOptions());
				spinner.setAdapter(deliveryOptionsAdapter);
				spinner.setSelection(deliveryOptionsAdapter.getCount());
			}
		}

		@Override
		protected Validator<RailDeliverySpinnerWithValidationIndicator> getValidator() {
			return mValidator;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, Location>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, Location>> retArr = new ArrayList<>();
			retArr.add(mValidDeliveryOption);
			return retArr;
		}
	};
}
