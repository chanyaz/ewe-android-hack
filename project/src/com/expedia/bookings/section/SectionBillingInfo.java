package com.expedia.bookings.section;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.mobiata.android.Log;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
		mFields.add(this.mDisplayCreditCardBrandName);
		mFields.add(this.mDisplayAddress);

		//Edit fields
		mFields.add(this.mEditCreditCardExpiration);
		mFields.add(this.mEditCreditCardNumber);
		mFields.add(this.mEditCreditCardSecurityCode);
		mFields.add(this.mEditFirstName);
		mFields.add(this.mEditLastName);
		mFields.add(this.mEditEmailAddress);
		mFields.add(this.mEditPhoneNumber);
	}

	/***
	 * Helper method, so when we update the card number we don't rebind everything
	 */
	private void rebindNumDependantFields() {
		mDisplayCreditCardNumber.bindData(mBillingInfo);
		mDisplayCreditCardNumberMasked.bindData(mBillingInfo);
		mDisplayCreditCardBrandIcon.bindData(mBillingInfo);
		mDisplayCreditCardBrandName.bindData(mBillingInfo);
		mDisplayCreditCardSecurityCodeInfo.bindData(mBillingInfo);
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
			field.setText((data.getNumber() != null) ? data.getNumber() : "");
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
					field.setText(
							Html.fromHtml(String.format(
									getResources().getString(R.string.blanked_out_credit_card_TEMPLATE),
									brandName, lastFourDigits)), TextView.BufferType.SPANNABLE);
				}
			}
			else {
				field.setText("");
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayCreditCardSecurityCode = new SectionField<TextView, BillingInfo>(
			R.id.display_creditcard_security_code) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			field.setText((data.getSecurityCode() != null) ? data.getSecurityCode() : "");
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
			else {
				field.setText("");
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
				else {
					field.setImageResource(R.drawable.ic_cc_unknown);
				}
			}
			else {
				field.setImageResource(R.drawable.ic_cc_unknown);
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
				else {
					field.setText("");
				}
			}
			else {
				field.setText("");
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
			else {
				field.setText("");
			}
		}
	};

	SectionField<TextView, BillingInfo> mDisplayEmailAddress = new SectionField<TextView, BillingInfo>(
			R.id.display_email_address) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			field.setText((data.getEmail() != null) ? data.getEmail() : "");
		}
	};

	SectionField<TextView, BillingInfo> mDisplayPhoneNumber = new SectionField<TextView, BillingInfo>(
			R.id.display_phone_number) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			field.setText((data.getTelephone() != null) ? data.getTelephone() : "");
		}
	};

	SectionField<TextView, BillingInfo> mDisplayCreditCardBrandName = new SectionField<TextView, BillingInfo>(
			R.id.display_creditcard_brand_name) {
		@Override
		public void onHasFieldAndData(TextView field, BillingInfo data) {
			field.setText((data.getBrandName() != null) ? data.getBrandName() : "");
		}
	};

	SectionField<SectionLocation, BillingInfo> mDisplayAddress = new SectionField<SectionLocation, BillingInfo>(
			R.id.section_location_address) {
		@Override
		public void onHasFieldAndData(SectionLocation field, BillingInfo data) {
			if (data.getLocation() != null) {
				field.bind(data.getLocation());
			}
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

	SectionFieldEditable<EditText, BillingInfo> mEditCreditCardNumber = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_creditcard_number) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						if (getData().getNumber() == null || !s.toString().equalsIgnoreCase(getData().getNumber())) {
							getData().setNumber(s.toString());

							CreditCardType type = CurrencyUtils.detectCreditCardBrand(mContext, getData().getNumber());
							if (type == null) {
								getData().setBrandCode(null);
								getData().setBrandName(null);
							}
							else {
								getData().setBrandCode(type.getCode());
								getData().setBrandName(type.name());
							}
							rebindNumDependantFields();
						}
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getNumber())) {
				field.setText(data.getNumber());
			}
		}

		@Override
		protected Validator<EditText> getValidator() {
			//Inline so that we can get the context
			return new Validator<EditText>() {
				@Override
				public int validate(EditText obj) {
					if (obj == null) {
						return ValidationError.ERROR_DATA_MISSING;
					}
					else {
						CreditCardType type = CurrencyUtils.detectCreditCardBrand(mContext, obj.getText().toString()
								.trim());
						if (type == null) {
							return ValidationError.ERROR_DATA_INVALID;
						}
						else {
							return ValidationError.NO_ERROR;
						}
					}
				}
			};
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditCreditCardExpiration = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_creditcard_expiration) {

		//TODO: This whole class needs better (localized) text to cal and visaversa conversion

		DateFormat mExpParser = new SimpleDateFormat("MM/yy");
		
		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.EXPIRATION_DATE_VALIDATOR_ET;
		}

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setExpirationDate(getCalFromExpStr(s.toString()));
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (data.getExpirationDate() != null) {
				String formattedExpr = mExpParser.format(data.getExpirationDate().getTime());
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
						
						Calendar cal = Calendar.getInstance();
						try {
							cal.setTime(mExpParser.parse(exp));
							return cal;
						}
						catch (ParseException e) {
							Log.e("Date Parse Error!");
							return null;
						}
				}
			}
			return null;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditCreditCardSecurityCode = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_creditcard_security_code) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setSecurityCode(s.toString());
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getSecurityCode())) {
				field.setText(data.getSecurityCode());
			}
			else {
				field.setText("");
			}
		}

		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditFirstName = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_first_name) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setFirstName(s.toString());
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getFirstName())) {
				field.setText(data.getFirstName());
			}
		}

		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditLastName = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_last_name) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setLastName(s.toString());
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getLastName())) {
				field.setText(data.getLastName());
			}
		}

		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditEmailAddress = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_email_address) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setEmail(s.toString());
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getEmail())) {
				field.setText(data.getEmail());
			}
		}

		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	SectionFieldEditable<EditText, BillingInfo> mEditPhoneNumber = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_phone_number) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setTelephone(s.toString());
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getTelephone())) {
				field.setText(data.getTelephone());
			}
		}

		@Override
		protected Validator<EditText> getValidator() {
			return CommonSectionValidators.REQUIRED_FIELD_VALIDATOR_ET;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			// TODO Auto-generated method stub
			return null;
		}
	};

}
