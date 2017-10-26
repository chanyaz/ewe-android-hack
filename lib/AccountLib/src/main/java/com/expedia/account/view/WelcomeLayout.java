package com.expedia.account.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.util.Utils;

public class WelcomeLayout extends RelativeLayout {
	private TextView vWelcomeText;

	public WelcomeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_loading_welcome, this);

		vWelcomeText = (TextView) findViewById(R.id.welcome_text);
	}

	public void styleizeFromAccountView(TypedArray a) {
		vWelcomeText.setTextColor(a.getColor(R.styleable.acct__AccountView_acct__welcome_text_color,
			getResources().getColor(R.color.acct__default_welcome_text_color)));
	}

	public void brandIt(String brand) {
		Utils.brandText(vWelcomeText, brand);
	}
}
