package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.mobiata.android.Log;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SectionBillingInfo extends LinearLayout implements ISection<BillingInfo>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();
	ArrayList<SectionField<?, BillingInfo>> mFields = new ArrayList<SectionField<?, BillingInfo>>();

	Context mContext;

	BillingInfo mBillingInfo;

	public SectionBillingInfo(Context context) {
		super(context);
		init(context);
	}

	public SectionBillingInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionBillingInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		//Display fields
		mFields.add(this.mDisplayCreditCardBrandIcon);
		mFields.add(this.mDisplayCreditCardExpiration);
		mFields.add(this.mDisplayCreditCardNumber);
		mFields.add(this.mDisplayCreditCardNumberMasked);
		mFields.add(this.mDisplayCreditCardSecurityCode);
		mFields.add(this.mDisplayCreditCardSecurityCodeInfo);
		mFields.add(this.mDisplayFullName);
		mFields.add(this.mDisplayEmailAddress);
		mFields.add(this.mDisplayPhoneNumber);

		//Edit fields
		mFields.add(this.mEditCreditCardExpiration);
		mFields.add(this.mEditCreditCardNumber);
		mFields.add(this.mEditCreditCardSecurityCode);
		mFields.add(this.mEditCreditCardType);
		mFields.add(this.mEditFirstName);
		mFields.add(this.mEditLastName);
		mFields.add(this.mEditEmailAddress);
		mFields.add(this.mEditPhoneNumber);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, BillingInfo> field : mFields) {
			field.bindField(this);
		}

	}

	@Override
	public void bind(BillingInfo data) {
		mBillingInfo = (BillingInfo) data;

		if (mBillingInfo != null) {
			for (SectionField<?, BillingInfo> field : mFields) {
				field.bindData(mBillingInfo);
			}
		}
	}

	public boolean hasValidInput() {
		boolean valid = true;
		for (SectionField<?, BillingInfo> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				SectionFieldEditable<?, BillingInfo> editable = (SectionFieldEditable<?, BillingInfo>) field;
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

	SectionField<TextView, BillingInfo> mDisplayCreditCardNumber = new SectionField<TextView, BillingInfo>(
			R.id.display_creditcard_number) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getNumber())) {
				field.setText(data.getNumber());
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayCreditCardNumberMasked = new SectionField<TextView, BillingInfo>(
			R.id.display_creditcard_number_masked) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getNumber())) {
				if (data.getNumber().length() > 4) {
					String lastFourDigits = data.getNumber().substring(data.getNumber().length() - 4);
					String brandName = (!TextUtils.isEmpty(data.getBrandName())) ? data.getBrandName() : "";
					field.setText(String.format(getResources().getString(R.string.blanked_out_credit_card_TEMPLATE),
							brandName, lastFourDigits));
				}
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayCreditCardSecurityCode = new SectionField<TextView, BillingInfo>(
			R.id.display_creditcard_security_code) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getSecurityCode())) {
				field.setText(data.getSecurityCode());
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayCreditCardExpiration = new SectionField<TextView, BillingInfo>(
			R.id.display_creditcard_expiration) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (data.getExpirationDate() != null) {
				//TODO:Don't hardcode this format string..
				field.setText(String.format("%02d/%02d", mBillingInfo.getExpirationDate().get(Calendar.MONTH),
						mBillingInfo
								.getExpirationDate().get(Calendar.YEAR)));
			}
		}
	};

	SectionField<ImageView, BillingInfo> mDisplayCreditCardBrandIcon = new SectionField<ImageView, BillingInfo>(
			R.id.display_creditcard_brand_icon) {
		@Override
		public void onHasFieldAndData(ImageView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getBrandName())) {
				CreditCardType cardType = CreditCardType.valueOf(data.getBrandName());
				if (cardType != null) {
					field.setImageResource(BookingInfoUtils.CREDIT_CARD_ICONS.get(cardType));
				}
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayCreditCardSecurityCodeInfo = new SectionField<TextView, BillingInfo>(
			R.id.display_creditcard_security_code_info) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getBrandName())) {
				CreditCardType cardType = CreditCardType.valueOf(data.getBrandName());
				if (cardType != null) {
					field.setText(BookingInfoUtils.CREDIT_CARD_SECURITY_LOCATION.get(cardType));
				}
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayFullName = new SectionField<TextView, BillingInfo>(
			R.id.display_full_name) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getFirstName()) || !TextUtils.isEmpty(data.getLastName())) {
				String fullName = "";
				fullName += (!TextUtils.isEmpty(data.getFirstName())) ? data.getFirstName() + " " : "";
				fullName += (!TextUtils.isEmpty(data.getLastName())) ? data.getLastName() : "";
				fullName = fullName.trim();
				field.setText(fullName);
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayEmailAddress = new SectionField<TextView, BillingInfo>(
			R.id.display_email_address) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getEmail())) {
				field.setText(data.getEmail());
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayPhoneNumber = new SectionField<TextView, BillingInfo>(
			R.id.display_phone_number) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getTelephone())) {
				//TODO:Format phone number
				field.setText(data.getTelephone());
			}
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, BillingInfo> mEditCreditCardNumber = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_creditcard_number) {
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
						getData().setNumber(s.toString());
					}
					SectionBillingInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getNumber())) {
				field.setText(data.getNumber());
			}
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditCreditCardExpiration = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_creditcard_expiration) {

		//TODO: This whole class needs better (localized) text to cal and visaversa conversion

		@Override
		protected boolean hasValidInput(EditText field) {
			if (field != null) {
				if (field.getText().length() == 0) {
					return false;
				}
				//TODO: Bad validator
				if (!field.getText().toString().matches("^\\d{1,2}/\\d{2}$")) {
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
						getData().setExpirationDate(getCalFromExpStr(s.toString()));
					}
					SectionBillingInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (data.getExpirationDate() != null) {
				//TODO:No hardcoded formatters!!!
				String formattedExpr = String.format("%02d/%02d", data.getExpirationDate().get(Calendar.MONTH), data
						.getExpirationDate().get(Calendar.YEAR));
				field.setText(formattedExpr);
			}
		}

		private Calendar getCalFromExpStr(String exp) {
			//TODO:This is not localized at all...
			if (TextUtils.isEmpty(exp)) {
				return null;
			}
			if (exp.contains("/")) {
				String[] splitStr = exp.split("/");
				if (splitStr.length != 2) {
					Log.e("split != 2");
					return null;
				}
				else {
					String cleanMonth = splitStr[0].replace("/", "").trim();
					String cleanYear = splitStr[1].replace("/", "").trim();

					int month = Integer.parseInt(cleanMonth);
					int year = Integer.parseInt(cleanYear);

					return new GregorianCalendar(year, month, 1);
				}
			}
			return null;
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditCreditCardSecurityCode = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_creditcard_security_code) {
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
						getData().setSecurityCode(s.toString());
					}
					SectionBillingInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getSecurityCode())) {
				field.setText(data.getSecurityCode());
			}
		}
	};

	SectionFieldEditable<Spinner, BillingInfo> mEditCreditCardType = new SectionFieldEditable<Spinner, BillingInfo>(
			R.id.edit_creditcard_type_spinner) {

		@Override
		protected void onFieldBind() {
			super.onFieldBind();
			if (hasBoundField()) {
				getField().setAdapter(new ArrayAdapter<CreditCardType>(SectionBillingInfo.this.mContext,
						android.R.layout.simple_list_item_1, CreditCardType.values()));
			}
		}

		@Override
		protected boolean hasValidInput(Spinner field) {
			return true;
		}

		@Override
		public void setChangeListener(Spinner field) {
			field.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					// TODO Auto-generated method stub
					if (getData() != null) {
						CreditCardType type = (CreditCardType) parent.getItemAtPosition(pos);
						getData().setBrandCode(type.getCode());
						getData().setBrandName(type.name());

						//Updates the card icon
						bind(getData());

						onChange();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}

		@Override
		protected void onHasFieldAndData(Spinner field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getBrandName())) {
				CreditCardType cardType = CreditCardType.valueOf(data.getBrandName());
				if (cardType != null) {
					@SuppressWarnings("unchecked")
					ArrayAdapter<CreditCardType> cardBrandAdapter = (ArrayAdapter<CreditCardType>) field
							.getAdapter();
					if (cardBrandAdapter != null) {
						int pos = cardBrandAdapter.getPosition(cardType);
						field.setSelection(pos);
					}
				}
			}
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditFirstName = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_first_name) {
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
						getData().setFirstName(s.toString());
					}
					SectionBillingInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getFirstName())) {
				field.setText(data.getFirstName());
			}
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditLastName = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_last_name) {
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
						getData().setLastName(s.toString());
					}
					SectionBillingInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getLastName())) {
				field.setText(data.getLastName());
			}
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditEmailAddress = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_email_address) {
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
						getData().setEmail(s.toString());
					}
					SectionBillingInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getEmail())) {
				field.setText(data.getEmail());
			}
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditPhoneNumber = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_phone_number) {
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
						getData().setTelephone(s.toString());
					}
					SectionBillingInfo.this.onChange();
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getTelephone())) {
				field.setText(data.getTelephone());
			}
		}
	};

}
