package com.expedia.bookings.section;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.widget.NumberPicker;
import com.expedia.bookings.widget.NumberPicker.Formatter;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
		mFields.add(this.mEditCreditCardNumber);
		mFields.add(this.mEditCreditCardSecurityCode);
		mFields.add(this.mEditFirstName);
		mFields.add(this.mEditLastName);
		mFields.add(this.mEditNameOnCard);
		mFields.add(this.mEditEmailAddress);
		mFields.add(this.mEditPhoneNumber);
		mFields.add(this.mEditCardExpirationDateTextBtn);
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

	SectionFieldEditable<EditText, BillingInfo> mEditNameOnCard = new SectionFieldEditable<EditText, BillingInfo>(
			R.id.edit_name_on_card) {

		@Override
		public void setChangeListener(EditText field) {
			field.addTextChangedListener(new AfterChangeTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (hasBoundData()) {
						getData().setNameOnCard(s.toString());
					}
					onChange(SectionBillingInfo.this);
				}
			});
		}

		@Override
		protected void onHasFieldAndData(EditText field, BillingInfo data) {
			field.setText(TextUtils.isEmpty(data.getNameOnCard()) ? "" : data.getNameOnCard());
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

	SectionFieldEditable<TextView, BillingInfo> mEditCardExpirationDateTextBtn = new SectionFieldEditable<TextView, BillingInfo>(
			R.id.edit_creditcard_exp_text_btn) {

		@Override
		public void setChangeListener(TextView field) {

			field.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(R.string.expiration_date);
					View exprPickerView;
					if (AndroidUtils.isHoneycombVersionOrHigher()) {
						LayoutInflater inflater = (LayoutInflater) mContext
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						exprPickerView = inflater.inflate(R.layout.widget_expiration_date_picker, null);
						final NumberPicker monthPicker = Ui.findView(exprPickerView, R.id.month_picker);
						final NumberPicker yearPicker = Ui.findView(exprPickerView, R.id.year_picker);

						Calendar now = Calendar.getInstance();

						//Set up month picker
						monthPicker.setMaxValue(12);
						monthPicker.setMinValue(1);
						if (hasBoundData() && getData().getExpirationDate() != null) {
							monthPicker.setValue(getData().getExpirationDate().get(Calendar.MONTH) + 1);
						}
						else {
							monthPicker.setValue(now.get(Calendar.MONTH) + 1);
						}
						monthPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
						monthPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

						//Set up year picker
						yearPicker.setMinValue(now.get(Calendar.YEAR));
						yearPicker.setMaxValue(now.get(Calendar.YEAR) + 25);
						if (hasBoundData() && getData().getExpirationDate() != null) {
							yearPicker.setValue(getData().getExpirationDate().get(Calendar.YEAR));
						}
						else {
							yearPicker.setValue(now.get(Calendar.YEAR));
						}
						yearPicker.setFormatter(new Formatter() {
							@Override
							public String format(int value) {
								return "" + value;
							}
						});
						yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
						
						builder.setPositiveButton(R.string.button_done, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Calendar expr = GregorianCalendar.getInstance();
								expr.set(yearPicker.getValue(), monthPicker.getValue() - 1, 1);
								if (hasBoundData()) {
									getData().setExpirationDate(expr);
									refreshText();
									onChange(SectionBillingInfo.this);
								}
							}
						});
					}
					else {
						//Older versions of android
						
						LayoutInflater inflater = (LayoutInflater) mContext
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						exprPickerView = inflater.inflate(R.layout.widget_compat_expiration_date_picker, null);
						final com.mobiata.android.widget.NumberPicker monthPicker = Ui.findView(exprPickerView, R.id.month_picker);
						final com.mobiata.android.widget.NumberPicker yearPicker = Ui.findView(exprPickerView, R.id.year_picker);

						Calendar now = Calendar.getInstance();

						//Set up month picker
						monthPicker.setRange(1, 12);
						monthPicker.setCurrent(hasBoundData() && getData().getExpirationDate() != null ? getData().getExpirationDate().get(Calendar.MONTH) + 1 : now.get(Calendar.MONTH) + 1);
						monthPicker.setFormatter(new com.mobiata.android.widget.NumberPicker.Formatter(){
							@Override
							public String toString(int value) {
								return String.format("%02d", value);
							}
							
						});

						//Set up year picker
						yearPicker.setRange(now.get(Calendar.YEAR), now.get(Calendar.YEAR) + 25);
						yearPicker.setCurrent(hasBoundData() && getData().getExpirationDate() != null ? getData().getExpirationDate().get(Calendar.YEAR) : now.get(Calendar.YEAR));
						yearPicker.setFormatter(new com.mobiata.android.widget.NumberPicker.Formatter(){
							@Override
							public String toString(int value) {
								return "" + value;
							}
							
						});
						
						builder.setPositiveButton(R.string.button_done, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Calendar expr = GregorianCalendar.getInstance();
								expr.set(yearPicker.getCurrent(), monthPicker.getCurrent() - 1, 1);
								if (hasBoundData()) {
									getData().setExpirationDate(expr);
									refreshText();
									onChange(SectionBillingInfo.this);
								}
							}
						});
					}

					//Set handler
					builder.setView(exprPickerView);
					AlertDialog alert = builder.create();
					alert.show();
				}
			});

		}

		private void refreshText() {
			if (hasBoundField() && hasBoundData()) {
				onHasFieldAndData(getField(), getData());
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			return null;
		}

		@Override
		protected void onHasFieldAndData(TextView field, BillingInfo data) {

			String btnTxt = "";
			if (data.getExpirationDate() != null) {
				String formatStr = mContext.getString(R.string.expires_colored_TEMPLATE);
				DateFormat df = new SimpleDateFormat("MM/yy");
				String bdayStr = df.format(data.getExpirationDate().getTime());
				btnTxt = String.format(formatStr, bdayStr);
			}
			field.setText(Html.fromHtml(btnTxt));
		}

		Validator<TextView> mValidator = new Validator<TextView>() {

			@Override
			public int validate(TextView obj) {
				int retVal = ValidationError.NO_ERROR;
				if (hasBoundData()) {
					if (getData().getExpirationDate() != null) {
						Calendar cal = getData().getExpirationDate();
						Calendar now = Calendar.getInstance();
						if (cal.getTimeInMillis() < now.getTimeInMillis()) {
							retVal = ValidationError.ERROR_DATA_INVALID;
						}
						else {
							retVal = ValidationError.NO_ERROR;
						}
					}
					else {
						retVal = ValidationError.ERROR_DATA_MISSING;
					}
				}
				else {
					retVal = ValidationError.ERROR_DATA_MISSING;
				}
				return retVal;
			}
		};

		@Override
		protected Validator<TextView> getValidator() {
			return mValidator;
		}
	};

}
