package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.mobiata.android.validation.Validator;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;

public class SectionLocation extends LinearLayout implements ISection<Location>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	ArrayList<SectionField<?, Location>> mFields = new ArrayList<SectionField<?, Location>>();

	Location mLocation;

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
		//Display fields
		mFields.add(this.mDisplayAddressLineOne);
		mFields.add(this.mDisplayAddressCity);
		mFields.add(this.mDisplayAddressState);
		mFields.add(this.mDisplayAddressPostalCode);

		//Edit fields
		mFields.add(this.mEditAddressLineOne);
		mFields.add(this.mEditAddressLineTwo);
		mFields.add(this.mEditAddressCity);
		mFields.add(this.mEditAddressState);
		mFields.add(this.mEditAddressPostalCode);
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

	SectionField<TextView, Location> mDisplayAddressLineOne = new SectionField<TextView, Location>(
			R.id.display_address_line_one) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			List<String> address = data.getStreetAddress();
			if (address != null && address.size() > 0) {
				field.setText((address.get(0) != null) ? address.get(0) : "");
			}
		}
	};

	SectionField<TextView, Location> mDisplayAddressCity = new SectionField<TextView, Location>(
			R.id.display_address_city) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			field.setText((data.getCity() != null) ? data.getCity() : "");
		}
	};

	SectionField<TextView, Location> mDisplayAddressState = new SectionField<TextView, Location>(
			R.id.display_address_state) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			field.setText((data.getStateCode() != null) ? data.getStateCode() : "");
		}
	};

	SectionField<TextView, Location> mDisplayAddressPostalCode = new SectionField<TextView, Location>(
			R.id.display_address_postal_code) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			field.setText((data.getPostalCode() != null) ? data.getPostalCode() : "");
		}
	};

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
			// TODO Auto-generated method stub
			return null;
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
			// TODO Auto-generated method stub
			return null;
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
			// TODO Auto-generated method stub
			return null;
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
			// TODO Auto-generated method stub
			return null;
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
			// TODO Auto-generated method stub
			return null;
		}
	};

}
