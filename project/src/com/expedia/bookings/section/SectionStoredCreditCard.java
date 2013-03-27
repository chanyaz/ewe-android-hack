package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;

public class SectionStoredCreditCard extends LinearLayout implements ISection<StoredCreditCard> {

	ArrayList<SectionField<?, StoredCreditCard>> mFields = new ArrayList<SectionField<?, StoredCreditCard>>();

	Context mContext;
	StoredCreditCard mStoredCard;
	boolean mUseActiveCreditCardIcon = true;
	int mActiveCardIconResId = 0;
	int mStoredCardIconResId = 0;

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
		mContext = context;

		mFields.add(mDisplayCard);

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.stored_credit_card_section);
			mActiveCardIconResId = a.getResourceId(R.styleable.stored_credit_card_section_activeIcon, 0);
			mStoredCardIconResId = a.getResourceId(R.styleable.stored_credit_card_section_storedIcon, 0);
			a.recycle();
		}
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, StoredCreditCard> field : mFields) {
			field.bindField(this);
		}
	}

	@Override
	public void bind(StoredCreditCard data) {
		mStoredCard = data;
		if (mStoredCard != null) {
			for (SectionField<?, StoredCreditCard> field : mFields) {
				field.bindData(mStoredCard);
			}
		}
	}

	/**
	 * This sets the state of the card icon (not the brand icon)
	 * The default is active.
	 * @param active - should we display the active or inactive icon
	 * @param bind - should we make a call to bind for the icon field?
	 */
	public void setUseActiveCardIcon(boolean active, boolean bind) {
		mUseActiveCreditCardIcon = active;
		if (bind) {
			mDisplayCard.bindData(mStoredCard);
		}
	}

	//////////////////////////////////////
	////// DISPLAY FIELDS
	//////////////////////////////////////

	SectionField<TextView, StoredCreditCard> mDisplayCard = new SectionField<TextView, StoredCreditCard>(
			R.id.display_stored_card_desc) {
		@Override
		public void onHasFieldAndData(TextView field, StoredCreditCard data) {
			// Text
			String desc = data.getDescription();
			if (TextUtils.isEmpty(desc)) {
				field.setText("");
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
				field.setText(sb.toString());
			}

			// Icon
			if (mUseActiveCreditCardIcon) {
				field.setCompoundDrawablesWithIntrinsicBounds(mActiveCardIconResId, 0, 0, 0);
			}
			else {
				field.setCompoundDrawablesWithIntrinsicBounds(mStoredCardIconResId, 0, 0, 0);
			}

		}
	};

}
