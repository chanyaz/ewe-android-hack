package com.expedia.bookings.section;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.Ui;

public class CreditCardSection extends LinearLayout {

	private ImageView mLogoImageView;
	private TextView mSignatureTextView;
	private TextView mNameTextView;

	public CreditCardSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mLogoImageView = Ui.findView(this, R.id.cc_logo_image_view);
		mSignatureTextView = Ui.findView(this, R.id.signature_text_view);
		mNameTextView = Ui.findView(this, R.id.name_text_view);

		// Configure custom typefaces
		mSignatureTextView.setTypeface(FontCache.getTypeface(Font.SIGNERICA_FAT), Typeface.BOLD);
		mNameTextView.setTypeface(FontCache.getTypeface(Font.OCRA_STD));
	}

	public void bind(String name, CreditCardType type) {
		int resId;
		switch (type) {
		case AMERICAN_EXPRESS:
			resId = R.drawable.ic_amex_grey;
			break;
		case CARTE_BLANCHE:
			resId = R.drawable.ic_carte_blanche_grey;
			break;
		case CHINA_UNION_PAY:
			resId = R.drawable.ic_union_pay_grey;
			break;
		case DINERS_CLUB:
			resId = R.drawable.ic_diners_club_grey;
			break;
		case DISCOVER:
			resId = R.drawable.ic_discover_grey;
			break;
		case JAPAN_CREDIT_BUREAU:
			resId = R.drawable.ic_jcb_grey;
			break;
		case MAESTRO:
			resId = R.drawable.ic_maestro_grey;
			break;
		case MASTERCARD:
			resId = R.drawable.ic_master_card_grey;
			break;
		case VISA:
		default:
			resId = R.drawable.ic_visa_grey;
			break;
		}

		mLogoImageView.setImageResource(resId);

		mSignatureTextView.setText(name);
		mNameTextView.setText(name.toUpperCase());
	}
}
