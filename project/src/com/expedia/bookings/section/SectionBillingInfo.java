package com.expedia.bookings.section;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.section.SectionBillingInfo.ExpirationPickerFragment.OnSetExpirationListener;
import com.expedia.bookings.section.ValidationIndicatorTextColorExclaimation;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.NumberPicker;
import com.expedia.bookings.widget.NumberPicker.Formatter;
import com.expedia.bookings.widget.NumberPicker.OnValueChangeListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.Validator;
import com.mobiata.android.widget.NumberPicker.OnChangedListener;

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

		//Validation indicator fields
		mFields.add(mValidCCNum);
		mFields.add(mValidSecCode);
		mFields.add(mValidNameOnCard);
		mFields.add(mValidFirstName);
		mFields.add(mValidLastName);
		mFields.add(mValidPhoneNumber);
		mFields.add(mValidEmail);
		mFields.add(mValidExpiration);

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
		SectionFieldEditable<?, BillingInfo> editable;
		boolean valid = true;
		for (SectionField<?, BillingInfo> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				editable = (SectionFieldEditable<?, BillingInfo>) field;
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
				DateFormat df = new SimpleDateFormat("MM/yy");
				String exprStr = df.format(data.getExpirationDate().getTime());
				field.setText(exprStr);
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
	////// VALIDATION INDICATOR FIELDS
	//////////////////////////////////////
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidCCNum = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_creditcard_number);
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidSecCode = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_creditcard_security_code);
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidNameOnCard = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_name_on_card);
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidFirstName = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_first_name);
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidLastName = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_last_name);
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidPhoneNumber = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_phone_number);
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidEmail = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_email_address);
	ValidationIndicatorTextColorExclaimation<BillingInfo> mValidExpiration = new ValidationIndicatorTextColorExclaimation<BillingInfo>(
			R.id.edit_creditcard_exp_text_btn);

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
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidCCNum);
			return retArr;
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
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidSecCode);
			return retArr;
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
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidFirstName);
			return retArr;
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
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidLastName);
			return retArr;
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
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidNameOnCard);
			return retArr;
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
			return CommonSectionValidators.EMAIL_VALIDATOR_ET;
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidEmail);
			return retArr;
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
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidPhoneNumber);
			return retArr;
		}
	};

	//This is our date picker DialogFragment. The coder has a responsibility to set setOnDateSetListener after any sort of creation event (including rotaiton)
	public static class ExpirationPickerFragment extends DialogFragment
	{
		private int mMonth;
		private int mYear;

		private static final String MONTH_TAG = "MONTH_TAG";
		private static final String YEAR_TAG = "YEAR_TAG";

		private OnSetExpirationListener mListener;

		public static interface OnSetExpirationListener {
			public void onExpirationSet(int month, int year);
		}

		public static ExpirationPickerFragment newInstance(Calendar cal, OnSetExpirationListener listener) {
			ExpirationPickerFragment frag = new ExpirationPickerFragment();
			frag.setOnSetExpirationListener(listener);
			frag.setDate(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		/**
		 * This won't directly update the gui, it will update the state, so that when create dialog is called this is what gets presented
		 * @param month 1 - 12 (not 0 - 11 like calendar)
		 * @param year 
		 */
		public void setDate(int month, int year) {
			mMonth = month;
			mYear = year;
		}

		public void setOnSetExpirationListener(final OnSetExpirationListener listener) {
			//We chain the listener
			OnSetExpirationListener tListener = new OnSetExpirationListener() {
				@Override
				public void onExpirationSet(int month, int year) {
					mMonth = month;
					mYear = year;
					listener.onExpirationSet(month, year);
				}
			};
			mListener = tListener;
		}

		@Override
		public void onDestroyView() {
			//This is a workaround for a rotation bug: http://code.google.com/p/android/issues/detail?id=17423
			if (getDialog() != null && getRetainInstance()) {
				getDialog().setDismissMessage(null);
			}
			super.onDestroyView();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			setRetainInstance(true);

			if (savedInstanceState != null) {
				if (savedInstanceState.containsKey(MONTH_TAG)
						&& savedInstanceState.containsKey(YEAR_TAG)) {
					setDate(savedInstanceState.getInt(MONTH_TAG),
							savedInstanceState.getInt(YEAR_TAG));
				}
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.expiration_date);
			View exprPickerView;
			if (AndroidUtils.isHoneycombVersionOrHigher()) {
				LayoutInflater inflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				exprPickerView = inflater.inflate(R.layout.widget_expiration_date_picker, null);
				final NumberPicker monthPicker = Ui.findView(exprPickerView, R.id.month_picker);
				final NumberPicker yearPicker = Ui.findView(exprPickerView, R.id.year_picker);

				Calendar now = Calendar.getInstance();

				//Set up month picker
				monthPicker.setMaxValue(12);
				monthPicker.setMinValue(1);
				monthPicker.setValue(mMonth);
				monthPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
				monthPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

				//Set up year picker
				yearPicker.setMinValue(now.get(Calendar.YEAR));
				yearPicker.setMaxValue(now.get(Calendar.YEAR) + 25);
				yearPicker.setValue(mYear);
				yearPicker.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return "" + value;
					}
				});
				yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

				OnValueChangeListener updateDatesListener = new OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						mListener.onExpirationSet(monthPicker.getValue(), yearPicker.getValue());
					}
				};

				monthPicker.setOnValueChangedListener(updateDatesListener);
				yearPicker.setOnValueChangedListener(updateDatesListener);

				builder.setPositiveButton(R.string.button_done, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mListener.onExpirationSet(monthPicker.getValue(), yearPicker.getValue());
					}
				});
			}
			else {
				//Older versions of android
				LayoutInflater inflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				exprPickerView = inflater.inflate(R.layout.widget_compat_expiration_date_picker, null);
				final com.mobiata.android.widget.NumberPicker monthPicker = Ui.findView(exprPickerView,
						R.id.month_picker);
				final com.mobiata.android.widget.NumberPicker yearPicker = Ui.findView(exprPickerView,
						R.id.year_picker);

				Calendar now = Calendar.getInstance();

				//Set up month picker
				monthPicker.setRange(1, 12);
				monthPicker.setCurrent(mMonth);
				monthPicker.setFormatter(new com.mobiata.android.widget.NumberPicker.Formatter() {
					@Override
					public String toString(int value) {
						return String.format("%02d", value);
					}

				});

				//Set up year picker
				yearPicker.setRange(now.get(Calendar.YEAR), now.get(Calendar.YEAR) + 25);
				yearPicker.setCurrent(mYear);
				yearPicker.setFormatter(new com.mobiata.android.widget.NumberPicker.Formatter() {
					@Override
					public String toString(int value) {
						return "" + value;
					}

				});

				OnChangedListener updateDatesListener = new OnChangedListener() {
					@Override
					public void onChanged(com.mobiata.android.widget.NumberPicker picker, int oldVal, int newVal) {
						mListener.onExpirationSet(monthPicker.getCurrent(), yearPicker.getCurrent());
					}
				};
				monthPicker.setOnChangeListener(updateDatesListener);
				yearPicker.setOnChangeListener(updateDatesListener);

				builder.setPositiveButton(R.string.button_done, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mListener.onExpirationSet(monthPicker.getCurrent(), yearPicker.getCurrent());
					}
				});
			}

			builder.setView(exprPickerView);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(Bundle out) {
			out.putInt(MONTH_TAG, mMonth);
			out.putInt(YEAR_TAG, mYear);
			super.onSaveInstanceState(out);
		}
	}

	SectionFieldEditable<TextView, BillingInfo> mEditCardExpirationDateTextBtn = new SectionFieldEditable<TextView, BillingInfo>(
			R.id.edit_creditcard_exp_text_btn) {
		private final static String TAG_EXPR_DATE_PICKER = "TAG_EXPR_DATE_PICKER";

		@Override
		public void setChangeListener(TextView field) {

			if (mContext instanceof FragmentActivity) {
				final FragmentActivity fa = (FragmentActivity) mContext;
				final OnSetExpirationListener listener = new OnSetExpirationListener() {
					@Override
					public void onExpirationSet(int month, int year) {
						Calendar expr = GregorianCalendar.getInstance();
						expr.set(year, month - 1, 1);
						if (hasBoundData()) {
							getData().setExpirationDate(expr);
							refreshText();
							onChange(SectionBillingInfo.this);
						}
					}
				};

				//If we already have created the fragment, we need to set the listener again
				ExpirationPickerFragment datePickerFragment = Ui.findSupportFragment(fa, TAG_EXPR_DATE_PICKER);
				if (datePickerFragment != null) {
					datePickerFragment.setOnSetExpirationListener(listener);
				}

				//Finally set the on click listener that shows the dialog
				field.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Calendar c = Calendar.getInstance();
						if (hasBoundData()) {
							if (getData().getExpirationDate() != null) {
								c = getData().getExpirationDate();
							}
						}

						ExpirationPickerFragment datePickerFragment = Ui.findSupportFragment(fa, TAG_EXPR_DATE_PICKER);
						if (datePickerFragment == null) {
							datePickerFragment = ExpirationPickerFragment.newInstance(c, listener);
						}
						datePickerFragment.show(fa.getSupportFragmentManager(), TAG_EXPR_DATE_PICKER);
					}
				});
			}
			else {
				Log.e("The Expiration picker is expecting a FragmentActivity to be the context. In it's current state, this will do nohting if the context is not a FragmentActivity");
			}
		}

		private void refreshText() {
			if (hasBoundField() && hasBoundData()) {
				onHasFieldAndData(getField(), getData());
			}
		}

		@Override
		protected ArrayList<SectionFieldValidIndicator<?, BillingInfo>> getPostValidators() {
			ArrayList<SectionFieldValidIndicator<?, BillingInfo>> retArr = new ArrayList<SectionFieldValidIndicator<?, BillingInfo>>();
			retArr.add(mValidExpiration);
			return retArr;
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
						Calendar lastMonth = Calendar.getInstance();
						lastMonth.add(Calendar.MONTH, -1);
						if (cal.getTimeInMillis() < lastMonth.getTimeInMillis()) {
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
