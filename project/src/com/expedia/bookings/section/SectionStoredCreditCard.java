package com.expedia.bookings.section;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.utils.Ui;

public class SectionStoredCreditCard extends LinearLayout implements ISection<StoredCreditCard> {

	private TextView mDescriptionView;
	private ImageView mIconView;
	private TextView mWalletTextView;

	private StoredCreditCard mStoredCard;
	private boolean mUseActiveCreditCardIcon = true;
	private int mActiveCardIconResId = 0;
	private int mStoredCardIconResId = 0;
	private int mWalletCardIconResId = 0;

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
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.stored_credit_card_section);
			mActiveCardIconResId = a.getResourceId(R.styleable.stored_credit_card_section_activeIcon, 0);
			mStoredCardIconResId = a.getResourceId(R.styleable.stored_credit_card_section_storedIcon, 0);
			mWalletCardIconResId = a.getResourceId(R.styleable.stored_credit_card_section_walletIcon,
					R.drawable.ic_google_wallet_logo);
			a.recycle();
		}
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mDescriptionView = Ui.findView(this, R.id.display_stored_card_desc);
		mIconView = Ui.findView(this, R.id.icon_view);
		mWalletTextView = Ui.findView(this, R.id.google_wallet_text_view);
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

					mDescriptionView.setTextColor(res.getColor(R.color.data_review_grey));
					mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
							res.getDimensionPixelSize(R.dimen.data_display_sub_text));
				}
				else {
					mWalletTextView.setVisibility(View.GONE);

					primaryTextView = mDescriptionView;
				}

				primaryTextView.setTextColor(res.getColor(R.color.data_review_dark));
				primaryTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						res.getDimensionPixelSize(R.dimen.data_display_primary_text));
			}

			// Icon
			int iconResId;
			if (mStoredCard.isGoogleWallet()) {
				iconResId = mWalletCardIconResId;
			}
			else if (mUseActiveCreditCardIcon) {
				iconResId = mActiveCardIconResId;
			}
			else {
				iconResId = mStoredCardIconResId;
			}

			if (mIconView != null) {
				mIconView.setImageResource(iconResId);
			}
			else {
				mDescriptionView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
			}
		}
	}

	/**
	 * This sets the state of the card icon (not the brand icon)
	 * The default is active.
	 * @param active - should we display the active or inactive icon
	 * @param bind - should we make a call to bind for the icon field?
	 */
	public void setUseActiveCardIcon(boolean active) {
		mUseActiveCreditCardIcon = active;
	}

	public StoredCreditCard getStoredCreditCard() {
		return mStoredCard;
	}
}
