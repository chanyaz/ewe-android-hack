package com.expedia.bookings.section;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CreditCardUtils;
import com.expedia.bookings.utils.Ui;

public class SectionStoredCreditCard extends LinearLayout implements ISection<StoredCreditCard> {

	private Context mContext;

	private TextView mDescriptionView;
	private ImageView mIconView;
	private com.expedia.bookings.widget.TextView mLccFeeTextView;
	private View mLccDivider;
	private View mCardNotSupportedImageView;

	private StoredCreditCard mStoredCard;

	private int mCardIconResId = 0;
	private ColorStateList mPrimaryTextColor;
	private ColorStateList mSecondaryTextColor;

	private LineOfBusiness mLob;

	public SectionStoredCreditCard(Context context) {
		super(context);
		init(context, null);
	}

	public SectionStoredCreditCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public SectionStoredCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		inflate(context, R.layout.widget_stored_credit_card, this);

		mContext = context;
		mDescriptionView = Ui.findView(this, R.id.display_stored_card_desc);
		mIconView = Ui.findView(this, R.id.icon_view);
		mLccFeeTextView = Ui.findView(this, R.id.card_fee_icon);
		mLccDivider = Ui.findView(this, R.id.card_fee_divider);
		mCardNotSupportedImageView = Ui.findView(this, R.id.card_not_supported_icon);

		// Set a few attributes that widget_stored_credit_card desires
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.stored_credit_card_section);
			mCardIconResId = a.getResourceId(R.styleable.stored_credit_card_section_cardIcon, 0);
			mPrimaryTextColor = a.getColorStateList(R.styleable.stored_credit_card_section_primaryTextColor);
			mSecondaryTextColor = a.getColorStateList(R.styleable.stored_credit_card_section_secondaryTextColor);
			a.recycle();
		}

		if (mPrimaryTextColor == null) {
			mPrimaryTextColor = context.getResources().getColorStateList(R.color.data_review_dark);
		}

		if (mSecondaryTextColor == null) {
			mSecondaryTextColor = context.getResources().getColorStateList(R.color.data_review_gray);
		}
	}

	public void configure(int cardResId, int primaryTextColorResId, int secondaryTextColorResId) {
		if (cardResId != 0) {
			mCardIconResId = cardResId;
		}

		if (primaryTextColorResId != 0) {
			mPrimaryTextColor = getResources().getColorStateList(primaryTextColorResId);
		}

		if (secondaryTextColorResId != 0) {
			mSecondaryTextColor = getResources().getColorStateList(secondaryTextColorResId);
		}
	}

	public void setLineOfBusiness(LineOfBusiness lob) {
		mLob = lob;
	}

	public void bindCardNotSupported() {
		if (mContext instanceof FragmentActivity) {
			mLccDivider.setVisibility(View.VISIBLE);
			mCardNotSupportedImageView.setVisibility(View.VISIBLE);
			mCardNotSupportedImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String text;
					PaymentType type = mStoredCard.getType();
					if (type != null) {
						int msg = 0;
						if (mLob == LineOfBusiness.FLIGHTS) {
							msg = R.string.airline_card_not_supported_TEMPLATE;
						}
						if (mLob == LineOfBusiness.HOTELS) {
							msg = R.string.hotel_card_not_supported_TEMPLATE;
						}

						String typeName = CreditCardUtils.getHumanReadableName(mContext, type);
						text = mContext.getString(msg, typeName);
					}
					else {
						int msg = 0;
						if (mLob == LineOfBusiness.FLIGHTS) {
							msg = R.string.airline_card_not_supported_generic;
						}
						if (mLob == LineOfBusiness.HOTELS) {
							msg = R.string.hotel_card_not_supported_generic;
						}

						text = mContext.getString(msg);
					}
					SimpleSupportDialogFragment df = SimpleSupportDialogFragment.newInstance(null, text);
					FragmentActivity fa = (FragmentActivity) mContext;
					df.show(fa.getSupportFragmentManager(), "cardNotSupported");
				}
			});
		}
	}

	@Override
	public void bind(StoredCreditCard data) {
		mStoredCard = data;

		if (mStoredCard != null) {
			// Text
			String desc = data.getDescription();
			if (TextUtils.isEmpty(desc)) {
				mDescriptionView.setText("");
			}
			else {
				//We replace american express with amex. Why? Because there is a mingle card for it, that's why!
				String amexRegex = "american\\s*express";
				String replacement = "AMEX";
				Pattern amexPattern = Pattern.compile(amexRegex, Pattern.CASE_INSENSITIVE);
				Matcher amexPatternMatcher = amexPattern.matcher(desc);
				StringBuffer sb = new StringBuffer();
				while (amexPatternMatcher.find()) {
					amexPatternMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
				}
				amexPatternMatcher.appendTail(sb);
				mDescriptionView.setText(sb.toString());
			}

			mDescriptionView.setTextColor(mPrimaryTextColor);
			mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getResources().getDimensionPixelSize(R.dimen.data_display_primary_text));

			// Icon
			int iconResId;

			// Show a credit card logo on tablet
			if (ExpediaBookingApp.useTabletInterface()) {
				iconResId = BookingInfoUtils.getTabletCardIcon(data.getType());
			}
			else {
				iconResId = mCardIconResId;
			}

			if (mIconView != null) {
				mIconView.setImageResource(iconResId);
			}
			else {
				mDescriptionView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
			}

			// LCC fee warning
			if (mContext instanceof FragmentActivity) {
				TripBucketItem item = null;
				if (mLob == LineOfBusiness.FLIGHTS) {
					item = Db.getTripBucket().getFlight();
				}
				if (mLob == LineOfBusiness.HOTELS) {
					item = Db.getTripBucket().getHotel();
				}
				if (mLob == null || item == null) {
					throw new RuntimeException("LineOfBusiness must be set and TripBucketItem cannot be null");
				}

				final PaymentType type = mStoredCard.getType();
				final Money cardFee = item.getPaymentFee(type);
				if (!item.isPaymentTypeSupported(type)) {
					Resources res = getResources();
					int errorIconResId;
					if (ExpediaBookingApp.useTabletInterface()) {
						errorIconResId = R.drawable.ic_checkout_error_state;
					}
					else {
						errorIconResId = R.drawable.ic_lcc_no_card_payment_selection;
						mDescriptionView.setTextColor(res.getColor(R.color.flight_card_invalid_cc_type_text_color));
					}
					mIconView.setImageResource(errorIconResId);
				}
				else if (cardFee != null && !cardFee.isZero()) {
					final String feeText = cardFee.getFormattedMoney();
					mLccFeeTextView.setVisibility(View.VISIBLE);
					mLccDivider.setVisibility(View.VISIBLE);
					mLccFeeTextView.setText(feeText);
					mLccFeeTextView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							int msg = 0;
							if (mLob == LineOfBusiness.FLIGHTS) {
								msg = R.string.airline_card_fee_select_TEMPLATE;
							}
							if (mLob == LineOfBusiness.HOTELS) {
								msg = R.string.hotel_card_fee_select_TEMPLATE;
							}
							String cardName = CreditCardUtils.getHumanReadableCardTypeName(mContext, type);
							String text = mContext.getString(msg, feeText, cardName);

							FragmentActivity fa = (FragmentActivity) mContext;
							SimpleSupportDialogFragment df = SimpleSupportDialogFragment.newInstance(null, text);
							df.show(fa.getSupportFragmentManager(), "lccDialog");
						}
					});
				}
			}
		}
	}

	public StoredCreditCard getStoredCreditCard() {
		return mStoredCard;
	}
}
