package com.expedia.bookings.section;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.NumberMaskFormatter;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;

public class CreditCardSection extends LinearLayout {

	private ImageView mLogoImageView;
	private TextView mSignatureTextView;

	public CreditCardSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mLogoImageView = Ui.findView(this, R.id.cc_logo_image_view);
		mSignatureTextView = Ui.findView(this, R.id.signature_text_view);

		// Configure custom typefaces
		// #2350: 4.4 kitkat has issue with custom fonts and bolding
		// https://code.google.com/p/android/issues/detail?id=61771
		if (AndroidUtils.getSdkVersion() == 19) {
			mSignatureTextView.setTypeface(FontCache.getTypeface(Font.SIGNERICA_FAT));
		}
		else {
			mSignatureTextView.setTypeface(FontCache.getTypeface(Font.SIGNERICA_FAT), Typeface.BOLD);
		}
	}

	public void bind(String name, CreditCardType type, String cardNumber) {
		int resId = 0;
		if (type != null) {
			switch (type) {
			case AMERICAN_EXPRESS:
				resId = R.drawable.ic_amex_grey_cvv;
				break;
			case CARTE_BLANCHE:
				resId = R.drawable.ic_carte_blanche_grey_cvv;
				break;
			case CARTE_BLEUE:
				resId = R.drawable.ic_carte_bleue_grey_cvv;
				break;
			case CHINA_UNION_PAY:
				resId = R.drawable.ic_union_pay_grey_cvv;
				break;
			case DINERS_CLUB:
				resId = R.drawable.ic_diners_club_grey_cvv;
				break;
			case DISCOVER:
				resId = R.drawable.ic_discover_grey_cvv;
				break;
			case JAPAN_CREDIT_BUREAU:
				resId = R.drawable.ic_jcb_grey_cvv;
				break;
			case MAESTRO:
				resId = R.drawable.ic_maestro_grey_cvv;
				break;
			case MASTERCARD:
				resId = R.drawable.ic_master_card_grey_cvv;
				break;
			case VISA:
				resId = R.drawable.ic_visa_grey_cvv;
				break;
			default:
				resId = R.drawable.ic_generic_card_cvv;
				break;
			}
		}

		mLogoImageView.setImageResource(resId);

		// #1116 - For some reason this typeface calculates the left edge
		// bounds incorrectly, so we add a space just in case.
		mSignatureTextView.setText(" " + name);

		// Fill in card digits
		TextView cardDigitsText = Ui.findView(this, R.id.obscured_card_digits);
		cardDigitsText.setText(NumberMaskFormatter.obscureCreditCardNumber(cardNumber));

		// Fill in member name
		TextView memberNameText = Ui.findView(this, R.id.member_name_text);
		if (TextUtils.isEmpty(name)) {
			memberNameText.setText(R.string.Preferred_Customer);
		}
		else {
			memberNameText.setText(name);
		}

		// Show front or back of card (front for amex, back for everything else)
		boolean amex = type == CreditCardType.AMERICAN_EXPRESS;
		showHide(amex, R.id.svg_amex_logo, R.id.svg_amex_head, R.id.obscured_card_digits, R.id.member_text, R.id.member_name_text);
		showHide(!amex, R.id.magnetic_stripe, R.id.authorized_signature_text, R.id.signature_strip_frame, R.id.not_valid_unless_signed_text, R.id.spring, R.id.cc_logo_image_view);
	}

	private void showHide(boolean visible, int... resIds) {
		int visibility = visible ? View.VISIBLE : View.GONE;
		for (int r : resIds) {
			View v = Ui.findView(this, r);
			if (v != null) {
				v.setVisibility(visibility);
			}
		}
	}
}
