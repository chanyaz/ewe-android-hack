package com.expedia.bookings.section;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
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
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;

public class SectionStoredCreditCard extends LinearLayout implements ISection<StoredCreditCard> {

	private Context mContext;

	private TextView mDescriptionView;
	private ImageView mIconView;
	private TextView mWalletTextView;
	private com.expedia.bookings.widget.TextView mLccFeeTextView;
	private View mLccDivider;
	private View mCardNotSupportedImageView;

	private StoredCreditCard mStoredCard;
	private FlightTrip mFlightTrip;

	private int mCardIconResId = 0;
	private ColorStateList mPrimaryTextColor;
	private ColorStateList mSecondaryTextColor;

	public SectionStoredCreditCard(Context context) {
		super(context);
		init(context, null);
	}

	public SectionStoredCreditCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public SectionStoredCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		inflate(context, R.layout.widget_stored_credit_card, this);

		mContext = context;
		mDescriptionView = Ui.findView(this, R.id.display_stored_card_desc);
		mIconView = Ui.findView(this, R.id.icon_view);
		mWalletTextView = Ui.findView(this, R.id.google_wallet_text_view);
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
			mSecondaryTextColor = context.getResources().getColorStateList(R.color.data_review_grey);
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

	public void bindCardNotSupportedLcc() {
		if (mContext instanceof FragmentActivity) {
			final FragmentActivity fa = (FragmentActivity) mContext;
			mLccDivider.setVisibility(View.VISIBLE);
			mCardNotSupportedImageView.setVisibility(View.VISIBLE);
			mCardNotSupportedImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String text;
					CreditCardType type = mStoredCard.getType();
					if (type != null) {
						String typeName = type.getHumanReadableName(mContext);
						text = mContext.getString(R.string.airline_card_not_supported_TEMPLATE, typeName);
					}
					else {
						text = mContext.getString(R.string.airline_card_not_supported_generic);
					}
					SimpleSupportDialogFragment.newInstance(null, text).show(fa.getSupportFragmentManager(),
						"cardNotSupported");
				}
			});
		}
	}

	public void bind(StoredCreditCard data, FlightTrip flightTrip) {
		mFlightTrip = flightTrip;
		bind(data);
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

			// Use different styling based on whether it's Google wallet or not
			if (mWalletTextView != null) {
				Resources res = getResources();
				TextView primaryTextView;
				if (mStoredCard.isGoogleWallet()) {
					mWalletTextView.setVisibility(View.VISIBLE);

					primaryTextView = mWalletTextView;

					mDescriptionView.setTextColor(mSecondaryTextColor);
					mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
							res.getDimensionPixelSize(R.dimen.data_display_sub_text));
				}
				else {
					mWalletTextView.setVisibility(View.GONE);

					primaryTextView = mDescriptionView;
				}

				primaryTextView.setTextColor(mPrimaryTextColor);
				primaryTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						res.getDimensionPixelSize(R.dimen.data_display_primary_text));
			}

			// Icon
			int iconResId;
			if (mStoredCard.isGoogleWallet()) {
				iconResId = R.drawable.ic_google_wallet_logo;
			}
			// Show a credit card logo on tablet
			else if (AndroidUtils.isTablet(getContext())) {
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
			if (mContext instanceof FragmentActivity && mFlightTrip != null) {
				final CreditCardType type = mStoredCard.getType();
				if (!mFlightTrip.isCardTypeSupported(type)) {
					Resources res = getResources();
					mIconView.setImageResource(R.drawable.ic_lcc_no_card_payment_selection);
					mDescriptionView.setTextColor(res.getColor(R.color.flight_card_invalid_cc_type_text_color));
				}
				else {
					final FragmentActivity fa = (FragmentActivity) mContext;
					Money cardFee = mFlightTrip.getCardFee(type);

					if (cardFee != null) {
						final String feeText = cardFee.getFormattedMoney();
						mLccFeeTextView.setVisibility(View.VISIBLE);
						mLccDivider.setVisibility(View.VISIBLE);
						mLccFeeTextView.setText(feeText);
						mLccFeeTextView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								String text = mContext.getString(R.string.airline_card_fee_select_TEMPLATE, feeText,
										CreditCardType.getHumanReadableCardTypeName(mContext, type));
								SimpleSupportDialogFragment.newInstance(null, text).show(
										fa.getSupportFragmentManager(), "lccDialog");
							}
						});
					}
				}
			}

		}
	}

	public StoredCreditCard getStoredCreditCard() {
		return mStoredCard;
	}
}
