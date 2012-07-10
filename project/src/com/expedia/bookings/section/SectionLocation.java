package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
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
		for (SectionField<?, Location> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				SectionFieldEditable<?, Location> editable = (SectionFieldEditable<?, Location>) field;
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

	SectionField<TextView, Location> mDisplayAddressLineOne = new SectionField<TextView, Location>(
			R.id.display_address_line_one) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			List<String> address = data.getStreetAddress();
			if (address != null && address.size() > 0) {
				if (!TextUtils.isEmpty(address.get(0))) {
					field.setText(address.get(0));
				}
			}
		}
	};

	SectionField<TextView, Location> mDisplayAddressCity = new SectionField<TextView, Location>(
			R.id.display_address_city) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			if (!TextUtils.isEmpty(data.getCity())) {
				field.setText(data.getCity());
			}
		}
	};

	SectionField<TextView, Location> mDisplayAddressState = new SectionField<TextView, Location>(
			R.id.display_address_state) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			if (!TextUtils.isEmpty(data.getStateCode())) {
				field.setText(data.getStateCode());
			}
		}
	};

	SectionField<TextView, Location> mDisplayAddressPostalCode = new SectionField<TextView, Location>(
			R.id.display_address_postal_code) {
		@Override
		public void onHasFieldAndData(TextView field, Location data) {
			if (!TextUtils.isEmpty(data.getPostalCode())) {
				field.setText(data.getPostalCode());
			}
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, Location> mEditAddressLineOne = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_line_one) {
		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				if (field.getText().length() == 0) {
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
					SectionLocation.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			if (data.getStreetAddress() != null) {
				List<String> address = data.getStreetAddress();
				if (address.size() > 0) {
					if (!TextUtils.isEmpty(address.get(0))) {
						field.setText(address.get(0));
					}
				}
			}
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressLineTwo = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_line_two) {
		@Override
		protected boolean hasValidInput(EditText field) {
			//Line two is not required
			return true;
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
					SectionLocation.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			if (data.getStreetAddress() != null) {
				List<String> address = data.getStreetAddress();
				if (address.size() > 1) {
					if (!TextUtils.isEmpty(address.get(1))) {
						field.setText(address.get(1));
					}
				}
			}
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressCity = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_city) {
		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				if (field.getText().length() == 0) {
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
						getData().setCity(s.toString());
					}
					SectionLocation.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			if (!TextUtils.isEmpty(data.getCity())) {
				field.setText(data.getCity());
			}
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressState = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_state) {
		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				if (field.getText().length() == 0) {
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
						getData().setStateCode(s.toString());
					}
					SectionLocation.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			if (!TextUtils.isEmpty(data.getStateCode())) {
				field.setText(data.getStateCode());
			}
		}
	};

	SectionFieldEditable<EditText, Location> mEditAddressPostalCode = new SectionFieldEditable<EditText, Location>(
			R.id.edit_address_postal_code) {
		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				if (field.getText().length() == 0) {
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
						getData().setPostalCode(s.toString());
					}
					SectionLocation.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, Location data) {
			if (!TextUtils.isEmpty(data.getPostalCode())) {
				field.setText(data.getPostalCode());
			}
		}
	};

}
