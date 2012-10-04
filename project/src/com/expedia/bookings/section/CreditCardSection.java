package com.expedia.bookings.section;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

public class CreditCardSection extends LinearLayout {

	private TextView mSignatureTextView;
	private TextView mNameTextView;

	public CreditCardSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mSignatureTextView = Ui.findView(this, R.id.signature_text_view);
		mNameTextView = Ui.findView(this, R.id.name_text_view);

		// Configure custom typefaces
		AssetManager am = getContext().getAssets();
		mSignatureTextView.setTypeface(Typeface.createFromAsset(am, "fonts/Signerica_Fat.ttf"), Typeface.BOLD);
		mNameTextView.setTypeface(Typeface.createFromAsset(am, "fonts/OCRAStd.otf"));
	}

	public void setName(String name) {
		mSignatureTextView.setText(name);
		mNameTextView.setText(name.toUpperCase());
	}
}
