package com.expedia.bookings.section;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.utils.NumberMaskFormatter;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AutoResizeTextView;
import com.mobiata.android.util.ViewUtils;

public class CreditCardSection extends RelativeLayout {

	private ImageView mLogoImageView;
	private AutoResizeTextView mSignatureTextView;

	public CreditCardSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mLogoImageView = Ui.findView(this, R.id.cc_logo_image_view);
		mSignatureTextView = Ui.findView(this, R.id.signature_text_view);
	}

	public void bind(String name, PaymentType type, String cardNumber) {
		int resId = 0;
		if (type != null) {
			switch (type) {
			case CARD_AMERICAN_EXPRESS:
				resId = R.drawable.ic_amex_grey_cvv;
				break;
			case CARD_CARTE_BLANCHE:
				resId = R.drawable.ic_carte_blanche_grey_cvv;
				break;
			case CARD_CARTE_BLEUE:
				resId = R.drawable.ic_carte_bleue_grey_cvv;
				break;
			case CARD_CHINA_UNION_PAY:
				resId = R.drawable.ic_union_pay_grey_cvv;
				break;
			case CARD_DINERS_CLUB:
				resId = R.drawable.ic_diners_club_grey_cvv;
				break;
			case CARD_DISCOVER:
				resId = R.drawable.ic_discover_grey_cvv;
				break;
			case CARD_JAPAN_CREDIT_BUREAU:
				resId = R.drawable.ic_jcb_grey_cvv;
				break;
			case CARD_MAESTRO:
				resId = R.drawable.ic_maestro_grey_cvv;
				break;
			case CARD_MASTERCARD:
				resId = R.drawable.ic_master_card_grey_cvv;
				break;
			case CARD_VISA:
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
		StringBuilder sb = new StringBuilder(name);
		sb.insert(0, ' ');

		// Always calculate a minimum text size so that we don't show the tiniest size possible
		// after a rotation, and so if the name becomes shorter, we'll also use a larger size.
		float minTextSize = ViewUtils.getTextSizeForMaxLines(getContext(), sb.toString(), mSignatureTextView.getPaint(), mSignatureTextView.getWidth(), 1, 40, 5);
		mSignatureTextView.setMinTextSize(minTextSize);
		mSignatureTextView.setText(sb.toString());

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
		boolean amex = type == PaymentType.CARD_AMERICAN_EXPRESS;
		showHide(amex, R.id.amex_container);
		showHide(!amex, R.id.other_cards_container, R.id.signature_strip_frame, R.id.cvv_prompt_text_view, R.id.cc_logo_image_view);
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
