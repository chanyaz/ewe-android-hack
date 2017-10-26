package com.expedia.account.view;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.expedia.account.R;

public class FacebookAPIHostLayout extends FrameLayout {

	private TextView vIntroMessage;

	public FacebookAPIHostLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_facebook_api_host, this);

		vIntroMessage = (TextView) findViewById(R.id.facebook_message);
	}

	public void setMessage(@StringRes int messageResId) {
		vIntroMessage.setText(messageResId);
	}

	public void setMessage(CharSequence message) {
		vIntroMessage.setText(message);
	}

}
